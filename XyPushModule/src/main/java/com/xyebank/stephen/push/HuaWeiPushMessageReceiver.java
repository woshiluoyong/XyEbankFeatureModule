package com.xyebank.stephen.push;

import android.content.Context;
import android.os.Bundle;

import com.huawei.hms.support.api.push.PushReceiver;

public class HuaWeiPushMessageReceiver extends PushReceiver {
    @Override
    public void onEvent(Context context, Event event, Bundle extras) {
        super.onEvent(context, event, extras);
        System.out.println("====华为推送==onEvent====>"+event.toString()+"===>"+extras.toString());
    }

    @Override
    public void onToken(Context context, String token, Bundle extras) {
        super.onToken(context, token, extras);
        System.out.println("====华为推送==onToken1====>"+token);
    }

    @Override
    public boolean onPushMsg(Context context, byte[] msgBytes, Bundle extras) {
        System.out.println("====华为推送==onPushMsg1====>"+(new String(msgBytes)));
        return super.onPushMsg(context, msgBytes, extras);
    }

    @Override
    public void onPushMsg(Context context, byte[] msg, String token) {
        System.out.println("====华为推送==onPushMsg2====>"+msg.length);
        super.onPushMsg(context, msg, token);
    }

    @Override
    public void onPushState(Context context, boolean pushState) {
        System.out.println("====华为推送==onPushState====>"+pushState);
        super.onPushState(context, pushState);
    }

    @Override
    public void onToken(Context context, String token) {
        super.onToken(context, token);
        System.out.println("====华为推送==onToken2====>"+token);
    }
}
