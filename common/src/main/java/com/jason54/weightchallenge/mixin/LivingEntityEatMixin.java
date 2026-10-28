package com.jason54.weightchallenge.mixin;

import com.jason54.weightchallenge.weight.WeightLogic;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityEatMixin {
    @Shadow
    private ItemStack useItem;

    @Inject(method = "completeUsingItem", at = @At("TAIL"))
    private void weightchallenge$onEat(CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (self instanceof ServerPlayer player && this.useItem != null && !this.useItem.isEmpty()) {
            WeightLogic.onFoodEaten(player, this.useItem.copy());
        }
    }
}
