package com.veritymimo.direct;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import javax.sound.sampled.SourceDataLine;
import net.minecraft.world.entity.player.Player;
import varmite.verity.entity.verity.VerityEntity;

public final class VerityApi {
    private static final String[] CANDIDATES = {
        "varmite.verity.client.audio.TTSHandler",
        "varmite.verity.entity.llm.AiAPI",
        "varmite.verity.entity.LLM.AiAPI"
    };
    private static Class<?> aiApi;
    private static Method sanitizeTranscript;
    private static boolean sanitizeTranscriptResolved;

    private VerityApi() {
    }

    private static Class<?> aiApi() {
        if (aiApi == null) {
            for (String name : CANDIDATES) {
                try {
                    aiApi = Class.forName(name);
                    break;
                }
                catch (ClassNotFoundException ignored) {
                }
            }
        }
        return aiApi;
    }

    public static boolean isCancelRequested() {
        try {
            Class<?> c = VerityApi.aiApi();
            if (c == null) {
                return false;
            }
            Field f = c.getField("cancelCurrentSpeech");
            return f.getBoolean(null);
        }
        catch (Throwable t) {
            return false;
        }
    }

    public static void setCancelRequested(boolean value) {
        try {
            Class<?> c = VerityApi.aiApi();
            if (c == null) {
                return;
            }
            Field f = c.getField("cancelCurrentSpeech");
            f.setBoolean(null, value);
        }
        catch (Throwable ignored) {
        }
    }

    public static void apply3DEffect(SourceDataLine line, Player player, VerityEntity verity) {
        try {
            Class<?> c = VerityApi.aiApi();
            if (c == null) {
                return;
            }
            Method m = c.getMethod("apply3DEffect", SourceDataLine.class, Player.class, VerityEntity.class);
            m.invoke(null, line, player, verity);
        }
        catch (Throwable ignored) {
        }
    }

    /**
     * Post-processes a transcript exactly like Verity does for its own STT providers.
     *
     * <p>Verity 6.0.0 added {@code TTSHandler.sanitizeTranscript(String)} (it repairs
     * names such as "verity/birdie/..." that speech models mishear) and calls it at the
     * end of {@code transcribeAudio}. The MiMo mixin returns from HEAD, so without this
     * call MiMo transcripts would skip that cleanup. The method did not exist on
     * 6.0.0-beta.x, hence the reflective, optional lookup.
     */
    public static String sanitizeTranscript(String text) {
        if (text == null || text.isBlank()) {
            return text;
        }
        Method m = VerityApi.sanitizeMethod();
        if (m == null) {
            return text;
        }
        try {
            Object result = m.invoke(null, text);
            return result instanceof String ? (String) result : text;
        }
        catch (Throwable ignored) {
            return text;
        }
    }

    private static Method sanitizeMethod() {
        if (!sanitizeTranscriptResolved) {
            sanitizeTranscriptResolved = true;
            try {
                sanitizeTranscript = Class.forName("varmite.verity.client.audio.TTSHandler")
                    .getMethod("sanitizeTranscript", String.class);
            }
            catch (Throwable ignored) {
                sanitizeTranscript = null;
            }
        }
        return sanitizeTranscript;
    }
}
