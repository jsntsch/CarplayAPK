package io.carplay.app.secured;

/* JADX INFO: loaded from: classes.dex */
public interface l5 {

    public enum a {
        VERIFIED,
        REJECTED,
        CONTRACT_UNAVAILABLE
    }

    static l5 b() {
        return new k5();
    }

    /* JADX INFO: Access modifiers changed from: private */
    static /* synthetic */ a e(String str, String str2, String str3, String str4, String str5) {
        return a.CONTRACT_UNAVAILABLE;
    }

    a f(String str, String str2, String str3, String str4, String str5);
}
