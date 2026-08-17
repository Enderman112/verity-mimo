package com.veritymimo.direct.tts;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.veritymimo.direct.MimoDirectNotifier;
import com.veritymimo.direct.config.MimoDirectConfig;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Base64;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import net.minecraft.world.entity.player.Player;
import varmite.verity.VerityConfig;
import varmite.verity.entity.llm.AiAPI;
import varmite.verity.entity.verity.VerityEntity;

public final class MimoTtsService {
    private static final String MODEL_ID = "mimo-v2.5-tts";
    private static final String DEFAULT_BASE_URL = "https://api.xiaomimimo.com/v1";
    private static final Map<String, String> VARIANT_STYLES = Map.ofEntries(
        Map.entry("happy", "\u7528\u5f00\u5fc3\u6109\u5feb\u7684\u8bed\u6c14\uff0c\u58f0\u97f3\u660e\u4eae\u6709\u6d3b\u529b\u3002"),
        Map.entry("happy_talking", "\u7528\u5f00\u5fc3\u6109\u5feb\u7684\u8bed\u6c14\uff0c\u58f0\u97f3\u660e\u4eae\u6709\u6d3b\u529b\u3002"),
        Map.entry("happy_sleep", "\u7528\u5e73\u9759\u6e29\u548c\u7684\u8bed\u6c14\uff0c\u8bed\u901f\u8212\u7f13\u3002"),
        Map.entry("neutral", ""),
        Map.entry("neutral_talking", ""),
        Map.entry("evil", "\u7528\u6124\u6012\u7684\u8bed\u6c14\uff0c\u58f0\u97f3\u63d0\u9ad8\u3001\u8bed\u6c14\u5f3a\u70c8\u3002"),
        Map.entry("evil_talking", "\u7528\u6124\u6012\u7684\u8bed\u6c14\uff0c\u58f0\u97f3\u63d0\u9ad8\u3001\u8bed\u6c14\u5f3a\u70c8\u3002"),
        Map.entry("smiling_evil", "\u7528\u5e73\u9759\u5374\u5a01\u80c1\u7684\u8bed\u6c14\u3002"),
        Map.entry("crazy", "\u7528\u7d27\u5f20\u6050\u60e7\u7684\u8bed\u6c14\uff0c\u58f0\u97f3\u53d1\u6296\u3002"),
        Map.entry("crazy_talking", "\u7528\u7d27\u5f20\u6050\u60e7\u7684\u8bed\u6c14\uff0c\u58f0\u97f3\u53d1\u6296\u3002"),
        Map.entry("serious_1", "\u7528\u5e73\u9759\u4e25\u8083\u7684\u8bed\u6c14\u3002"),
        Map.entry("serious_2", "\u7528\u4f4e\u6c89\u60b2\u4f24\u7684\u8bed\u6c14\u3002"),
        Map.entry("serious_3", "\u7528\u5bb3\u6015\u7684\u8bed\u6c14\u3002"),
        Map.entry("serious_talking", "\u7528\u5e73\u9759\u4e25\u8083\u7684\u8bed\u6c14\u3002"),
        Map.entry("hurt", "\u7528\u60b2\u4f24\u4f4e\u843d\u7684\u8bed\u6c14\u3002"),
        Map.entry("noface", "\u7528\u5e73\u9759\u6ca1\u6709\u60c5\u611f\u7684\u8bed\u6c14\u3002")
    );

    private MimoTtsService() {
    }

    public static boolean isActive() {
        return Boolean.TRUE.equals(MimoDirectConfig.ENABLE_MIMO_TTS.get());
    }

    public static void synthesizeAndPlay(String text, Player player, VerityEntity verity) {
        if (!Boolean.TRUE.equals(VerityConfig.USE_TTS.get())) {
            return;
        }
        CompletableFuture.runAsync(() -> {
            AiAPI.cancelCurrentSpeech = false;
            try {
                String apiKey = (String) MimoDirectConfig.MIMO_API_KEY.get();
                if (apiKey == null || apiKey.isBlank()) {
                    MimoDirectNotifier.notify(player, "Verity MiMo: no API key set. See the Xiaomi MiMo category in Verity's config.");
                    return;
                }
                String voice = (String) MimoDirectConfig.MIMO_TTS_VOICE.get();
                if (voice == null || voice.isBlank()) {
                    MimoDirectNotifier.notify(player, "Verity MiMo: TTS voice is empty. Set it in Verity's config (Xiaomi MiMo category).");
                    return;
                }
                String style = MimoTtsService.buildStyleInstruction(verity);
                JsonObject body = MimoTtsService.buildRequestBody(text, style, voice);
                String apiUrl = MimoTtsService.apiUrl();
                HttpRequest request = HttpRequest.newBuilder().uri(URI.create(apiUrl + "/chat/completions")).timeout(Duration.ofSeconds(90L)).header("api-key", apiKey).header("Authorization", "Bearer " + apiKey).header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(body.toString())).build();
                HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(30L)).build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() != 200) {
                    MimoDirectNotifier.notify(player, "Verity MiMo TTS failed (HTTP " + response.statusCode() + "). Check the key or quota.");
                    System.err.println("[Verity MiMo TTS Error]: " + response.statusCode() + " - " + response.body());
                    return;
                }
                String audioData = MimoTtsService.extractAudioData(response.body());
                if (audioData == null || audioData.isBlank()) {
                    MimoDirectNotifier.notify(player, "Verity MiMo TTS: response contained no audio.");
                    return;
                }
                byte[] wavBytes = MimoTtsService.decodeBase64(audioData);
                if (wavBytes == null || wavBytes.length == 0) {
                    MimoDirectNotifier.notify(player, "Verity MiMo TTS: could not decode audio.");
                    return;
                }
                MimoTtsService.playWav(wavBytes, player, verity);
            }
            catch (Exception e) {
                if (verity != null) {
                    verity.clientIsTalking = false;
                }
                MimoDirectNotifier.notify(player, "Verity MiMo TTS connection failed: " + MimoTtsService.truncate(String.valueOf(e.getMessage())));
                e.printStackTrace();
            }
        });
    }

    private static void playWav(byte[] wavBytes, Player player, VerityEntity verity) {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(wavBytes);
             AudioInputStream audioStream = AudioSystem.getAudioInputStream(new BufferedInputStream(bais));) {
            AudioFormat format = audioStream.getFormat();
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            try (SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);) {
                line.open(format);
                line.start();
                if (verity != null) {
                    verity.clientIsTalking = true;
                }
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = audioStream.read(buffer)) != -1) {
                    if (AiAPI.cancelCurrentSpeech) {
                        line.flush();
                        break;
                    }
                    AiAPI.apply3DEffect(line, player, verity);
                    line.write(buffer, 0, bytesRead);
                }
                if (!AiAPI.cancelCurrentSpeech) {
                    line.drain();
                }
            }
            finally {
                if (verity != null) {
                    verity.clientIsTalking = false;
                }
            }
        }
        catch (Exception e) {
            if (verity != null) {
                verity.clientIsTalking = false;
            }
            System.err.println("[Verity MiMo TTS] Failed to play audio.");
            e.printStackTrace();
        }
    }

    private static String apiUrl() {
        String base = (String) MimoDirectConfig.MIMO_BASE_URL.get();
        if (base == null || base.isBlank()) {
            return DEFAULT_BASE_URL;
        }
        String trimmed = base.trim();
        while (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }

    private static JsonObject buildRequestBody(String text, String style, String voice) {
        JsonObject root = new JsonObject();
        root.addProperty("model", MODEL_ID);
        JsonArray messages = new JsonArray();
        if (style != null && !style.isBlank()) {
            JsonObject userMsg = new JsonObject();
            userMsg.addProperty("role", "user");
            userMsg.addProperty("content", style);
            messages.add(userMsg);
        }
        JsonObject assistantMsg = new JsonObject();
        assistantMsg.addProperty("role", "assistant");
        assistantMsg.addProperty("content", text == null ? "" : text);
        messages.add(assistantMsg);
        root.add("messages", (JsonElement) messages);
        JsonObject audio = new JsonObject();
        audio.addProperty("format", "wav");
        audio.addProperty("voice", voice);
        root.add("audio", (JsonElement) audio);
        root.addProperty("stream", false);
        return root;
    }

    private static String buildStyleInstruction(VerityEntity verity) {
        String custom = (String) MimoDirectConfig.MIMO_TTS_STYLE.get();
        String base;
        if (custom != null && !custom.isBlank()) {
            base = custom.trim();
        } else {
            base = MimoTtsService.variantStyle(verity);
        }
        double speed = (Double) MimoDirectConfig.MIMO_TTS_SPEED.get();
        StringBuilder sb = new StringBuilder(base == null ? "" : base);
        if (speed > 1.15) {
            sb.append("\u8bed\u901f\u504f\u5feb\u3002");
        } else if (speed < 0.85) {
            sb.append("\u8bed\u901f\u7f13\u6162\u3002");
        }
        return sb.toString().trim();
    }

    private static String variantStyle(VerityEntity verity) {
        if (verity == null) {
            return "";
        }
        try {
            String variant = verity.getVariant();
            if (variant == null || variant.isBlank()) {
                return "";
            }
            String normalized = variant.toLowerCase(Locale.ROOT);
            if (normalized.endsWith(".png")) {
                normalized = normalized.substring(0, normalized.length() - 4);
            }
            String style = VARIANT_STYLES.get(normalized);
            return style == null ? "" : style;
        }
        catch (Throwable t) {
            return "";
        }
    }

    private static String extractAudioData(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            JsonElement choices = root.get("choices");
            if (choices == null || !choices.isJsonArray() || choices.getAsJsonArray().isEmpty()) {
                return null;
            }
            JsonObject message = choices.getAsJsonArray().get(0).getAsJsonObject().getAsJsonObject("message");
            if (message == null) {
                return null;
            }
            JsonObject audio = message.getAsJsonObject("audio");
            if (audio == null) {
                return null;
            }
            JsonElement data = audio.get("data");
            return data == null || data.isJsonNull() ? null : data.getAsString();
        }
        catch (Exception e) {
            return null;
        }
    }

    private static byte[] decodeBase64(String data) {
        try {
            String padded = data;
            int mod = data.length() % 4;
            if (mod != 0) {
                StringBuilder sb = new StringBuilder(data);
                for (int i = 0; i < 4 - mod; ++i) {
                    sb.append('=');
                }
                padded = sb.toString();
            }
            return Base64.getDecoder().decode(padded);
        }
        catch (IllegalArgumentException e) {
            return null;
        }
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
