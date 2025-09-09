package pl.karoldronia.artefacts.artefact.impl.dragon.controller;

import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import pl.karoldronia.artefacts.artefact.ArtefactService;
import pl.karoldronia.artefacts.artefact.ability.Ability;
import pl.karoldronia.artefacts.artefact.ability.dragon.DragonAbility;
import pl.karoldronia.artefacts.artefact.ability.dragon.DragonAbilityTrigger;
import pl.karoldronia.artefacts.artefact.impl.dragon.DragonArtefact;
import pl.karoldronia.artefacts.artefact.item.ArtefactItemsUtil;
import pl.karoldronia.artefacts.notice.NoticeService;
import pl.karoldronia.artefacts.profile.Profile;
import pl.karoldronia.artefacts.profile.ProfileRepository;
import pl.karoldronia.artefacts.util.DurationUtil;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class DragonArtefactAbilityController implements Listener {

    private final ProfileRepository profileRepository;
    private final NoticeService noticeService;
    private final ArtefactService artefactService;

    @EventHandler
    void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Profile profile = this.profileRepository.findOrCreate(player);

        Action action = event.getAction();

        if (!ArtefactItemsUtil.isHoldingArtefact(profile.getArtefactId(), player)) {
            return;
        }

        this.artefactService.findArtefact(profile.getArtefactId()).ifPresent(artefact -> {
            if (!artefact.getId().equalsIgnoreCase(DragonArtefact.ID)) {
                return;
            }

            List<Ability> abilities = artefact.getAbilities();

            if (abilities.isEmpty()) {
                return;
            }

            DragonAbilityTrigger dragonAbilityTrigger = this.getTrigger(player, action);
            if (dragonAbilityTrigger == null) {
                return;
            }

            Optional<Ability> dragonAbilityOptional = abilities.stream()
                    .filter(ability -> ability instanceof DragonAbility)
                    .filter(ability -> ((DragonAbility) ability).getDragonTrigger() == dragonAbilityTrigger)
                    .findFirst();

            dragonAbilityOptional.ifPresent(ability -> {
                if (!(ability instanceof DragonAbility dragonAbility)) {
                    return;
                }

                if (profile.hasCooldown(dragonAbility)) {
                    Duration abilityCooldown = profile.getAbilityCooldown(dragonAbility.getId());
                    this.noticeService.create()
                            .notice(messages -> messages.abilityCooldown)
                            .player(player.getUniqueId())
                            .placeholder("{cooldown}", DurationUtil.format(abilityCooldown))
                            .send();
                    return;
                }

                DragonAbilityTrigger trigger = dragonAbility.getDragonTrigger();
                if (trigger == DragonAbilityTrigger.RPM_SHIFT && !profile.isUpgraded()) {
                    this.noticeService.create()
                            .notice(messages -> messages.upgradedRequired)
                            .player(player.getUniqueId())
                            .send();
                    return;
                }

                if (trigger == DragonAbilityTrigger.LPM_SHIFT && !profile.isDragonUpgraded()) {
                    this.noticeService.create()
                            .notice(messages -> messages.dragonUpgradedRequired)
                            .player(player.getUniqueId())
                            .send();
                    return;
                }

                ability.performAbility(player, profile);
                profile.setAbilityCooldown(ability.getId(), ability.getCooldown());
            });
        });
    }

    private DragonAbilityTrigger getTrigger(Player player, Action action) {
        boolean sneaking = player.isSneaking();

        if (action == Action.RIGHT_CLICK_BLOCK || action == Action.RIGHT_CLICK_AIR) {
            return sneaking ? DragonAbilityTrigger.RPM_SHIFT : DragonAbilityTrigger.RPM;
        }

        if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
            if (!sneaking) {
                return null;
            }

            return DragonAbilityTrigger.LPM_SHIFT;
        }
        return null;
    }

}