package com.anarchy.swipebacklayout;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.anarchy.swipeback.SwipeBack;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    public void openSecondActivity(View view){
        startActivity(new Intent(this,SecondActivity.class));
    }
}
