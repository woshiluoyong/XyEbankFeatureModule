package com.xyebank.stephen.testpush;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.httpurlconnectionutil.RomUtils;
import com.huawei.android.hms.agent.HMSAgent;
import com.huawei.android.hms.agent.common.handler.ConnectHandler;
import com.huawei.android.hms.agent.push.handler.GetTokenHandler;
import com.xyebank.stephen.push.StephenPushUtils;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import cn.jpush.android.api.JPushInterface;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ((DemoApplication)getApplication()).setMainActivity(this);
        StephenPushUtils.getInstance().setActivityForBindHw(this);

        findViewById(R.id.set_accept_time).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                StephenPushUtils.getInstance().uploadStephenPushStatistics("292", StephenPushUtils.StatisticsTypeArrival);
                StephenPushUtils.getInstance().uploadStephenPushStatistics("293", StephenPushUtils.StatisticsTypeClick);
                System.out.println("=======jPush==getRegistrationID=>"+JPushInterface.getRegistrationID(MainActivity.this));
            }
        });
        findViewById(R.id.pause_push).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                //MiPushClient.pausePush(MainActivity.this, null);
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("stephenpush://com.xyebank.stephen.push/notification?action=stephen-test-parameter"));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setAction(Intent.ACTION_VIEW);
                //intent.putExtra("msg", "就圣诞节倒计时");
                String intnetUri = intent.toUri(Intent.URI_INTENT_SCHEME);
                System.out.println("=======stephen=uri==>====" + intnetUri);
            }
        });
        findViewById(R.id.resume_push).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                //Intent intent = new Intent(MainActivity.this, MainActivity2.class);
                //startActivity(intent);
                StephenPushUtils.getInstance().uploadStephenPushToken("18381062895","123",StephenPushUtils.AppTypeJRY);
            }
        });

        String pushParam = getIntent().getStringExtra(PushTranslateActivity.PushParamKey);
        if(!TextUtils.isEmpty(pushParam))receiveFromPush(pushParam);
    }

    public void receiveFromPush(String param){
        System.out.println("====com.stephen.push===接收统一操作的Push参数=====>"+param);
    }
}
