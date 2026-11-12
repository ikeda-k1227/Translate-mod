package jp.kotoba.client.mixin;

import jp.kotoba.client.KotobaClient;
import net.minecraft.network.chat.Component;
import net.minecraft.world.scores.Objective;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Objective.class)
abstract class ObjectiveMixin {
    @Inject(method = "getDisplayName", at = @At("RETURN"), cancellable = true)
    private void kotoba$translateScoreboardTitle(CallbackInfoReturnable<Component> cir) {
        Component original = cir.getReturnValue();
        if (original != null) cir.setReturnValue(KotobaClient.translateScoreboard(original));
    }

    @Inject(method = "getFormattedDisplayName", at = @At("RETURN"), cancellable = true)
    private void kotoba$translateFormattedScoreboardTitle(CallbackInfoReturnable<Component> cir) {
        Component original = cir.getReturnValue();
        if (original != null) cir.setReturnValue(KotobaClient.translateScoreboard(original));
    }
}
