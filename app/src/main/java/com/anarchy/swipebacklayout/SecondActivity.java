package com.anarchy.swipebacklayout;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.anarchy.swipeback.SwipeBack;

/**
 * Version 2.1.1
 * <p>
 * Date: 16/10/13 15:13
 * Author: zhendong.wu@shoufuyou.com
 * <p/>
 * Copyright © 2016 Shanghai Xiaotu Network Technology Co., Ltd.
 */

public class SecondActivity  extends AppCompatActivity{
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        SwipeBack swipeBack = SwipeBack.attachActivity(this);
        swipeBack.setDragMode(SwipeBack.DRAG_FULL);
        swipeBack.setDirection(SwipeBack.DIRECTION_LEFT|SwipeBack.DIRECTION_TOP|SwipeBack.DIRECTION_BOTTOM|SwipeBack.DIRECTION_RIGHT);
    }
}