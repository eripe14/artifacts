package pl.karoldronia.artefacts.artefact.protect;

import org.bukkit.Location;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class ArtefactProtectService {

    private final ArtefactProtectRepository repository;
    private final Map<UUID, ArtefactProtect> protects = new ConcurrentHashMap<>();

    public ArtefactProtectService(ArtefactProtectRepository repository) {
        this.repository = repository;

        this.loadAll();
    }

    public void createProtect(
            double minX,
            double minY,
            double minZ,
            double maxX,
            double maxY,
            double maxZ,
            String worldName
    ) {
        ArtefactProtect artefactProtect = this.repository.findOrCreateByPath(UUID.randomUUID());
        artefactProtect.setMinX(minX);
        artefactProtect.setMinY(minY);
        artefactProtect.setMinZ(minZ);
        artefactProtect.setMaxX(maxX);
        artefactProtect.setMaxY(maxY);
        artefactProtect.setMaxZ(maxZ);
        artefactProtect.setWorldName(worldName);

        this.repository.save(artefactProtect);
        this.protects.put(artefactProtect.getUniqueId(), artefactProtect);
    }

    public boolean isInProtect(Location location) {
        for (ArtefactProtect protect : this.protects.values()) {
            if (protect.contains(location)) {
                return true;
            }
        }

        return false;
    }

    private CompletableFuture<Void> loadAll() {
        return CompletableFuture.supplyAsync(() -> {
            for (ArtefactProtect artefactProtect : this.repository.findAll()) {
                this.protects.put(artefactProtect.getUniqueId(), artefactProtect);
            }

            return null;
        });
    }
}
