package ru.dedoxyribose.timerviewapplication;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;

import java.text.SimpleDateFormat;
import java.util.Date;

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
    private String mCurFormattedTime;

    /**
     * The Maximum value that this SeekArc can be set to
     */
    private int mFullTime = 60000;


    private long mPreviousSystemTime;

    /**
     * The Drawable for the play button
     */
    private Drawable mPlayIcon;
    private int mPlayButtonTriangleSideLength=-1;
    private Path mPlayTriangle;

    /**
     * The Drawable for the pause button
     */
    private Drawable mPauseIcon;
    private Path mPauseShape;

    private Drawable mFinishIcon;
    private Path mFinishShape;

    private Drawable mBackgroundDrawable;

    private int mProgressWidth = 12;
    private int mArcWidth = 12;
    private boolean mEnabled = true;

    //
    // internal variables
    //
    /**
     * The counts of point update to determine whether to change previous progress.
     */
    private int mThisTouchUpdateTimes = 0;
    private float mPreviousProgress = -1;
    private float mCurrentProgress = 0;
    private float mProgressBeforeTouch = 0;

    private String mTimeFormat = "mm:ss";

    private int mArcRadius = 0;
    private RectF mArcRect = new RectF();
    private Paint mArcPaint;

    private float mRealProgressSweep = 0;
    private float mVisibleProgressSweep = 0;
    private Paint mProgressPaint;

    private ValueAnimator mSweepAnimation;

    private float mBigTextSize = 40;
    private float mSmallTextSize = 16;
    private Paint mBigTextPaint;
    private Paint mSmallTextPaint;
    private Rect mBigTextRect = new Rect();
    private Rect mSmallTextRect = new Rect();

    private Paint mBackPaint;

    private Paint mPlayTrianglePaint;
    private Paint mPauseShapePaint;
    private Paint mFinishShapePaint;

    private int mTranslateX;
    private int mTranslateY;

    private boolean mIsTouchProgress=false;
    private boolean mIsPlaying=false;
    private boolean mIsAttached=false;

    private boolean mAllowMoveForward=true;
    private boolean mAllowMoveBackward=true;

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
        int arcColor = ContextCompat.getColor(context, R.color.colorArc);
        int progressColor = ContextCompat.getColor(context, R.color.colorProgress);

        int bigTextColor = ContextCompat.getColor(context, R.color.colorText);
        int smallTextColor = ContextCompat.getColor(context, R.color.colorText);

        int backColor = ContextCompat.getColor(context, R.color.colorBack);

        int playButtonTint = ContextCompat.getColor(context, R.color.colorProgress);
        int pauseButtonTint = ContextCompat.getColor(context, R.color.colorProgress);
        int finishIconTint = ContextCompat.getColor(context, R.color.colorProgress);

        mProgressWidth = (int) (mProgressWidth * density);
        mArcWidth = (int) (mArcWidth * density);
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
                    mPlayIcon.setColorFilter(new PorterDuffColorFilter(playButtonTint, PorterDuff.Mode.SRC_ATOP));
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
                    mPauseIcon.setColorFilter(new PorterDuffColorFilter(pauseButtonTint, PorterDuff.Mode.SRC_ATOP));
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
                    mFinishIcon.setColorFilter(new PorterDuffColorFilter(finishIconTint, PorterDuff.Mode.SRC_ATOP));
                }
            }

            mBackgroundDrawable = a.getDrawable(R.styleable.TimerView_backgroundDrawable);

            mCurTime = a.getInteger(R.styleable.TimerView_points, mCurTime);
            mFullTime = a.getInteger(R.styleable.TimerView_fulltime, mFullTime);
            mCurTime = a.getInteger(R.styleable.TimerView_fulltime, mCurTime);

            mProgressWidth = (int) a.getDimension(R.styleable.TimerView_progressWidth, mProgressWidth);
            progressColor = a.getColor(R.styleable.TimerView_progressColor, progressColor);

            mArcWidth = (int) a.getDimension(R.styleable.TimerView_arcWidth, mArcWidth);
            arcColor = a.getColor(R.styleable.TimerView_arcColor, arcColor);

            mPlayButtonTriangleSideLength = (int) a.getDimension(R.styleable.TimerView_playButtonTriangleSideLength,
                    mPlayButtonTriangleSideLength);

            mBigTextSize = (int) a.getDimension(R.styleable.TimerView_bigTextSize, mBigTextSize);
            mSmallTextSize = (int) a.getDimension(R.styleable.TimerView_smallTextSize, mSmallTextSize);
            bigTextColor = a.getColor(R.styleable.TimerView_bigTextColor, bigTextColor);
            smallTextColor = a.getColor(R.styleable.TimerView_smallTextColor, smallTextColor);

            backColor = a.getColor(R.styleable.TimerView_backgroundColor, backColor);

            mAllowMoveForward = a.getBoolean(R.styleable.TimerView_allowMoveForward, mAllowMoveForward);
            mAllowMoveBackward = a.getBoolean(R.styleable.TimerView_allowMoveBackward, mAllowMoveBackward);

            mEnabled = a.getBoolean(R.styleable.TimerView_enabled, mEnabled);
            a.recycle();
        }

        // range check
        mCurTime = (mCurTime > mFullTime) ? mFullTime : mCurTime;

        formatCurTime();

        mRealProgressSweep = (float) mCurTime / valuePerDegree();
        mVisibleProgressSweep = mRealProgressSweep;

        mArcPaint = new Paint();
        mArcPaint.setColor(arcColor);
        mArcPaint.setAntiAlias(true);
        mArcPaint.setStyle(Paint.Style.STROKE);
        mArcPaint.setStrokeWidth(mArcWidth);

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
        mPauseShapePaint.setStrokeWidth(mArcRadius/PAUSE_SHAPE_STROKE_WIDTH_FACTOR);

        mFinishShapePaint = new Paint();
        mFinishShapePaint.setColor(finishIconTint);
        mFinishShapePaint.setAntiAlias(true);
        mFinishShapePaint.setStyle(Paint.Style.STROKE);
        mFinishShapePaint.setStrokeWidth(mArcRadius/FINISH_SHAPE_STROKE_WIDTH_FACTOR);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        final int width = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        final int height = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        final int min = Math.min(width, height);

        mTranslateX = (int) (width * 0.5f);
        mTranslateY = (int) (height * 0.5f);

        int arcDiameter = min - getPaddingLeft() - mArcWidth;
        mArcRadius = arcDiameter / 2;
        float top = height / 2 - (arcDiameter / 2);
        float left = width / 2 - (arcDiameter / 2);
        mArcRect.set(left, top, left + arcDiameter, top + arcDiameter);

        mBigTextPaint.getTextBounds(formatTime(0), 0, mCurFormattedTime.length(), mBigTextRect);
        mSmallTextPaint.getTextBounds(formatTime(0), 0, mCurFormattedTime.length(), mSmallTextRect);

        mPlayTriangle = getEquilateralTriangle(mPlayButtonTriangleSideLength!=-1?
                mPlayButtonTriangleSideLength:(int) (mArcRadius/PLAY_TRIANGLE_SIZE_FACTOR));

        mPauseShape = getPauseShape((int) (mArcRadius/PAUSE_SHAPE_SIZE_FACTOR));
        mPauseShapePaint.setStrokeWidth(mArcRadius/PAUSE_SHAPE_STROKE_WIDTH_FACTOR);

        mFinishShape = getFinishShape((int) (mArcRadius/FINISH_SHAPE_SIZE_FACTOR));
        mFinishShapePaint.setStrokeWidth(mArcRadius/FINISH_SHAPE_STROKE_WIDTH_FACTOR);

        if (mBackgroundDrawable!=null) {
            int backHalfWidth = (min - getPaddingLeft()) / 2;
            int backHalfHeight =  (min - getPaddingLeft()) / 2;

            mBackgroundDrawable.setBounds(-backHalfWidth, -backHalfHeight, backHalfWidth,
                    backHalfHeight);
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        if (mBackgroundDrawable!=null) {
            canvas.translate(mTranslateX, mTranslateY);
            mBackgroundDrawable.draw(canvas);
            canvas.translate(-mTranslateX,-mTranslateY);
        }
        else canvas.drawArc(mArcRect, ANGLE_OFFSET, 360, false, mBackPaint);


        if (mIsPlaying) {

            int xPos = mTranslateX - mBigTextRect.width() / 2;
            int yPos = (int) ((mArcRect.centerY()) - ((mBigTextPaint.descent() + mBigTextPaint.ascent()) / 2));

            canvas.drawText(mCurFormattedTime, xPos, yPos, mBigTextPaint);
        } else if (mCurTime<mFullTime || mIsTouchProgress) {

            int xPos = mTranslateX - mSmallTextRect.width() / 2;
            int yPos = (int) (mTranslateY+mArcRadius*0.5f - ((mSmallTextPaint.descent() + mSmallTextPaint.ascent()) / 2));

            canvas.drawText(mCurFormattedTime, xPos, yPos, mSmallTextPaint);

        }

        // draw the arc and progress
        canvas.drawArc(mArcRect, ANGLE_OFFSET, 360, false, mArcPaint);
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
            int yPos = (int) (mTranslateY+mArcRadius*0.5f);

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

        updateProgress((int) (mCurrentProgress+timeDiff), false);
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
        mCurFormattedTime = sdfDate.format(new Date(mCurTime));
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
                    if (mOnTimerViewChangeListener != null)
                        mOnTimerViewChangeListener.onStartTrackingTouch(this);
                    mThisTouchUpdateTimes=0;
                    mProgressBeforeTouch=mCurrentProgress;
//					updateOnTouch(event);
                    if (touchHitsArc(event)) mIsTouchProgress=true;
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (mIsTouchProgress)
                        updateOnTouch(event);
                    break;
                case MotionEvent.ACTION_UP:
                    if (!mIsTouchProgress) {
                        if (touchHitsInner(event)) {
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

        int outerRadius=mArcRadius+mArcWidth;
        int innerRadius=outerRadius-mArcWidth;
        if (mArcWidth/mArcRadius<0.25) innerRadius= (int) (mArcRadius-mArcWidth*1.5f); //so that it would be easier to hit


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

        if (fromUser && ((progress>mProgressBeforeTouch && !mAllowMoveForward) ||
                (progress<mProgressBeforeTouch && !mAllowMoveBackward)))
            return;

        // record previous and current progress change
        if (mThisTouchUpdateTimes == 1) {
            mCurrentProgress = progress;
            mPreviousProgress = progress;
        } else {
            mPreviousProgress = mCurrentProgress;
            mCurrentProgress = progress;
        }

        if (fromUser) {
            int quater=mFullTime/4;

            if (mCurrentProgress<=quater && mPreviousProgress>=mFullTime-quater) {
                progress=mFullTime;
                mCurrentProgress=mFullTime;
            }
            else if (mCurrentProgress>=mFullTime-quater && mPreviousProgress<=quater) {
                progress=0;
                mCurrentProgress=0;
            }
        }

        mCurTime = progress;
        formatCurTime();

        if (mOnTimerViewChangeListener != null) {
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
         * Notification that the point value has changed.
         *
         * @param timerView The TimerView view whose value has changed
         * @param time     The current time value.
         */
        void onTimeChangedByUser(TimerView timerView, int time);

        void onStartTrackingTouch(TimerView timerView);

        void onStopTrackingTouch(TimerView timerView);

        void onPlayStarted(TimerView timerView);

        void onPlayStopped(TimerView timerView);

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

    private static Path getFinishShape(int side) {

        Path path = new Path();
        path.moveTo(-0.15625f*side, 0.64843f*side);
        path.lineTo(-0.843f*side, -0.039f*side);
        path.moveTo(-0.289f*side, 0.484f*side);
        path.lineTo(0.835f*side, -0.648f*side);

        return path;
    }

    public void setPoints(int points) {
        points = points > mFullTime ? mFullTime : points;
        updateProgress(points, false);
    }

    public int getPoints() {
        return mCurTime;
    }

    public int getProgressWidth() {
        return mProgressWidth;
    }

    public void setProgressWidth(int mProgressWidth) {
        this.mProgressWidth = mProgressWidth;
        mProgressPaint.setStrokeWidth(mProgressWidth);
    }

    public int getArcWidth() {
        return mArcWidth;
    }

    public void setArcWidth(int mArcWidth) {
        this.mArcWidth = mArcWidth;
        mArcPaint.setStrokeWidth(mArcWidth);
    }

    public boolean isEnabled() {
        return mEnabled;
    }

    public void setEnabled(boolean enabled) {
        this.mEnabled = enabled;
    }

    public int getProgressColor() {
        return mProgressPaint.getColor();
    }

    public void setProgressColor(int color) {
        mProgressPaint.setColor(color);
        invalidate();
    }

    public int getArcColor() {
        return mArcPaint.getColor();
    }

    public void setArcColor(int color) {
        mArcPaint.setColor(color);
        invalidate();
    }

    public void setBigTextColor(int textColor) {
        mBigTextPaint.setColor(textColor);
        invalidate();
    }

    public void setBigTextSize(float textSize) {
        mBigTextSize = textSize;
        mBigTextPaint.setTextSize(mBigTextSize);
        mBigTextPaint.getTextBounds(formatTime(0), 0, mCurFormattedTime.length(), mBigTextRect);
        invalidate();
    }

    public void setSmallTextColor(int textColor) {
        mSmallTextPaint.setColor(textColor);
        invalidate();
    }

    public void setSmallTextSize(float textSize) {
        mSmallTextSize = textSize;
        mSmallTextPaint.setTextSize(mSmallTextSize);
        mSmallTextPaint.getTextBounds(formatTime(0), 0, mCurFormattedTime.length(), mSmallTextRect);
        invalidate();
    }


    public int getFullTime() {
        return mFullTime;
    }

    public void setFullTime(int fullTime) {
        if (fullTime <= 0)
            throw new IllegalArgumentException("Max should not be less than min.");
        this.mFullTime = fullTime;
    }

    public void setOnTimerViewChangeListener(OnTimerViewChangeListener onTimerViewChangeListener) {
        mOnTimerViewChangeListener = onTimerViewChangeListener;
    }

    public int getPlayButtonTriangleSideLength() {
        return mPlayButtonTriangleSideLength;
    }

    public float getBigTextSize() {
        return mBigTextSize;
    }

    public float getSmallTextSize() {
        return mSmallTextSize;
    }

    public boolean isIsPlaying() {
        return mIsPlaying;
    }

    public boolean isAllowMoveForward() {
        return mAllowMoveForward;
    }

    public boolean isAllowMoveBackward() {
        return mAllowMoveBackward;
    }

    public void setPlayButtonTriangleSideLength(int playButtonTriangleSideLength) {
        this.mPlayButtonTriangleSideLength = playButtonTriangleSideLength;
        if (mPlayButtonTriangleSideLength <= 0)
            throw new IllegalArgumentException("playButtonTriangleSideLength should not be less than min.");
        mPlayTriangle=getEquilateralTriangle(mPlayButtonTriangleSideLength);
    }

    public void setAllowMoveForward(boolean allowMoveForward) {
        this.mAllowMoveForward = allowMoveForward;
    }

    public void setAllowMoveBackward(boolean allowMoveBackward) {
        this.mAllowMoveBackward = allowMoveBackward;
    }
}
