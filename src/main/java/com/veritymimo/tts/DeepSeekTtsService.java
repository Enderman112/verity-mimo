package com.veritymimo.tts;

import com.google.gson.JsonObject;
import com.veritymimo.MimoNotifier;
import com.veritymimo.VerityApi;
import com.veritymimo.config.MimoAddonConfig;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import net.minecraft.world.entity.player.Player;
import varmite.verity.VerityConfig;
import varmite.verity.entity.verity.VerityEntity;

/**
 * DeepSeek TTS backend.
 *
 * <p>{@code POST /v1/tools/tts} takes a JSON body and returns the audio bytes
 * directly ({@code audio/wav} by default). That is very different from MiMo, which
 * wraps a base64 payload inside an OpenAI-style chat response, so the request and
 * response handling live here instead of being shared.
 *
 * <p>DeepSeek does not support a speed parameter, so {@code mimoTtsSpeed} only
 * affects the MiMo backend.
 */
public final class DeepSeekTtsService {
    private static volatile HttpClient httpClient;

    private DeepSeekTtsService() {
    }

    public static void synthesizeAndPlay(String text, Player player, VerityEntity verity) {
        if (!Boolean.TRUE.equals(VerityConfig.USE_TTS.get())) {
            return;
        }
        if (text == null || text.isBlank()) {
            return;
        }
        CompletableFuture.runAsync(() -> {
            VerityApi.setCancelRequested(false);
            try {
                String apiKey = (String) MimoAddonConfig.DEEPSEEK_API_KEY.get();
                if (apiKey == null || apiKey.isBlank()) {
                    MimoNotifier.notify(player, "Verity MiMo: no DeepSeek API key set. See the Xiaomi MiMo category in Verity's config.");
                    return;
                }
                JsonObject body = new JsonObject();
                body.addProperty("text", text);
                body.addProperty("voice_id", MimoAddonConfig.DEEPSEEK_TTS_VOICE.get().id());
                body.addProperty("format", "wav");
                body.addProperty("filter_markdown", Boolean.TRUE);
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(MimoAddonConfig.resolveDeepSeekUrl() + "/v1/tools/tts"))
                    .timeout(Duration.ofSeconds(90L))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                    .build();
                HttpResponse<byte[]> response = DeepSeekTtsService.httpClient().send(request, HttpResponse.BodyHandlers.ofByteArray());
                if (response.statusCode() != 200) {
                    String detail = new String(response.body(), StandardCharsets.UTF_8);
                    MimoNotifier.notify(player, "Verity MiMo TTS (DeepSeek) failed (HTTP " + response.statusCode() + "). Check the key or quota.");
                    System.err.println("[Verity MiMo TTS Error]: " + response.statusCode() + " - " + DeepSeekTtsService.truncate(detail));
                    return;
                }
                byte[] wavBytes = response.body();
                if (wavBytes == null || wavBytes.length == 0) {
                    MimoNotifier.notify(player, "Verity MiMo TTS (DeepSeek): response contained no audio.");
                    return;
                }
                TtsPlayback.playWav(wavBytes, player, verity);
            }
            catch (Exception e) {
                TtsPlayback.clearTalkingIfIdle(verity);
                MimoNotifier.notify(player, "Verity MiMo TTS (DeepSeek) connection failed: " + DeepSeekTtsService.truncate(String.valueOf(e.getMessage())));
                e.printStackTrace();
            }
        });
    }

    private static HttpClient httpClient() {
        HttpClient client = httpClient;
        if (client == null) {
            synchronized (DeepSeekTtsService.class) {
                client = httpClient;
                if (client == null) {
                    client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(30L)).build();
                    httpClient = client;
                }
            }
        }
        return client;
    }

    private static String truncate(String s) {
        if (s == null) {
            return "unknown error";
        }
        if (s.length() > 120) {
            return s.substring(0, 117) + "...";
        }
        return s;
    }
}
