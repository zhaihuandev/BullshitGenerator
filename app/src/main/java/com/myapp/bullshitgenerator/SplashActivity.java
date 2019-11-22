package com.myapp.bullshitgenerator;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        //发送两秒钟的延迟
        handler.sendMessageDelayed(Message.obtain(),2000);
    }

    protected Handler handler = new Handler(){
        public void handleMessage(Message msg){
            //如果当前Activity已经退出，不处理消息，直接返回
            if(isFinishing()){
                return;
            }
            //进入主页面
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            //结束活动
            finish();
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}
