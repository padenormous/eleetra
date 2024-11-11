package padenormous.giddyup209.mixin;

import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import padenormous.giddyup209.RocketBoostTracker;

@Mixin(PlayerEntity.class)
public abstract class PlayerTickMixin {
    @Inject(method = "tick", at = @At("HEAD"))
    private void onPlayerTick(CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity)(Object)this;
        if (player.isFallFlying()) {
            RocketBoostTracker.onPlayerTick(player);
        }
    }
}