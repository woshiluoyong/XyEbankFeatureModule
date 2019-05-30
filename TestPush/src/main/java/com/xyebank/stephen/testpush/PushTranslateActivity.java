package com.xyebank.stephen.testpush;

import android.app.Activity;
import android.os.Bundle;

import com.xyebank.stephen.push.StephenPushUtils;

public class PushTranslateActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);

        String action = "--";
        try {
            action = getIntent().getData().getQueryParameter("action");
        } catch (Exception e){
            e.printStackTrace();
        }
        System.out.println(StephenPushUtils.getInstance().isActivityRunning(this, MainActivity.class.getName())+"===push==action=====>"+action);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        System.out.println("=====PushTranslateActivity=====onBackPressed==========>");
        finish();
    }
}
