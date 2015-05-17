package com.test.mungipark.lolipop_project;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import android.view.View;
import android.view.View.OnClickListener;

import android.util.Log;//디버깅용 로그캣



public class MainActivity extends Activity{

    private Button jogstart;
    private Button manbo_reset;
    private Intent intent;

    //Initial mService
    BackgroundService mService;//서비스 담을 객체(바인딩할 대상)
    boolean mBound = false;

    private TextView man;
    private int manbo_count=0;//만보기 카운터

    private BackgroundService.Listener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //버튼 초기화
        jogstart = (Button)findViewById(R.id.Jog_start);
        manbo_reset = (Button)findViewById(R.id.Jog_reset);
        man = (TextView)findViewById(R.id.Jog_Count_Text);


        man.setText("걸음 횟수 : " + manbo_count + "걸음");

        manbo_reset.setOnClickListener(new OnClickListener() {//만보계 리셋
            public void onClick(View v) {
                //BackgroundService 중단
                stopService(intent);
            }
        });
        jogstart.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //매개변수 : 변수이름, 전달내용
                startService(intent);
            }
        });


    }


    @Override
    public void onStart() {

        super.onStart();
        //Intent : 여러 Activity간 데이터 주고 받는데 쓰는 객체이다.
        intent = new Intent(this, BackgroundService.class);//intent 선언(대상은 BackgroundService Class)
        //BackgroundService Class와 바인드(연결)한다.
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }
    //Defines callbacks for service binding.
    private ServiceConnection mConnection = new ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            BackgroundService.LocalBinder binder = (BackgroundService.LocalBinder) service;
            mService = binder.getService();//BackgroundService 자체를 바인딩 연결함.
            mBound = true;

            //커스텀 리스너로 걸음걸이 MainActivity에 갱신.(UI 간접 접근)
            listener = new BackgroundService.Listener() {
                @Override
                public void onValueChanged(int count) {
                    man.setText("걸음 횟수 : " + String.valueOf(count) + "걸음");
                }
            };
            mService.setOnValueChanged(listener);//BackgroundService의 리스너와 연결
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBound = false;
        }
    };


    @Override
    public void onStop() {

        super.onStop();
        if(mBound){
            unbindService(mConnection);
            mBound = false;
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
