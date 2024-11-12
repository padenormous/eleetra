package padenormous.giddyup209;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Box;
import net.minecraft.entity.data.DataTracker;
import java.util.ArrayList;
import java.util.List;

public class RocketBoostTracker {
    public static int remainingBoosts = 0;
    public static PlayerEntity activePlayer = null;
    private static final int BOATS_PER_BOOST = 60;
    private static final List<BoatEntity> activeBoats = new ArrayList<>();

    public static void startBoost(PlayerEntity player, int fireworkCount) {
        // Clear any existing boats first
        clearBoostEntities();

        remainingBoosts = fireworkCount;
        activePlayer = player;

        // Create boost entities in player's direction
        Vec3d lookDir = player.getRotationVector();
        Vec3d playerPos = player.getPos();
        Vec3d boostPos = playerPos.add(lookDir.multiply(0.5));

        // Stack multiple boats at the boost position
        for (int i = 0; i < fireworkCount * BOATS_PER_BOOST; i++) {
            BoatEntity boat = new BoatEntity(EntityType.BOAT, player.getWorld());
            boat.setPosition(boostPos.x, boostPos.y, boostPos.z);
            boat.setInvisible(true);
            boat.setNoGravity(true);
            // Make the boat non-interactive
            boat.setInvulnerable(true);
            boat.setSilent(true);
            player.getWorld().spawnEntity(boat);
            activeBoats.add(boat);
        }

        // Schedule cleanup after a short delay
        player.getWorld().getServer().execute(() -> {
            clearBoostEntities();
        });

        // Play rocket sound
        player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENTITY_FIREWORK_ROCKET_LAUNCH,
                SoundCategory.PLAYERS,
                1.0F,
                1.0F);
    }

    public static void clearBoostEntities() {
        // Remove all tracked boats
        for (BoatEntity boat : activeBoats) {
            boat.discard();
        }
        activeBoats.clear();
    }

    public static void onPlayerTick(PlayerEntity player) {
        if (remainingBoosts > 0) {
            Vec3d lookDir = player.getRotationVector();
            Vec3d currentVel = player.getVelocity();

            double boostScale = remainingBoosts * 0.1;
            Vec3d additionalBoost = lookDir.multiply(boostScale);

            player.addVelocity(additionalBoost.x, additionalBoost.y, additionalBoost.z);
            player.velocityModified = true;

            remainingBoosts--;

            if (remainingBoosts <= 0) {
                clearBoostEntities();
                activePlayer = null;
            }
        }
    }
}