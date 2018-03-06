package com.example.administrator.mymousic.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.RemoteViews;

import com.example.administrator.mymousic.R;
import com.example.administrator.mymousic.service.MusicService;

/**
 * Implementation of App Widget functionality.
 */
public class MusicAppWidget extends AppWidgetProvider {
    //保存各个小工具的id
    private static int[] sAppWidgetIds;
    SharedPreferences sp;

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId, String musicName, boolean isPlaying, Bitmap thumb) {

        //创建RemoteViews
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.music_app_widget);
        //添加界面元素的逻辑控制代码，例如按钮、文字、图片等等
        //设置音乐的名称
        views.setTextViewText(R.id.music_name, musicName);

        //设置按钮响应的对象，这里是MusicService
        final ComponentName serviceName = new ComponentName(context, MusicService.class);

        //设置下一首按钮对应的PendingIntent
        //通过MusicService.ACTION_PLAY_MUSIC_NEXT定义隐性Intent，唤醒MusicService的响应
        Intent nextIntent = new Intent(MusicService.ACTION_PLAY_MUSIC_NEXT);
        nextIntent.setComponent(serviceName);
        PendingIntent nextPendingIntent = PendingIntent.getService(context,
                0, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.next_btn2, nextPendingIntent);

        //设置前一首按钮对应的PendingIntent
        //通过MusicService.ACTION_PLAY_MUSIC_PRE定义隐性Intent，唤醒MusicService的响应
        Intent preIntent = new Intent(MusicService.ACTION_PLAY_MUSIC_PRE);
        preIntent.setComponent(serviceName);
        PendingIntent prePendingIntent = PendingIntent.getService(context,
                0, preIntent, 0);
        views.setOnClickPendingIntent(R.id.pre_btn2, prePendingIntent);

        //设置播放暂停按钮对应的PendingIntent
        //通过MusicService.ACTION_PLAY_MUSIC_TOGGLE定义隐性Intent，唤醒MusicService的响应
        Intent toggleIntent = new Intent(MusicService.ACTION_PLAY_MUSIC_TOGGLE);
        toggleIntent.setComponent(serviceName);
        PendingIntent togglePendingIntent = PendingIntent.getService(context,
                0, toggleIntent, 0);
        views.setOnClickPendingIntent(R.id.play_btn2, togglePendingIntent);

        //设置播放暂停按钮的图标
        views.setInt(R.id.play_btn2, "setBackgroundResource", isPlaying ? R.drawable.pause_press : R.drawable.play_press);
        //设置音乐的封面
        if (thumb != null) {
            views.setImageViewBitmap(R.id.image_thumb, thumb);
        } else {
            views.setImageViewResource(R.id.image_thumb, R.mipmap.ic_launcher);
        }
        //通过appWidgetId，为指定的小工具界面更新
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them

        sp = context.getSharedPreferences("appwidget", Context.MODE_PRIVATE);
        sp.edit().putInt("appid", appWidgetIds[0]).apply();
        if (sp.getInt("appid", 0) == 0) {
            sAppWidgetIds = appWidgetIds;
        } else {
            appWidgetIds[0] = sp.getInt("appid", 0);
            sAppWidgetIds = appWidgetIds;
        }
    Log.e("appid",sp.getInt("appid", 0)+"==appid");
        //使用默认的参数更新App widget
        performUpdates(context, context.getString(R.string.no_song), false, null);
        //发送广播给MusicService，让它调用接口，更新App widget
        Intent updateIntent = new Intent(MusicService.ACTION_PLAY_MUSIC_UPDATE);
        context.sendBroadcast(updateIntent);
    }

    //对外提供的更新所有小工具的界面接口，需要传入音乐的名称、当前是否播放、音乐封面等参数
    public static void performUpdates(Context context, String musicName, boolean isPlaying, Bitmap thumb) {
        //如果没有小工具的id，就没法更新界面

        if (sAppWidgetIds == null || sAppWidgetIds.length == 0) {
            Log.e("sAppWidgetIds", sAppWidgetIds + "===id");

            return;
        }


        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        Log.e("sAppWidgetIds111111", sAppWidgetIds + "===id");
        //遍历每个桌面上的小工具，根据id逐个更新界面
        for (int appWidgetId : sAppWidgetIds) {

            updateAppWidget(context, appWidgetManager, appWidgetId, musicName, isPlaying, thumb);
        }
    }

  /*  @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        Intent i = new Intent(context, MusicService.class);
        context.startService(i);
    }*/

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
        Intent i = new Intent(context, MusicService.class);
        context.startService(i);
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
        Intent stopUpdateIntent = new Intent(context, MusicService.class);
        context.stopService(stopUpdateIntent);
    }
}

