package com.xyebank.stephen.push;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.os.Process;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cretin.www.httpurlconnectionutil.HttpUtils;
import com.cretin.www.httpurlconnectionutil.callback.HttpCallbackStringListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.huawei.android.hms.agent.HMSAgent;
import com.huawei.android.hms.agent.common.handler.ConnectHandler;
import com.huawei.android.hms.agent.push.handler.GetTokenHandler;
import com.xiaomi.channel.commonutils.logger.LoggerInterface;
import com.xiaomi.mipush.sdk.Logger;
import com.xiaomi.mipush.sdk.MiPushClient;
import cn.jpush.android.api.JPushInterface;

public class StephenPushUtils {
    private static volatile StephenPushUtils singleton;
    public static final int PushTypeJG = 0,PushTypeXM = 1,PushTypeHW = 2;
    private int bootPushType = PushTypeJG;//0极光,1小米,2华为
    private Application context = null;
    private boolean isShowInfoMsg = false;
    private static String miPushAppID = null, miPushAppKEY = null;//小米push相关参数

    private StephenPushUtils() {}

    public static StephenPushUtils getInstance() {
        if(null == singleton){
            synchronized (StephenPushUtils.class) {
                if(null == singleton){
                    singleton = new StephenPushUtils();
                }// end of if
            }// end
        }// end of if
        return singleton;
    }

    public void initStephenPush(Application context, boolean isShowMsg, int bootPushType){
        initStephenPush(context, isShowMsg, bootPushType,null, null);
    }

    public void initStephenPush(final Application context,boolean isShowMsg,final int bootPushType,final String miPushAppId,final String miPushAppKey){
        this.context = context;
        this.isShowInfoMsg = isShowMsg;
        Map<String, Object> map = new HashMap<>();
        map.put("offset", 0);
        map.put("limit", 20);
        map.put("isRecommend", 0);
        map.put("isUpdateTime", 0);
        map.put("isVip", 0);
        map.put("isRealNameAuth", 0);
        map.put("isGuarantee", 0);
        HttpUtils.doPost(context, "http://120.78.168.168:9922/api/cargo/list", new HttpCallbackStringListener() {
            @Override
            public void onFinish(String response) {
                System.out.println("======com.stephen.push======>Push开关请求Ok:" + response);
                initStephenPushCore(bootPushType, miPushAppId, miPushAppKey);
            }

            @Override
            public void onError(Exception e) {
                String msg = (null != e ? e.toString() : "Push开关请求报错为空!");
                System.out.println("======com.stephen.push======>doPost Error:" + msg);
                if(isShowInfoMsg)Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
            }
        }, map);
    }

    private void initStephenPushCore(int bootPushType, String miPushAppId, String miPushAppKey){//注册启动push服务
        if(bootPushType == PushTypeXM && (TextUtils.isEmpty(miPushAppId) || TextUtils.isEmpty(miPushAppKey))){
            String msg = "启用小米推送必须同时设置小米的AppId和AppKey,已取消启动推送,请设置值后重试!";
            if(isShowInfoMsg)Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
            System.out.println("=====com.stephen.push=====>"+msg);
            return;
        }// end of if
        this.bootPushType = bootPushType;
        miPushAppID = miPushAppId;
        miPushAppKEY = miPushAppKey;
        switch (bootPushType) {
            case PushTypeXM://小米
                MiPushClient.registerPush(context, miPushAppID, miPushAppKEY);
                Logger.setLogger(context, new LoggerInterface() {

                    @Override
                    public void setTag(String tag) {}

                    @Override
                    public void log(String content, Throwable t) {
                        System.out.println("=====com.stephen.push=====>" + content + "=====>" + (null != t ? t.getMessage() : ""));
                    }

                    @Override
                    public void log(String content) {
                        System.out.println("=====com.stephen.push=====>" + content);
                    }
                });
                if(isShowInfoMsg)Toast.makeText(context, "初始化小米推送", Toast.LENGTH_LONG).show();
                break;
            case PushTypeHW://华为
                HMSAgent.init(context);
                if(isShowInfoMsg)Toast.makeText(context, "初始化华为推送", Toast.LENGTH_LONG).show();
                break;
            default://极光
                JPushInterface.init(context);
                JPushInterface.setDebugMode(true);
                if(isShowInfoMsg)Toast.makeText(context, "初始化极光推送", Toast.LENGTH_LONG).show();
                break;
        }// end of switch
    }

    //应用主activity的onCreate方法中必须调用
    public void startHuaWeiPush(Activity activity){
        if(bootPushType != PushTypeHW){
            System.out.println("=====com.stephen.push=====>只是提示:如果开始华为推送必须先初始化华为推送而不是其他推送!");
            return;
        }// end of if
        HMSAgent.connect(activity, new ConnectHandler() {
            @Override
            public void onConnect(int rst) {
                System.out.println("===com.stephen.push===华为推送==onConnect====>"+rst);
            }
        });
        HMSAgent.Push.getToken(new GetTokenHandler() {
            @Override
            public void onResult(int rst) {
                System.out.println("===com.stephen.push===华为推送==GetTokenHandler====>"+rst);
            }
        });
    }

    public void uploadStephenPushToken(int curPushType,String pushToken){
        if(bootPushType != curPushType){
            String msg = "Push上报Token请求的推送和预设的推送不一致,请检查!";
            if(isShowInfoMsg)Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
            System.out.println("=====com.stephen.push=====>"+msg);
            return;
        }// end of if
        Map<String, Object> map = new HashMap<>();
        map.put("offset", 0);
        map.put("limit", 20);
        map.put("isRecommend", 0);
        map.put("isUpdateTime", 0);
        map.put("isVip", 0);
        map.put("isRealNameAuth", 0);
        map.put("isGuarantee", 0);
        HttpUtils.doPost(context, "http://120.78.168.168:9922/api/cargo/list", new HttpCallbackStringListener() {
            @Override
            public void onFinish(String response) {
                System.out.println("======com.stephen.push======>Push上报Token请求Ok:" + response);
            }

            @Override
            public void onError(Exception e) {
                String msg = (null != e ? e.toString() : "Push上报Token请求报错为空!");
                System.out.println("======com.stephen.push======>doPost Error:" + msg);
                if(isShowInfoMsg)Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
            }
        }, map);
    }

    public boolean shouldInit(Context context) {
        List<ActivityManager.RunningAppProcessInfo> processInfos = ((ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE)).getRunningAppProcesses();
        if(null != processInfos && processInfos.size() > 0) for(ActivityManager.RunningAppProcessInfo info : processInfos) if(null != info && info.pid == Process.myPid() && context.getPackageName().equals(info.processName)) return true;
        return false;
    }

    public boolean isAppRunning(Context context){
        ActivityManager activityManager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        for(ActivityManager.RunningAppProcessInfo appProcess : appProcesses)if(appProcess.processName.equals(context.getPackageName()))return true;
        return false;
    }

    public boolean isActivityRunning(Context context, String checkActivityName){
        boolean isRunning = false;
        if(!TextUtils.isEmpty(checkActivityName)){
            ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningTaskInfo> list = am.getRunningTasks(100);
            if(null != list && list.size() > 0){
                for(ActivityManager.RunningTaskInfo info : list) {
                    if(null != info && ((null != info.topActivity && checkActivityName.equals(info.topActivity.getClassName())) || (null != info.baseActivity && checkActivityName.equals(info.baseActivity.getClassName())))){
                        isRunning = true;
                        break;
                    }// end of if
                }// end of for
            }// end of if
        }// end of if
        return isRunning;
    }

    public Object fromJson(String json, Class clazz) {
        Object obj = null;
        try {
            GsonBuilder gsonb = new GsonBuilder();
            Gson gson = gsonb.create();
            obj = gson.fromJson(json,clazz);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return obj;
    }
}
