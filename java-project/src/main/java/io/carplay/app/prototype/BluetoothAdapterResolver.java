package io.carplay.app.prototype;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import io.carplay.app.secured.ProtoMessageBase;
import java.util.Objects;

/* JADX INFO: loaded from: classes.dex */
public final class BluetoothAdapterResolver {
    static final String a = "ocarplay-local-transport";
    static final String b = "phone_bluetooth_address";

    public static final class a extends ProtoMessageBase {
        private final String a;
        private final String b;

        public a(String str, String str2) {
            this.a = str;
            this.b = str2;
        }

        public final boolean equals(Object obj) {
            if (!(obj instanceof a)) {
                return false;
            }
            a aVar = (a) obj;
            return Objects.equals(this.a, aVar.a) && Objects.equals(this.b, aVar.b);
        }

        public String g() {
            return this.a;
        }

        public String h() {
            return this.b;
        }

        public final int hashCode() {
            String str = this.a;
            String str2 = this.b;
            return Objects.hashCode(str2) + (Objects.hashCode(str) * 31);
        }

        public final String toString() {
            Object[] objArr = {this.a, this.b};
            String[] strArrSplit = "a;b".length() == 0 ? new String[0] : "a;b".split(";");
            StringBuilder sb = new StringBuilder();
            ProtoMessageBase.f(a.class, sb, "[");
            for (int i = 0; i < strArrSplit.length; i++) {
                sb.append(strArrSplit[i]);
                sb.append("=");
                sb.append(objArr[i]);
                if (i != strArrSplit.length - 1) {
                    sb.append(", ");
                }
            }
            sb.append("]");
            return sb.toString();
        }
    }

    private BluetoothAdapterResolver() {
    }

    /* JADX WARN: Code duplicated, block: B:12:0x0028  */
    public static a a(Context context, String str) {
        String strA = null;
        String strA2 = io.carplay.app.secured.n.a(str);
        try {
            if (context.checkSelfPermission("android.permission.BLUETOOTH_CONNECT") != 0) {
                strA = null;
            } else {
                BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(BluetoothManager.class);
                BluetoothAdapter adapter = bluetoothManager == null ? null : bluetoothManager.getAdapter();
                if (adapter != null) {
                    strA = io.carplay.app.secured.n.a(adapter.getAddress());
                } else {
                    strA = null;
                }
            }
        } catch (RuntimeException unused) {
        }
        if (strA != null && !strA.equals(strA2)) {
            return new a(strA, "PUBLIC_ADAPTER");
        }
        String strB = b(context);
        return (strB == null || strB.equals(strA2)) ? new a(null, "UNAVAILABLE") : new a(strB, "USER_ENTERED");
    }

    public static String b(Context context) {
        try {
            return io.carplay.app.secured.n.a(context.getSharedPreferences(a, 0).getString(b, ""));
        } catch (RuntimeException unused) {
            return null;
        }
    }
}
