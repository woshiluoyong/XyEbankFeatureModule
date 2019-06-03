package com.xyebank.stephen.push;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.Process;
import android.text.TextUtils;
import android.widget.Toast;

import com.httpurlconnectionutil.HttpUtils;
import com.httpurlconnectionutil.RomUtils;
import com.httpurlconnectionutil.callback.HttpCallbackStringListener;
import com.huawei.android.hms.agent.HMSAgent;
import com.huawei.android.hms.agent.common.handler.ConnectHandler;
import com.huawei.android.hms.agent.push.handler.GetTokenHandler;
import com.xiaomi.channel.commonutils.logger.LoggerInterface;
import com.xiaomi.mipush.sdk.Logger;
import com.xiaomi.mipush.sdk.MiPushClient;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.jpush.android.api.JPushInterface;

public class StephenPushUtils {
    private static volatile StephenPushUtils singleton;
    private static final String PushTokenKey = "pushToken";
    public static final int PushTypeJG = 0,PushTypeXM = 1,PushTypeHW = 2;
    public static final String AppTypeSJD = "sjd",AppTypeJRY = "jry",AppTypeXYQB = "xyqb";
    private Map<Integer,String> pushTypeMap = new HashMap<>();
    private int bootPushType = PushTypeJG;//0极光,1小米,2华为
    private Application context = null;
    private boolean isShowInfoMsg = false;
    private String serverBaseIpPort = "http://192.168.2.10:9966";
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

    public void initStephenPush(Application context, boolean isShowMsg){
        initStephenPush(context, isShowMsg, null,null, null);
    }

    public void initStephenPush(Application context, boolean isShowMsg, String uploadBaseIpPort){
        initStephenPush(context, isShowMsg, uploadBaseIpPort,null, null);
    }

    public void initStephenPush(final Application context,boolean isShowMsg,String uploadBaseIpPort,final String miPushAppId,final String miPushAppKey){
        this.context = context;
        this.isShowInfoMsg = isShowMsg;
        if(!TextUtils.isEmpty(uploadBaseIpPort))this.serverBaseIpPort = uploadBaseIpPort;
        pushTypeMap.put(PushTypeJG,"JG");
        pushTypeMap.put(PushTypeXM,"XM");
        pushTypeMap.put(PushTypeHW,"HW");
        Map<String, Object> map = new HashMap<>();
        map.put("mblModel", RomUtils.getBrandName());
        map.put("version", RomUtils.getVersion());
        HttpUtils.doPost(context, serverBaseIpPort+"/push/service/pushOnOff", new HttpCallbackStringListener() {
            @Override
            public void onFinish(String response) {
                System.out.println("======com.stephen.push======>Push开关请求Ok:" + response);
                if(!TextUtils.isEmpty(response)){
                    for(Map.Entry<Integer, String> entry : pushTypeMap.entrySet()) {
                        if(response.equals(entry.getValue())){
                            bootPushType = entry.getKey();
                            break;
                        }// end of if
                    }// end of for
                }else{
                    bootPushType = PushTypeJG;
                }
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
        System.out.println("=====com.stephen.push====开始注册启动push服务===>"+pushTypeMap.get(bootPushType));
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
        recordStephenMiPushToken();//谨慎起见,再设一次
        recordStephenJPushToken();//谨慎起见,再设一次
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

    private void recordStephenMiPushToken(){
        String pushToken = MiPushClient.getRegId(context);
        System.out.println("======com.stephen.push====MiPushToken=>"+pushToken);
        if(!TextUtils.isEmpty(pushToken))SharedUtil.putString(context,PushTokenKey+PushTypeXM, pushToken);
    }

    private void recordStephenJPushToken(){
        String pushToken = JPushInterface.getRegistrationID(context);
        System.out.println("======com.stephen.push====JPushToken=>"+pushToken);
        if(!TextUtils.isEmpty(pushToken))SharedUtil.putString(context,PushTokenKey+PushTypeJG, pushToken);
    }

    //记录推送对应token
    public void recordStephenPushToken(int curPushType,String pushToken){
        if(bootPushType != curPushType){
            String msg = "记录PushToken推送和获取的推送类型不一致,请检查!";
            if(isShowInfoMsg)Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
            System.out.println("=====com.stephen.push=====>"+msg);
            return;
        }// end of if
        switch(curPushType){
            case PushTypeJG:
            case PushTypeXM:
            case PushTypeHW:
                SharedUtil.putString(context,PushTokenKey+bootPushType, pushToken);
                break;
            default:
                String msg = "记录PushToken推送标识类型不存在,请检查!";
                if(isShowInfoMsg)Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                System.out.println("=====com.stephen.push=====>"+msg);
                break;
        }// end of switch
    }

    //上报推送对应token及信息
    public void uploadStephenPushToken(String mobileNo,String userId,String appType){
        String pushToken = SharedUtil.getString(context,PushTokenKey+bootPushType);
        if(TextUtils.isEmpty(pushToken)){
            String msg = "准备上报Token未获取成功,请检查!";
            if(isShowInfoMsg)Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
            System.out.println("=====com.stephen.push=====>"+msg);
            return;
        }// end of if
        switch(appType){
            case AppTypeSJD:
            case AppTypeJRY:
            case AppTypeXYQB:
                System.out.println("=====com.stephen.push====("+appType+")开始上报Token===>"+pushToken);
                break;
            default:
                String msg = "准备上报Token对应的应用类型不存在,请检查!";
                if(isShowInfoMsg)Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                System.out.println("=====com.stephen.push=====>"+msg);
                return;
        }// end of switch
        Map<String, Object> map = new HashMap<>();
        map.put("mblNo", mobileNo);//手机号
        map.put("userId", userId);//用户编号
        map.put("plateformId", pushTypeMap.get(bootPushType));//平台编号（XM:小米；HW:华为；JG：极光）
        map.put("appId", appType);//(手机贷：sjd/金融苑：jry/享宇钱包：xyqb)
        map.put("pushId", pushToken);//设备推送平台ID
        map.put("osType", 2);//系统类型（1、IOS;2、android）
        HttpUtils.doPost(context, serverBaseIpPort+"/push/service/pushPlateForm", new HttpCallbackStringListener() {
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
}
