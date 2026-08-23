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
}
