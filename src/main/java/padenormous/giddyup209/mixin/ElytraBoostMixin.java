package padenormous.giddyup209.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FireworkRocketItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FireworkRocketItem.class)
public abstract class ElytraBoostMixin {
    // Vanilla speed for 1 rocket
    private static final double SINGLE_ROCKET_SPEED = 1.6;

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void onFireworkUse(World world, PlayerEntity player, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        if (!world.isClient) {
            ItemStack fireworks = player.getStackInHand(hand);

            if (player.isFallFlying() && fireworks.isOf(Items.FIREWORK_ROCKET)) {
                boolean useFullStack = player.isSneaking() && hand == Hand.MAIN_HAND;
                int fireworksToUse = useFullStack ? fireworks.getCount() : 1;

                // Send message about rockets activated
                String rocketText = fireworksToUse == 1 ? "Rocket" : "Rockets";
                player.sendMessage(Text.literal("§a" + fireworksToUse + " " + rocketText + " Activated!"), true);

                // Get the player's look vector
                Vec3d lookVec = player.getRotationVector();

                // Calculate speed:
                // For single rocket: vanilla 1.6 blocks per tick (32 blocks per second)
                // For multiple rockets: (31.25 blocks per rocket per second)
                // So 64 rockets = 2000 blocks per second
                double blocksPerSecond;
                if (fireworksToUse == 1) {
                    blocksPerSecond = SINGLE_ROCKET_SPEED * 20; // Convert vanilla tick speed to blocks per second
                } else {
                    blocksPerSecond = 31.25 * fireworksToUse; // 31.25 * 64 = 2000
                }

                // Convert blocks per second to blocks per tick for Minecraft
                double blocksPerTick = blocksPerSecond / 20.0;

                // Debug message showing actual speed
                player.sendMessage(Text.literal("§eSpeed: " + String.format("%.1f", blocksPerSecond) + " blocks/second"), true);

                // Apply the velocity
                Vec3d newVelocity = lookVec.multiply(blocksPerTick);
                player.setVelocity(newVelocity);
                player.velocityModified = true;

                // Reset fall distance for safety
                player.fallDistance = 0.0f;

                // Disable collisions temporarily for high speeds
                if (fireworksToUse > 1) {
                    player.noClip = true;
                    world.getServer().execute(() -> {
                        player.noClip = false;
                    });
                }

                // Consume fireworks
                fireworks.setCount(fireworks.getCount() - fireworksToUse);

                // Play sound effect
                world.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.ENTITY_FIREWORK_ROCKET_LAUNCH,
                        SoundCategory.PLAYERS,
                        1.0F,
                        1.0F);

                cir.setReturnValue(TypedActionResult.success(fireworks));
            }
        }
    }
}