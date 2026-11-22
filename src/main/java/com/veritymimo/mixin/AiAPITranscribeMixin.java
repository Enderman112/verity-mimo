package com.veritymimo.mixin;

import com.veritymimo.VerityApi;
import com.veritymimo.config.MimoAddonConfig;
import com.veritymimo.stt.MimoSttService;
import javax.sound.sampled.AudioFormat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import varmite.verity.VerityConfig;

/**
 * Takes over microphone transcription at {@code TTSHandler.transcribeAudio}.
 *
 * <p>Verity 6.0.0 applies {@code TTSHandler.sanitizeTranscript} to every provider's
 * result (it fixes "Verity" being misheard as "verdi/birdie/..."). A HEAD injection
 * bypasses that post-processing, so the result is passed through the same helper via
 * {@link VerityApi#sanitizeTranscript(String)}. The helper is resolved reflectively so
 * the addon still works on 6.0.0-beta.9, which does not have it.
 */
@Pseudo
@Mixin(targets = {"varmite.verity.client.audio.TTSHandler", "varmite.verity.entity.llm.AiAPI", "varmite.verity.entity.LLM.AiAPI"}, remap = false)
public abstract class AiAPITranscribeMixin {
    @Inject(method = "transcribeAudio", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private static void mimo$transcribe(byte[] pcmData, AudioFormat format, CallbackInfoReturnable<String> cir) {
        try {
            if (!AiAPITranscribeMixin.isMimoAsrActive()) {
                return;
            }
            cir.setReturnValue(VerityApi.sanitizeTranscript(MimoSttService.transcribe(pcmData, format)));
        }
        catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private static boolean isMimoAsrActive() {
        return Boolean.TRUE.equals(MimoAddonConfig.ENABLE_MIMO_ASR.get());
    }
}
