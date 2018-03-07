package com.example.administrator.mymousic.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.administrator.mymousic.Bean.Music;
import com.example.administrator.mymousic.db.DBHelper;
import com.example.administrator.mymousic.Util.Utils;

import com.example.administrator.mymousic.db.PlayListContentProvider;
import com.example.administrator.mymousic.listener.OnStateChangeListenr;
import com.example.administrator.mymousic.sensor.ShakeListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.example.administrator.mymousic.widget.MusicAppWidget.performUpdates;

/**
 * Created by Administrator on 2018-03-03 0003.
 */

public class MusicService extends Service {

    public static final String ACTION_PLAY_MUSIC_PRE = "com.music.playpre";
    public static final String ACTION_PLAY_MUSIC_NEXT = "com.music.playnext";
    public static final String ACTION_PLAY_MUSIC_TOGGLE = "com.music.playtoggle";
    //定义广播名称
    public static final String ACTION_PLAY_MUSIC_UPDATE = "com.music.playupdate";
    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_PLAY_MUSIC_UPDATE.equals(action)) {
                updateAppWidget(mCurrentMusicItem);
            }
        }
    };
    //定义循环发送的消息
    private final int MSG_PROGRESS_UPDATE = 0;

    private List<Music> mPlayList;
    private ContentResolver mResolver;
    private MediaPlayer mMusicPlayer;
    private List<OnStateChangeListenr> mListenerList = new ArrayList<OnStateChangeListenr>();
    //当前是否为播放暂停状态
    private boolean mPaused;
    //存放当前要播放的音乐
    private Music mCurrentMusicItem;
    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_PROGRESS_UPDATE: {
                    //将音乐的时长和当前播放的进度保存到MusicItem数据结构中，
                    mCurrentMusicItem.playedTime = mMusicPlayer.getCurrentPosition();
                    mCurrentMusicItem.duration = mMusicPlayer.getDuration();

                    //通知监听者当前的播放进度
                    for (OnStateChangeListenr l : mListenerList) {
                        l.onPlayProgressChange(mCurrentMusicItem);
                    }

                    //将当前的播放进度保存到数据库中
                    updateMusicItem(mCurrentMusicItem);

                    //间隔一秒发送一次更新播放进度的消息
                    mHandler.sendEmptyMessageDelayed(MSG_PROGRESS_UPDATE, 1000);
                }
                break;
            }


            return false;
        }
    });

    private ShakeListener mShakeListener;
    private Vibrator vibrator;


    @Override
    public void onCreate() {
        super.onCreate();
//获取ContentProvider的解析器，避免以后每次使用的时候都要重新获取
        mResolver = getContentResolver();
        //保存播放列表
        mPlayList = new ArrayList<Music>();
        mMusicPlayer = new MediaPlayer();
        initPlayList();
        //注册监听器，当收到ACTION_PLAY_MUSIC_UPDATE广播的时候，将触发mIntentReceiver的onReceive()方法被调用
        IntentFilter commandFilter = new IntentFilter();
        commandFilter.addAction(ACTION_PLAY_MUSIC_UPDATE);
        registerReceiver(mIntentReceiver, commandFilter);
        mPaused = false;
        mMusicPlayer.setOnCompletionListener(mOnCompletionListener);
        vibrator = (Vibrator) getBaseContext().getSystemService(Context.VIBRATOR_SERVICE);
        mShakeListener = new ShakeListener(getBaseContext());
        mShakeListener.setOnShakeListener(new ShakeListener.OnShakeListener() {

            @Override
            public void onShake() {
                // TODO Auto-generated method stub
                mShakeListener.stop();
                startVibrator();
                //vibrator.cancel();
                mShakeListener.start();
            }
        });

    }

    //震动函数
    void startVibrator() {
        vibrator.vibrate(500);
        random_a_song();
    }

    /**
     * 摇一摇切换随机歌曲
     */
    private void random_a_song() {
        Random rand = new Random();
        int i = rand.nextInt(mPlayList.size());
        mCurrentMusicItem = mPlayList.get(i);
        playMusicItem(mCurrentMusicItem, true);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mShakeListener.start();
        if (intent != null) {
            String action = intent.getAction();
            if (action != null) {
                if (ACTION_PLAY_MUSIC_PRE.equals(action)) {
                    playPreInner();
                } else if (ACTION_PLAY_MUSIC_NEXT.equals(action)) {
                    playNextInner();
                } else if (ACTION_PLAY_MUSIC_TOGGLE.equals(action)) {
                    if (isPlayingInner()) {
                        pauseInner();
                    } else {
                        playInner();
                    }
                }
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    //将音乐的播放信息更新到App widget中
    private void updateAppWidget(Music item) {
        if (item != null) {
            //创建音乐封面
            if (item.thumb == null) {
                ContentResolver res = getContentResolver();
                item.thumb = Utils.createThumbFromUir(res, item.albumUri);
            }
            Log.e("widget", "更新" + item.toString());
            //调用App widget提供的更新接口开始更新
            performUpdates(MusicService.this, item.name, isPlayingInner(), item.thumb);
        }
    }

    private MediaPlayer.OnCompletionListener mOnCompletionListener = new MediaPlayer.OnCompletionListener() {

        @Override
        public void onCompletion(MediaPlayer mp) {
            //将当前播放的音乐记录时间重置为0，更新到数据库
            //下次播放就可以从头开始
            mCurrentMusicItem.playedTime = 0;
            updateMusicItem(mCurrentMusicItem);
            //播放下一首音乐
            playNextInner();
        }
    };

    private void initPlayList() {
        mPlayList.clear();

        Cursor cursor = mResolver.query(
                PlayListContentProvider.CONTENT_SONGS_URI,
                null,
                null,
                null,
                null);

        while (cursor.moveToNext()) {
            String songUri = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.SONG_URI));
            String albumUri = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.ALBUM_URI));
            String name = cursor.getString(cursor.getColumnIndex(DBHelper.NAME));
            long playedTime = cursor.getLong(cursor.getColumnIndexOrThrow(DBHelper.LAST_PLAY_TIME));
            long duration = cursor.getLong(cursor.getColumnIndexOrThrow(DBHelper.DURATION));
            Music item = new Music(name, Uri.parse(songUri), Uri.parse(albumUri), duration, (int) playedTime);
            mPlayList.add(item);
        }
        cursor.close();
        if (mPlayList.size() > 0) {
            mCurrentMusicItem = mPlayList.get(0);
        }
    }

    public class MusicServiceIBinder extends Binder {
        //一次添加多首音乐
        public void addPlayList(List<Music> items) {
            addPlayListInner(items);
        }

        //一次添加一首音乐
        public void addPlayList(Music item) {
            addPlayListInner(item, true);
        }

        public void play() {
            playInner();

        }

        public void playNext() {
            playNextInner();

        }

        public void playPre() {
            playPreInner();
        }

        public void pause() {
            pauseInner();
        }

        public void seekTo(int pos) {
            seekToInner(pos);
        }

        public void registerOnStateChangeListener(OnStateChangeListenr l) {
            registerOnStateChangeListenerInner(l);

        }

        public void unregisterOnStateChangeListener(OnStateChangeListenr l) {
            unregisterOnStateChangeListenerInner(l);
        }

        public Music getCurrentMusic() {
            return getCurrentMusicInner();
        }

        public boolean isPlaying() {
            return isPlayingInner();
        }

        public List<Music> getPlayList() {
            return mPlayList;
        }

    }


    //一次添加多首歌曲具体实现
    public void addPlayListInner(List<Music> items) {
//清空数据库中的playlist_table
        mResolver.delete(PlayListContentProvider.CONTENT_SONGS_URI, null, null);
        //清空缓存的播放列表
        mPlayList.clear();
        //将每首音乐添加到播放列表的缓存和数据库中
        for (Music item : items) {
            //利用现成的代码，便于代码的维护
            addPlayListInner(item, false);
        }
        //添加完成后，开始播放
        mCurrentMusicItem = mPlayList.get(0);
        playInner();
    }

    //一次添加一首歌曲具体实现
    public void addPlayListInner(Music item, boolean needPlay) {
        if (mPlayList.contains(item)) {
            mCurrentMusicItem = item;
            playInner();
            return;
        }
        mPlayList.add(0, item);

        insertMusicItemToContentProvider(item);
        if (needPlay) {
            //添加完成后，开始播放
            mCurrentMusicItem = mPlayList.get(0);
            playInner();
        }

    }

    /**
     * 歌曲的准备
     * @param item
     */
    private void prepareToPlay(Music item) {

        try {
            mMusicPlayer.reset();
            mMusicPlayer.setDataSource(MusicService.this, item.songUri);
            mMusicPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 歌曲的播放及更新到桌面小控件
     * @param item
     * @param reload
     */
    private void playMusicItem(Music item, boolean reload) {
        if (item == null) {
            return;
        }
        if (reload) {
            prepareToPlay(item);
        }

        mMusicPlayer.start();
        seekToInner((int) item.playedTime);
        for (OnStateChangeListenr l : mListenerList) {
            l.onPlay(item);
        }
        //音乐播放时更新到App widget
        updateAppWidget(mCurrentMusicItem);
        mPaused = false;
        //移除现有的更新消息，重新启动更新
        mHandler.removeMessages(MSG_PROGRESS_UPDATE);
        mHandler.sendEmptyMessage(MSG_PROGRESS_UPDATE);

    }

    /**
     * 下一首
     */
    public void playNextInner() {

        int currentIndex = mPlayList.indexOf(mCurrentMusicItem);
        if (currentIndex < mPlayList.size() - 1) {
            mCurrentMusicItem = mPlayList.get(currentIndex + 1);
            playMusicItem(mCurrentMusicItem, true);
        } else {
            mCurrentMusicItem = mPlayList.get(0);
            playMusicItem(mCurrentMusicItem, true);
        }
    }

    public void playInner() {
        //如果之前没有选定要播放的音乐，就选列表中的第一首音乐开始播放
        if (mCurrentMusicItem == null && mPlayList.size() > 0) {
            mCurrentMusicItem = mPlayList.get(0);
        }

        //如果是从暂停状态恢复播放音乐，那么不需要重新加载音乐；
        //如果是从完全没有播放过的状态开始播放音乐，那么就需要重新加载音乐
        if (mPaused) {
            playMusicItem(mCurrentMusicItem, false);
        } else {
            playMusicItem(mCurrentMusicItem, true);
        }
    }

    public void playPreInner() {
        int currentIndex = mPlayList.indexOf(mCurrentMusicItem);
        if (currentIndex - 1 >= 0) {
            //获取当前播放（或者被加载）音乐的上一首音乐
            //如果前面有要播放的音乐，把那首音乐设置成要播放的音乐
            //并重新加载该音乐，开始播放
            mCurrentMusicItem = mPlayList.get(currentIndex - 1);
            playMusicItem(mCurrentMusicItem, true);
        } else {
            mCurrentMusicItem = mPlayList.get(mPlayList.size() - 1);
            playMusicItem(mCurrentMusicItem, true);
        }
    }

    public void pauseInner() {
        //暂停当前正在播放的音乐
        mMusicPlayer.pause();
        //将播放状态的改变通知给监听者
        for (OnStateChangeListenr l : mListenerList) {
            l.onPause(mCurrentMusicItem);
        }
        updateAppWidget(mCurrentMusicItem);
        //设置为暂停播放状态
        mPaused = true;
        //移除现有的更新消息，重新启动更新
        mHandler.removeMessages(MSG_PROGRESS_UPDATE);


    }

    public void seekToInner(int pos) {
        //将音乐拖动到指定的时间
        mMusicPlayer.seekTo(pos);
    }

    public void registerOnStateChangeListenerInner(OnStateChangeListenr l) {
        mListenerList.add(l);
    }

    public void unregisterOnStateChangeListenerInner(OnStateChangeListenr l) {
        mListenerList.remove(l);
    }

    public Music getCurrentMusicInner() {
        return mCurrentMusicItem;
    }

    public boolean isPlayingInner() {
        return mMusicPlayer.isPlaying();
    }

    private final IBinder mBinder = new MusicServiceIBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    //访问ContentProvider，保存一条数据
    private void insertMusicItemToContentProvider(Music item) {

        ContentValues cv = new ContentValues();
        cv.put(DBHelper.NAME, item.name);
        cv.put(DBHelper.DURATION, item.duration);
        cv.put(DBHelper.LAST_PLAY_TIME, item.playedTime);
        cv.put(DBHelper.SONG_URI, item.songUri.toString());
        cv.put(DBHelper.ALBUM_URI, item.albumUri.toString());
        Uri uri = mResolver.insert(PlayListContentProvider.CONTENT_SONGS_URI, cv);
    }

    //将播放时间更新到ContentProvider中
    private void updateMusicItem(Music item) {

        ContentValues cv = new ContentValues();
        cv.put(DBHelper.DURATION, item.duration);
        cv.put(DBHelper.LAST_PLAY_TIME, item.playedTime);
        String strUri = item.songUri.toString();
        mResolver.update(PlayListContentProvider.CONTENT_SONGS_URI, cv, DBHelper.SONG_URI + "=\"" + strUri + "\"", null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mMusicPlayer.release();
        //当MusicService销毁的时候，清空监听器列表
        mListenerList.clear();
        mHandler.removeMessages(MSG_PROGRESS_UPDATE);
        //注销监听器，防止内存泄漏
        unregisterReceiver(mIntentReceiver);
        mShakeListener.stop();
    }
}
