package com.veritymimo.direct.stt;

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
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import varmite.verity.VerityConfig;

/**
 * DeepSeek ASR backend.
 *
 * <p>{@code POST /v1/tools/asr} expects a multipart/form-data upload in the
 * {@code file} field (16-bit PCM WAV or Ogg Opus, at most 8 MiB / 120 s) and answers
 * with a flat {@code {"text": "..."}} object. There is no language parameter: the
 * language is detected automatically.
 */
public final class DeepSeekSttService {
    private static final long MAX_UPLOAD_BYTES = 8L * 1024 * 1024;

    private DeepSeekSttService() {
    }

    public static String transcribe(byte[] pcmData, AudioFormat format) {
        if (pcmData == null || pcmData.length == 0 || format == null || format.getFrameSize() <= 0) {
            System.err.println("[Verity MiMo ASR] empty/invalid recording (pcm=" + (pcmData == null ? "null" : String.valueOf(pcmData.length)) + " bytes, format=" + (format == null ? "null" : format.toString()) + ")");
            return "";
        }
        try {
            byte[] wavData;
            try (ByteArrayInputStream bais = new ByteArrayInputStream(pcmData);
                 AudioInputStream ais = new AudioInputStream(bais, format, pcmData.length / format.getFrameSize());
                 ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                AudioSystem.write(ais, AudioFileFormat.Type.WAVE, baos);
                wavData = baos.toByteArray();
            }
            double seconds = pcmData.length / (double)(format.getSampleRate() * format.getFrameSize());
            System.out.println("[Verity MiMo ASR] captured " + pcmData.length + " bytes (" + String.format("%.1f", seconds) + "s @ " + format.getSampleRate() + "Hz) -> wav " + wavData.length + " bytes [DeepSeek]");
            if (wavData.length > MAX_UPLOAD_BYTES) {
                System.err.println("[Verity MiMo ASR] audio too large (" + (wavData.length / 1024 / 1024) + "MB, DeepSeek limit 8MB). Recording was too long.");
                return "";
            }
            if (seconds > 120.0) {
                System.err.println("[Verity MiMo ASR] audio too long (" + String.format("%.1f", seconds) + "s, DeepSeek limit 120s).");
                return "";
            }
            String apiKey = (String)MimoDirectConfig.DEEPSEEK_API_KEY.get();
            if (apiKey == null || apiKey.isBlank()) {
                System.err.println("[Verity MiMo] DeepSeek API key not set. See the Xiaomi MiMo category in Verity's config.");
                return "";
            }
            String boundary = "----VerityMiMo" + System.currentTimeMillis();
            byte[] body = DeepSeekSttService.buildMultipartBody(boundary, wavData);
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(MimoDirectConfig.resolveDeepSeekUrl() + "/v1/tools/asr"))
                .timeout(Duration.ofSeconds(60L))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                .build();
            HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(30L)).build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                String detail = response.body() == null ? "" : response.body();
                System.err.println("[Verity MiMo ASR Error]: " + response.statusCode() + " - " + (detail.length() > 200 ? detail.substring(0, 200) : detail));
                return "";
            }
            String result = DeepSeekSttService.extractText(response.body());
            if (result == null || result.isEmpty()) {
                System.out.println("[Verity MiMo ASR] no speech detected in audio (empty result).");
            }
            return result;
        }
        catch (Exception e) {
            System.err.println("[Verity MiMo ASR] recognition failed.");
            e.printStackTrace();
            return "";
        }
    }

    private static byte[] buildMultipartBody(String boundary, byte[] wavData) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream(wavData.length + 512);
        out.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
        out.write("Content-Disposition: form-data; name=\"file\"; filename=\"speech.wav\"\r\n".getBytes(StandardCharsets.UTF_8));
        out.write("Content-Type: audio/wav\r\n\r\n".getBytes(StandardCharsets.UTF_8));
        out.write(wavData);
        out.write(("\r\n--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
        return out.toByteArray();
    }

    private static String extractText(String json) {
        if (json == null || json.isBlank()) {
            return "";
        }
        try {
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            JsonElement text = root.get("text");
            if (text == null || text.isJsonNull()) {
                return "";
            }
            String value = text.getAsString();
            if (value == null) {
                return "";
            }
            value = value.trim();
            return ".".equals(value) ? "" : value;
        }
        catch (Exception e) {
            return "";
        }
    }
}
