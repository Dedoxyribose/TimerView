package ru.dedoxyribose.timerviewapplication;

import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import ru.dedoxyribose.timerview.TimerView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "TAG TVA";
    TimerView mTimerView1;
    TimerView mTimerView2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTimerView1 = (TimerView) findViewById(R.id.timerView1);
        mTimerView2 = (TimerView) findViewById(R.id.timerView2);

        mTimerView1.setOnTimerViewChangeListener(new TimerView.OnTimerViewChangeListener() {
            @Override
            public void onTimeChangedByUser(TimerView timerView, int time) {
                Log.d(TAG, "onTimeChangedByUser time="+time);
            }

            @Override
            public void onStartTrackingTouch(TimerView timerView) {
                Log.d(TAG, "onStartTrackingTouch");
            }

            @Override
            public void onStopTrackingTouch(TimerView timerView) {
                Log.d(TAG, "onStopTrackingTouch");
            }

            @Override
            public void onPlayStarted(TimerView timerView) {
                Log.d(TAG, "onPlayStarted");
            }

            @Override
            public void onPlayStopped(TimerView timerView) {
                Log.d(TAG, "onPlayStopped");
            }

            @Override
            public void onPlayFinished(TimerView timerView) {
                Log.d(TAG, "onPlayFinished");
            }
        });


        //for TimerView2 we will set values manually, not in xml

        mTimerView2.setGrooveWidth(20);
        mTimerView2.setProgressWidth(20);

        mTimerView2.setCountdown(false);

        mTimerView2.setGrooveColor(ContextCompat.getColor(getApplication(), R.color.colorArc));
        mTimerView2.setProgressColor(ContextCompat.getColor(getApplication(), R.color.colorProgress));
        mTimerView2.setBigTextColor(ContextCompat.getColor(getApplication(), R.color.colorProgress));
        mTimerView2.setSmallTextColor(ContextCompat.getColor(getApplication(), R.color.colorProgress));
        mTimerView2.setCircleBackgroundColor(ContextCompat.getColor(getApplication(), R.color.colorBack));
        mTimerView2.setPlayButtonIconTint(ContextCompat.getColor(getApplication(), R.color.colorProgress));
        mTimerView2.setPauseButtonIconTint(ContextCompat.getColor(getApplication(), R.color.colorProgress));

        mTimerView2.setBigTextSize(30);
        mTimerView2.setSmallTextSize(20);
        mTimerView2.setPlayButtonTriangleSideLength(40);

        mTimerView2.setFinishIcon(R.drawable.ic_checkmark);
        mTimerView2.setFinishIconTint(ContextCompat.getColor(getApplication(), R.color.colorProgress));

        // you can also manually set icon for the play button
        // mTimerView2.setPlayButtonIcon(your resource/drawable here);

        // and the pause button
        // mTimerView2.setPauseButtonIcon(your resource/drawable here);

        // and the background circle drawable
        // mTimerView2.setCircleBackgroundDrawable(your resource/drawable here);

        mTimerView2.setTimeFormat("mm.ss");


    }
}
