package com.veritymimo.direct.mixin;

import com.veritymimo.direct.VerityApi;
import com.veritymimo.direct.stt.MimoSttService;
import javax.sound.sampled.AudioFormat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Takes over microphone transcription at {@code TTSHandler.transcribeAudio}.
 *
 * <p>Verity 6.0.0 applies {@code TTSHandler.sanitizeTranscript} to every provider's
 * result (it fixes "Verity" being misheard as "verdi/birdie/..."). A HEAD
 * injection bypasses that post-processing, so the result is passed through the same
 * helper via {@link VerityApi#sanitizeTranscript(String)}. The helper is resolved
 * reflectively so the mod still works on 6.0.0-beta.x builds without it.
 */
@Pseudo
@Mixin(targets = "varmite.verity.client.audio.TTSHandler", remap = false)
public abstract class TtsHandlerTranscribeMixin {
    @Inject(method = "transcribeAudio", at = @At("HEAD"), cancellable = true, remap = false, require = 1)
    private static void mimo$transcribe(byte[] pcmData, AudioFormat format, CallbackInfoReturnable<String> cir) {
        try {
            if (!MimoSttService.isActive()) {
                return;
            }
            cir.setReturnValue(VerityApi.sanitizeTranscript(MimoSttService.transcribe(pcmData, format)));
        }
        catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
