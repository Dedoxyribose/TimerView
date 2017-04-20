# TimerView

[![License](https://img.shields.io/badge/license-Apache%202-green.svg)](https://www.apache.org/licenses/LICENSE-2.0)

![TimerView](https://cloud.githubusercontent.com/assets/4084682/25245264/48c27454-261d-11e7-9e57-c34b24be49c6.png)

A simple android countdown timer. Ready-to-use and fully customizable.

## Features

- Much appearance customization (colors, line thickness, icons) via xml and programmatically
- Customizable time format
- Countdown (00:59) or countup (00:01) time appearance
- Let the user drag and drop current time value (or forbid him to do so)
- Smooth drag animation

## Integration

Clone this project. Direct Gradle integration coming soon.

## Usage

Add this to your xml layout file.

```xml
<ru.dedoxyribose.timerview.TimerView
        android:id="@+id/timerView1"
        android:layout_width="200dp"
        android:layout_height="200dp"
        app:fulltime="15000"/>
```

Then bind it in your activity and register a listener

```Java

    mTimerView1 = (TimerView) findViewById(R.id.timerView1);

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
```

You can programmatically start and stop the timer.

```Java

    mTimerView1.play();
    //mTimerView1.stop();
```
## Customization

You can customize the appearance of the timer. Take a look at this diagrams.

![TimerView](https://cloud.githubusercontent.com/assets/4084682/25245267/48ce5b5c-261d-11e7-8ec3-97195aa9ab12.png)
![TimerView](https://cloud.githubusercontent.com/assets/4084682/25245265/48c359aa-261d-11e7-8cfa-c3c7b991d903.png)
![TimerView](https://cloud.githubusercontent.com/assets/4084682/25245266/48c508c2-261d-11e7-91d4-60b167ed3222.png)

Via xml:

```xml

<ru.dedoxyribose.timerview.TimerView
        android:id="@+id/timerView1"
        android:layout_width="200dp"
        android:layout_height="200dp"
        android:layout_centerHorizontal="true"
        android:layout_centerInParent="false"
        android:layout_centerVertical="false"
        android:layout_marginBottom="45dp"
        android:layout_marginTop="45dp"
        app:grooveWidth="17dp"
        app:progressWidth="8dp"
        app:grooveColor="@color/colorPrimaryDark"
        app:progressColor="@color/colorAccent"
        app:backgroundCircleColor="@color/colorPrimary"
        app:countdown="true"
        app:allowMoveForward="true"
        app:allowMoveBackward="false"
        app:bigTextSize="25sp"
        app:bigTextColor="@color/colorAccent"
        app:fulltime="8000"
        app:playButtonTriangleSideLength="45dp"
        />

```

or in Java code:

```Java

    mTimerView1.setGrooveWidth(20);
    mTimerView1.setProgressWidth(20);

    mTimerView1.setCountdown(false);

    mTimerView1.setGrooveColor(ContextCompat.getColor(getApplication(), R.color.colorArc));
    mTimerView1.setProgressColor(ContextCompat.getColor(getApplication(), R.color.colorProgress));
    mTimerView1.setBigTextColor(ContextCompat.getColor(getApplication(), R.color.colorProgress));
    mTimerView1.setSmallTextColor(ContextCompat.getColor(getApplication(), R.color.colorProgress));
    mTimerView1.setCircleBackgroundColor(ContextCompat.getColor(getApplication(), R.color.colorBack));
    mTimerView1.setPlayButtonIconTint(ContextCompat.getColor(getApplication(), R.color.colorProgress));
    mTimerView1.setPauseButtonIconTint(ContextCompat.getColor(getApplication(), R.color.colorProgress));

    mTimerView1.setBigTextSize(30);
    mTimerView1.setSmallTextSize(20);
    mTimerView1.setPlayButtonTriangleSideLength(40);

    mTimerView1.setFinishIcon(R.drawable.ic_checkmark);
    mTimerView1.setFinishIconTint(ContextCompat.getColor(getApplication(), R.color.colorProgress));


    mTimerView1.setPlayButtonIcon(R.drawable.play_icon);
    mTimerView1.setPauseButtonIcon(R.drawable.pause_icon);
    mTimerView1.setCircleBackgroundDrawable(R.drawable.back_icon);

    mTimerView1.setTimeFormat("mm.ss");


```

## Sample

Clone this repository and check out the `app` module.

## License

    Copyright 2017 Dedoxyribose

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
