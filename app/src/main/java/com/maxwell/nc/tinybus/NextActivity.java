package com.maxwell.nc.tinybus;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

import com.maxwell.nc.library.TinyBus;
import com.maxwell.nc.library.annotation.TinyEvent;

public class NextActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TinyBus.register(this);
    }

    @Override
    protected void onDestroy() {
        TinyBus.unRegister(this);
        super.onDestroy();
    }

    @TinyEvent(sticky = true)
    public void onStickyEvent(String event) {
        TinyBus.removeSticky(event);
        //默认主线程，显示出来
        Toast.makeText(this, event, Toast.LENGTH_SHORT).show();
    }

}
