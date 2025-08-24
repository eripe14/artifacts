package pl.karoldronia.artefacts.artefact.impl.dragon;

import org.bukkit.Location;

import java.util.UUID;

public class DragonBreathArea {
    private final UUID playerId;
    private Location currentLocation;
    private final int durationSeconds;
    private final double damagePerTick;
    private int currentTick = 0;
    private boolean active = true;

    public DragonBreathArea(UUID playerId, int durationSeconds, double damagePerTick) {
        this.playerId = playerId;
        this.durationSeconds = durationSeconds;
        this.damagePerTick = damagePerTick;
    }

    public void updateLocation(Location newLocation) {
        this.currentLocation = newLocation.clone();
    }

    public void tick() {
        currentTick++;
        if (currentTick >= durationSeconds) {
            active = false;
        }
    }

    // Getters
    public UUID getPlayerId() { return playerId; }
    public Location getCurrentLocation() { return currentLocation; }
    public double getDamagePerTick() { return damagePerTick; }
    public int getCurrentTick() { return currentTick; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}