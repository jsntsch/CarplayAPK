package io.carplay.app.secured;

import java.util.List;
import java.util.Objects;

/* JADX INFO: loaded from: classes.dex */
/**
 * Iap2Ncm
 *
 * <p>原混淆名：{@code io.carplay.app.secured.q1}（jadx 产物 q1.java）
 *
 * <p>命名依据：bluetooth/iap2 bootstrap / host iap2/ncm / vehicle wi-fi/rtsp/media/input
 */
public interface Iap2Ncm extends MediaFormat, AutoCloseable {

    public static final class a extends ProtoMessageBase {
        private final b a;
        private final boolean b;
        private final List<String> c;

        public a(b bVar, boolean z, List<String> list) {
            List<String> listCopyOf = List.copyOf(list);
            this.a = bVar;
            this.b = z;
            this.c = listCopyOf;
        }

        public final boolean equals(Object obj) {
            if (!(obj instanceof a)) {
                return false;
            }
            a aVar = (a) obj;
            return this.b == aVar.b && Objects.equals(this.a, aVar.a) && Objects.equals(this.c, aVar.c);
        }

        public boolean g() {
            return this.b;
        }

        public b h() {
            return this.a;
        }

        public final int hashCode() {
            boolean z = this.b;
            b bVar = this.a;
            List<String> list = this.c;
            return Objects.hashCode(list) + ((Objects.hashCode(bVar) + (Boolean.hashCode(z) * 31)) * 31);
        }

        public List<String> i() {
            return this.c;
        }

        public final String toString() {
            Object[] objArr = {this.a, Boolean.valueOf(this.b), this.c};
            String[] strArrSplit = "a;b;c".length() == 0 ? new String[0] : "a;b;c".split(";");
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

    public enum b {
        WIRELESS,
        PHONE_USB_DIRECT,
        EXTERNAL_USB_BRIDGE
    }

    static List<a> d() {
        return List.of(new a(b.WIRELESS, false, List.of("Bluetooth/iAP2 bootstrap", "normal verified pairing/authentication", "vehicle Wi-Fi/RTSP/media/input")), new a(b.PHONE_USB_DIRECT, false, List.of("initial DEVICE descriptor/EP0", "0x51 response and role swap", "HOST iAP2/NCM", "normal authentication/media")), new a(b.EXTERNAL_USB_BRIDGE, false, List.of("optional added hardware", "UDC/role/power", "bridge-side normal CarPlay", "phone media/control link")));
    }

    @Override // java.lang.AutoCloseable
    void close();

    a h();
}
