package com.lody.virtual.helper.compat;

import android.content.ClipData;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.net.Uri.Builder;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.helper.utils.Reflect;
import com.lody.virtual.helper.utils.VLog;
//import com.umeng.analytics.pro.b;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class UriCompat {
    public static String AUTH = "virtual.fileprovider";
    private static boolean DEBUG = false;
    private static final String TAG = "UriCompat";

    public static boolean needFake(Intent intent) {
        String pkg = intent.getPackage();
        if (pkg != null && VirtualCore.get().isAppInstalled(pkg)) {
            return false;
        }
        ComponentName componentName = intent.getComponent();
        if (componentName == null || !VirtualCore.get().isAppInstalled(componentName.getPackageName())) {
            return true;
        }
        return false;
    }

    public static Uri fakeFileUri(Uri uri) {
        if (uri == null) {
            return null;
        }
//        if (!b.W.equals(uri.getScheme())) {
//            return null;
//        }
        String auth = uri.getAuthority();
        if (AUTH.equals(auth)) {
            return uri;
        }
        Uri u = uri.buildUpon().authority(AUTH).appendQueryParameter("__va_auth", auth).build();
        if (DEBUG) {
            Log.i(TAG, "fake uri:" + uri + "->" + u);
        }
        return u;
    }

    public static Uri wrapperUri(Uri uri) {
        if (uri == null) {
            return null;
        }
        String auth = uri.getQueryParameter("__va_auth");
        if (TextUtils.isEmpty(auth)) {
            return uri;
        }
        Builder builder = uri.buildUpon().authority(auth);
        Set<String> names = uri.getQueryParameterNames();
        Map<String, String> querys = null;
        if (names != null && names.size() > 0) {
            querys = new HashMap();
            for (String name : names) {
                querys.put(name, uri.getQueryParameter(name));
            }
        }
        builder.clearQuery();
        for (Entry<String, String> e : querys.entrySet()) {
            if (!"__va_auth".equals(e.getKey())) {
                builder.appendQueryParameter((String) e.getKey(), (String) e.getValue());
            }
        }
        Uri u = builder.build();
        if (!DEBUG) {
            return u;
        }
        Log.i(TAG, "wrapperUri uri:" + uri + "->" + u);
        return u;
    }

    public static Intent fakeFileUri(Intent intent) {
        if (needFake(intent)) {
            Uri u;
            Uri uri = intent.getData();
            if (uri != null) {
                u = fakeFileUri(uri);
                if (u != null) {
                    if (DEBUG) {
                        Log.i(TAG, "fake data uri:" + uri + "->" + u);
                    }
                    intent.setData(u);
                }
            }
            if (VERSION.SDK_INT >= 16) {
                ClipData data = intent.getClipData();
                if (data != null) {
                    int count = data.getItemCount();
                    for (int i = 0; i < count; i++) {
                        ClipData.Item item = data.getItemAt(i);
                        u = fakeFileUri(item.getUri());
                        if (u != null) {
                            Reflect.on(item).set("mUri", u);
                        }
                    }
                }
            }
            Bundle extras = intent.getExtras();
            if (extras != null) {
                boolean changed2 = false;
                for (String key : extras.keySet()) {
                    Object val = extras.get(key);
                    if (val instanceof Intent) {
                        Intent i2 = fakeFileUri((Intent) val);
                        if (i2 != null) {
                            changed2 = true;
                            extras.putParcelable(key, i2);
                        }
                    } else if (val instanceof Uri) {
                        u = fakeFileUri((Uri) val);
                        if (u != null) {
                            changed2 = true;
                            extras.putParcelable(key, u);
                        }
                    }
                }
                if (changed2) {
                    intent.putExtras(extras);
                }
            }
        } else if (DEBUG) {
            VLog.i(TAG, "don't need fake intent", new Object[0]);
        }
        return intent;
    }
}