package pl.karoldronia.artefacts.artefact.impl.ocean.ability;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import pl.karoldronia.artefacts.artefact.ability.Ability;
import pl.karoldronia.artefacts.artefact.ability.AbilityResult;
import pl.karoldronia.artefacts.artefact.impl.ocean.OceanArtefactConfig;
import pl.karoldronia.artefacts.notice.NoticeService;
import pl.karoldronia.artefacts.profile.Profile;

import java.time.Duration;
import java.util.List;

public class OceanArtefactAbility implements Ability {

    private final OceanArtefactConfig artefactConfig;
    private final NoticeService noticeService;

    public OceanArtefactAbility(OceanArtefactConfig artefactConfig, NoticeService noticeService) {
        this.artefactConfig = artefactConfig;
        this.noticeService = noticeService;
    }

    @Override
    public String getId() {
        return "ocean";
    }

    @Override
    public AbilityResult performAbility(Player player, Profile profile) {
        Location center = player.getLocation();
        World world = player.getWorld();

        for (int i = -45; i <= 45; i += 5) {
            Vector direction = player.getLocation().getDirection().clone().normalize();
            direction.rotateAroundY(Math.toRadians(i));
            Vector offset = direction.multiply(1.5);

            Location particleLoc = center.clone().add(offset.getX(), 0.1, offset.getZ());
            world.spawnParticle(Particle.BLOCK, particleLoc, 10, 0.2, 0.1, 0.2, Material.STONE.createBlockData());
            world.spawnParticle(Particle.SWEEP_ATTACK, particleLoc.add(0, 0.5, 0), 1);
        }

        world.playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 1.2f, 0.9f);
        world.playSound(center, Sound.ITEM_TRIDENT_THUNDER, 0.8f, 1.4f);

        List<Player> targets = getTargets(player, profile, this.artefactConfig.abilityRadius).stream()
                .filter(target -> isInFront(player, target))
                .toList();

        for (Player target : targets) {
            Vector knockback = target.getLocation().toVector().subtract(center.toVector()).normalize();
            knockback.setY(0.4);
            target.setVelocity(knockback.multiply(this.artefactConfig.pushForce));

            // Nausea
            target.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 20 * 5, 0));

            double newHealth = Math.max(0, target.getHealth() - this.artefactConfig.damage);
            target.setHealth(newHealth);

            target.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR, target.getLocation().add(0, 1, 0), 10);
        }

        this.noticeService.create()
                .player(player.getUniqueId())
                .notice(messages -> messages.oceanAbility)
                .placeholder("{affected}", String.valueOf(targets.size()))
                .send();

        return AbilityResult.SUCCESS;
    }

    @Override
    public Duration getCooldown() {
        return this.artefactConfig.abilityCooldown;
    }

    private boolean isInFront(Player source, Player target) {
        Vector directionToTarget = target.getLocation().toVector().subtract(source.getLocation().toVector()).normalize();
        Vector playerDirection = source.getLocation().getDirection().normalize();
        double angle = Math.toDegrees(playerDirection.angle(directionToTarget));
        return angle <= (90.0 / 2);
    }

}