package com.test.mungipark.lolipop_project;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;

/**
 * Created by MungiPark on 2015. 5. 18..
 * 시간이 되면(오후 11시) DB 업데이트를 위한 서비스 클래스
 */
public class DBupdateService extends Service{

    private String DB_Result;
    private int Count;
    private Intent intent;

    BackgroundService mService;
    boolean mBound = false;


    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            BackgroundService.LocalBinder binder = (BackgroundService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBound = false;
        }
    };



    public void onCreate(){
        super.onCreate();
        Log.d("DBUpdate Service ", "OnCreat()를 호출");
        intent = new Intent(this, BackgroundService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    public int onStartCommand(Intent intent, int flags, int startID){


        new inputDB().execute();

        return super.onStartCommand(intent, flags, startID);
    }

    //만보계 정보(날짜, 걸음걸이수) 넣는 스레드 메소드.
    private class inputDB extends AsyncTask<Void, Void, String> {

        @Override
        // 백그라운드에서 작업할 작업을 지시함
        protected String doInBackground(Void... params) {
            String output;
            Log.d("Input DB 작업 : ", String.valueOf(mBound));
            if(mBound){
                Count = mService.getCount();
                Log.d("mService : ", "실행완료");
            }
            output = InsertData();
            return output;
        }

        protected void onPostExecute(String temp) {


        }
    }

    //DB데이터 쓰는 함수 - AsyncTask 스레드에 탐재할 함수
    private String InsertData() {
        URL url = null;
        try {
            url = new URL("http://192.168.0.103/insert_menu(Manbo).php");

            HttpURLConnection http = (HttpURLConnection) url.openConnection();//php접속

            http.setDefaultUseCaches(false);
            http.setDoInput(true);//서버 읽기 모드
            http.setDoOutput(true);//서버 쓰기 모드
            http.setRequestMethod("POST");//POST방식 전송(보안용)
            http.setRequestProperty("content-type", "application/x-www-form-urlencoded");

            //php에 파라미터 넘겨주는 작업 시작.
            StringBuffer buffer = new StringBuffer();
            buffer.append("date").append("=").append(get_Date().toString()).append("&");
            buffer.append("walk").append("=").append(String.valueOf(Count));
            Log.d("DBUpdateService Count(in InsertData) ", String.valueOf(Count));

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
            while ((str = reader.readLine()) != null) {
                builder.append(str + "\n");
            }

            DB_Result = builder.toString();


        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {

        }

        return DB_Result;
    }

    //날짜 받아오는 함수 : 20150518 이런식으로.
    private String get_Date() {
        String temp;
        if (Integer.valueOf(Calendar.getInstance().get(Calendar.MONTH) + 1) < 10) {
            temp = String.valueOf(Calendar.getInstance().get(Calendar.YEAR) + "0" +
                    (Calendar.getInstance().get(Calendar.MONTH) + 1));
        } else {
            temp = String.valueOf(Calendar.getInstance().get(Calendar.YEAR) +
                    (Calendar.getInstance().get(Calendar.MONTH) + 1));
        }
        if (Integer.valueOf(Calendar.getInstance().get(Calendar.DAY_OF_MONTH)) < 10) {
            temp = temp + "0" + String.valueOf(Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
        } else {
            temp = temp + String.valueOf(Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
        }
        return temp;
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
