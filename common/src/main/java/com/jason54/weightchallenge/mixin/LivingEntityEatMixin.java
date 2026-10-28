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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityEatMixin {
    @Shadow
    private ItemStack useItem;

    /** 1.21.x: completeUsingItem returns ItemStack */
    @Inject(method = "completeUsingItem()Lnet/minecraft/world/item/ItemStack;", at = @At("TAIL"), require = 0)
    private void weightchallenge$onEatReturn(CallbackInfoReturnable<ItemStack> cir) {
        weightchallenge$handleEat();
    }

    /** 26.x: completeUsingItem returns void */
    @Inject(method = "completeUsingItem()V", at = @At("TAIL"), require = 0)
    private void weightchallenge$onEatVoid(CallbackInfo ci) {
        weightchallenge$handleEat();
    }

    private void weightchallenge$handleEat() {
        LivingEntity self = (LivingEntity) (Object) this;
        if (self instanceof ServerPlayer player && this.useItem != null && !this.useItem.isEmpty()) {
            WeightLogic.onFoodEaten(player, this.useItem.copy());
        }
    }
}
