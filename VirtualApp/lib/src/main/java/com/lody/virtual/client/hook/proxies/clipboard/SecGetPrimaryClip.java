package com.lody.virtual.client.hook.proxies.clipboard;

import android.content.ClipData;
import android.os.Environment;
import android.util.Log;

import com.lody.virtual.client.hook.base.ReplaceLastPkgMethodProxy;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Method;


public class SecGetPrimaryClip extends ReplaceLastPkgMethodProxy {
    final SecClipData secClipData =new SecClipData();

    public SecGetPrimaryClip(String name) {
        super(name);
        Log.d("SecGetPrimaryClip", "SecGetPrimaryClip: 已启动");
    }

    @Override
    public Object call(Object who, Method method, Object... args) throws Throwable {

        if ("getPrimaryClip".equals(method.getName())){
            Log.d("SecGetPrimaryClip", "getPrimaryClip: 已启动");
            getClipData();
            Thread.sleep(100);
            if (secClipData ==null){
                return ClipData.newPlainText(null, null);
            }else {
                String s= secClipData.getClipData().toString();
                return ClipData.newPlainText(null, s);
            }
        }
        return super.call(who, method, args);
    }

    private void getClipData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                File file=new File(Environment.getExternalStorageDirectory().getPath()+"/cd.tx");
                if (file.exists()){
                    ObjectInputStream objectInputStream=null;
                    try {
                        objectInputStream=new ObjectInputStream(new FileInputStream(file));
                        SecClipData clipData = (SecClipData) objectInputStream.readObject();
                        secClipData.setClipData(clipData.getClipData());

                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }finally {
                        try {
                            if (objectInputStream != null){
                                objectInputStream.close();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }).start();
    }
}
