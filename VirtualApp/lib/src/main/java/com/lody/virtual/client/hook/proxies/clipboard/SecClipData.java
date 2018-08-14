package com.lody.virtual.client.hook.proxies.clipboard;

import android.util.Log;

import java.io.Serializable;

public class SecClipData implements Serializable{
    public String clipData="";

    public void SecClipData(){
        Log.d("", "框架执行");
    }

    public void setClipData(String clipData){
        this.clipData=clipData;

    }

    public String getClipData(){
        return clipData;
    }

}
