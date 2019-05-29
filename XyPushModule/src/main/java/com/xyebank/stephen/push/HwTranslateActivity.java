package com.xyebank.stephen.push;

import android.app.Activity;
import android.os.Bundle;

/**
 * 1、本 demo 可以直接运行，设置 topic 和 alias。
 * 服务器端使用 appsecret 即可以向demo发送广播和单点的消息。<br/>
 * 2、为了修改本 demo 为使用你自己的 appid，你需要修改几个地方：DemoApplication.java 中的 APP_ID,
 * APP_KEY，AndroidManifest.xml 中的 packagename，和权限 permission.MIPUSH_RECEIVE 的前缀为你的 packagename。
 *
 * @author wangkuiwei
 */
public class HwTranslateActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);

        String action = "--";
        try {
            action = getIntent().getData().getQueryParameter("action");
            //ActionUtil.startAction(this, ActionUtil.PREFIX + action, true);
        } catch (Exception e){
            e.printStackTrace();
        }
        System.out.println("=====action=====>"+action);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        System.out.println("=====HwTranslateActivity=====onBackPressed==========>");
        finish();
    }
}
