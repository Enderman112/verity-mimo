package com.veritymimo.direct.mixin;

import com.veritymimo.direct.stt.MimoSttService;
import javax.sound.sampled.AudioFormat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "varmite.verity.entity.llm.AiAPI", remap = false)
public abstract class AiAPITranscribeMixin {
    @Inject(method = "transcribeAudio", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private static void mimo$transcribe(byte[] pcmData, AudioFormat format, CallbackInfoReturnable<String> cir) {
        try {
            if (!MimoSttService.isActive()) {
                return;
            }
            cir.setReturnValue(MimoSttService.transcribe(pcmData, format));
        }
        catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
