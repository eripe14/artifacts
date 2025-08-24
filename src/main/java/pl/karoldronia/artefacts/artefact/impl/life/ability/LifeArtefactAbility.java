package pl.karoldronia.artefacts.artefact.impl.life.ability;

import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import pl.karoldronia.artefacts.artefact.ability.Ability;
import pl.karoldronia.artefacts.artefact.ability.AbilityResult;
import pl.karoldronia.artefacts.artefact.impl.life.LifeArtefactConfig;
import pl.karoldronia.artefacts.notice.NoticeService;
import pl.karoldronia.artefacts.profile.Profile;

import java.time.Duration;

public class LifeArtefactAbility implements Ability {

    private final LifeArtefactConfig artefactConfig;
    private final NoticeService noticeService;

    public LifeArtefactAbility(LifeArtefactConfig artefactConfig, NoticeService noticeService) {
        this.artefactConfig = artefactConfig;
        this.noticeService = noticeService;
    }

    @Override
    public String getId() {
        return "life";
    }

    @Override
    public AbilityResult performAbility(Player player, Profile profile) {
        double maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        player.setHealth(maxHealth);

        this.noticeService.create()
                .notice(messages -> messages.lifeAbility)
                .player(player.getUniqueId())
                .send();
        return AbilityResult.SUCCESS;
    }

    @Override
    public Duration getCooldown() {
        return this.artefactConfig.abilityCooldown;
    }
}