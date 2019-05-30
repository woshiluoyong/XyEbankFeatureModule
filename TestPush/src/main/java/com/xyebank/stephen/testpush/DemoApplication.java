package com.xyebank.stephen.testpush;

import android.app.Application;

import com.xyebank.stephen.push.StephenPushUtils;

public class DemoApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        if(StephenPushUtils.getInstance().shouldInit(this))StephenPushUtils.getInstance().initStephenPush(this,true, StephenPushUtils.PushTypeXM,"2882303761518011547","5661801118547");
    }
}