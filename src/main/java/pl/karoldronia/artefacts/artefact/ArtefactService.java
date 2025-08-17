package pl.karoldronia.artefacts.artefact;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;
import pl.karoldronia.artefacts.profile.ProfileRepository;

import java.util.*;

public class ArtefactService {

    private final Plugin plugin;
    private final ProfileRepository profileRepository;

    private final Random random = new Random();
    private final Map<String, Artefact> artefacts = new HashMap<>();

    public ArtefactService(Plugin plugin, ProfileRepository profileRepository) {
        this.plugin = plugin;
        this.profileRepository = profileRepository;
    }

    public void addArtefact(Artefact artefact) {
        this.artefacts.put(artefact.getId(), artefact);
        artefact.registerControllers(
                this.plugin,
                this.plugin.getServer(),
                this.profileRepository
        );
    }

    public Artefact getRandomArtefact(@Nullable Artefact previous) {
        List<Artefact> availableArtefacts = new ArrayList<>(this.artefacts.values());

        if (previous != null) {
            availableArtefacts.remove(previous);
        }

        if (availableArtefacts.isEmpty()) {
            return null; // No other artefacts available
        }

        return availableArtefacts.get(random.nextInt(availableArtefacts.size()));
    }

    public void shutdown() {
        for (Artefact artefact : this.artefacts.values()) {
            artefact.shutdown();
        }
    }

    public Optional<Artefact> findArtefact(String id) {
        return Optional.ofNullable(this.artefacts.get(id));
    }

    public Collection<Artefact> findAll() {
        return Collections.unmodifiableCollection(this.artefacts.values());
    }

}