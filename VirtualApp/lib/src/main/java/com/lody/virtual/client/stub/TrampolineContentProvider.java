package com.lody.virtual.client.stub;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.pm.ProviderInfo;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Build;
import android.os.CancellationSignal;
import android.os.IInterface;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.ipc.VActivityManager;
import com.lody.virtual.client.ipc.VPackageManager;
import com.lody.virtual.helper.compat.ContentProviderCompat;
import com.lody.virtual.helper.compat.UriCompat;
import com.lody.virtual.os.VUserHandle;

import java.io.FileNotFoundException;
import java.lang.reflect.Constructor;

/**
 * Author: liuqiang
 * Date: 2018-08-25
 * Time: 12:03
 * Description: 类似于调板的中间层
 * 打通外部应用调用内部插件的过程
 */
public class TrampolineContentProvider extends ContentProvider {

    private static final boolean DEBUG = false;

    public boolean onCreate() {
        return true;
    }

    private Uri wrapperUri(String form, Uri uri) {
        return UriCompat.wrapperUri(uri);
    }

    public Cursor query(Uri uri, String[] strArr, String str, String[] strArr2, String str2) {
        if (VASettings.PROVIDER_ONLY_FILE) {
            return null;
        }
        try {
            return ContentProviderCompat.crazyAcquireContentProvider(getContext(), wrapperUri("query", uri)).query(uri, strArr, str, strArr2, str2);
        } catch (Exception e) {
            return new MatrixCursor(new String[0]);
        }
    }

    public String getType(Uri uri) {
        String str = null;
        if (VASettings.PROVIDER_ONLY_FILE) {
            return str;
        }
        Uri a = wrapperUri("getType", uri);
        try {
            return ContentProviderCompat.crazyAcquireContentProvider(getContext(), a).getType(a);
        } catch (Exception e) {
            return str;
        }
    }

    public Uri insert(Uri uri, ContentValues contentValues) {
        if (VASettings.PROVIDER_ONLY_FILE) {
            return uri;
        }
        Uri a = wrapperUri("insert", uri);
        try {
            return ContentProviderCompat.crazyAcquireContentProvider(getContext(), a).insert(a, contentValues);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public int bulkInsert(Uri uri, ContentValues[] values) {
        int i = 0;
        if (VASettings.PROVIDER_ONLY_FILE) {
            return i;
        }
        Uri a = wrapperUri("insert", uri);
        try {
            return ContentProviderCompat.crazyAcquireContentProvider(getContext(), a).bulkInsert(a, values);
        } catch (Throwable e) {
            e.printStackTrace();
            return i;
        }
    }

    public int delete(Uri uri, String str, String[] strArr) {
        int i = 0;
        if (VASettings.PROVIDER_ONLY_FILE) {
            return i;
        }
        Uri a = wrapperUri("delete", uri);
        try {
            return ContentProviderCompat.crazyAcquireContentProvider(getContext(), a).delete(a, str, strArr);
        } catch (Exception e) {
            e.printStackTrace();
            return i;
        }
    }

    public int update(Uri uri, ContentValues contentValues, String str, String[] strArr) {
        int i = 0;
        if (VASettings.PROVIDER_ONLY_FILE) {
            return i;
        }
        Uri a = wrapperUri("update", uri);
        try {
            return ContentProviderCompat.crazyAcquireContentProvider(getContext(), a).update(a, contentValues, str, strArr);
        } catch (Exception e) {
            e.printStackTrace();
            return i;
        }
    }

    public AssetFileDescriptor openAssetFile(Uri uri, String str) throws FileNotFoundException {
        Log.d("Q_M", "openAssetFile");

        Uri a = wrapperUri("openAssetFile", uri);
        try {
            return ContentProviderCompat.crazyAcquireContentProvider(getContext(), a).openAssetFile(a, str);
        } catch (Throwable th) {
            return null;
        }
    }

    @TargetApi(19)
    public AssetFileDescriptor openAssetFile(Uri uri, String str, CancellationSignal cancellationSignal) throws FileNotFoundException {
        //Log.d("Q_M", "openAssetFile19 " + uri);

        Uri parsedUri = wrapperUri("openAssetFile2", uri);

        //Log.d("Q_M", "openAssetFile19 还原后的uri:" + a);

        int userId = VUserHandle.myUserId();
        ProviderInfo providerInfo = VPackageManager.get().resolveContentProvider(parsedUri.getAuthority(), 0, userId);


        try {
            return retrieveContentProviderClient(providerInfo).openAssetFile(parsedUri, str, cancellationSignal);

//            return ContentProviderCompat.crazyAcquireContentProvider(getContext(), a).openAssetFile(a, str, cancellationSignal);
        } catch (Throwable th) {
            Log.e("Q_M", "error = " + th.toString());
            return null;
        }
    }

    public ParcelFileDescriptor openFile(Uri uri, String str) throws FileNotFoundException {
        Log.d("Q_M", "openFile");
        Uri a = wrapperUri("openFile", uri);
        try {
            return ContentProviderCompat.crazyAcquireContentProvider(getContext(), a).openFile(a, str);
        } catch (Exception e) {
            return null;
        }
    }

    @TargetApi(19)
    public ParcelFileDescriptor openFile(Uri uri, String str, CancellationSignal cancellationSignal) throws FileNotFoundException {
        Log.d("Q_M", "openFile19");
        Uri a = wrapperUri("openFile2", uri);
        try {
            return ContentProviderCompat.crazyAcquireContentProvider(getContext(), a).openFile(a, str, cancellationSignal);
        } catch (Exception e) {
            return null;
        }
    }

    private ContentProviderClient retrieveContentProviderClient(ProviderInfo providerInfo) {
        try {
            Object obj = null;
            //是因为这里为null 为啥？
            //因为在 VActivityManagerService 有个自定义进程判断，为啥？
            IInterface provider = VActivityManager.get().acquireProviderClient(VUserHandle.myUserId(), providerInfo);

//            Log.d("Q_M", "IInterface VUserHandle.myUserId = " + VUserHandle.myUserId());
//            Log.d("Q_M", "IInterface providerInfo = " + providerInfo);
//            Log.d("Q_M", "IInterface provider = " + provider);

            Constructor constructor;
            if (Build.VERSION.SDK_INT >= 16) {
                constructor = ContentProviderClient.class.getDeclaredConstructors()[0];
                constructor.setAccessible(true);
                try {
                    obj = constructor.newInstance(new Object[]{VirtualCore.get().getContext().getContentResolver(), provider, Boolean.TRUE});
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            } else {
                constructor = ContentProviderClient.class.getDeclaredConstructors()[0];
                constructor.setAccessible(true);
                try {
                    obj = constructor.newInstance(new Object[]{getContext().getContentResolver(), provider});
                } catch (Exception e22) {
                    e22.printStackTrace();
                }
            }
            return (ContentProviderClient) obj;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}