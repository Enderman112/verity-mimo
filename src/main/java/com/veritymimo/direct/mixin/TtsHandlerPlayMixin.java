package com.veritymimo.direct.mixin;

import com.veritymimo.direct.tts.MimoTtsService;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import varmite.verity.VerityConfig;
import varmite.verity.entity.verity.VerityEntity;

/**
 * Hooks Verity's single client-side speech entry point.
 *
 * <p>Verity 6.0.0-beta.7 moved TTS/STT out of {@code AiAPI} into
 * {@code varmite.verity.client.audio.TTSHandler}. The client plays a
 * {@code PlayTtsPayload} through {@code PlayTtsClientHandler.handlePacket}, which
 * calls {@code TTSHandler.playTTS} for every line of speech, so this is the only
 * hook the mod needs. The AiAPI fallback targets are gone from the supported
 * version range; {@code require = 1} makes a future rename/signature change fail
 * loudly instead of silently disabling the mod. {@code @Pseudo} keeps the mod
 * loadable on pre-beta.7 builds that still used {@code AiAPI} only.
 */
@Pseudo
@Mixin(targets = "varmite.verity.client.audio.TTSHandler", remap = false)
public abstract class TtsHandlerPlayMixin {
    @Inject(method = "playTTS", at = @At("HEAD"), cancellable = true, remap = false, require = 1)
    private static void mimo$playTts(String text, Player player, VerityEntity verity, CallbackInfo ci) {
        try {
            if (!MimoTtsService.isActive()) {
                return;
            }
            // playTTS checks USE_TTS itself; bail out before cancelling so that
            // turning Verity's TTS off really disables all speech.
            if (!Boolean.TRUE.equals(VerityConfig.USE_TTS.get())) {
                return;
            }
            if (text == null || text.isBlank()) {
                return;
            }
            ci.cancel();
            MimoTtsService.synthesizeAndPlay(text, player, verity);
        }
        catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
