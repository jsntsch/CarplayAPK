package io.carplay.app.secured;

import java.util.Objects;

/* JADX INFO: compiled from: CarPlus */
/* JADX INFO: loaded from: classes.dex */
public abstract /* synthetic */ class w1 {
    public static int a(long j, int i, int i2) {
        return (Long.hashCode(j) + i) * i2;
    }

    public static int b(String str, int i, int i2) {
        return (Objects.hashCode(str) + i) * i2;
    }

    public static long c(long j, long j2, long j3, long j4) {
        return (j * j2) + j3 + j4;
    }

    public static String d(String str, int i) {
        return str + i;
    }

    public static String e(String str, String str2) {
        return str + str2;
    }

    public static void f(Class cls, StringBuilder sb, String str) {
        sb.append(cls.getSimpleName());
        sb.append(str);
    }
}
