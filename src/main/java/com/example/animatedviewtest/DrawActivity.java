package com.example.animatedviewtest;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.animatedviewtest.getpath.DrawingWithBezier;


public class DrawActivity extends Activity {

    private DrawingWithBezier mDrawingWithBezier;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw);
        mDrawingWithBezier = (DrawingWithBezier) findViewById(R.id.mDrawingWithBezier);
        findViewById(R.id.but_toshow).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(DrawActivity.this, ShowAnimateActivity.class);
                startActivity(i);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        mDrawingWithBezier.reset();
        ShowAnimateActivity.mPathStringList.clear();
    }
}
