package jp.kotoba.client.mixin;

import jp.kotoba.client.KotobaClient;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
abstract class EntityMixin {
    @Inject(method = "getCustomName", at = @At("RETURN"), cancellable = true)
    private void kotoba$translateHologramName(CallbackInfoReturnable<Component> cir) {
        Component original = cir.getReturnValue();
        if (original != null) cir.setReturnValue(KotobaClient.translateHologram(original));
    }
}
