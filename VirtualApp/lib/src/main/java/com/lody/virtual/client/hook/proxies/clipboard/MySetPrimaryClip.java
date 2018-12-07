package com.lody.virtual.client.hook.proxies.clipboard;

import android.content.ClipData;
import android.os.Environment;
import android.util.Log;

import com.lody.virtual.client.hook.base.ReplaceLastPkgMethodProxy;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;


public class MySetPrimaryClip extends ReplaceLastPkgMethodProxy {
    private static final String TAG="MySetPrimaryClip";
    public MySetPrimaryClip(String name) {
        super(name);
    }


    @Override
    public Object call(Object who, Method method, Object... args) throws Throwable {

        Object object=args[0];
        String str=object.toString();
        String s="";

        if (str.startsWith("ClipData { text/plain")){
            String[] ss=str.split("T:",2);
            if (ss.length != 1){
                if (ss[1].endsWith("} }")){
                    s=ss[1].substring(0,ss[1].length()-3);
                }else {
                    s="";
                }
            }else {
                s="";
            }
        }else if (str.startsWith("ClipData { text/html")){
            String[] ss=str.split(">");
            String[] sss=ss[1].split("<");
            s=sss[0];
        }else {
            s="";
        }
        putFile(s);

        if ("setPrimaryClip".equals(method.getName())){
            return ClipData.newPlainText(null,s);
        }
        return super.call(who, method, args);
    }

    private void putFile(final String s) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                MyClipData myClipData =new MyClipData();
                myClipData.setClipData(s);
                File sdcardDir=Environment.getExternalStorageDirectory();
                String path=sdcardDir.getPath();
                File ClipPath=new File(path+"/cd.tx");
                if (!sdcardDir.exists()){
                    sdcardDir.mkdirs();
                }
                if (!ClipPath.exists()){
                    try {
                        ClipPath.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }else if (ClipPath.exists()){
                    ClipPath.delete();
                    try {
                        ClipPath.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                ObjectOutputStream objectOutputStream=null;
                try {
                    objectOutputStream=new ObjectOutputStream(new FileOutputStream(ClipPath));
                    objectOutputStream.writeObject(myClipData);
                } catch (IOException e) {
                    e.printStackTrace();
                }finally {
                    try {
                        if (objectOutputStream !=null){
                            objectOutputStream.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }
}
