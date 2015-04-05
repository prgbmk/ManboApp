package com.test.mungipark.lolipop_project;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.View;
import android.view.View.OnClickListener;

//import android.util.Log;//디버깅용 로그캣



public class MainActivity extends Activity implements SensorEventListener{

    private Button jogstart;
    private Button manbo_reset;


    private long lastTime;

    private float speed;
    private float lastX;
    private float lastY;
    private float lastZ;

    private float x, y, z;

    private static final int SHAKE_THRESHOLD = 800;

    private SensorManager sensorManager;
    private Sensor accelerormeterSensor;

    private TextView man;
    int manbo_count=0;//만보기 카운터

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //버튼 초기화
        jogstart = (Button)findViewById(R.id.Jog_start);
        manbo_reset = (Button)findViewById(R.id.Jog_reset);
        man = (TextView)findViewById(R.id.Jog_Count_Text);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        //Sensor 객체에 가속도 센서를 가져온다.
        accelerormeterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        man.setText("걸음 횟수 : "+manbo_count+"걸음");

        manbo_reset.setOnClickListener(new OnClickListener(){//만보계 리셋
            public void onClick(View v){
                manbo_count=0;
                man.setText("걸음 횟수 : "+manbo_count+"걸음");
            }
        });


    }

    @Override
    public void onStart() {

        super.onStart();

        if (accelerormeterSensor != null)

            sensorManager.registerListener(this, accelerormeterSensor,

                    SensorManager.SENSOR_DELAY_NORMAL);

    }


    @Override
    public void onStop() {

        super.onStop();

        if (sensorManager != null)

            sensorManager.unregisterListener((SensorEventListener) this);

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

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @SuppressWarnings("deprecation")
    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            long currentTime = System.currentTimeMillis();

            long gabOfTime = (currentTime - lastTime);

            if (gabOfTime > 100) {

                lastTime = currentTime;
                //각 축 센서 [0] = x 축, [1] = y 축, [2] = z 축
                x = event.values[0];

                y = event.values[1];

                z = event.values[2];

                speed = Math.abs(x + y + z - lastX - lastY - lastZ) /


                        gabOfTime * 10000;

                if (speed > SHAKE_THRESHOLD) {
                    manbo_count++;
                    man.setText("걸음 횟수 : " +manbo_count+"걸음");

                    // 일정 변위 변동량 초과!

                }

                lastX = event.values[0];

                lastY = event.values[1];

                lastZ = event.values[2];

            }

        }

    }

}
