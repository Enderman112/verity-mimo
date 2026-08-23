package com.veritymimo.mixin;

import com.veritymimo.config.MimoAddonConfig;
import com.veritymimo.stt.MimoSttService;
import javax.sound.sampled.AudioFormat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import varmite.verity.VerityConfig;

@Pseudo
@Mixin(targets = {"varmite.verity.client.audio.TTSHandler", "varmite.verity.entity.llm.AiAPI", "varmite.verity.entity.LLM.AiAPI"}, remap = false)
public abstract class AiAPITranscribeMixin {
    @Inject(method = "transcribeAudio", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private static void mimo$transcribe(byte[] pcmData, AudioFormat format, CallbackInfoReturnable<String> cir) {
        try {
            if (!AiAPITranscribeMixin.isMimoAsrActive()) {
                return;
            }
            cir.setReturnValue(MimoSttService.transcribe(pcmData, format));
        }
        catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private static boolean isMimoAsrActive() {
        return Boolean.TRUE.equals(MimoAddonConfig.ENABLE_MIMO_ASR.get());
    }
}
