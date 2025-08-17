package pl.karoldronia.artefacts.artefact.impl.fire;

import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import pl.karoldronia.artefacts.artefact.Artefact;
import pl.karoldronia.artefacts.artefact.ability.Ability;
import pl.karoldronia.artefacts.artefact.impl.fire.ability.FireArtefactAbility;
import pl.karoldronia.artefacts.artefact.impl.fire.ability.FireArtefactUpgradedAbility;
import pl.karoldronia.artefacts.notice.NoticeService;
import pl.karoldronia.artefacts.profile.ProfileRepository;

import java.util.List;

public class FireArtefact implements Artefact {

    public static final String ID = "fire";

    private final Plugin plugin;
    private final FireArtefactConfig artefactConfig;
    private final NoticeService noticeService;

    public FireArtefact(Plugin plugin, FireArtefactConfig artefactConfig, NoticeService noticeService) {
        this.plugin = plugin;
        this.artefactConfig = artefactConfig;
        this.noticeService = noticeService;
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public void givePassiveEffects(Player player) {

    }

    @Override
    public List<Ability> getAbilities() {
        return List.of(
                new FireArtefactAbility(this.artefactConfig, this.noticeService),
                new FireArtefactUpgradedAbility(this.plugin, this.artefactConfig, this.noticeService)
        );
    }

    @Override
    public void registerControllers(Plugin plugin, Server server, ProfileRepository profileRepository) {
        server.getPluginManager().registerEvents(
                new FireArtefactController(profileRepository),
                plugin
        );
    }
}