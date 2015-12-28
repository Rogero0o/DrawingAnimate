package com.example.animatedviewtest;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;

import com.example.animatedviewtest.util.AnimatedMuzeiLogoFragment;

import java.util.ArrayList;


public class ShowAnimateActivity extends Activity {
    private Handler mHandler = new Handler();
    public static ArrayList<String> mPathStringList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final AnimatedMuzeiLogoFragment logoFragment = (AnimatedMuzeiLogoFragment)
                getFragmentManager().findFragmentById(R.id.animated_logo_fragment);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                logoFragment.start();
            }
        }, 1000);

    }
}
