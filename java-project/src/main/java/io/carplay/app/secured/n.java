package io.carplay.app.secured;

import java.util.Locale;

/* JADX INFO: loaded from: classes.dex */
public final class n {
    private n() {
    }

    public static String a(String str) {
        if (str == null) {
            return null;
        }
        String upperCase = str.trim().toUpperCase(Locale.ROOT);
        if (upperCase.matches("[0-9A-F]{2}(?::[0-9A-F]{2}){5}") && !upperCase.equals("02:00:00:00:00:00") && !upperCase.equals("00:00:00:00:00:00") && (Integer.parseInt(upperCase.substring(0, 2), 16) & 1) == 0) {
            return upperCase;
        }
        return null;
    }
}
