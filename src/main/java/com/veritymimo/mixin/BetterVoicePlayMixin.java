package com.veritymimo.mixin;

import com.veritymimo.config.MimoAddonConfig;
import com.veritymimo.tts.MimoTtsService;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import varmite.verity.VerityConfig;
import varmite.verity.entity.verity.VerityEntity;

/**
 * Hooks Smiley's Better Voice 6.0.0-beta.9+ so MiMo can take over Verity's speech.
 *
 * <p>Better Voice beta.9 rewrote its integration: its own TtsHandlerMixin injects into
 * {@code varmite.verity.client.audio.TTSHandler.playTTS} and delegates to
 * {@code FishAudioManager.handleSpeech(...)}, cancelling Verity's native TTS only when
 * that call returns true. Hooking {@code handleSpeech} keeps us inside Better Voice's
 * pipeline and lets it own the cancel decision, instead of competing for the same HEAD
 * of {@code TTSHandler.playTTS}.
 *
 * <p>The previous Better Voice (6.0.0, package {@code com.gabe.veritycartesia}) exposed
 * {@code BetterVoiceTtsService.play} plus a public SpeechPlayback/WavPlayer pipeline.
 * None of those classes exist any more, so the playback layer now lives in
 * {@link MimoTtsService}.
 */
@Pseudo
@Mixin(targets = "com.toast5.smileysbettervoice.client.FishAudioManager", remap = false)
public abstract class BetterVoicePlayMixin {
    @Inject(method = "handleSpeech", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private static void mimo$handleSpeech(String text, Player player, VerityEntity verity, CallbackInfoReturnable<Boolean> cir) {
        try {
            if (!Boolean.TRUE.equals(MimoAddonConfig.ENABLE_MIMO_TTS.get())) {
                return;
            }
            // Better Voice injects at the HEAD of TTSHandler.playTTS, i.e. before Verity
            // itself checks USE_TTS, so the guard has to live here.
            if (!Boolean.TRUE.equals(VerityConfig.USE_TTS.get())) {
                return;
            }
            if (text == null || text.isBlank()) {
                return;
            }
            // Claim the line: reporting true makes Better Voice cancel Verity's native TTS,
            // which is what we want while MiMo plays the audio instead.
            cir.setReturnValue(Boolean.TRUE);
            MimoTtsService.synthesizeAndPlay(text, player, verity);
        }
        catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
