package pl.karoldronia.artefacts.artefact.ability;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.Plugin;
import pl.karoldronia.artefacts.artefact.ArtefactService;
import pl.karoldronia.artefacts.artefact.ability.charging.ChargingAbility;
import pl.karoldronia.artefacts.artefact.ability.charging.ChargingSession;
import pl.karoldronia.artefacts.artefact.impl.dragon.DragonArtefact;
import pl.karoldronia.artefacts.artefact.item.ArtefactItemsUtil;
import pl.karoldronia.artefacts.artefact.protect.ArtefactProtectService;
import pl.karoldronia.artefacts.notice.NoticeService;
import pl.karoldronia.artefacts.profile.Profile;
import pl.karoldronia.artefacts.profile.ProfileRepository;
import pl.karoldronia.artefacts.util.DurationUtil;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AbilityController implements Listener {

    private final Plugin plugin;
    private final ProfileRepository profileRepository;
    private final ArtefactService artefactService;
    private final NoticeService noticeService;
    private final ArtefactProtectService protectService;

    private final Map<UUID, ChargingSession> chargingSessions = new HashMap<>();

    public AbilityController(Plugin plugin, ProfileRepository profileRepository, ArtefactService artefactService, NoticeService noticeService, ArtefactProtectService protectService) {
        this.plugin = plugin;
        this.profileRepository = profileRepository;
        this.artefactService = artefactService;
        this.noticeService = noticeService;
        this.protectService = protectService;
    }

    @EventHandler
    void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Profile profile = this.profileRepository.findOrCreate(player);

        Action action = event.getAction();
        boolean sneaking = player.isSneaking();

        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            this.stopCharging(player.getUniqueId());
            return;
        }

        if (!ArtefactItemsUtil.isHoldingArtefact(profile.getArtefactId(), player)) {
            return;
        }

        this.artefactService.findArtefact(profile.getArtefactId()).ifPresent(artefact -> {
            if (this.protectService.isInProtect(player.getLocation())) {
                this.noticeService.create()
                        .notice(messages -> messages.cannotUseArtefactInProtectZone)
                        .player(player.getUniqueId())
                        .send();
                return;
            }

            if (artefact.getId().equalsIgnoreCase(DragonArtefact.ID)) {
                return;
            }

            List<Ability> abilities = artefact.getAbilities();

            if (abilities.isEmpty()) {
                return;
            }

            AbilityTrigger trigger = sneaking ? AbilityTrigger.RPM_SHIFT : AbilityTrigger.RPM;
            Ability ability = abilities.stream()
                    .filter(a -> a.getTrigger() == trigger)
                    .findFirst()
                    .orElse(null);

            if (ability == null) {
                return;
            }

            if (profile.hasCooldown(ability)) {
                Duration abilityCooldown = profile.getAbilityCooldown(ability.getId());
                this.noticeService.create()
                        .notice(messages -> messages.abilityCooldown)
                        .player(player.getUniqueId())
                        .placeholder("{cooldown}", DurationUtil.format(abilityCooldown))
                        .send();
                return;
            }

            if (ability.requireUpgrade() && !profile.isUpgraded()) {
                this.noticeService.create()
                        .notice(messages -> messages.upgradedRequired)
                        .player(player.getUniqueId())
                        .send();
                return;
            }

            profile.setAbilityCooldown(ability.getId(), ability.getCooldown());
            if (ability instanceof ChargingAbility chargingAbility) {
                this.startCharging(player, profile, chargingAbility);
                return;
            }

            ability.performAbility(player, profile);
        });
    }

    private void startCharging(Player player, Profile profile, ChargingAbility ability) {
        UUID playerId = player.getUniqueId();

        // If already charging, don't start new session
        if (chargingSessions.containsKey(playerId)) {
            return;
        }

        Duration chargingDuration = ability.getChargingDuration();
        long totalTicks = chargingDuration.toMillis() / 50; // Convert to ticks (20 ticks = 1 second)

        // Create charging session
        ChargingSession session = new ChargingSession(player, profile, ability, totalTicks, this.plugin, () -> stopCharging(playerId));
        chargingSessions.put(playerId, session);

        // Start charging task
        session.start();
    }

    private void stopCharging(UUID playerId) {
        ChargingSession session = chargingSessions.remove(playerId);
        if (session != null) {
            session.cancel();
        }
    }
}