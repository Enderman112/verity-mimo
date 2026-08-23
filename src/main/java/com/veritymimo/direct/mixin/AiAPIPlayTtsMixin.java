package com.veritymimo.direct.mixin;

import com.veritymimo.direct.tts.MimoTtsService;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import varmite.verity.entity.verity.VerityEntity;

@Pseudo
@Mixin(targets = {"varmite.verity.client.audio.TTSHandler", "varmite.verity.entity.llm.AiAPI", "varmite.verity.entity.LLM.AiAPI"}, remap = false)
public abstract class AiAPIPlayTtsMixin {
    @Inject(method = "playTTS", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private static void mimo$playTts(String text, Player player, VerityEntity verity, CallbackInfo ci) {
        try {
            if (!MimoTtsService.isActive()) {
                return;
            }
            MimoTtsService.synthesizeAndPlay(text, player, verity);
            ci.cancel();
        }
        catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
