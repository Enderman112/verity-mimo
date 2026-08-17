package com.gabe.veritycartesia.util;

import varmite.verity.entity.verity.VerityEntity;

public class ThrowTts {
    public static boolean shouldSkipThrowScream(VerityEntity verity, String text) { return false; }
    public static boolean isThrowScream(String text) { return false; }
    public static String withScreamingTag(String text) { return text; }
}
