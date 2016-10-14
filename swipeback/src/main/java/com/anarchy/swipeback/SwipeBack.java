package com.anarchy.swipeback;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.FrameLayout;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Version 2.1.1
 * <p>
 * Date: 16/10/12 11:08
 * Author: zhendong.wu@shoufuyou.com
 * <p/>
 * Copyright © 2016 Shanghai Xiaotu Network Technology Co., Ltd.
 */

public class SwipeBack extends FrameLayout {
    /*拖动模式*/
    /**
     * 不进行拖动操作
     */
    public static final int DRAG_NONE = 0;
    public static final int EDGE_LEFT = ViewDragHelper.EDGE_LEFT;
    public static final int EDGE_RIGHT = ViewDragHelper.EDGE_RIGHT;
    public static final int EDGE_TOP = ViewDragHelper.EDGE_TOP;
    public static final int EDGE_BOTTOM = ViewDragHelper.EDGE_BOTTOM;
    public static final int EDGE_ALL = ViewDragHelper.EDGE_ALL;
    /**
     * 对整个布局控制
     */
    public static final int DRAG_FULL = 1 << 4;


    public static final int DIRECTION_LEFT = 1;
    public static final int DIRECTION_RIGHT = 1 << 1;
    public static final int DIRECTION_TOP = 1 << 2;
    public static final int DIRECTION_BOTTOM = 1 << 3;


    @IntDef(value = {DRAG_NONE, DRAG_FULL, EDGE_LEFT, EDGE_RIGHT, EDGE_TOP, EDGE_BOTTOM, EDGE_ALL}, flag = true)
    @Retention(RetentionPolicy.SOURCE)
    @interface DragMode {

    }

    @IntDef(value = {DIRECTION_LEFT, DIRECTION_RIGHT, DIRECTION_TOP, DIRECTION_BOTTOM}, flag = true)
    @Retention(RetentionPolicy.SOURCE)
    @interface Direction {

    }

    private static final String TAG = SwipeBack.class.getSimpleName();
    private static final boolean DEBUG = true;
    private static final int START_SHADOW_COLOR = 0x3D000000;
    private static final int END_SHADOW_COLOR = 0x10000000;
    private static final int DEFAULT_BACKGROUND_COLOR = 0xFFF1F2F3;
    private static final float DEFAULT_THRESHOLD = 0.4f;
    private static final int INVALID_POINTER = -1;


    private final Activity mActivity;
    private final ViewDragHelper mViewDragHelper;
    private int mDragMode = EDGE_LEFT;
    private int mDirection = DIRECTION_LEFT;
    private float mThreshold = DEFAULT_THRESHOLD;
    private float mRatio = 0f;
    private int mEndShadowColor = END_SHADOW_COLOR;
    private int mStartShadowColor = START_SHADOW_COLOR;


    private int mRecordPointerId;
    private float mInitialX;
    private float mInitialY;
    private float mCheckedX;
    private float mCheckedY;

    private SwipeBack(Activity activity, float sensitivity) {
        super(activity);
        mActivity = activity;
        mViewDragHelper = ViewDragHelper.create(this, sensitivity, new ViewDragCallBack());
        mViewDragHelper.setEdgeTrackingEnabled(ViewDragHelper.EDGE_LEFT);
    }


    /**
     * @see #attachActivity(Activity, float)
     */
    public static SwipeBack attachActivity(@NonNull Activity activity) {
        return attachActivity(activity, 1f);
    }


    public static SwipeBack attachActivity(@NonNull Activity activity, float sensitivity) {
        return attachActivity(activity, sensitivity, DEFAULT_BACKGROUND_COLOR, false);
    }

    /**
     * @param activity        request attach activity
     * @param sensitivity     Multiplier for how sensitive the helper should be about detecting
     *                        the start of a drag. Larger values are more sensitive. 1.0f is normal.
     * @param backgroundColor set background color to root view
     * @param force           either force set background color to root view if force is false(default) and the
     *                        root view is transparent then the background color will set to root view,if root view
     *                        have any color or other background  the background color could not be set
     * @return swipeBack instance
     */
    public static SwipeBack attachActivity(@NonNull Activity activity, float sensitivity, int backgroundColor, boolean force) {
        SwipeBack swipeBack = new SwipeBack(activity, sensitivity);
        ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
        if (decorView.getChildCount() > 1) {
            Log.e(TAG, "decor child is greater than one");
        }
        View child = decorView.getChildAt(0);
        if (force) {
            child.setBackgroundColor(backgroundColor);
        } else if (child.getBackground() == null
                && child.getBackground() instanceof ColorDrawable
                && ((ColorDrawable) child.getBackground()).getColor() == 0) {
            child.setBackgroundColor(backgroundColor);
        } else {
            child.setBackgroundDrawable(activity.getWindow().getDecorView().getBackground());
        }
        activity.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        decorView.removeAllViews();
        swipeBack.addView(child);
        decorView.addView(swipeBack);
        return swipeBack;
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (mDragMode == DRAG_NONE) return false;
        switch (MotionEventCompat.getActionMasked(ev)) {
            case MotionEvent.ACTION_DOWN:
                int index = ev.getActionIndex();
                mRecordPointerId = ev.getPointerId(index);
                mInitialX = ev.getX(index);
                mInitialY = ev.getY(index);
                break;
            case MotionEvent.ACTION_MOVE:
                if (mRecordPointerId == INVALID_POINTER) break;
                int indexMove = ev.findPointerIndex(mRecordPointerId);
                mCheckedX = ev.getX(indexMove);
                mCheckedY = ev.getY(indexMove);
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                mRecordPointerId = INVALID_POINTER;
                break;


        }
        return mViewDragHelper.shouldInterceptTouchEvent(ev);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mDragMode == DRAG_NONE) return false;
        mViewDragHelper.processTouchEvent(event);
        return true;
    }


    @Override
    public void computeScroll() {
        if (mViewDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }


    private void drawShadow() {
        int startAlpha = Color.alpha(mEndShadowColor);
        int startR = Color.red(mEndShadowColor);
        int startG = Color.green(mEndShadowColor);
        int startB = Color.blue(mEndShadowColor);
        int endAlpha = Color.alpha(mStartShadowColor);
        int endR = Color.red(mStartShadowColor);
        int endG = Color.green(mStartShadowColor);
        int endB = Color.blue(mStartShadowColor);
        int alpha = endAlpha + Math.round((startAlpha - endAlpha) * mRatio);
        int red = endR + Math.round((startR - endR) * mRatio);
        int green = endG + Math.round((startG - endG) * mRatio);
        int blue = endB + Math.round((startB - endB) * mRatio);
        setBackgroundColor(Color.argb(alpha, red, green, blue));
    }

    /**
     * @param dragMode {@link #DRAG_NONE},{@link #EDGE_LEFT},{@link #EDGE_RIGHT},
     *                 {@link #EDGE_BOTTOM},{@link #EDGE_TOP},{@link #EDGE_ALL},{@link #DRAG_FULL}
     */
    public void setDragMode(@DragMode int dragMode) {
        mDragMode = dragMode;
        switch (dragMode) {
            case DRAG_NONE:
            case DRAG_FULL:
                mViewDragHelper.setEdgeTrackingEnabled(DRAG_NONE);
                break;
            default:
                mViewDragHelper.setEdgeTrackingEnabled(dragMode);

        }
    }

    /**
     * set allow direction
     *
     * @param direction left,right,top,bottom
     */
    public void setDirection(@Direction int direction) {
        mDirection = direction;
        if ((mDirection & DIRECTION_BOTTOM) == DIRECTION_BOTTOM && mActivity != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                    && (mActivity.getWindow().getAttributes().flags & WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS) == 0) {
                int id = mActivity.getResources().getIdentifier("status_bar_height","dimen","android");
                if(id > 0){
                    final int statusBarHeight = mActivity.getResources().getDimensionPixelSize(id);
                    getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                        @SuppressLint("NewApi")
                        @Override
                        public boolean onPreDraw() {
                            getViewTreeObserver().removeOnPreDrawListener(this);
                            View child = getChildAt(0);
                            Rect rect = new Rect(0,statusBarHeight,child.getWidth(),child.getHeight());
                            child.setClipBounds(rect);
                            return false;
                        }
                    });
                    View child = getChildAt(0);
                    Rect clipBounds = new Rect(0,statusBarHeight,child.getWidth(),child.getHeight());
                    child.setClipBounds(clipBounds);
                }

            }
        }
    }


    public void setEndShadowColor(int endShadowColor) {
        mEndShadowColor = endShadowColor;
    }

    public void setStartShadowColor(int startShadowColor) {
        mStartShadowColor = startShadowColor;
    }


    private boolean checkTouchSlop(int directions, int pointerId) {
        if (pointerId != mRecordPointerId) return false;
        final boolean checkHorizontal = (directions & ViewDragHelper.DIRECTION_HORIZONTAL) == ViewDragHelper.DIRECTION_HORIZONTAL;
        final boolean checkVertical = (directions & ViewDragHelper.DIRECTION_VERTICAL) == ViewDragHelper.DIRECTION_VERTICAL;
        final float dx = mCheckedX - mInitialX;
        final float dy = mCheckedY - mInitialY;
        if (checkHorizontal && checkVertical) {
            return dx * dx + dy * dy > mViewDragHelper.getTouchSlop() * mViewDragHelper.getTouchSlop();
        } else if (checkHorizontal) {
            return Math.abs(dx) > mViewDragHelper.getTouchSlop();
        } else if (checkVertical) {
            return Math.abs(dy) > mViewDragHelper.getTouchSlop();
        }
        return false;
    }

    class ViewDragCallBack extends ViewDragHelper.Callback {

        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            boolean edge = mViewDragHelper.isEdgeTouched(ViewDragHelper.EDGE_LEFT, pointerId)
                    || mViewDragHelper.isEdgeTouched(ViewDragHelper.EDGE_RIGHT, pointerId)
                    || mViewDragHelper.isEdgeTouched(ViewDragHelper.EDGE_TOP, pointerId)
                    || mViewDragHelper.isEdgeTouched(ViewDragHelper.EDGE_BOTTOM, pointerId)
                    || mViewDragHelper.isEdgeTouched(ViewDragHelper.EDGE_ALL, pointerId);
            boolean directionHorizontal = (mDirection & (DIRECTION_LEFT | DIRECTION_RIGHT)) != 0
                    && checkTouchSlop(ViewDragHelper.DIRECTION_HORIZONTAL, pointerId);
            boolean directionVertical = (mDirection & (DIRECTION_TOP | DIRECTION_BOTTOM)) != 0
                    && checkTouchSlop(ViewDragHelper.DIRECTION_VERTICAL, pointerId);
            boolean direction = mDragMode == DRAG_FULL && (directionHorizontal || directionVertical);
            log("try capture view (edge:" + edge + ",directionHorizontal:" + directionHorizontal
                    + ",directionVertical:" + directionVertical + ",direction:" + direction);
            return edge || direction;
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            int masked = mDirection & (DIRECTION_LEFT | DIRECTION_RIGHT);
            if (masked != 0) {
                if (masked == (DIRECTION_LEFT | DIRECTION_RIGHT)) return left;
                if (masked == DIRECTION_LEFT) return Math.min(Math.max(0, left), getWidth());
                return Math.max(Math.min(0, left), -getWidth());
            }
            return super.clampViewPositionHorizontal(child, left, dx);
        }

        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            int masked = mDirection & (DIRECTION_TOP | DIRECTION_BOTTOM);
            if (masked != 0) {
                if (masked == (DIRECTION_TOP | DIRECTION_BOTTOM)) return top;
                if (masked == DIRECTION_TOP) return Math.min(Math.max(0, top), getHeight());
                return Math.max(Math.min(0, top), -getHeight());
            }
            return super.clampViewPositionVertical(child, top, dy);
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            int finalLeft = getFinalLeft(releasedChild, xvel);
            int finalTop = getFinalTop(releasedChild, yvel);
            log("final point  (finalLeft:" + finalLeft + ",finalTop:" + finalTop + ")");
            mViewDragHelper.settleCapturedViewAt(finalLeft, finalTop);
            ViewCompat.postInvalidateOnAnimation(SwipeBack.this);
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            float ratioHorizontal = 0f;
            float ratioVertical = 0f;
            if (left >= 0 && left <= getWidth()) {
                ratioHorizontal = left / (float) getWidth();
            } else if (left <= 0 && left + changedView.getWidth() >= 0) {
                ratioHorizontal = -left / (float) getWidth();
            }
            if (top >= 0 && top <= getHeight()) {
                ratioVertical = top / (float) getHeight();
            } else if (top <= 0 && top + getHeight() >= 0) {
                ratioVertical = -top / (float) getHeight();
            }
            mRatio = Math.max(ratioHorizontal, ratioVertical);
            log("ratio:" + mRatio);
            if (mRatio == 0) {
                setBackgroundColor(0);
            }
            if (mRatio > 0) {
                drawShadow();
            }
            if (mRatio >= 1f && mActivity != null) {
                mActivity.getWindow().setWindowAnimations(0);
                mActivity.finish();
                mActivity.overridePendingTransition(0, 0);
            }

        }

        @Override
        public void onViewDragStateChanged(int state) {

        }

        @Override
        public void onViewCaptured(View capturedChild, int activePointerId) {

        }
    }

    private int getFinalLeft(View releasedChild, float xvel) {
        boolean checkLeft = (mDirection & DIRECTION_LEFT) == DIRECTION_LEFT;
        boolean checkRight = (mDirection & DIRECTION_RIGHT) == DIRECTION_RIGHT;
        if (xvel > 0 && checkLeft) {
            return getWidth();
        }
        if (xvel < 0 && checkRight) {
            return -getWidth();
        }
        if (checkLeft && releasedChild.getLeft() > 0 && releasedChild.getLeft() < getWidth() * mThreshold) {
            return 0;
        }
        if (checkRight && releasedChild.getRight() < getWidth() && releasedChild.getRight() > getWidth() * (1 - mThreshold)) {
            return 0;
        }
        if (checkLeft && releasedChild.getLeft() > getWidth() * mThreshold && releasedChild.getLeft() < getWidth()) {
            return getWidth();
        }
        if (checkRight && releasedChild.getRight() < getWidth() * (1 - mThreshold) && releasedChild.getRight() > 0) {
            return -getWidth();
        }
        return 0;
    }

    private int getFinalTop(View releasedChild, float yvel) {
        boolean checkTop = (mDirection & DIRECTION_TOP) == DIRECTION_TOP;
        boolean checkBottom = (mDirection & DIRECTION_BOTTOM) == DIRECTION_BOTTOM;
        if (yvel > 0 && checkBottom) {
            return getHeight();
        }
        if (yvel < 0 && checkTop) {
            return -getHeight();
        }
        if (checkTop && releasedChild.getTop() > 0 && releasedChild.getTop() < getHeight() * mThreshold) {
            return 0;
        }
        if (checkBottom && releasedChild.getBottom() < getHeight() && releasedChild.getBottom() > getHeight() * (1 - mThreshold)) {
            return 0;
        }
        if (checkTop && releasedChild.getTop() > getHeight() * mThreshold && releasedChild.getTop() < getHeight()) {
            return getHeight();
        }
        if (checkBottom && releasedChild.getBottom() < getHeight() * (1 - mThreshold) && releasedChild.getBottom() > 0) {
            return -getHeight();
        }
        return 0;
    }


    private void log(String message) {
        if (DEBUG)
            Log.w(TAG, message);
    }

}