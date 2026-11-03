package io.carplay.app.secured;

import java.security.Permission;
import java.util.HashSet;
import java.util.Set;

/* JADX INFO: loaded from: classes.dex */
public class h2 extends Permission {
    private final Set<String> a;

    public h2(String str) {
        super(str);
        HashSet hashSet = new HashSet();
        this.a = hashSet;
        hashSet.add(str);
    }

    public boolean equals(Object obj) {
        return (obj instanceof h2) && this.a.equals(((h2) obj).a);
    }

    @Override // java.security.Permission
    public String getActions() {
        return this.a.toString();
    }

    public int hashCode() {
        return this.a.hashCode();
    }

    @Override // java.security.Permission
    public boolean implies(Permission permission) {
        if (!(permission instanceof h2)) {
            return false;
        }
        h2 h2Var = (h2) permission;
        return getName().equals(h2Var.getName()) || this.a.containsAll(h2Var.a);
    }
}
