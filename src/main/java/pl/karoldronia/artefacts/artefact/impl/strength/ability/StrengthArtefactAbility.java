package pl.karoldronia.artefacts.artefact.impl.strength.ability;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import pl.karoldronia.artefacts.artefact.ability.Ability;
import pl.karoldronia.artefacts.artefact.ability.AbilityResult;
import pl.karoldronia.artefacts.artefact.impl.strength.StrengthArtefactConfig;
import pl.karoldronia.artefacts.notice.NoticeService;
import pl.karoldronia.artefacts.profile.Profile;

import java.time.Duration;

public class StrengthArtefactAbility implements Ability {

    private final StrengthArtefactConfig artefactConfig;
    private final NoticeService noticeService;

    public StrengthArtefactAbility(StrengthArtefactConfig artefactConfig, NoticeService noticeService) {
        this.artefactConfig = artefactConfig;
        this.noticeService = noticeService;
    }

    @Override
    public String getId() {
        return "strength";
    }

    @Override
    public AbilityResult performAbility(Player player, Profile profile) {
        player.addPotionEffect(new PotionEffect(
                PotionEffectType.STRENGTH,
                Integer.MAX_VALUE,
                2,
                false,
                true,
                true
        ));

        this.noticeService.create()
                .notice(messages -> messages.strengthAbility)
                .player(player.getUniqueId())
                .send();

        return AbilityResult.SUCCESS;
    }

    @Override
    public Duration getCooldown() {
        return this.artefactConfig.abilityCooldown;
    }
}