package ru.dedoxyribose.timerviewapplication;

import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    TimerView mTimerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTimerView = (TimerView) findViewById(R.id.timerView);
        mTimerView.setOnTimerViewChangeListener(new TimerView.OnTimerViewChangeListener() {
            @Override
            public void onTimeChangedByUser(TimerView timerView, int time) {
                Log.d("TVA main", "onTimeChangedByUser time="+time);
            }

            @Override
            public void onStartTrackingTouch(TimerView timerView) {
                Log.d("TVA main", "onStartTrackingTouch");
            }

            @Override
            public void onStopTrackingTouch(TimerView timerView) {
                Log.d("TVA main", "onStopTrackingTouch");
            }

            @Override
            public void onPlayStarted(TimerView timerView) {
                Log.d("TVA main", "onPlayStarted");
            }

            @Override
            public void onPlayStopped(TimerView timerView) {
                Log.d("TVA main", "onPlayStopped");
            }

            @Override
            public void onPlayFinished(TimerView timerView) {
                Log.d("TVA main", "onPlayFinished");
            }
        });

        mTimerView.postDelayed(new Runnable() {
            @Override
            public void run() {
                /*mTimerView.setPlayButtonIconTint(ContextCompat.getColor(getApplication(), R.color.colorAdd));
                mTimerView.setPauseButtonIconTint(ContextCompat.getColor(getApplication(), R.color.colorAdd));
                mTimerView.setFinishIconTint(ContextCompat.getColor(getApplication(), R.color.colorAdd));

                mTimerView.setPlayButtonIcon(R.mipmap.ic_launcher);
                mTimerView.setCustomBackgroundDrawable(R.mipmap.ic_launcher);

                mTimerView.setCurTime(0);*/

                //mTimerView.setAllowMoveBackward(false);

                //mTimerView.setPlayButtonTriangleSideLength(150);

                //mTimerView.setProgressWidth(58);

                //mTimerView.setCircleBackgroundColor(ContextCompat.getColor(getApplication(), R.color.colorAdd));

                //mTimerView.setSmallTextSize(48);

                //mTimerView.setTimeFormat("hh:mm:ss");

                //mTimerView.setBigTextColor(ContextCompat.getColor(getApplication(), R.color.colorAdd));

                //mTimerView.setCountdown(true);



            }
        }, 2000);

        //mTimerView.setProgressWidth(188);

        mTimerView.setTimeFormat("HH:mm:ss");



        //mTimerView.setFullTime(8000);

        //mTimerView.setSmallTextColor(ContextCompat.getColor(getApplication(), R.color.colorAdd));
    }
}
