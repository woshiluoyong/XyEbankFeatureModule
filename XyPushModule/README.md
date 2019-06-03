# Push模块集成使用说明
* 在项目根build.gradle处加入

```
allprojects {
    repositories {
        ...
        maven {url 'http://developer.huawei.com/repo/'}
        maven { url 'https://jitpack.io' }
    }
}
```

* 依赖本项目XyPushModule,注意:如果你项目用的gradle插件版本低于3.0,需要修改push模块项目中的implementation和api改成compile

* 在业务项目的build.gradle处加入
* 极光和华为的配置代码到manifestPlaceholders
* dependencies处依赖上项目XyPushModule

```
apply plugin: 'com.android.application'

android {
    ...

    defaultConfig {
        ...

        manifestPlaceholders = [
            JPUSH_PKGNAME: applicationId,
            JPUSH_APPKEY: "18ecbb94a863e01709c61007", //JPush 上注册的包名对应的 AppKey
            JPUSH_CHANNEL: "developer-default", //暂时填写默认值即可
            HAIWEI_APPID: "100816565" //华为push 上生成应用对应的 AppId
        ]
    }

    ....
}

dependencies {
    ...

    api project(':XyPushModule')
}
```

* AndroidManifest.xml文件里面application节点配置一个自定义Application,比如android:name=".DemoApplication"
* application节点里面新建一个自定义Activity,代码:

```
<activity android:name=".PushTranslateActivity"><!--Push相关-->
    <intent-filter>
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <data android:host="com.xyebank.stephen.push" android:path="/notification" android:scheme="stephenpush" />
    </intent-filter>
</activity>
```

* 然后在DemoApplication中onCreate方法启动注册Push,initStephenPush的第二个参数和第三个参数是小米的AppId和AppKey

```
if(StephenPushUtils.getInstance().shouldInit(this))StephenPushUtils.getInstance().initStephenPush(this,true, 小米AppId, 小米AppKey);
```

* 然后主Activity中(比如MainActivity)添加如下代码

```
onCreate中:
StephenPushUtils.getInstance().startHuaWeiPush(this);

String pushParam = getIntent().getStringExtra(PushTranslateActivity.PushParamKey);
if(!TextUtils.isEmpty(pushParam))receiveFromPush(pushParam);

定义方法:
public void receiveFromPush(String param){
    System.out.println("===com.stephen.push====接收统一操作的Push参数=====>"+param);
}
```

* 最后在PushTranslateActivity中添加如下代码处理后台返回参数 *(注:====>处代码使用时必须在DemoApplication中定义private MainActivity mainActivity;然后为mainActivity设置上Getter/Settter,
  此处操作也可以换成发自定义广播,然后在MainActivity的onCreate中加上((DemoApplication)getApplication()).setMainActivity(this);),下面代码中MainActivity.class.getName()的MainActivity也要相应替换成你主Activity*

```
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main3);

    String pushParam = null;
    try {
        pushParam = getIntent().getData().getQueryParameter("action");
    } catch (Exception e){
        e.printStackTrace();
    }
    boolean isRunning = StephenPushUtils.getInstance().isActivityRunning(this, MainActivity.class.getName());//判断主Activity是否在运行来确定app是否被杀死
    System.out.println("===com.stephen.push===PushTranslateActivity===isRunning===>"+isRunning+"==pushParam===>"+pushParam);

    if(isRunning){
        ====>((DemoApplication)getApplication()).getMainActivity().receiveFromPush(pushParam);
    }else{
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(PushParamKey, pushParam);
        startActivity(intent);
    }
    finish();
}
```

* 最后就是回到MainActivity中的receiveFromPush方法中根据参数处理对应跳转逻辑

# 注意:后台推送处需要推送的是自定义动作哈,值为:"intent://com.xyebank.stephen.push/notification?action=stephen-test-parameter#Intent;scheme=stephenpush;launchFlags=0x10000000;end",里面的action=stephen-test-parameter就是键值对参数