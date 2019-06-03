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

/**
 * 1、本 demo 可以直接运行，设置 topic 和 alias。
 * 服务器端使用 appsecret 即可以向demo发送广播和单点的消息。<br/>
 * 2、为了修改本 demo 为使用你自己的 appid，你需要修改几个地方：DemoApplication.java 中的 APP_ID,
 * APP_KEY，AndroidManifest.xml 中的 packagename，和权限 permission.MIPUSH_RECEIVE 的前缀为你的 packagename。
 *
 * @author wangkuiwei
 */
public class MainActivity extends Activity {
    public static MainActivity MainINSTANCE = null;
    public static List<String> logList = new CopyOnWriteArrayList<String>();

    private TextView mLogView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ((DemoApplication)getApplication()).setMainActivity(this);
        StephenPushUtils.getInstance().startHuaWeiPush(this);

        mLogView = (TextView) findViewById(R.id.log);
        // 设置接收消息时间
        findViewById(R.id.set_accept_time).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                /*new TimeIntervalDialog(MainActivity.this, new TimeIntervalInterface() {

                    @Override
                    public void apply(int startHour, int startMin, int endHour,
                                      int endMin) {
                        MiPushClient.setAcceptTime(MainActivity.this, startHour, startMin, endHour, endMin, null);
                    }

                    @Override
                    public void cancel() {
                        //ignore
                    }
                }).show();*/

                System.out.println("=======jPush==getRegistrationID=>"+JPushInterface.getRegistrationID(MainActivity.this));
            }
        });
        // 暂停推送
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
        System.out.println("=====接收统一操作的Push参数=====>"+param);
    }
}
