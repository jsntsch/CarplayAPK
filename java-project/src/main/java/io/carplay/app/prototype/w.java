package io.carplay.app.prototype;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;

/* JADX INFO: loaded from: classes.dex */
final class w {
    private w() {
    }

    public static Network a(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(ConnectivityManager.class);
        if (connectivityManager == null) {
            return null;
        }
        Network activeNetwork = connectivityManager.getActiveNetwork();
        NetworkCapabilities networkCapabilities = activeNetwork == null ? null : connectivityManager.getNetworkCapabilities(activeNetwork);
        if ((networkCapabilities != null && networkCapabilities.hasTransport(4)) || b(networkCapabilities)) {
            return activeNetwork;
        }
        Network[] allNetworks = connectivityManager.getAllNetworks();
        if (allNetworks != null) {
            for (Network network : allNetworks) {
                NetworkCapabilities networkCapabilities2 = connectivityManager.getNetworkCapabilities(network);
                if (b(networkCapabilities2) && !networkCapabilities2.hasTransport(4)) {
                    return network;
                }
            }
        }
        return null;
    }

    private static boolean b(NetworkCapabilities networkCapabilities) {
        return networkCapabilities != null && networkCapabilities.hasCapability(12) && networkCapabilities.hasCapability(16);
    }
}
