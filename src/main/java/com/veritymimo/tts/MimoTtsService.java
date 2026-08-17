package com.veritymimo.tts;

import com.gabe.veritycartesia.tts.EmotionDetector;
import com.gabe.veritycartesia.tts.SpeechPlayback;
import com.gabe.veritycartesia.tts.WavPlayer;
import com.gabe.veritycartesia.util.EmotionTags;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.veritymimo.MimoNotifier;
import com.veritymimo.config.MimoAddonConfig;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Base64;
import java.util.Locale;
import net.minecraft.world.entity.player.Player;
import varmite.verity.entity.verity.VerityEntity;

public final class MimoTtsService {
    private static final String MODEL_ID = "mimo-v2.5-tts";
    private static final String DEFAULT_BASE_URL = "https://api.xiaomimimo.com/v1";

    private MimoTtsService() {
    }

    public static void synthesizeAndPlay(String text, Player player, VerityEntity verity, Integer verityEntityId, long generation) {
        String apiKey = (String) MimoAddonConfig.MIMO_API_KEY.get();
        if (apiKey == null || apiKey.isBlank()) {
            SpeechPlayback.clearActiveLine(generation);
            MimoNotifier.notify(player, "Verity MiMo: no API key set. Mods \u2192 Verity MiMo Addon \u2192 paste a key from https://platform.xiaomimimo.com/#/console/api-keys");
            return;
        }
        String voiceId = (String) MimoAddonConfig.MIMO_TTS_VOICE.get();
        if (voiceId == null || voiceId.isBlank()) {
            SpeechPlayback.clearActiveLine(generation);
            MimoNotifier.notify(player, "Verity MiMo: TTS voice is empty. Set one in the addon config (e.g. \u82cf\u6253).");
            return;
        }
        String stripped = EmotionTags.strip(text);
        String emotion = EmotionDetector.detect(stripped, verity);
        try {
            if (!SpeechPlayback.isCurrent(generation)) {
                SpeechPlayback.clearBusy(generation);
                return;
            }
            JsonObject body = MimoTtsService.buildRequestBody(stripped, emotion, voiceId);
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(MimoTtsService.apiUrl() + "/chat/completions")).timeout(Duration.ofSeconds(90L)).header("api-key", apiKey).header("Authorization", "Bearer " + apiKey).header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(body.toString())).build();
            HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(30L)).build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                SpeechPlayback.clearActiveLine(generation);
                MimoNotifier.notify(player, "Verity MiMo TTS failed (HTTP " + response.statusCode() + "). Check the addon API key or your MiMo quota. Details: " + MimoTtsService.truncate(response.body()));
                return;
            }
            String audioData = MimoTtsService.extractAudioData(response.body());
            if (audioData == null || audioData.isBlank()) {
                SpeechPlayback.clearActiveLine(generation);
                MimoNotifier.notify(player, "Verity MiMo TTS: response contained no audio.");
                return;
            }
            byte[] wavBytes = MimoTtsService.decodeBase64(audioData);
            if (wavBytes == null || wavBytes.length == 0) {
                SpeechPlayback.clearActiveLine(generation);
                MimoNotifier.notify(player, "Verity MiMo TTS: could not decode audio.");
                return;
            }
            if (!SpeechPlayback.isCurrent(generation)) {
                SpeechPlayback.clearBusy(generation);
                return;
            }
            SpeechPlayback.clearCancelForPlayback(generation);
            WavPlayer.playStream(new ByteArrayInputStream(wavBytes), player, verity, verityEntityId, generation);
        }
        catch (Exception e) {
            if (verity != null && SpeechPlayback.isCurrent(generation)) {
                verity.clientIsTalking = false;
            }
            SpeechPlayback.clearActiveLine(generation);
            MimoNotifier.notify(player, "Verity MiMo TTS connection failed: " + MimoTtsService.truncate(String.valueOf(e.getMessage())));
            e.printStackTrace();
        }
    }

    private static String apiUrl() {
        String base = (String) MimoAddonConfig.MIMO_BASE_URL.get();
        if (base == null || base.isBlank()) {
            return DEFAULT_BASE_URL;
        }
        String trimmed = base.trim();
        while (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }

    private static JsonObject buildRequestBody(String text, String emotion, String voiceId) {
        JsonObject root = new JsonObject();
        root.addProperty("model", MODEL_ID);
        JsonArray messages = new JsonArray();
        String style = MimoTtsService.buildStyleInstruction(emotion);
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
        audio.addProperty("voice", voiceId);
        root.add("audio", (JsonElement) audio);
        root.addProperty("stream", false);
        return root;
    }

    private static String buildStyleInstruction(String emotion) {
        String custom = (String) MimoAddonConfig.MIMO_TTS_STYLE.get();
        String base;
        if (custom != null && !custom.isBlank()) {
            base = custom.trim();
        } else {
            base = MimoTtsService.styleForEmotion(emotion);
        }
        double speed = (Double) MimoAddonConfig.MIMO_TTS_SPEED.get();
        StringBuilder sb = new StringBuilder(base == null ? "" : base);
        if (speed > 1.15) {
            sb.append("\u8bed\u901f\u504f\u5feb\u3002");
        } else if (speed < 0.85) {
            sb.append("\u8bed\u901f\u7f13\u6162\u3002");
        }
        return sb.toString().trim();
    }

    private static String styleForEmotion(String emotion) {
        if (emotion == null) {
            return null;
        }
        switch (emotion.toLowerCase(Locale.ROOT)) {
            case "happy":
            case "content":
                return "\u7528\u5f00\u5fc3\u6109\u5feb\u7684\u8bed\u6c14\uff0c\u58f0\u97f3\u660e\u4eae\u6709\u6d3b\u529b\u3002";
            case "calm":
                return "\u7528\u5e73\u9759\u6e29\u548c\u7684\u8bed\u6c14\uff0c\u8bed\u901f\u8212\u7f13\u3002";
            case "excited":
                return "\u7528\u5174\u594b\u6fc0\u52a8\u7684\u8bed\u6c14\uff0c\u8bed\u901f\u504f\u5feb\uff0c\u58f0\u97f3\u9ad8\u4eae\u6709\u6d3b\u529b\u3002";
            case "sad":
                return "\u7528\u60b2\u4f24\u4f4e\u843d\u7684\u8bed\u6c14\uff0c\u58f0\u97f3\u4f4e\u6c89\u3002";
            case "angry":
                return "\u7528\u6124\u6012\u7684\u8bed\u6c14\uff0c\u58f0\u97f3\u63d0\u9ad8\u3001\u8bed\u6c14\u5f3a\u70c8\u3002";
            case "scared":
                return "\u7528\u5bb3\u6015\u7d27\u5f20\u7684\u8bed\u6c14\uff0c\u58f0\u97f3\u98a4\u6296\u6025\u4fc3\u3002";
            default:
                return null;
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
