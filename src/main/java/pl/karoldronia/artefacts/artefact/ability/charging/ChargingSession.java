package pl.karoldronia.artefacts.artefact.ability.charging;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import pl.karoldronia.artefacts.profile.Profile;

public class ChargingSession {

    private static final int TOTAL_PROGRESS_BARS = 20;
    private static final String PROGRESS_CHAR = "█";
    private static final String FRAME_COLOR = "§8";
    private static final String EMPTY_COLOR = "§7";

    private final Player player;
    private final Profile profile;
    private final ChargingAbility ability;
    private final long totalTicks;
    private final Plugin plugin;
    private final Runnable onStop;

    private BukkitTask chargingTask;
    private long currentTicks = 0;

    public ChargingSession(Player player, Profile profile, ChargingAbility ability, long totalTicks, Plugin plugin, Runnable onStop) {
        this.player = player;
        this.profile = profile;
        this.ability = ability;
        this.totalTicks = totalTicks;
        this.plugin = plugin;
        this.onStop = onStop;
    }

    public void start() {
        chargingTask = new BukkitRunnable() {
            @Override
            public void run() {
                currentTicks++;

                // Calculate progress
                double progress = Math.min(1.0, (double) currentTicks / totalTicks);
                int percentage = (int) (progress * 100);

                // Create progress bar for action bar
                String progressBar = createProgressBar(progress);
                String actionBarMessage = String.format("§bCharging Ability... %s §f%d%%", progressBar, percentage);

                // Send action bar message
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(actionBarMessage));

                // Check if player is still online
                if (!player.isOnline()) {
                    cancel();
                    onStop.run();
                    return;
                }

                // Check if charging is complete
                if (currentTicks >= totalTicks) {
                    onChargingComplete();
                }
            }
        }.runTaskTimer(plugin, 0L, 1L); // Run every tick
    }

    public void cancel() {
        if (chargingTask != null && !chargingTask.isCancelled()) {
            chargingTask.cancel();
        }
        clearActionBar();
    }

    public boolean isActive() {
        return chargingTask != null && !chargingTask.isCancelled();
    }

    public double getProgress() {
        return Math.min(1.0, (double) currentTicks / totalTicks);
    }

    public Player getPlayer() {
        return this.player;
    }

    private void onChargingComplete() {
        // Charging complete - show completion message
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("§aAbility Charged!"));

        // Execute ability after small delay for visual feedback
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            ability.performAbility(player, profile);
            onStop.run();
        }, 10L); // 0.5 second delay

        cancel();
    }

    private void clearActionBar() {
        if (player.isOnline()) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(""));
        }
    }

    private String createProgressBar(double progress) {
        int filledBars = (int) (progress * TOTAL_PROGRESS_BARS);

        StringBuilder progressBar = new StringBuilder(FRAME_COLOR + "[");

        for (int i = 0; i < TOTAL_PROGRESS_BARS; i++) {
            if (i < filledBars) {
                progressBar.append(getProgressColor(progress)).append(PROGRESS_CHAR);
            } else {
                progressBar.append(EMPTY_COLOR).append(PROGRESS_CHAR);
            }
        }

        progressBar.append(FRAME_COLOR).append("]");
        return progressBar.toString();
    }

    private String getProgressColor(double progress) {
        if (progress < 0.33) {
            return "§c"; // Red
        } else if (progress < 0.66) {
            return "§e"; // Yellow
        } else if (progress < 1.0) {
            return "§b"; // Blue
        } else {
            return "§a"; // Green
        }
    }
}