package com.harlan.watchboard;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.harlan.view.WatchBoard;

public class MainActivity extends AppCompatActivity {

    private WatchBoard mWatchBoard = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mWatchBoard = (WatchBoard) findViewById(R.id.watch_board);
    }
}
