package com.xyebank.stephen.push;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Process;
import android.text.TextUtils;
import android.widget.Toast;

import com.httpurlconnectionutil.HttpUtils;
import com.httpurlconnectionutil.RomUtils;
import com.httpurlconnectionutil.callback.HttpCallbackBytesListener;
import com.httpurlconnectionutil.callback.HttpCallbackStringListener;
import com.huawei.android.hms.agent.HMSAgent;
import com.huawei.android.hms.agent.common.handler.ConnectHandler;
import com.huawei.android.hms.agent.push.handler.GetTokenHandler;
import com.xiaomi.channel.commonutils.logger.LoggerInterface;
import com.xiaomi.mipush.sdk.Logger;
import com.xiaomi.mipush.sdk.MiPushClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.jpush.android.api.JPushInterface;

public class StephenPushUtils {
    private static volatile StephenPushUtils singleton;
    private static final String PushTokenKey = "pushToken",PushPlatformIdKey = "platformId";
    public static final int PushTypeJG = 0,PushTypeXM = 1,PushTypeHW = 2;
    public static final String AppTypeSJD = "sjd",AppTypeJRY = "jry",AppTypeXYQB = "xyqb",ExtraPushRecordId = "pushRecordId";
    public static final int StatisticsTypeArrival = 0,StatisticsTypeClick = 1;
    private Map<Integer,String> pushTypeMap = new HashMap<>();
    private int bootPushType = PushTypeJG;//0极光,1小米,2华为
    private Application context = null;
    private Activity activityForHw = null;
    private boolean isShowInfoMsg = false,isNeedUploadToken = true;
    private String serverBaseIpPort = "https://sjd-test.xycredit.com.cn";
    private String miPushAppID = null, miPushAppKEY = null;//小米push相关参数

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
        initStephenPush(context, isShowMsg, null);
    }

    public void initStephenPush(final Application context,boolean isShowMsg,String uploadBaseIpPort){
        this.context = context;
        this.isShowInfoMsg = isShowMsg;
        if(!TextUtils.isEmpty(uploadBaseIpPort))this.serverBaseIpPort = uploadBaseIpPort;
        pushTypeMap.put(PushTypeJG,"JG");
        pushTypeMap.put(PushTypeXM,"XM");
        pushTypeMap.put(PushTypeHW,"HW");
        bootPushType = findPushType(SharedUtil.getString(this.context, PushPlatformIdKey));
        final String miPushAppId = getMetaDataVal(this.context,"MiPushAppId","AppId=");
        final String miPushAppKey = getMetaDataVal(this.context,"MiPushAppKey","AppKey=");
        Map<String, Object> map = new HashMap<>();
        map.put("mblModel", RomUtils.getBrandName());
        map.put("version", RomUtils.getVersion());
        map.put("plateformId", SharedUtil.getString(this.context, PushPlatformIdKey));//本地存储的平台编号（XM:小米；HW:华为；JG：极光）
        HttpUtils.doPost(context,isShowInfoMsg, serverBaseIpPort+"/push/service/pushOnOff", new HttpCallbackStringListener() {
            @Override
            public void onFinish(String response) {
                //response = "{\"body\":\"XM\",\"errMsg\":\"操作成功\",\"errorCode\":0,\"message\":null,\"signature\":null}";//test
                String msg = "Push开关请求Ok:" + response;
                System.out.println("======com.stephen.push======>"+msg);
                if(isShowInfoMsg)Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                if(!TextUtils.isEmpty(response)){
                    String getPlatformId = null;
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if(null != jsonObject && jsonObject.has("errorCode") && 0 == jsonObject.getInt("errorCode") && jsonObject.has("body")){
                            jsonObject = new JSONObject(jsonObject.getString("body"));
                            if(null != jsonObject && jsonObject.has("plateformId")){
                                getPlatformId = jsonObject.getString("plateformId");
                                SharedUtil.putString(context, PushPlatformIdKey, getPlatformId);
                            }// end of if
                            if(null != jsonObject && jsonObject.has("isNext"))isNeedUploadToken = jsonObject.getBoolean("isNext");//true:收集/false不收集
                        }// end of if
                    }catch (Exception e){e.printStackTrace();}
                    bootPushType = findPushType(getPlatformId);
                }else{
                    bootPushType = PushTypeJG;
                }
                initStephenPushCore(bootPushType, miPushAppId, miPushAppKey);
            }

            @Override
            public void onError(Exception e) {
                String msg = (null != e ? e.toString() : "Push开关请求报错为空!");
                System.out.println("======com.stephen.push======>Push开关初始化请求Error:" + msg);
                if(isShowInfoMsg)Toast.makeText(context, msg, Toast.LENGTH_LONG).show();

                bootPushType = PushTypeJG;
                initStephenPushCore(bootPushType, miPushAppId, miPushAppKey);
            }
        }, map);
    }

    private int findPushType(String flagStr){
        if(!TextUtils.isEmpty(flagStr)){
            for(Map.Entry<Integer, String> entry : pushTypeMap.entrySet()) {
                if(flagStr.equals(entry.getValue())){
                    return entry.getKey();
                }// end of if
            }// end of for
        }// end of if
        return PushTypeJG;
    }

    private void initStephenPushCore(final int bootPushType, String miPushAppId, String miPushAppKey){//注册启动push服务
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
        (new Handler()).postDelayed(new Runnable() {
            @Override
            public void run() {
                recordStephenMiPushToken();//谨慎起见,再设一次
                recordStephenJPushToken();//谨慎起见,再设一次
                if(bootPushType == PushTypeHW){
                    setActivityForBindHw(null);
                    HMSAgent.Push.getToken(new GetTokenHandler() {
                        @Override
                        public void onResult(int rst) {
                            System.out.println("===com.stephen.push===华为推送==GetTokenHandler====>"+(0 == rst ? "成功" : "失败:Code:"+rst));
                        }
                    });
                }// end of if
            }
        }, 3000);
    }

    //应用主activity的onCreate方法中必须调用
    public void setActivityForBindHw(Activity activityForHw){
        if(null != activityForHw)this.activityForHw = activityForHw;
        if(null != this.activityForHw)HMSAgent.connect(this.activityForHw, new ConnectHandler() {
            @Override
            public void onConnect(int rst) {
                System.out.println("===com.stephen.push===华为推送==onConnect====>"+(0 == rst ? "成功" : "失败:Code:"+rst));
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

    public String getStephenPushToken(){
        return getStephenPushToken(bootPushType);
    }

    public String getStephenPushToken(int curPushType){
        String pushToken = SharedUtil.getString(context,PushTokenKey+curPushType);
        switch(curPushType){
            case PushTypeJG:
                if(TextUtils.isEmpty(pushToken))pushToken = JPushInterface.getRegistrationID(context);
                break;
            case PushTypeXM:
                if(TextUtils.isEmpty(pushToken))pushToken = MiPushClient.getRegId(context);
                break;
        }// end of switch
        return (TextUtils.isEmpty(pushToken)) ? "" : pushToken;
    }

    //上报推送对应token及信息
    public void uploadStephenPushToken(String mobileNo,String userId,String appType){
        if(!isNeedUploadToken){
            String msg = "后台决定不需要上报Token,上报操作取消!";
            if(isShowInfoMsg)Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
            System.out.println("=====com.stephen.push=====>"+msg);
            return;
        }// end of if
        String pushToken = SharedUtil.getString(context,PushTokenKey+bootPushType);
        if(TextUtils.isEmpty(pushToken)){
            String msg = "准备上报Token未获取成功,请检查!";
            if(isShowInfoMsg)Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
            System.out.println("=====com.stephen.push=====>"+msg);
            return;
        }// end of if
        /*switch(appType){
            case AppTypeSJD:
            case AppTypeJRY:
            case AppTypeXYQB:
                System.out.println("=====com.stephen.push====("+appType+")开始上报Token===>"+pushToken);
                break;
            default:
                String msg = "准备上报Token对应的应用类型不在预设字段里面,请确认是否正确!";
                if(isShowInfoMsg)Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                System.out.println("=====com.stephen.push=====>"+msg);
                //return;
        }// end of switch*/
        System.out.println("=====com.stephen.push====("+appType+")开始上报Token===>"+pushToken);
        Map<String, Object> map = new HashMap<>();
        map.put("mblNo", mobileNo);//手机号
        map.put("userId", userId);//用户编号
        map.put("plateformId", pushTypeMap.get(bootPushType));//平台编号（XM:小米；HW:华为；JG：极光）
        map.put("storeId", appType);//(手机贷：sjd/金融苑：jry/享宇钱包：xyqb)
        map.put("pushId", pushToken);//设备推送平台ID
        map.put("osType", 2);//系统类型（1、IOS;2、android）
        HttpUtils.doPost(context,isShowInfoMsg, serverBaseIpPort+"/push/service/pushPlateForm", new HttpCallbackStringListener() {
            @Override
            public void onFinish(String response) {
                String msg = "Push上报Token请求Ok:" + response;
                System.out.println("======com.stephen.push======>"+msg);
                if(isShowInfoMsg)Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(Exception e) {
                String msg = (null != e ? e.toString() : "Push上报Token请求报错为空!");
                System.out.println("======com.stephen.push======>doPost Error:" + msg);
                if(isShowInfoMsg)Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
            }
        }, map);
    }

    //上报推送到达/点击统计事件
    public void uploadStephenPushStatistics(String pushMsgStr,int uploadType){
        if (TextUtils.isEmpty(pushMsgStr))return;
        String pushRecordId = null;
        try {
            JSONObject jsonObject = new JSONObject(pushMsgStr);
            if(null != jsonObject && jsonObject.has(ExtraPushRecordId))pushRecordId = jsonObject.getString(ExtraPushRecordId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        uploadStephenPushStatistics(uploadType, pushRecordId);
    }

    public void uploadStephenPushStatistics(int uploadType,String pushRecordId){
        if (TextUtils.isEmpty(pushRecordId))return;
        String uploadUrl = null;
        switch (uploadType) {
            case StatisticsTypeArrival:
                uploadUrl = "/countArrival";
                break;
            case StatisticsTypeClick:
                uploadUrl = "/countClick";
                break;
        }// end of switch
        if (TextUtils.isEmpty(uploadUrl))return;
        Map<String, Object> map = new HashMap<>();
        map.put("pushRecordId", pushRecordId);//推送任务记录ID
        HttpUtils.doPost(context,isShowInfoMsg, serverBaseIpPort+"/push/service"+uploadUrl, new HttpCallbackStringListener() {
            @Override
            public void onFinish(String response) {
                System.out.println("======com.stephen.push======>Push上报统计事件请求Ok:" + response);
            }

            @Override
            public void onError(Exception e) {
                String msg = (null != e ? e.toString() : "Push上报统计事件请求报错为空!");
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

    private String getMetaDataVal(Context context, String dataKey, String subStr){
        String metaDataVal = null;
        try {
            ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            metaDataVal = appInfo.metaData.getString(dataKey);
            if(!TextUtils.isEmpty(metaDataVal) && !TextUtils.isEmpty(subStr))metaDataVal = metaDataVal.substring(metaDataVal.indexOf(subStr)+subStr.length());
        } catch (PackageManager.NameNotFoundException e1) {
            e1.printStackTrace();
        }
        return metaDataVal;
    }
}
