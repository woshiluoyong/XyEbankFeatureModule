package com.xyebank.stephen.testpush;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class MainActivity2 extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        System.out.println("=====MainActivity2=====onActivityResult==========>");
    }
}
