package com.veritymimo.mixin;

import com.gabe.veritycartesia.config.CartesiaConfig;
import com.gabe.veritycartesia.tts.FallAudioService;
import com.gabe.veritycartesia.tts.SpeechPlayback;
import com.gabe.veritycartesia.tts.TtsRequestGate;
import com.gabe.veritycartesia.util.ThrowTts;
import com.gabe.veritycartesia.util.TtsSpeechText;
import com.veritymimo.config.MimoAddonConfig;
import com.veritymimo.tts.MimoTtsService;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import varmite.verity.VerityConfig;
import varmite.verity.entity.verity.VerityEntity;

@Pseudo
@Mixin(targets = "com.gabe.veritycartesia.tts.BetterVoiceTtsService", remap = false)
public abstract class BetterVoicePlayMixin {
    @Inject(method = "play", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private static void mimo$play(String text, Player player, VerityEntity verity, CallbackInfo ci) {
        try {
            if (!BetterVoicePlayMixin.isMimoActive()) {
                return;
            }
            if (!Boolean.TRUE.equals(VerityConfig.USE_TTS.get())) {
                ci.cancel();
                return;
            }
            if (!CartesiaConfig.isEnabled()) {
                ci.cancel();
                return;
            }
            if (text == null || text.isBlank()) {
                ci.cancel();
                return;
            }
            if (ThrowTts.shouldSkipThrowScream(verity, text)) {
                ci.cancel();
                return;
            }
            String spokenText = ThrowTts.isThrowScream(text) ? ThrowTts.withScreamingTag(text) : text;
            String ttsText = TtsSpeechText.forSpeech(spokenText);
            Integer verityEntityId = verity != null ? Integer.valueOf(verity.m_19879_()) : null;
            if (SpeechPlayback.isSameActiveLine(verityEntityId, spokenText)) {
                ci.cancel();
                return;
            }
            if (FallAudioService.isFallLine(spokenText)) {
                if (SpeechPlayback.isBusy() || SpeechPlayback.hasActiveLine() || verity != null && verity.clientIsTalking) {
                    ci.cancel();
                    return;
                }
                long fallGen = SpeechPlayback.beginOverlay();
                SpeechPlayback.setActiveLine(verityEntityId, text);
                FallAudioService.play(player, verity, fallGen);
                ci.cancel();
                return;
            }
            long generation = SpeechPlayback.beginNew();
            SpeechPlayback.setActiveLine(verityEntityId, spokenText);
            boolean acquired = TtsRequestGate.tryAcquire();
            if (!acquired) {
                try {
                    acquired = TtsRequestGate.tryAcquire(2L, TimeUnit.SECONDS);
                }
                catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    SpeechPlayback.clearBusy(generation);
                    ci.cancel();
                    return;
                }
            }
            if (!acquired) {
                SpeechPlayback.clearBusy(generation);
                ci.cancel();
                return;
            }
            CompletableFuture.runAsync(() -> {
                try {
                    if (!SpeechPlayback.isCurrent(generation)) {
                        return;
                    }
                    MimoTtsService.synthesizeAndPlay(ttsText, player, verity, verityEntityId, generation);
                }
                finally {
                    TtsRequestGate.release();
                }
            });
            ci.cancel();
        }
        catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private static boolean isMimoActive() {
        return Boolean.TRUE.equals(MimoAddonConfig.ENABLE_MIMO_TTS.get());
    }
}
