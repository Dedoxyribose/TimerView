package ru.dedoxyribose.timerviewapplication;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Ryan on 18.04.2017.
 */

public class TimerView extends View {

    public static int INVALID_VALUE = -1;

    private static final float PLAY_TRIANGLE_SIZE_FACTOR = 2.0f;
    private static final float PAUSE_SHAPE_SIZE_FACTOR = 8.0f;
    private static final float PAUSE_SHAPE_STROKE_WIDTH_FACTOR = 17.0f;
    private static final float PAUSE_SHAPE_WIDTH_TO_HEIGHT_FACTOR = 0.6f;
    private static final float FINISH_SHAPE_SIZE_FACTOR = 2.0f;
    private static final float FINISH_SHAPE_STROKE_WIDTH_FACTOR = 8f;

    /**
     * Offset = -90 indicates that the progress starts from 12 o'clock.
     */
    private static final int ANGLE_OFFSET = -90;

    /**
     * The current points value.
     */
    private int mCurTime = 0;

    /**
     * The string containing formatted representation of time how it will be print out on the timer
     */
    private String mCurFormattedTime;

    /**
     * The time representing the whole cycle of the timer
     */
    private int mFullTime = 60000;

    /**
     * The system time on the previous animation step
     */
    private long mPreviousSystemTime;

    /**
     * The Drawable for the play button
     */
    private Drawable mPlayIcon;

    /**
     * The lenght of the side of the triangle representing the play button (if custom drawable not set by user)
     */
    private int mPlayButtonTriangleSideLength=-1;

    /**
     * The shape of the triangle representing the play button (if custom drawable not set by user)
     */
    private Path mPlayTriangle;

    /**
     * The Drawable for the pause button
     */
    private Drawable mPauseIcon;

    /**
     * The shape of the pause button (if custom drawable not set by user)
     */
    private Path mPauseShape;

    /**
     * The Drawable for the finish icon
     */
    private Drawable mFinishIcon;

    /**
     * The shape of the finish icon (if custom drawable not set by user)
     */
    private Path mFinishShape;

    /**
     * The Drawable for the background
     */
    private Drawable mBackgroundDrawable;

    /**
     * The width of the progress arc
     */
    private int mProgressWidth = 12;

    /**
     * The width of the groove arc (under progress)
     */
    private int mGrooveWidth = 12;

    /**
     * The width of the groove arc or if it's <=0, the width of the progress arc
     */
    private int mArcWidth = 12;

    /**
     * The radius of the view area
     */
    private int mFullRadius = 12;

    /**
     * The amount of touch events received at the current touch session
     */
    private boolean mCountdown = true;

    /**
     * The amount of touch events received at the current touch session
     */
    private int mThisTouchUpdateTimes = 0;

    /**
     * The time of the previous moment
     */
    private int mPreviousTime = -1;

    /**
     * The time before current touch session started
     */
    private int mTimeBeforeTouch = 0;

    /**
     * The format for the time representation on the screen
     */
    private String mTimeFormat = "mm:ss";

    /**
     * The radius of the inner arc
     */
    private int mArcRadius = 0;
    private RectF mArcRect = new RectF();

    private RectF mBackRect = new RectF();

    private float mRealProgressSweep = 0;

    /**
     * The progress sweep value adjusted by the animation
     */
    private float mVisibleProgressSweep = 0;
    private Paint mProgressPaint;

    /**
     * whether the view is touchable
     */
    private boolean mEnabled;

    private Paint mGroovePaint;

    /**
     * The animator for smooth transitions
     */
    private ValueAnimator mSweepAnimation;

    private float mBigTextSize = 40;
    private float mSmallTextSize = 16;
    private Paint mBigTextPaint;
    private Paint mSmallTextPaint;
    private Rect mBigTextRect = new Rect();
    private Rect mSmallTextRect = new Rect();

    private Paint mBackPaint;

    private Integer mPlayButtonTint = null;
    private Integer mPauseButtonTint = null;
    private Integer mFinishIconTint = null;

    private Paint mPlayTrianglePaint;
    private Paint mPauseShapePaint;
    private Paint mFinishShapePaint;

    /**
     * The center X of the canvas
     */
    private int mTranslateX;

    /**
     * The center Y of the canvas
     */
    private int mTranslateY;

    /**
     * Indicates whether the touch is in progress
     */
    private boolean mIsTouchProgress = false;

    /**
     * Indicates whether the playing is in the progress
     */
    private boolean mIsPlaying = false;

    /**
     * Indicates whether the view is attached to the window
     */
    private boolean mIsAttached = false;

    /**
     * Indicates whether the user is allowed to move the time forward manually
     */
    private boolean mAllowMoveForward = true;

    /**
     * Indicates whether the user is allowed to move the time backward manually
     */
    private boolean mAllowMoveBackward = true;

    /**
     * The current touch angle of arc.
     */
    private double mTouchAngle;
    private OnTimerViewChangeListener mOnTimerViewChangeListener;
    
    public TimerView(Context context) {
        super(context);
        init(context, null);
    }

    public TimerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public TimerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {

        float density = getResources().getDisplayMetrics().density;

        // Defaults, may need to link this into theme settings
        int grooveColor = ContextCompat.getColor(context, R.color.colorPrimaryDark);
        int progressColor = ContextCompat.getColor(context, R.color.colorAccent);

        int bigTextColor = ContextCompat.getColor(context, R.color.colorAccent);
        int smallTextColor = ContextCompat.getColor(context, R.color.colorAccent);

        int backColor = ContextCompat.getColor(context, R.color.colorPrimary);

        int playButtonTint = ContextCompat.getColor(context, R.color.colorAccent);
        int pauseButtonTint = ContextCompat.getColor(context, R.color.colorAccent);
        int finishIconTint = ContextCompat.getColor(context, R.color.colorAccent);

        mProgressWidth = (int) (mProgressWidth * density);
        mGrooveWidth = (int) (mGrooveWidth * density);
        mBigTextSize = (int) (mBigTextSize * density);
        mSmallTextSize = (int) (mSmallTextSize * density);

        if (attrs != null) {
            // Attribute initialization
            final TypedArray a = context.obtainStyledAttributes(attrs,
                    R.styleable.TimerView, 0, 0);

            if (a.hasValue(R.styleable.TimerView_timeFormat))
                mTimeFormat = a.getString(R.styleable.TimerView_timeFormat);

            playButtonTint = a.getColor(R.styleable.TimerView_playButtonTint, playButtonTint);

            mPlayIcon = a.getDrawable(R.styleable.TimerView_playIcon);
            if (mPlayIcon != null) {
                int playIconHalfWidth = mPlayIcon.getIntrinsicWidth() / 2;
                int playIconHalfHeight = mPlayIcon.getIntrinsicHeight() / 2;

                mPlayIcon.setBounds(-playIconHalfWidth, -playIconHalfHeight, playIconHalfWidth,
                        playIconHalfHeight);

                if (a.hasValue(R.styleable.TimerView_playButtonTint)) {
                    mPlayButtonTint=playButtonTint;
                    mPlayIcon.setColorFilter(new PorterDuffColorFilter(mPlayButtonTint, PorterDuff.Mode.SRC_ATOP));
                }
            }

            pauseButtonTint = a.getColor(R.styleable.TimerView_pauseButtonTint, pauseButtonTint);

            mPauseIcon = a.getDrawable(R.styleable.TimerView_pauseIcon);
            if (mPauseIcon != null) {
                int playIconHalfWidth = mPauseIcon.getIntrinsicWidth() / 2;
                int playIconHalfHeight = mPauseIcon.getIntrinsicHeight() / 2;

                mPauseIcon.setBounds(-playIconHalfWidth, -playIconHalfHeight, playIconHalfWidth,
                        playIconHalfHeight);

                if (a.hasValue(R.styleable.TimerView_pauseButtonTint)) {
                    mPauseButtonTint=pauseButtonTint;
                    mPauseIcon.setColorFilter(new PorterDuffColorFilter(mPauseButtonTint, PorterDuff.Mode.SRC_ATOP));
                }
            }

            finishIconTint = a.getColor(R.styleable.TimerView_finishIconTint, finishIconTint);

            mFinishIcon = a.getDrawable(R.styleable.TimerView_finishIcon);
            if (mFinishIcon != null) {
                int finishIconHalfWidth = mFinishIcon.getIntrinsicWidth() / 2;
                int finishIconHalfHeight = mFinishIcon.getIntrinsicHeight() / 2;

                mFinishIcon.setBounds(-finishIconHalfWidth, -finishIconHalfHeight, finishIconHalfWidth,
                        finishIconHalfHeight);

                if (a.hasValue(R.styleable.TimerView_finishIconTint)) {
                    mFinishIconTint = finishIconTint;
                    mFinishIcon.setColorFilter(new PorterDuffColorFilter(mFinishIconTint, PorterDuff.Mode.SRC_ATOP));
                }
            }

            mBackgroundDrawable = a.getDrawable(R.styleable.TimerView_backgroundCircleDrawable);

            mFullTime = a.getInteger(R.styleable.TimerView_fulltime, mFullTime);
            mCurTime = a.getInteger(R.styleable.TimerView_curtime, mCurTime);

            mProgressWidth = (int) a.getDimension(R.styleable.TimerView_progressWidth, mProgressWidth);
            progressColor = a.getColor(R.styleable.TimerView_progressColor, progressColor);

            mGrooveWidth = (int) a.getDimension(R.styleable.TimerView_grooveWidth, mGrooveWidth);
            grooveColor = a.getColor(R.styleable.TimerView_grooveColor, grooveColor);

            mPlayButtonTriangleSideLength = (int) a.getDimension(R.styleable.TimerView_playButtonTriangleSideLength,
                    mPlayButtonTriangleSideLength);

            mBigTextSize = (int) a.getDimension(R.styleable.TimerView_bigTextSize, mBigTextSize);
            mSmallTextSize = (int) a.getDimension(R.styleable.TimerView_smallTextSize, mSmallTextSize);
            bigTextColor = a.getColor(R.styleable.TimerView_bigTextColor, bigTextColor);
            smallTextColor = a.getColor(R.styleable.TimerView_smallTextColor, smallTextColor);

            backColor = a.getColor(R.styleable.TimerView_backgroundCircleColor, backColor);

            mAllowMoveForward = a.getBoolean(R.styleable.TimerView_allowMoveForward, mAllowMoveForward);
            mAllowMoveBackward = a.getBoolean(R.styleable.TimerView_allowMoveBackward, mAllowMoveBackward);

            mEnabled = a.getBoolean(R.styleable.TimerView_enabled, mEnabled);

            mCountdown = a.getBoolean(R.styleable.TimerView_countdown, mCountdown);

            a.recycle();
        }

        // range check
        mCurTime = (mCurTime > mFullTime) ? mFullTime : mCurTime;

        formatCurTime();

        mRealProgressSweep = (float) mCurTime / valuePerDegree();
        mVisibleProgressSweep = mRealProgressSweep;

        mGroovePaint = new Paint();
        mGroovePaint.setColor(grooveColor);
        mGroovePaint.setAntiAlias(true);
        mGroovePaint.setStyle(Paint.Style.STROKE);
        mGroovePaint.setStrokeWidth(mGrooveWidth);

        mBackPaint = new Paint();
        mBackPaint.setColor(backColor);
        mBackPaint.setAntiAlias(true);
        mBackPaint.setStyle(Paint.Style.FILL);

        mProgressPaint = new Paint();
        mProgressPaint.setColor(progressColor);
        mProgressPaint.setAntiAlias(true);
        mProgressPaint.setStyle(Paint.Style.STROKE);
        mProgressPaint.setStrokeWidth(mProgressWidth);

        mBigTextPaint = new Paint();
        mBigTextPaint.setColor(bigTextColor);
        mBigTextPaint.setAntiAlias(true);
        mBigTextPaint.setStyle(Paint.Style.FILL);
        mBigTextPaint.setTextSize(mBigTextSize);

        mSmallTextPaint = new Paint();
        mSmallTextPaint.setColor(smallTextColor);
        mSmallTextPaint.setAntiAlias(true);
        mSmallTextPaint.setStyle(Paint.Style.FILL);
        mSmallTextPaint.setTextSize(mSmallTextSize);

        mPlayTrianglePaint = new Paint();
        mPlayTrianglePaint.setColor(playButtonTint);
        mPlayTrianglePaint.setAntiAlias(true);
        mPlayTrianglePaint.setStyle(Paint.Style.FILL);

        mPauseShapePaint = new Paint();
        mPauseShapePaint.setColor(pauseButtonTint);
        mPauseShapePaint.setAntiAlias(true);
        mPauseShapePaint.setStyle(Paint.Style.STROKE);
        mPauseShapePaint.setStrokeWidth(mFullRadius /PAUSE_SHAPE_STROKE_WIDTH_FACTOR);

        mFinishShapePaint = new Paint();
        mFinishShapePaint.setColor(finishIconTint);
        mFinishShapePaint.setAntiAlias(true);
        mFinishShapePaint.setStyle(Paint.Style.STROKE);
        mFinishShapePaint.setStrokeWidth(mFullRadius /FINISH_SHAPE_STROKE_WIDTH_FACTOR);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        final int width = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        final int height = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        final int min = Math.min(width, height);

        mTranslateX = (int) (width * 0.5f);
        mTranslateY = (int) (height * 0.5f);

        mArcWidth=(mGrooveWidth>0)?mGrooveWidth:mProgressWidth;

        mFullRadius = (min - getPaddingLeft()) / 2;
        int arcDiameter = min - getPaddingLeft() - mArcWidth;
        mArcRadius = arcDiameter / 2;
        float top = height / 2 - (arcDiameter / 2);
        float left = width / 2 - (arcDiameter / 2);
        mArcRect.set(left, top, left + arcDiameter, top + arcDiameter);

        mBackRect.set(height / 2 - mFullRadius, width / 2 - mFullRadius, height / 2 + mFullRadius, width / 2 + mFullRadius);

        mBigTextPaint.getTextBounds(formatTime(0), 0, mCurFormattedTime.length(), mBigTextRect);
        mSmallTextPaint.getTextBounds(formatTime(0), 0, mCurFormattedTime.length(), mSmallTextRect);

        mPlayTriangle = getEquilateralTriangle(mPlayButtonTriangleSideLength!=-1?
                mPlayButtonTriangleSideLength:(int) (mFullRadius /PLAY_TRIANGLE_SIZE_FACTOR));

        mPauseShape = getPauseShape((int) (mFullRadius /PAUSE_SHAPE_SIZE_FACTOR));
        mPauseShapePaint.setStrokeWidth(mFullRadius /PAUSE_SHAPE_STROKE_WIDTH_FACTOR);

        float finishShapeStokeWidth = mFullRadius /FINISH_SHAPE_STROKE_WIDTH_FACTOR;
        mFinishShape = getFinishShape((int) (mFullRadius /FINISH_SHAPE_SIZE_FACTOR), finishShapeStokeWidth);
        mFinishShapePaint.setStrokeWidth(finishShapeStokeWidth);

        if (mBackgroundDrawable!=null) {
            int backHalfWidth = (min - getPaddingLeft()) / 2;
            int backHalfHeight =  (min - getPaddingLeft()) / 2;

            mBackgroundDrawable.setBounds(-backHalfWidth, -backHalfHeight, backHalfWidth,
                    backHalfHeight);
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }


    public static int dpToPx(float dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static int pxToDp(int px) {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }


    @Override
    protected void onDraw(Canvas canvas) {

        if (mBackgroundDrawable!=null) {
            canvas.translate(mTranslateX, mTranslateY);
            mBackgroundDrawable.draw(canvas);
            canvas.translate(-mTranslateX,-mTranslateY);
        }
        else canvas.drawArc(mBackRect, ANGLE_OFFSET, 360, false, mBackPaint);


        if (mIsPlaying) {

            int xPos = mTranslateX - mBigTextRect.width() / 2;
            int yPos = (int) ((mArcRect.centerY()) - ((mBigTextPaint.descent() + mBigTextPaint.ascent()) / 2));

            canvas.drawText(mCurFormattedTime, xPos, yPos, mBigTextPaint);
        } else if (mCurTime<mFullTime || mIsTouchProgress) {

            int xPos = mTranslateX - mSmallTextRect.width() / 2;
            int yPos = (int) (mTranslateY+ mFullRadius *0.5f - ((mSmallTextPaint.descent() + mSmallTextPaint.ascent()) / 2));

            canvas.drawText(mCurFormattedTime, xPos, yPos, mSmallTextPaint);

        }

        // draw the arc and progress
        if (mGrooveWidth>0)
            canvas.drawArc(mArcRect, ANGLE_OFFSET, 360, false, mGroovePaint);

        if (mProgressWidth>0)
            canvas.drawArc(mArcRect, ANGLE_OFFSET, mVisibleProgressSweep, false, mProgressPaint);

        if (!mIsPlaying) {
            canvas.translate(mTranslateX, mTranslateY);

            if (mCurTime<mFullTime || mIsTouchProgress) {
                if (mPlayIcon!=null)
                    mPlayIcon.draw(canvas);
                else canvas.drawPath(mPlayTriangle, mPlayTrianglePaint);
            } else {
                if (mFinishIcon!=null)
                    mFinishIcon.draw(canvas);
                else canvas.drawPath(mFinishShape, mFinishShapePaint);
            }


        }
        else {

            int xPos = canvas.getWidth() / 2;
            int yPos = (int) (mTranslateY+mFullRadius*0.5f);

            canvas.translate(xPos, yPos);

            if (mPauseIcon!=null)
                mPauseIcon.draw(canvas);
            else canvas.drawPath(mPauseShape, mPauseShapePaint);
        }

    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        mIsAttached=true;

        final Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {

                if (mIsPlaying && !mIsTouchProgress)
                    incrementTime();
                mPreviousSystemTime=System.currentTimeMillis();

                if (mIsAttached)
                    handler.post(this);
            }
        });

    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        mIsAttached=false;
    }

    private void incrementTime() {
        long timeDiff = System.currentTimeMillis()-mPreviousSystemTime;

        updateProgress((int) (mCurTime+timeDiff), false);
        if (mCurTime>mFullTime) {
            mCurTime=mFullTime;
            formatCurTime();
            stop();

            if (mOnTimerViewChangeListener!=null)
                mOnTimerViewChangeListener.onPlayFinished(this);
        }
    }

    private void formatCurTime() {
        SimpleDateFormat sdfDate = new SimpleDateFormat(mTimeFormat);
        sdfDate.setTimeZone(TimeZone.getTimeZone("UTC"));
        mCurFormattedTime = sdfDate.format(new Date(mCountdown?(mFullTime-mCurTime):mCurTime));
    }

    private String formatTime(long time) {
        SimpleDateFormat sdfDate = new SimpleDateFormat(mTimeFormat);
        return sdfDate.format(new Date(time));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mEnabled) {
            this.getParent().requestDisallowInterceptTouchEvent(true);

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mThisTouchUpdateTimes=0;
                    mTimeBeforeTouch =mCurTime;
//					updateOnTouch(event);
                    if (touchHitsArc(event) && (mAllowMoveBackward || mAllowMoveForward)) {
                        mIsTouchProgress=true;
                        if (mOnTimerViewChangeListener != null)
                            mOnTimerViewChangeListener.onStartTrackingTouch(this);
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (mIsTouchProgress)
                        updateOnTouch(event);
                    break;
                case MotionEvent.ACTION_UP:
                    if (!mIsTouchProgress) {
                        if (touchHitsInner(event) && mCurTime<mFullTime) {
                            if (mIsPlaying) stop(); else play();
                        }
                    }
                case MotionEvent.ACTION_CANCEL:
                    if (mIsTouchProgress) {
                        mIsTouchProgress=false;
                        if (mOnTimerViewChangeListener != null)
                            mOnTimerViewChangeListener.onStopTrackingTouch(this);
                    }
                    setPressed(false);
                    this.getParent().requestDisallowInterceptTouchEvent(false);
                    break;
            }
            return true;
        }
        return false;
    }

    public void play() {
        if (!mIsPlaying && mOnTimerViewChangeListener!=null)
            mOnTimerViewChangeListener.onPlayStarted(this);
        mPreviousSystemTime=System.currentTimeMillis();
        mIsPlaying=true;
    }

    public void stop() {
        if (mIsPlaying && mOnTimerViewChangeListener!=null && mCurTime<mFullTime)
            mOnTimerViewChangeListener.onPlayStopped(this);
        mIsPlaying=false;
        invalidate();
    }

    private boolean touchHitsArc(MotionEvent event) {

        float x=event.getX()-mTranslateX;
        float y=event.getY()-mTranslateY;

        int outerRadius= mFullRadius;
        int innerRadius=outerRadius-mArcWidth;

        if (mArcWidth*1.0f / mFullRadius <0.25f) {
            innerRadius= (int) (outerRadius-mArcWidth*1.5f); //so that it would be easier to hit
        }


        return ((x*x+y*y<=outerRadius*outerRadius) && (x*x+y*y>=innerRadius*innerRadius));
    }

    private boolean touchHitsInner(MotionEvent event) {

        float x=event.getX()-mTranslateX;
        float y=event.getY()-mTranslateY;

        int innerRadius=mArcRadius;

        return ((x*x+y*y<=innerRadius*innerRadius));
    }


    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        invalidate();
    }


    private void updateOnTouch(MotionEvent event) {
        setPressed(true);
        mTouchAngle = convertTouchEventPointToAngle(event.getX(), event.getY());
        int progress = convertAngleToProgress(mTouchAngle);
        updateProgress(progress, true);
    }

    private double convertTouchEventPointToAngle(float xPos, float yPos) {
        // transform touch coordinate into component coordinate
        float x = xPos - mTranslateX;
        float y = yPos - mTranslateY;

        double angle = Math.toDegrees(Math.atan2(y, x) + (Math.PI / 2));
        angle = (angle < 0) ? (angle + 360) : angle;
//		System.out.printf("(%f, %f) %f\n", x, y, angle);
        return angle;
    }

    private int convertAngleToProgress(double angle) {
        return (int) Math.round(valuePerDegree() * angle);
    }

    private float valuePerDegree() {
        return (float) (mFullTime) / 360.0f;
    }


    private void updateProgress(int progress, boolean fromUser) {

        mThisTouchUpdateTimes++;
        if (progress == INVALID_VALUE) {
            return;
        }

        if (fromUser && ((progress> mTimeBeforeTouch && !mAllowMoveForward) ||
                (progress< mTimeBeforeTouch && !mAllowMoveBackward)))
            return;

        // record previous and current progress change
        if (mThisTouchUpdateTimes == 1) {
            mCurTime = progress;
            mPreviousTime = progress;
        } else {
            mPreviousTime = mCurTime;
            mCurTime = progress;
        }

        if (fromUser) {
            int quater=mFullTime/4;

            if (mCurTime<=quater && mPreviousTime >=mFullTime-quater) {
                progress=mFullTime;
                mCurTime=mFullTime;
            }
            else if (mCurTime>=mFullTime-quater && mPreviousTime <=quater) {
                progress=0;
                mCurTime=0;
            }
        }

        formatCurTime();

        if (mOnTimerViewChangeListener != null && fromUser) {
            mOnTimerViewChangeListener
                    .onTimeChangedByUser(this, progress);
        }

        mRealProgressSweep = (float) progress / valuePerDegree();

        if (fromUser) {
            if (mSweepAnimation==null) {
                mSweepAnimation = getAnimation(mVisibleProgressSweep, mRealProgressSweep, true);
                mSweepAnimation.start();
            } else {
                mSweepAnimation.setFloatValues(mVisibleProgressSweep, mRealProgressSweep);
            }
        } else {
            if (mSweepAnimation==null)  mVisibleProgressSweep=mRealProgressSweep;
            else {
                mSweepAnimation.setFloatValues(mVisibleProgressSweep, mRealProgressSweep);
            }
        }





        invalidate();

    }

    private ValueAnimator getAnimation(float from, float to, boolean smooth) {

        ValueAnimator sweepAnimation = ValueAnimator.ofFloat(mVisibleProgressSweep, mRealProgressSweep);
        sweepAnimation.setDuration(600);
        if (!smooth) sweepAnimation.setInterpolator(new LinearInterpolator());
        sweepAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                mVisibleProgressSweep = (float)valueAnimator.getAnimatedValue();
                invalidate();
            }
        });
        sweepAnimation.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                mSweepAnimation = null;
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });

        return sweepAnimation;

    }

    public interface OnTimerViewChangeListener {

        /**
         * Notification that the time was manually changed by user
         *
         * @param timerView The corresponding TimerView
         * @param time     The current time value.
         */
        void onTimeChangedByUser(TimerView timerView, int time);

        /**
         * Notification user started to manually change the value of the time
         *
         * @param timerView The corresponding TimerView
         */
        void onStartTrackingTouch(TimerView timerView);

        /**
         * Notification user finished to manually change the value of the time
         *
         * @param timerView The corresponding TimerView
         */
        void onStopTrackingTouch(TimerView timerView);

        /**
         * Notification that timer has started to play
         *
         * @param timerView The corresponding TimerView
         */
        void onPlayStarted(TimerView timerView);

        /**
         * Notification that timer has stopped
         *
         * @param timerView The corresponding TimerView
         */
        void onPlayStopped(TimerView timerView);

        /**
         * Notification that timer has finished
         *
         * @param timerView The corresponding TimerView
         */
        void onPlayFinished(TimerView timerView);
    }

    private static Path getEquilateralTriangle(int side) {

        int r = (int) (side*Math.sqrt(3)/6.0);

        Point p1 = new Point(0 - r, 0 - side/2);
        Point p2 = null, p3 = null;
        p2 = new Point(p1.x, p1.y + side);
        p3 = new Point(p1.x + r*3, 0);

        Path path = new Path();
        path.moveTo(p1.x, p1.y);
        path.lineTo(p2.x, p2.y);
        path.lineTo(p3.x, p3.y);

        return path;
    }

    private static Path getPauseShape(int height) {

        int width= (int) (height*PAUSE_SHAPE_WIDTH_TO_HEIGHT_FACTOR);

        Path path = new Path();
        path.moveTo(-width, -height);
        path.lineTo(-width, height);
        path.moveTo(width, -height);
        path.lineTo(width, height);

        return path;
    }

    private static Path getFinishShape(int side, float width) {

        Path path = new Path();

        width/=2.0f;
        float widthPart= (float) Math.sqrt(width*width/2);
        float len1=side*1.5f;
        float len2=side*0.6f;
        float len1Part=(float) Math.sqrt(len1*len1/2);
        float len2Part=(float) Math.sqrt(len2*len2/2);

        path.moveTo(-0.289f*side, 0.484f*side);
        path.lineTo(-0.289f*side-len2Part, 0.484f*side-len2Part);
        path.moveTo(-0.289f*side-widthPart, 0.484f*side+widthPart);
        path.lineTo(-0.289f*side-widthPart+len1Part, 0.484f*side+widthPart-len1Part);

        return path;
    }

    /**
     * Change the current time progress value
     *
     * @param curtime the new value of time
     */

    public void setCurTime(int curtime) {
        curtime = curtime > mFullTime ? mFullTime : curtime;
        updateProgress(curtime, false);
    }

    /**
     * Get the current time progress value
     *
     */
    public int getCurTime() {
        return mCurTime;
    }

    /**
     * Get the progress width value
     *
     */
    public int getProgressWidth() {
        return mProgressWidth;
    }

    /**
     * Change the progress width value
     *
     * @param progressWidth the new progress width value. Keep in mind that this value must be equal or smaller
     *                    than the groove width value (unless it's 0). Otherwise the progress arc won't be able to fit the view
     */
    public void setProgressWidth(int progressWidth) {
        this.mProgressWidth = progressWidth;

        int min = Math.min(mTranslateX*2, mTranslateY*2);

        int width=mTranslateX*2;
        int height=mTranslateY*2;

        mArcWidth = (mGrooveWidth>0)?mGrooveWidth:mProgressWidth;

        int arcDiameter = min - getPaddingLeft() - mArcWidth;
        mArcRadius = arcDiameter / 2;
        float top = height / 2 - (arcDiameter / 2);
        float left = width / 2 - (arcDiameter / 2);
        mArcRect.set(left, top, left + arcDiameter, top + arcDiameter);

        mProgressPaint.setStrokeWidth(progressWidth);
        invalidate();
    }

    /**
     * Get the groove width value
     *
     */
    public int getGrooveWidth() {
        return mGrooveWidth;
    }

    /**
     * Set the groove width value
     *
     * @param grooveWidth the new groove width value. Keep in mind that this value must be either equal or bigger
     *                    than the progress width value or it must be 0. Otherwise the progress arc won't be able to fit the view
     */
    public void setGrooveWidth(int grooveWidth) {
        this.mGrooveWidth = grooveWidth;

        int min = Math.min(mTranslateX*2, mTranslateY*2);

        int width=mTranslateX*2;
        int height=mTranslateY*2;

        mArcWidth = (mGrooveWidth>0)?mGrooveWidth:mProgressWidth;

        int arcDiameter = min - getPaddingLeft() - mArcWidth;
        mArcRadius = arcDiameter / 2;
        float top = height / 2 - (arcDiameter / 2);
        float left = width / 2 - (arcDiameter / 2);
        mArcRect.set(left, top, left + arcDiameter, top + arcDiameter);

        mGroovePaint.setStrokeWidth(grooveWidth);
        invalidate();
    }

    /**
     * Get the view is touchable for the user
     *
     */
    public boolean isEnabled() {
        return mEnabled;
    }

    public void setEnabled(boolean enabled) {
        this.mEnabled = enabled;
    }

    /**
     * Get the progress color value
     *
     */
    public int getProgressColor() {
        return mProgressPaint.getColor();
    }

    /**
     * Set the progress color value
     *
     * @param color the new progress color value
     */
    public void setProgressColor(int color) {
        mProgressPaint.setColor(color);
        invalidate();
    }

    /**
     * Get the arc color value
     *
     */
    public int getArcColor() {
        return mGroovePaint.getColor();
    }

    /**
     * Set the arc color value
     *
     * @param color the new arc color value
     */
    public void setArcColor(int color) {
        mGroovePaint.setColor(color);
        invalidate();
    }


    /**
     * Get the big text color value
     *
     */
    public void setBigTextColor(int textColor) {
        mBigTextPaint.setColor(textColor);
        invalidate();
    }

    /**
     * Set the big text color value
     *
     * @param textSize the new big text color value
     */
    public void setBigTextSize(float textSize) {
        mBigTextSize = textSize;
        mBigTextPaint.setTextSize(mBigTextSize);
        mBigTextPaint.getTextBounds(formatTime(0), 0, mCurFormattedTime.length(), mBigTextRect);
        invalidate();
    }

    /**
     * Get the small text color value
     *
     */
    public void setSmallTextColor(int textColor) {
        mSmallTextPaint.setColor(textColor);
        invalidate();
    }

    /**
     * Set the small text color value
     *
     * @param textSize the new small text color value
     */
    public void setSmallTextSize(float textSize) {
        mSmallTextSize = textSize;
        mSmallTextPaint.setTextSize(mSmallTextSize);
        mSmallTextPaint.getTextBounds(formatTime(0), 0, mCurFormattedTime.length(), mSmallTextRect);
        invalidate();
    }

    /**
     * Get the time value representing the whole cycle of the timer (the max value)
     *
     */
    public int getFullTime() {
        return mFullTime;
    }

    /**
     * Set the time value representing the whole cycle of the timer (the max value)
     *
     * @param fullTime the new fulltime value
     */
    public void setFullTime(int fullTime) {
        if (fullTime <= 0)
            throw new IllegalArgumentException("FullTime should be greater than 0.");
        this.mFullTime = fullTime;
        updateProgress(mCurTime, false);
    }

    /**
     * Set the listener to the events of the timer
     *
     */
    public void setOnTimerViewChangeListener(OnTimerViewChangeListener onTimerViewChangeListener) {
        mOnTimerViewChangeListener = onTimerViewChangeListener;
    }

    /**
     * Get the length of the side of the play triangle (shown if no custom play button drawable is set)
     *
     */
    public int getPlayButtonTriangleSideLength() {
        return mPlayButtonTriangleSideLength;
    }


    /**
     * Get the size of the big text (shown while playing)
     *
     */
    public float getBigTextSize() {
        return mBigTextSize;
    }

    /**
     * Get the size of the small text (shown while not playing)
     *
     */
    public float getSmallTextSize() {
        return mSmallTextSize;
    }

    /**
     * Whether the timer is playing at the moment
     *
     */
    public boolean isIsPlaying() {
        return mIsPlaying;
    }

    /**
     * Whether the user is allowed to manually move the timer forward
     *
     */
    public boolean isAllowMoveForward() {
        return mAllowMoveForward;
    }

    /**
     * Whether the user is allowed to manually move the timer backward
     *
     */
    public boolean isAllowMoveBackward() {
        return mAllowMoveBackward;
    }

    /**
     * Set the length of the side of the play triangle (shown if no custom play button drawable is set)
     *
     * @param playButtonTriangleSideLength the new length of the side of the play triangle, in pixels
     */
    public void setPlayButtonTriangleSideLength(int playButtonTriangleSideLength) {
        this.mPlayButtonTriangleSideLength = playButtonTriangleSideLength;
        if (mPlayButtonTriangleSideLength <= 0)
            throw new IllegalArgumentException("playButtonTriangleSideLength should not be less than min.");
        mPlayTriangle=getEquilateralTriangle(mPlayButtonTriangleSideLength);
        invalidate();
    }


    /**
     * Specify whether the user is allowed to manually move the timer forward
     *
     * @param allowMoveForward whether the user is allowed to manually move the timer forward
     */
    public void setAllowMoveForward(boolean allowMoveForward) {
        this.mAllowMoveForward = allowMoveForward;
    }

    /**
     * Specify whether the user is allowed to manually move the timer backward
     *
     * @param allowMoveBackward whether the user is allowed to manually move the timer backward
     */
    public void setAllowMoveBackward(boolean allowMoveBackward) {
        this.mAllowMoveBackward = allowMoveBackward;
    }

    /**
     * Set the play button icon
     *
     * @param playButtonIcon drawable for the play button icon
     */
    public void setPlayButtonIcon(@NonNull Drawable playButtonIcon) {

        mPlayIcon = playButtonIcon;

        int playIconHalfWidth = mPlayIcon.getIntrinsicWidth() / 2;
        int playIconHalfHeight = mPlayIcon.getIntrinsicHeight() / 2;

        mPlayIcon.setBounds(-playIconHalfWidth, -playIconHalfHeight, playIconHalfWidth,
                playIconHalfHeight);

        if (mPlayButtonTint!=null) {
            mPlayIcon.setColorFilter(new PorterDuffColorFilter(mPlayButtonTint, PorterDuff.Mode.SRC_ATOP));
        }

        invalidate();
    }

    /**
     * Set the pause button icon
     *
     * @param pauseButtonIcon drawable for the pause button icon
     */
    public void setPauseButtonIcon(@NonNull Drawable pauseButtonIcon) {

        mPauseIcon = pauseButtonIcon;

        int iconHalfWidth = mPauseIcon.getIntrinsicWidth() / 2;
        int iconHalfHeight = mPauseIcon.getIntrinsicHeight() / 2;

        mPauseIcon.setBounds(-iconHalfWidth, -iconHalfHeight, iconHalfWidth,
                iconHalfHeight);

        if (mPauseButtonTint!=null) {
            mPauseIcon.setColorFilter(new PorterDuffColorFilter(mPauseButtonTint, PorterDuff.Mode.SRC_ATOP));
        }

        invalidate();
    }

    /**
     * Set the finish icon
     *
     * @param icon drawable for the finish icon
     */
    public void setFinishIcon(@NonNull Drawable icon) {

        mFinishIcon = icon;

        int iconHalfWidth = mFinishIcon.getIntrinsicWidth() / 2;
        int iconHalfHeight = mFinishIcon.getIntrinsicHeight() / 2;

        mFinishIcon.setBounds(-iconHalfWidth, -iconHalfHeight, iconHalfWidth,
                iconHalfHeight);

        if (mFinishIconTint!=null) {
            mFinishIcon.setColorFilter(new PorterDuffColorFilter(mFinishIconTint, PorterDuff.Mode.SRC_ATOP));
        }

        invalidate();
    }

    /**
     * Set the finish icon tint
     *
     * @param color the new color tint for the finish icon
     */
    public void setFinishIconTint(int color) {

        mFinishIconTint = color;
        if (mFinishIcon!=null) {
            mFinishIcon.setColorFilter(new PorterDuffColorFilter(mFinishIconTint, PorterDuff.Mode.SRC_ATOP));
        }

        mFinishShapePaint.setColor(mFinishIconTint);

        invalidate();
    }

    /**
     * Set the play icon tint
     *
     * @param color the new color tint for the play icon
     */
    public void setPlayButtonIconTint(int color) {

        mPlayButtonTint = color;
        if (mPlayIcon!=null) {
            mPlayIcon.setColorFilter(new PorterDuffColorFilter(mPlayButtonTint, PorterDuff.Mode.SRC_ATOP));
        }

        mPlayTrianglePaint.setColor(mPlayButtonTint);

        invalidate();
    }

    /**
     * Set the pause icon tint
     *
     * @param color the new color tint for the pause icon
     */
    public void setPauseButtonIconTint(int color) {

        mPauseButtonTint = color;
        if (mPauseIcon!=null) {
            mPauseIcon.setColorFilter(new PorterDuffColorFilter(mPauseButtonTint, PorterDuff.Mode.SRC_ATOP));
        }

        mPauseShapePaint.setColor(mPauseButtonTint);

        invalidate();
    }

    /**
     * Set the format for the time to draw on the screen
     *
     * @param timeFormat the string containing the time format (e.g. 'mm:ss')
     */
    public void setTimeFormat(String timeFormat) {
        this.mTimeFormat = timeFormat;
        formatCurTime();
        mBigTextPaint.getTextBounds(formatTime(0), 0, mCurFormattedTime.length(), mBigTextRect);
        mSmallTextPaint.getTextBounds(formatTime(0), 0, mCurFormattedTime.length(), mSmallTextRect);
        invalidate();
    }

    /**
     * Set the current time format
     *
     */
    public String getTimeFormat() {
        return mTimeFormat;
    }

    /**
     * Set the background drawable for the circle
     *
     * @param drawable the background drawable (should probably be shaped like a circle)
     */

    public void setCircleBackgroundDrawable(@NonNull Drawable drawable) {

        final int min = Math.min(mTranslateX*2, mTranslateY*2);

        mBackgroundDrawable = drawable;

        int backHalfWidth = (min - getPaddingLeft()) / 2;
        int backHalfHeight =  (min - getPaddingLeft()) / 2;

        mBackgroundDrawable.setBounds(-backHalfWidth, -backHalfHeight, backHalfWidth,
                backHalfHeight);

        invalidate();
    }

    /**
     * Set the background color for the background circle
     *
     * @param color the background color for the background circle
     */

    public void setCircleBackgroundColor(int color) {

        mBackPaint.setColor(color);
        invalidate();
    }


    /**
     * Set the play button icon
     *
     * @param resId drawable resource for the play button icon
     */
    public void setPlayButtonIcon(@DrawableRes int resId) {

        mPlayIcon = new BitmapDrawable(getResources(), BitmapFactory.decodeResource(getResources(), resId));

        int playIconHalfWidth = mPlayIcon.getIntrinsicWidth() / 2;
        int playIconHalfHeight = mPlayIcon.getIntrinsicHeight() / 2;

        mPlayIcon.setBounds(-playIconHalfWidth, -playIconHalfHeight, playIconHalfWidth,
                playIconHalfHeight);

        if (mPlayButtonTint!=null) {
            mPlayIcon.setColorFilter(new PorterDuffColorFilter(mPlayButtonTint, PorterDuff.Mode.SRC_ATOP));
        }

        invalidate();
    }

    /**
     * Set the pause button icon
     *
     * @param resId drawable resource for the pause button icon
     */
    public void setPauseButtonIcon(@DrawableRes int resId) {

        mPauseIcon = new BitmapDrawable(getResources(), BitmapFactory.decodeResource(getResources(), resId));

        int iconHalfWidth = mPauseIcon.getIntrinsicWidth() / 2;
        int iconHalfHeight = mPauseIcon.getIntrinsicHeight() / 2;

        mPauseIcon.setBounds(-iconHalfWidth, -iconHalfHeight, iconHalfWidth,
                iconHalfHeight);

        if (mPauseButtonTint!=null) {
            mPauseIcon.setColorFilter(new PorterDuffColorFilter(mPauseButtonTint, PorterDuff.Mode.SRC_ATOP));
        }

        invalidate();
    }

    /**
     * Set the finish icon
     *
     * @param resId drawable resource for the finish icon
     */
    public void setFinishIcon(@DrawableRes int resId) {

        mFinishIcon = new BitmapDrawable(getResources(), BitmapFactory.decodeResource(getResources(), resId));

        int iconHalfWidth = mFinishIcon.getIntrinsicWidth() / 2;
        int iconHalfHeight = mFinishIcon.getIntrinsicHeight() / 2;

        mFinishIcon.setBounds(-iconHalfWidth, -iconHalfHeight, iconHalfWidth,
                iconHalfHeight);

        if (mFinishIconTint!=null) {
            mFinishIcon.setColorFilter(new PorterDuffColorFilter(mFinishIconTint, PorterDuff.Mode.SRC_ATOP));
        }

        invalidate();
    }


    /**
     * Set the background drawable
     *
     * @param resId the background drawable resource (should probably be shaped like a circle)
     */

    public void setCustomBackgroundDrawable(@DrawableRes int resId) {

        final int min = Math.min(mTranslateX*2, mTranslateY*2);

        mBackgroundDrawable = new BitmapDrawable(getResources(), BitmapFactory.decodeResource(getResources(), resId));

        int backHalfWidth = (min - getPaddingLeft()) / 2;
        int backHalfHeight =  (min - getPaddingLeft()) / 2;

        mBackgroundDrawable.setBounds(-backHalfWidth, -backHalfHeight, backHalfWidth,
                backHalfHeight);

        invalidate();
    }

    /**
     * Whether the timer is in the countdown mode
     *
     */
    public boolean isCountdown() {
        return mCountdown;
    }


    /**
     * Specify whether the timer should be in the countdown mode
     *
     * @param countdown whether the timer is in the countdown mode
     */
    public void setCountdown(boolean countdown) {
        this.mCountdown = countdown;

        formatCurTime();
        mBigTextPaint.getTextBounds(formatTime(0), 0, mCurFormattedTime.length(), mBigTextRect);
        mSmallTextPaint.getTextBounds(formatTime(0), 0, mCurFormattedTime.length(), mSmallTextRect);
        invalidate();
    }
}
