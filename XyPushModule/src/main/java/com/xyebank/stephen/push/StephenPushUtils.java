package com.xyebank.stephen.push;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.os.Process;
import android.text.TextUtils;
import android.util.Log;

import java.util.List;

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

    public void initStephenPush(Application context, int bootPushType){
        initStephenPush(context, bootPushType, null, null);
    }

    public void initStephenPush(Application context, int bootPushType, String miPushAppId, String miPushAppKey){//注册启动push服务
        if(bootPushType == PushTypeXM && (TextUtils.isEmpty(miPushAppId) || TextUtils.isEmpty(miPushAppKey))){
            System.out.println("=====com.stephen.push=====>启用小米推送必须同时设置小米的AppId和AppKey,已取消启动推送,请设置值后重试!");
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
                break;
            case PushTypeHW://华为
                HMSAgent.init(context);
                break;
            default://极光
                JPushInterface.init(context);
                JPushInterface.setDebugMode(true);
                break;
        }// end of switch
    }

    //应用主activity的onCreate方法中必须调用
    public void startHuaWeiPush(Activity activity){
        if(bootPushType != PushTypeHW){
            System.out.println("=====com.stephen.push=====>如果开始华为推送必须先初始化华为推送而不是其他推送!");
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
}
