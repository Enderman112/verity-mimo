package com.veritymimo.tts;

import com.veritymimo.VerityApi;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import net.minecraft.world.entity.player.Player;
import varmite.verity.entity.verity.VerityEntity;

/**
 * Shared playback layer for every TTS backend (MiMo and DeepSeek).
 *
 * <p>Plays a WAV through a {@link SourceDataLine}, applies Verity's 3D attenuation
 * via {@code TTSHandler.apply3DEffect} and honours {@code cancelCurrentSpeech} for
 * interrupts. Keeping this separate means both backends behave identically once
 * they hand us audio bytes, even though their HTTP responses differ.
 */
public final class TtsPlayback {
    private static final Object LOCK = new Object();
    private static SourceDataLine activeLine;

    private TtsPlayback() {
    }

    public static void playWav(byte[] wavBytes, Player player, VerityEntity verity) {
        SourceDataLine line = null;
        try (ByteArrayInputStream bais = new ByteArrayInputStream(wavBytes);
             AudioInputStream audioStream = AudioSystem.getAudioInputStream(new BufferedInputStream(bais))) {
            AudioFormat format = audioStream.getFormat();
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            // Stop the previous line first: Verity can send a new line before the
            // previous one finished (it does not call interruptSpeech on the client),
            // and SourceDataLine is exclusive, so opening a second line would fail.
            TtsPlayback.stopActivePlayback();
            line = (SourceDataLine)AudioSystem.getLine(info);
            line.open(format);
            line.start();
            TtsPlayback.activatePlayback(line, verity);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = audioStream.read(buffer)) != -1) {
                if (VerityApi.isCancelRequested()) {
                    line.flush();
                    break;
                }
                VerityApi.apply3DEffect(line, player, verity);
                line.write(buffer, 0, bytesRead);
            }
            if (!VerityApi.isCancelRequested()) {
                line.drain();
            }
        }
        catch (Exception e) {
            if (!TtsPlayback.wasSuperseded(line) && (line == null || line.isOpen())) {
                System.err.println("[Verity MiMo TTS] Failed to play audio.");
                e.printStackTrace();
            }
        }
        finally {
            TtsPlayback.releasePlayback(line, verity);
            if (line != null) {
                try {
                    line.close();
                }
                catch (Throwable ignored) {
                }
            }
        }
    }

    public static void clearTalkingIfIdle(VerityEntity verity) {
        synchronized (LOCK) {
            if (verity != null && activeLine == null) {
                verity.clientIsTalking = false;
            }
        }
    }

    private static void stopActivePlayback() {
        SourceDataLine line;
        synchronized (LOCK) {
            line = activeLine;
            activeLine = null;
        }
        if (line != null) {
            try {
                line.stop();
            }
            catch (Throwable ignored) {
            }
            try {
                line.flush();
            }
            catch (Throwable ignored) {
            }
            try {
                line.close();
            }
            catch (Throwable ignored) {
            }
        }
    }

    private static void activatePlayback(SourceDataLine line, VerityEntity verity) {
        synchronized (LOCK) {
            activeLine = line;
            if (verity != null) {
                verity.clientIsTalking = true;
            }
        }
    }

    private static boolean wasSuperseded(SourceDataLine line) {
        synchronized (LOCK) {
            return activeLine != null && activeLine != line;
        }
    }

    private static void releasePlayback(SourceDataLine line, VerityEntity verity) {
        synchronized (LOCK) {
            if (line != null && activeLine == line) {
                activeLine = null;
                if (verity != null) {
                    verity.clientIsTalking = false;
                }
            }
        }
    }
}
