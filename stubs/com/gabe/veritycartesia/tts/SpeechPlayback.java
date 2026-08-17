package com.gabe.veritycartesia.tts;

public class SpeechPlayback {
    public static boolean isBusy() { return false; }
    public static boolean hasActiveLine() { return false; }
    public static boolean isSameActiveLine(Integer entityId, String text) { return false; }
    public static long beginNew() { return 0L; }
    public static long beginOverlay() { return 0L; }
    public static void setActiveLine(Integer entityId, String text) {}
    public static void clearBusy(long generation) {}
    public static boolean isCurrent(long generation) { return true; }
    public static void clearActiveLine(long generation) {}
    public static void clearCancelForPlayback(long generation) {}
}
