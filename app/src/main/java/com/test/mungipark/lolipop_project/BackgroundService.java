package com.test.mungipark.lolipop_project;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by MungiPark on 2015. 5. 17..
 * Background용 서비스 돌릴 클래스 객체
 */
public class BackgroundService extends Service implements SensorEventListener{

    private String result;
    private int manbo_count = 0;

    private long lastTime;

    private float speed;
    private float lastX;
    private float lastY;
    private float lastZ;

    private float x, y, z;

    private static final int SHAKE_THRESHOLD = 800;

    private SensorManager sensorManager;
    private Sensor accelerormeterSensor;

    //센서값 바뀐걸 넘겨주기위한 리스너 정의 구간.
    private Listener myListener;

    public interface Listener{//리스너 정의
        public void onValueChanged(int count);
    }

    public void setOnValueChanged(Listener listener){//리스너 연결구간
        this.myListener = listener;
    }

    public void onValueChanged(int count){

    }
    //리스너 정의 끝.

    //바인더 - 센서값 전송하기 위한 통신
    private final IBinder mBinder = new LocalBinder();

    //MainActivity에서 이 메소드를 호출하면 이 클래스 자체를 넘겨준다.
    public class LocalBinder extends Binder {
        BackgroundService getService(){
            return BackgroundService.this;
        }
    }


    //생성자 생성
    public BackgroundService (){ }

    public void onCreate(){
        super.onCreate();
        Log.d("Background Service : ", "OnCreat()를 호출");

    }
    public int onStartCommand(Intent intent, int flags, int startID){

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        //Sensor 객체에 가속도 센서를 가져온다.
        accelerormeterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        //센서 매니저 고고
        if (sensorManager != null)
            sensorManager.registerListener(this, accelerormeterSensor, SensorManager.SENSOR_DELAY_GAME);
        return super.onStartCommand(intent, flags, startID);
    }

    public void onDestroy(){
        super.onDestroy();
        Log.d("Background Service: ", "onDestory()를 호출");
        if (sensorManager != null)

            sensorManager.unregisterListener((SensorEventListener) this);
    }


    @Override
    public IBinder onBind(Intent intent) {

        return mBinder;
    }

    //센서값이 체인지 될 때 마다 Intent전송.
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            long currentTime = System.currentTimeMillis();

            long gabOfTime = (currentTime - lastTime);

            if (gabOfTime > 100) {//0.1초당 측정..

                lastTime = currentTime;
                //각 축 센서 [0] = x 축, [1] = y 축, [2] = z 축
                x = event.values[0];

                y = event.values[1];

                z = event.values[2];

                speed = Math.abs(x + y + z - lastX - lastY - lastZ) /


                        gabOfTime * 10000;

                if (speed > SHAKE_THRESHOLD) {
                    //걸음걸이 계속 올려.
                    manbo_count++;
                    Log.d("Manbo Changed :", String.valueOf(manbo_count));
                    //getManbo_count();
                    if(myListener != null){
                        myListener.onValueChanged(manbo_count);

                    }
                    // 일정 변위 변동량 초과!
                }

                lastX = event.values[0];

                lastY = event.values[1];

                lastZ = event.values[2];

            }

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
