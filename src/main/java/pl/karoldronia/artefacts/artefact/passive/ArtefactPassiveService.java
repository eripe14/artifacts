package pl.karoldronia.artefacts.artefact.passive;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import pl.karoldronia.artefacts.artefact.Artefact;
import pl.karoldronia.artefacts.artefact.ArtefactService;
import pl.karoldronia.artefacts.artefact.item.ArtefactItemsUtil;
import pl.karoldronia.artefacts.profile.Profile;
import pl.karoldronia.artefacts.profile.ProfileRepository;
import pl.karoldronia.artefacts.scheduler.Scheduler;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ArtefactPassiveService {

    private final Scheduler scheduler;
    private final ArtefactService artefactService;
    private final ProfileRepository profileRepository;

    private volatile long lastCacheUpdate = 0;
    private static final long CACHE_REFRESH_INTERVAL = 30_000; // 30 sec

    private final Map<UUID, String> cachedArtefactIds = new ConcurrentHashMap<>();
    private final Map<UUID, Set<PotionEffectType>> activeEffects = new ConcurrentHashMap<>();

    public ArtefactPassiveService(Scheduler scheduler, ArtefactService artefactService, ProfileRepository profileRepository) {
        this.scheduler = scheduler;
        this.artefactService = artefactService;
        this.profileRepository = profileRepository;
    }

    public void passiveEffects() {
        this.scheduler.timerSync(() -> {
            try {
                refreshCacheIfNeeded();
                processAllPlayers();
            } catch (Exception exception) {
                System.out.println(exception.getMessage());
            }
        }, Duration.ofSeconds(1), Duration.ofSeconds(1));
    }

    private void processAllPlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            try {
                processPlayer(player);
            } catch (Exception e) {
                System.out.println("Error processing player " + player.getName() + ": " + e.getMessage());
            }
        }
    }

    private void processPlayer(Player player) {
        UUID playerId = player.getUniqueId();
        String artefactId = cachedArtefactIds.get(playerId);
        Set<PotionEffectType> currentEffects = activeEffects.getOrDefault(playerId, new HashSet<>());

        if (artefactId == null) {
            if (!currentEffects.isEmpty()) {
                removeAllEffects(player, currentEffects);
                activeEffects.remove(playerId);
            }
            return;
        }

        Optional<Artefact> artefactOpt = artefactService.findArtefact(artefactId);
        if (artefactOpt.isEmpty()) {
            if (!currentEffects.isEmpty()) {
                removeAllEffects(player, currentEffects);
                activeEffects.remove(playerId);
            }
            return;
        }

        Artefact artefact = artefactOpt.get();
        boolean isHoldingArtefact = ArtefactItemsUtil.isHoldingArtefact(artefact.getId(), player);

        if (isHoldingArtefact) {
            List<PotionEffectType> artefactEffects = artefact.givePassiveEffects(player);
            Set<PotionEffectType> shouldHaveEffects = new HashSet<>(artefactEffects);

            if (!currentEffects.equals(shouldHaveEffects)) {
                Set<PotionEffectType> toRemove = new HashSet<>(currentEffects);
                toRemove.removeAll(shouldHaveEffects);

                for (PotionEffectType effect : toRemove) {
                    player.removePotionEffect(effect);
                }

                Set<PotionEffectType> toAdd = new HashSet<>(shouldHaveEffects);
                toAdd.removeAll(currentEffects);

                if (!toAdd.isEmpty()) {
                    artefact.givePassiveEffects(player);
                }

                activeEffects.put(playerId, shouldHaveEffects);
            }
        } else {
            if (!currentEffects.isEmpty()) {
                removeAllEffects(player, currentEffects);
                activeEffects.remove(playerId);
            }
        }
    }

    private void removeAllEffects(Player player, Set<PotionEffectType> effects) {
        for (PotionEffectType effect : effects) {
            player.removePotionEffect(effect);
        }
    }

    private void refreshCacheIfNeeded() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastCacheUpdate > CACHE_REFRESH_INTERVAL) {
            // To może być w async jeśli chcesz - ale wtedy przenieś to poza timerSync
            List<Profile> allProfiles = this.profileRepository.findAll().stream().toList();

            Map<UUID, String> newCache = new HashMap<>();
            for (Profile profile : allProfiles) {
                if (profile.getArtefactId() != null && !profile.getArtefactId().trim().isEmpty()) {
                    newCache.put(profile.getUniqueId(), profile.getArtefactId());
                }
            }

            cachedArtefactIds.clear();
            cachedArtefactIds.putAll(newCache);

            lastCacheUpdate = currentTime;
        }
    }

    // Metoda do natychmiastowego sprawdzenia gracza - dla eventów
    public void checkPlayerImmediately(Player player) {
        if (player != null && player.isOnline()) {
            processPlayer(player);
        }
    }

    public void invalidatePlayerCache(UUID playerId) {
        cachedArtefactIds.remove(playerId);
        //activeEffects.remove(playerId);
    }

    public void forceRefreshCache() {
        lastCacheUpdate = 0;
        cachedArtefactIds.clear();
        activeEffects.clear();
    }

    public int getCachedPlayersCount() {
        return cachedArtefactIds.size();
    }

    public int getActiveEffectsCount() {
        return activeEffects.values().stream()
                .mapToInt(Set::size)
                .sum();
    }

    public void updatePlayerCache(UUID playerId, String artefactId) {
        if (artefactId != null && !artefactId.trim().isEmpty()) {
            cachedArtefactIds.put(playerId, artefactId);
        } else {
            cachedArtefactIds.remove(playerId);
        }
    }

    public void onPlayerQuit(UUID playerId) {
        activeEffects.remove(playerId);
    }

    public void onPlayerJoin(UUID playerId) {
        activeEffects.remove(playerId);
    }
}