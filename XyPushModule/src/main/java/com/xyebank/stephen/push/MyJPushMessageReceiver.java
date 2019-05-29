package com.xyebank.stephen.push;

import android.app.Notification;
import android.content.Context;

import cn.jpush.android.api.CmdMessage;
import cn.jpush.android.api.CustomMessage;
import cn.jpush.android.api.JPushMessage;
import cn.jpush.android.api.NotificationMessage;
import cn.jpush.android.helper.Logger;
import cn.jpush.android.service.JPushMessageReceiver;

/**
 * 自定义JPush message 接收器,包括操作tag/alias的结果返回(仅仅包含tag/alias新接口部分)
 * */
public class MyJPushMessageReceiver extends JPushMessageReceiver {

    @Override
    public void onTagOperatorResult(Context context,JPushMessage jPushMessage) {
        //TagAliasOperatorHelper.getInstance().onTagOperatorResult(context,jPushMessage);
        System.out.println("=======JPush====[MyJPushReceiver] onTagOperatorResult : " + jPushMessage.toString());
        super.onTagOperatorResult(context, jPushMessage);
    }
    @Override
    public void onCheckTagOperatorResult(Context context,JPushMessage jPushMessage){
        //TagAliasOperatorHelper.getInstance().onCheckTagOperatorResult(context,jPushMessage);
        System.out.println("=====JPush======[MyJPushReceiver] onCheckTagOperatorResult : " + jPushMessage.toString());
        super.onCheckTagOperatorResult(context, jPushMessage);
    }
    @Override
    public void onAliasOperatorResult(Context context, JPushMessage jPushMessage) {
        //TagAliasOperatorHelper.getInstance().onAliasOperatorResult(context,jPushMessage);
        System.out.println("=====JPush======[MyJPushReceiver] onAliasOperatorResult : " + jPushMessage.toString());
        super.onAliasOperatorResult(context, jPushMessage);
    }

    @Override
    public void onMobileNumberOperatorResult(Context context, JPushMessage jPushMessage) {
        //TagAliasOperatorHelper.getInstance().onMobileNumberOperatorResult(context,jPushMessage);
        System.out.println("======JPush=====[MyJPushReceiver] onMobileNumberOperatorResult : " + jPushMessage.toString());
        super.onMobileNumberOperatorResult(context, jPushMessage);
    }

    @Override
    public Notification getNotification(Context context, NotificationMessage notificationMessage) {
        System.out.println("=====JPush======getNotification=============>"+notificationMessage.toString());
        return super.getNotification(context, notificationMessage);
    }

    @Override
    public void onMessage(Context context, CustomMessage customMessage) {
        super.onMessage(context, customMessage);
        System.out.println("=====JPush======onMessage=============>"+customMessage.toString());
    }

    @Override
    public void onNotifyMessageOpened(Context context, NotificationMessage notificationMessage) {
        super.onNotifyMessageOpened(context, notificationMessage);
        System.out.println("=====JPush======onNotifyMessageOpened=============>"+notificationMessage.toString());
    }

    @Override
    public void onNotifyMessageArrived(Context context, NotificationMessage notificationMessage) {
        super.onNotifyMessageArrived(context, notificationMessage);
        System.out.println("======JPush=====onNotifyMessageArrived=============>"+notificationMessage.toString());
    }

    @Override
    public void onNotifyMessageDismiss(Context context, NotificationMessage notificationMessage) {
        super.onNotifyMessageDismiss(context, notificationMessage);
        System.out.println("======JPush=====onNotifyMessageDismiss=============>"+notificationMessage.toString());
    }

    @Override
    public void onRegister(Context context, String s) {
        super.onRegister(context, s);
        System.out.println("====JPush=======onRegister=============>"+s);
    }

    @Override
    public void onConnected(Context context, boolean b) {
        super.onConnected(context, b);
        System.out.println("====JPush=======onConnected=============>"+b);
    }

    @Override
    public void onCommandResult(Context context, CmdMessage cmdMessage) {
        super.onCommandResult(context, cmdMessage);
        System.out.println("=====JPush======onCommandResult=============>"+cmdMessage.toString());
    }
}