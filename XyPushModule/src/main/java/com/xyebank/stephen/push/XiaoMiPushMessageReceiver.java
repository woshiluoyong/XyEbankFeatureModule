package com.xyebank.stephen.push;

import android.content.Context;
import android.text.TextUtils;

import com.xiaomi.mipush.sdk.ErrorCode;
import com.xiaomi.mipush.sdk.MiPushClient;
import com.xiaomi.mipush.sdk.MiPushCommandMessage;
import com.xiaomi.mipush.sdk.MiPushMessage;
import com.xiaomi.mipush.sdk.PushMessageReceiver;

import java.util.List;

public class XiaoMiPushMessageReceiver extends PushMessageReceiver {

    private String mRegId;
    private String mTopic;
    private String mAlias;
    private String mAccount;
    private String mStartTime;
    private String mEndTime;

    @Override
    public void onReceivePassThroughMessage(Context context, MiPushMessage message) {
        System.out.println("====com.stephen.push==XMPush======onReceivePassThroughMessage is called. " + (null != message ? message.toString() : ""));
    }

    @Override
    public void onNotificationMessageClicked(Context context, MiPushMessage message) {
        System.out.println("====com.stephen.push==XMPush======onNotificationMessageClicked is called. " + (null != message ? message.toString() : ""));
    }

    @Override
    public void onNotificationMessageArrived(Context context, MiPushMessage message) {
        System.out.println("====com.stephen.push==XMPush======onNotificationMessageArrived is called. " + (null != message ? message.toString() : ""));
        if(null != message && null != message.getExtra() && !TextUtils.isEmpty(message.getExtra().get(StephenPushUtils.ExtraPushRecordId))){
            StephenPushUtils.getInstance().uploadStephenPushStatistics(StephenPushUtils.StatisticsTypeArrival,message.getExtra().get(StephenPushUtils.ExtraPushRecordId));
        }// end of if
    }

    @Override
    public void onCommandResult(Context context, MiPushCommandMessage message) {
        System.out.println("====com.stephen.push==XMPush======onCommandResult is called. " + (null != message ? message.toString() : ""));
        String command = message.getCommand();
        List<String> arguments = message.getCommandArguments();
        String cmdArg1 = ((arguments != null && arguments.size() > 0) ? arguments.get(0) : null);
        String cmdArg2 = ((arguments != null && arguments.size() > 1) ? arguments.get(1) : null);
        String log;
        if (MiPushClient.COMMAND_REGISTER.equals(command)) {
            if (message.getResultCode() == ErrorCode.SUCCESS) {
                mRegId = cmdArg1;
                log = "register_success:" + mRegId;
                StephenPushUtils.getInstance().recordStephenPushToken(StephenPushUtils.PushTypeXM, mRegId);
            } else {
                log = "register_fail:" + message.getReason();
            }
        } else if (MiPushClient.COMMAND_SET_ALIAS.equals(command)) {
            if (message.getResultCode() == ErrorCode.SUCCESS) {
                mAlias = cmdArg1;
                log = "set_alias_success:" + mAlias;
            } else {
                log = "set_alias_fail:" + message.getReason();
            }
        } else if (MiPushClient.COMMAND_UNSET_ALIAS.equals(command)) {
            if (message.getResultCode() == ErrorCode.SUCCESS) {
                mAlias = cmdArg1;
                log = "unset_alias_success:" + mAlias;
            } else {
                log = "unset_alias_fail:" + message.getReason();
            }
        } else if (MiPushClient.COMMAND_SET_ACCOUNT.equals(command)) {
            if (message.getResultCode() == ErrorCode.SUCCESS) {
                mAccount = cmdArg1;
                log = "set_account_success:" + mAccount;
            } else {
                log = "set_account_fail:" + message.getReason();
            }
        } else if (MiPushClient.COMMAND_UNSET_ACCOUNT.equals(command)) {
            if (message.getResultCode() == ErrorCode.SUCCESS) {
                mAccount = cmdArg1;
                log = "unset_account_success:" + mAccount;
            } else {
                log = "unset_account_fail:" + message.getReason();
            }
        } else if (MiPushClient.COMMAND_SUBSCRIBE_TOPIC.equals(command)) {
            if (message.getResultCode() == ErrorCode.SUCCESS) {
                mTopic = cmdArg1;
                log = "subscribe_topic_success:" + mTopic;
            } else {
                log = "subscribe_topic_fail:" + message.getReason();
            }
        } else if (MiPushClient.COMMAND_UNSUBSCRIBE_TOPIC.equals(command)) {
            if (message.getResultCode() == ErrorCode.SUCCESS) {
                mTopic = cmdArg1;
                log = "unsubscribe_topic_success:" + mTopic;
            } else {
                log = "unsubscribe_topic_fail:" + message.getReason();
            }
        } else if (MiPushClient.COMMAND_SET_ACCEPT_TIME.equals(command)) {
            if (message.getResultCode() == ErrorCode.SUCCESS) {
                mStartTime = cmdArg1;
                mEndTime = cmdArg2;
                log = "set_accept_time_success:" + mStartTime + "/" + mEndTime;
            } else {
                log = "set_accept_time_fail:" + message.getReason();
            }
        } else {
            log = message.getReason();
        }
        System.out.println("=====com.stephen.push==XMPush======onCommandResult====结果===>" + log);
    }

    @Override
    public void onReceiveRegisterResult(Context context, MiPushCommandMessage message) {
        System.out.println("====com.stephen.push==XMPush======onReceiveRegisterResult is called. " + (null != message ? message.toString() : ""));
        String command = message.getCommand();
        List<String> arguments = message.getCommandArguments();
        String cmdArg1 = ((arguments != null && arguments.size() > 0) ? arguments.get(0) : null);
        String log;
        if (MiPushClient.COMMAND_REGISTER.equals(command)) {
            if (message.getResultCode() == ErrorCode.SUCCESS) {
                mRegId = cmdArg1;
                log = "register_success:"+mRegId;
                StephenPushUtils.getInstance().recordStephenPushToken(StephenPushUtils.PushTypeXM, mRegId);
            } else {
                log = "register_fail";
            }
        } else {
            log = message.getReason();
        }
        System.out.println("=====com.stephen.push==XMPush======onReceiveRegisterResult====结果===>" + log);
    }

    @Override
    public void onRequirePermissions(Context context, String[] permissions) {
        super.onRequirePermissions(context, permissions);
        System.out.println("====com.stephen.push==XMPush======onRequirePermissions is called. need permission" + arrayToString(permissions));
    }

    public String arrayToString(String[] strings) {
        String result = " ";
        for (String str : strings) result = result + str + " ";
        return result;
    }
}
