package com.jason54.weightchallenge.mixin;

import com.jason54.weightchallenge.weight.WeightLogic;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityFallDamageMixin {
    /**
     * 1.21.11+ / 26.x: {@code calculateFallDamage(double, float) -> int}.
     */
    @Inject(method = "calculateFallDamage(DF)I", at = @At("RETURN"), cancellable = true)
    private void weightchallenge$heavyFall(double fallDistance, float damageMultiplier, CallbackInfoReturnable<Integer> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (self instanceof ServerPlayer player) {
            cir.setReturnValue(WeightLogic.modifyFallDamage(player, cir.getReturnValue()));
        }
    }
}
