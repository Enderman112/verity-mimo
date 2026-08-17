package com.veritymimo.direct.stt;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.veritymimo.direct.config.MimoDirectConfig;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Base64;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import varmite.verity.VerityConfig;

public final class MimoSttService {
    private static final String MODEL_ID = "mimo-v2.5-asr";

    private MimoSttService() {
    }

    public static boolean isActive() {
        return Boolean.TRUE.equals(MimoDirectConfig.ENABLE_MIMO_ASR.get());
    }

    public static String transcribe(byte[] pcmData, AudioFormat format) {
        if (pcmData == null || pcmData.length == 0 || format == null || format.getFrameSize() <= 0) {
            return "";
        }
        try {
            byte[] wavData;
            try (ByteArrayInputStream bais = new ByteArrayInputStream(pcmData);
                 AudioInputStream ais = new AudioInputStream(bais, format, pcmData.length / format.getFrameSize());
                 ByteArrayOutputStream baos = new ByteArrayOutputStream();) {
                AudioSystem.write(ais, AudioFileFormat.Type.WAVE, baos);
                wavData = baos.toByteArray();
            }
            String apiKey = (String) MimoDirectConfig.MIMO_API_KEY.get();
            if (apiKey == null || apiKey.isBlank()) {
                System.err.println("[Verity MiMo] MiMo API key not set. See the Xiaomi MiMo category in Verity's config.");
                return "";
            }
            String dataUrl = "data:audio/wav;base64," + Base64.getEncoder().encodeToString(wavData);
            JsonObject body = MimoSttService.buildRequestBody(dataUrl);
            String apiUrl = MimoDirectConfig.resolveApiUrl();
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(apiUrl + "/chat/completions")).timeout(Duration.ofSeconds(60L)).header("api-key", apiKey).header("Authorization", "Bearer " + apiKey).header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(body.toString())).build();
            HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(30L)).build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                System.err.println("[Verity MiMo STT Error]: " + response.statusCode() + " - " + response.body());
                return "";
            }
            return MimoSttService.extractText(response.body());
        }
        catch (Exception e) {
            System.err.println("[Verity MiMo STT] recognition failed.");
            e.printStackTrace();
            return "";
        }
    }

    private static JsonObject buildRequestBody(String dataUrl) {
        JsonObject root = new JsonObject();
        root.addProperty("model", MODEL_ID);
        JsonArray messages = new JsonArray();
        JsonObject userMsg = new JsonObject();
        userMsg.addProperty("role", "user");
        JsonArray content = new JsonArray();
        JsonObject audioPart = new JsonObject();
        audioPart.addProperty("type", "input_audio");
        JsonObject inputAudio = new JsonObject();
        inputAudio.addProperty("data", dataUrl);
        audioPart.add("input_audio", (JsonElement) inputAudio);
        content.add(audioPart);
        userMsg.add("content", (JsonElement) content);
        messages.add(userMsg);
        root.add("messages", (JsonElement) messages);
        JsonObject asrOptions = new JsonObject();
        String lang = (String) MimoDirectConfig.MIMO_STT_LANGUAGE.get();
        asrOptions.addProperty("language", lang == null || lang.isBlank() ? "zh" : lang);
        root.add("asr_options", (JsonElement) asrOptions);
        root.addProperty("stream", false);
        return root;
    }

    private static String extractText(String json) {
        if (json == null || json.isBlank()) {
            return "";
        }
        try {
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            JsonElement choices = root.get("choices");
            if (choices == null || !choices.isJsonArray() || choices.getAsJsonArray().isEmpty()) {
                return "";
            }
            JsonObject message = choices.getAsJsonArray().get(0).getAsJsonObject().getAsJsonObject("message");
            if (message == null) {
                return "";
            }
            JsonElement content = message.get("content");
            if (content == null || content.isJsonNull()) {
                return "";
            }
            String text;
            if (content.isJsonArray()) {
                StringBuilder sb = new StringBuilder();
                for (JsonElement part : content.getAsJsonArray()) {
                    if (part.isJsonObject() && part.getAsJsonObject().has("text")) {
                        sb.append(part.getAsJsonObject().get("text").getAsString());
                    }
                }
                text = sb.toString();
            } else {
                text = content.getAsString();
            }
            if (".".equals(text) || text.isEmpty()) {
                return "";
            }
            return text.trim();
        }
        catch (Exception e) {
            return "";
        }
    }
}
