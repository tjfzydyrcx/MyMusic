package com.example.tjfmusic;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.example.tjfmusic.service.PlayService;


/**
 * Created by Administrator on 2018-03-03 0003.
 */

public class App extends Application {
    public static Context mContext;
    public static int mScreenWidth;
    public static int mSreenHeight;


    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        startService(new Intent(this, PlayService.class));

        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        mScreenWidth = dm.widthPixels;
        mSreenHeight = dm.heightPixels;

    }
}
