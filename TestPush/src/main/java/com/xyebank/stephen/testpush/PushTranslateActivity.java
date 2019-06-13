package com.xyebank.stephen.testpush;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.xyebank.stephen.push.StephenPushUtils;

public class PushTranslateActivity extends Activity {
    public static final String PushParamKey = "PushParamKey";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main3);

        String pushParam = null;
        try {
            pushParam = getIntent().getData().getQueryParameter("action");
            StephenPushUtils.getInstance().uploadStephenPushStatistics(pushParam,StephenPushUtils.StatisticsTypeClick);
        } catch (Exception e){
            e.printStackTrace();
        }
        boolean isRunning = StephenPushUtils.getInstance().isActivityRunning(this, MainActivity.class.getName());//判断主Activity是否在运行来确定app是否被杀死
        System.out.println("===com.stephen.push====PushTranslateActivity===isRunning===>"+isRunning+"==pushParam===>"+pushParam);
        if(isRunning){
            ((DemoApplication)getApplication()).getMainActivity().receiveFromPush(pushParam);
        }else{
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra(PushParamKey, pushParam);
            startActivity(intent);
        }
        finish();
    }
}
