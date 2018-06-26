package com.lody.virtual.client.hook.proxies.clipboard;

import android.content.ClipData;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.hook.base.ReplaceLastPkgMethodProxy;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Method;


public class MyGetPrimaryClip extends ReplaceLastPkgMethodProxy {
    final MyClipData myClipData =new MyClipData();

    public MyGetPrimaryClip(String name) {
        super(name);
    }

    @Override
    public Object call(Object who, Method method, Object... args) throws Throwable {

        if ("getPrimaryClip".equals(method.getName())){
            getClipData();
            Thread.sleep(100);
            if (myClipData ==null){
                return ClipData.newPlainText(null, null);
            }else {
                String s= myClipData.getClipData().toString();
                return ClipData.newPlainText(null, s);
            }
        }
        return super.call(who, method, args);
    }

    private void getClipData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                File file= null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    file = new File(VirtualCore.get().getContext().getDataDir().getPath()+"/cd.tx");
                }
                if (file.exists()){
                    ObjectInputStream objectInputStream=null;
                    try {
                        objectInputStream=new ObjectInputStream(new FileInputStream(file));
                        MyClipData clipData = (MyClipData) objectInputStream.readObject();
                        myClipData.setClipData(clipData.getClipData());

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
