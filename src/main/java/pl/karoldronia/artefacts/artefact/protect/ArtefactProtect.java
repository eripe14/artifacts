package pl.karoldronia.artefacts.artefact.protect;

import eu.okaeri.persistence.document.Document;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bukkit.Location;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
public class ArtefactProtect extends Document {

    private double minX, minY, minZ;
    private double maxX, maxY, maxZ;
    private String worldName;

    public UUID getUniqueId() {
        return this.getPath().toUUID();
    }

    public boolean contains(Location location) {
        if (!this.worldName.equals(location.getWorld().getName())) {
            return false;
        }

        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();

        return x >= this.minX && x <= this.maxX &&
                y >= this.minY && y <= this.maxY &&
                z >= this.minZ && z <= this.maxZ;
    }

}
