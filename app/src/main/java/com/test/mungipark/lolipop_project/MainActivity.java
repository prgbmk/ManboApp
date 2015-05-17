package com.test.mungipark.lolipop_project;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import android.view.View;
import android.view.View.OnClickListener;

import android.util.Log;//디버깅용 로그캣
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.util.Calendar;

/**
 * Created by MungiPark on 2015. 5. 18..
 * Mysql 정보 교환하기 위해서 Manifest에 꼭 Internet Permission 해줘야함
 *
 */

public class MainActivity extends Activity{

    private Button jogstart;
    private Button manbo_reset;
    private Button db_Btn;
    private Intent intent;

    private String DB_Result;
    private String[] DB_data;//DB_data[0] - date | DB_date[1] - Count
    private String[] tempDB_str;//Parsing용 임시 객체

    //Initial mService
    BackgroundService mService;//서비스 담을 객체(바인딩할 대상)
    boolean mBound = false;

    //TextView
    private TextView manboTxt;//걸음걸이 확인용
    private TextView timeTxt;//현재시간 확인용
    private TextView dbTxt;//D 확인용


    private int manbo_count=0;//만보기 카운터

    private BackgroundService.Listener listener;

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
                    manboTxt.setText("걸음 횟수 : " + String.valueOf(count) + "걸음");
                }
            };
            mService.setOnValueChanged(listener);//BackgroundService의 리스너와 연결
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBound = false;
        }
    };


    //저장된 만보계 정보 보는 메소드 돌리는 스레드
    private class showDB extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {
            String output;
            Log.d("Show DB스레드 작업 : ", "실행완료");
            output = ShowData();
            return output;
        }
        //UI Update
        protected void onPostExecute(String tmp){
            setDBResult(DB_data);
        }
    }

    //DB데이터 읽는 함수(해당 날짜의 데이터만 읽어오기) - AsyncTask 스레드에 탐재할 함수
    private String ShowData(){
        URL url = null;
        try {
            //url = new URL("http://192.168.0.104/show_data_date.php");
            url = new URL("http://192.168.0.103/show_data_date(Manbo).php");

            HttpURLConnection http = (HttpURLConnection) url.openConnection();//php접속

            http.setDefaultUseCaches(false);
            http.setDoInput(true);//서버 읽기 모드
            http.setDoOutput(true);//서버 쓰기 모드
            http.setRequestMethod("POST");//POST방식 전송(보안용)
            http.setRequestProperty("content-type", "application/x-www-form-urlencoded");

            //php에 파라미터 넘겨주는 작업 시작.
            StringBuffer buffer = new StringBuffer();
            buffer.append("date").append("=").append(get_Date().toString());
            //buffer.append("date").append("=").append("20150518");

            //Php에 파라미터 값 넘기기
            OutputStreamWriter outStream = new OutputStreamWriter(http.getOutputStream(), "UTF-8");
            PrintWriter writer = new PrintWriter(outStream);
            writer.write(buffer.toString());
            writer.flush();

            //파라미터값 넘기고나서 나오는 결과 받기
            InputStreamReader tmp = new InputStreamReader(http.getInputStream(), "UTF-8");
            BufferedReader reader = new BufferedReader(tmp);
            StringBuilder builder = new StringBuilder();
            String str;
            while((str = reader.readLine()) != null){
                builder.append(str + "\n");
            }
            DB_Result = builder.toString();

            //문자열 분리 및 파싱
            tempDB_str = DB_Result.split("\n");
            DB_data = tempDB_str[1].split(String.valueOf("[*]"));


            Log.d("show_data_date(Manbo).php <Date>", DB_Result);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e){

        }

        return DB_Result;
    }

    //날짜 받아오는 함수 : 20150518 이런식으로.
    private String get_Date(){
        if(Integer.valueOf(Calendar.getInstance().get(Calendar.MONTH) + 1) >=10){
            return String.valueOf(Calendar.getInstance().get(Calendar.YEAR) +
                    (Calendar.getInstance().get(Calendar.MONTH) + 1) +
                    Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
        }
        else{
            return  String.valueOf(Calendar.getInstance().get(Calendar.YEAR) + "0" +
                    (Calendar.getInstance().get(Calendar.MONTH) + 1) +
                    Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //객체 인스턴트 초기화
        jogstart = (Button)findViewById(R.id.Jog_start);
        manbo_reset = (Button)findViewById(R.id.Jog_reset);
        manboTxt = (TextView)findViewById(R.id.Jog_Count_Text);
        dbTxt = (TextView)findViewById(R.id.db_Txt);
        //DB연동용 객체 인스턴스
        timeTxt = (TextView)findViewById(R.id.currentTime);
        db_Btn = (Button)findViewById(R.id.dbBtn);

        manboTxt.setText("걸음 횟수 : " + manbo_count + "걸음");
        timeTxt.setText(get_Date());

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

        //DB확인용
        db_Btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                new showDB().execute();
                //dbTxt.setText(DB_Result);
            }
        });
    }

    //UI Update Method
    private void setDBResult(String[] result){
        dbTxt.setText(result[0]);
        dbTxt.append("에는 ");
        dbTxt.append(result[1]);
    }

    @Override
    public void onStart() {

        super.onStart();
        //Intent : 여러 Activity간 데이터 주고 받는데 쓰는 객체이다.
        intent = new Intent(this, BackgroundService.class);//intent 선언(대상은 BackgroundService Class)
        //BackgroundService Class와 바인드(연결)한다.
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

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
