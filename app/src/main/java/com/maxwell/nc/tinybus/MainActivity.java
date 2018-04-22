package com.maxwell.nc.tinybus;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.maxwell.nc.library.TinyBus;

/**
 * 主界面
 */
public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onClick(View view) {
        TinyBus.postSticky("toNext");
        startActivity(new Intent(this, NextActivity.class));
    }

}
