package padenormous.giddyup209.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FireworkRocketItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import padenormous.giddyup209.RocketBoostTracker;

@Mixin(FireworkRocketItem.class)
public abstract class ElytraBoostMixin {

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void onFireworkUse(World world, PlayerEntity player, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        if (!world.isClient) {
            ItemStack fireworks = player.getStackInHand(hand);

            if (player.isFallFlying() && fireworks.isOf(Items.FIREWORK_ROCKET)) {
                boolean useFullStack = player.isSneaking() && hand == Hand.MAIN_HAND;
                int fireworksToUse = useFullStack ? fireworks.getCount() : 1;

                // Start the physics-based boost
                RocketBoostTracker.startBoost(player, fireworksToUse);

                // Consume fireworks
                fireworks.setCount(fireworks.getCount() - fireworksToUse);

                cir.setReturnValue(TypedActionResult.success(fireworks));
            }
        }
    }
}