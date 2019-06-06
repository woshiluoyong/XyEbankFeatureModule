package com.xyebank.stephen.testpush;

import android.app.Application;

import com.xyebank.stephen.push.StephenPushUtils;

public class DemoApplication extends Application {
    private MainActivity mainActivity;

    @Override
    public void onCreate() {
        super.onCreate();

        if(StephenPushUtils.getInstance().shouldInit(this))StephenPushUtils.getInstance().initStephenPush(this,true);
    }

    public MainActivity getMainActivity() {
        return mainActivity;
    }

    public void setMainActivity(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }
}