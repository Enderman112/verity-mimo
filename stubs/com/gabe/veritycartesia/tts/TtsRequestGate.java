package com.gabe.veritycartesia.tts;

import java.util.concurrent.TimeUnit;

public class TtsRequestGate {
    public static boolean tryAcquire() { return true; }
    public static boolean tryAcquire(long timeout, TimeUnit unit) throws InterruptedException { return true; }
    public static void release() {}
}
