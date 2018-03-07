package com.example.administrator.mymousic;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.administrator.mymousic.Adapter.MusicItemAdapter;
import com.example.administrator.mymousic.Bean.Music;
import com.example.administrator.mymousic.Task.MusicUpdateTask;
import com.example.administrator.mymousic.Util.Utils;
import com.example.administrator.mymousic.service.MusicService;
import com.example.administrator.mymousic.listener.OnStateChangeListenr;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018-03-02 0002.
 */

public class MusicListActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, View.OnClickListener {
    List<Music> mDataList = new ArrayList<>();
    private MusicUpdateTask musicUpdateTask;
    ListView mlistView;
    MusicItemAdapter mAdapter;
    ImageView mOn, mPause, mUnder_the;
    SeekBar seekBar;
    MusicService.MusicServiceIBinder binder;
    TextView mMusicTitle, mPlayedTime, mDurationTime;
    private OnStateChangeListenr mStateChangeListenr = new OnStateChangeListenr() {

        @Override
        public void onPlayProgressChange(Music item) {
            //更新播放进度信息
            updatePlayingInfo(item);
        }

        @Override
        public void onPlay(Music item) {
            //更新播放按钮背景
            mPause.setImageResource(R.drawable.pause_press);
            updatePlayingInfo(item);
            //激活控制区域
            enableControlPanel(true);
        }

        @Override
        public void onPause(Music item) {
            //更新播放按钮背景
            mPause.setImageResource(R.drawable.play_press);
            //激活控制区域
            enableControlPanel(true);

        }
    };

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            binder = (MusicService.MusicServiceIBinder) service;
            binder.registerOnStateChangeListener(mStateChangeListenr);
            //获取播放列表中可播的音乐
            Music item = binder.getCurrentMusic();
            if (item == null) {
                //没有可播的音乐，是控制区域不可操作
                enableControlPanel(false);
            } else {
                //根据当前被加载的音乐信息更新控制区域信息
                updatePlayingInfo(item);
            }
            if (binder.isPlaying()) {
                //如果音乐处于播放状态降按钮背景设置成暂停图标
                mPause.setImageResource(R.drawable.pause_press);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mislist);
        mlistView = (ListView) findViewById(R.id.music_listview);
        mMusicTitle = (TextView) findViewById(R.id.music_title);
        mDurationTime = (TextView) findViewById(R.id.music_duration);
        mPlayedTime = (TextView) findViewById(R.id.music_play_time);
        mOn = (ImageView) findViewById(R.id.music_Ona);
        mPause = (ImageView) findViewById(R.id.music_pause_and_play);
        mUnder_the = (ImageView) findViewById(R.id.music_under_the);
        seekBar = (SeekBar) findViewById(R.id.seekBar);

        mAdapter = new MusicItemAdapter(this, R.layout.mis_list_item, mDataList);
        mlistView.setAdapter(mAdapter);
        musicUpdateTask = new MusicUpdateTask(mlistView, mDataList, this);
        musicUpdateTask.execute();
        Intent i = new Intent(this, MusicService.class);
        //启动MusicService
        startService(i);
        //实现绑定操作
        bindService(i, mServiceConnection, BIND_AUTO_CREATE);
        initListener();
    }

    public void initListener() {
        mlistView.setOnItemClickListener(this);
        mOn.setOnClickListener(this);
        mPause.setOnClickListener(this);
        mUnder_the.setOnClickListener(this);
        //设置多选modal模式
        mlistView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
        //设置多选模式变化的监听器
        mlistView.setMultiChoiceModeListener(mMultiChoiceListener);
        seekBar.setOnSeekBarChangeListener(mOnSeekBarChangeListener);
    }

    //进度条拖动的监听器
    private SeekBar.OnSeekBarChangeListener mOnSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            //停止拖动时，根据进度条的位置来设定播放的位置
            if (binder != null) {
                binder.seekTo(seekBar.getProgress());
            }
        }
    };

    //更新播放信息
    private void updatePlayingInfo(Music item) {
//        Log.e("itemss==",item.toString());
        //将毫秒单位的时间，转化成xx:xx形式的时间
        String times = Utils.convertMSecendToTime(item.getDuration());
        mDurationTime.setText(times);

        times = Utils.convertMSecendToTime(item.getPlayedTime());

        mPlayedTime.setText(times);

        //设置进度条的最大值
        seekBar.setMax((int) item.getDuration());
        //设置进度条的当前值
        seekBar.setProgress(item.getPlayedTime());

        mMusicTitle.setText(item.getName());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (musicUpdateTask != null && musicUpdateTask.getStatus() == AsyncTask.Status.RUNNING) {
            musicUpdateTask.cancel(true);
        }
        musicUpdateTask = null;
        //手动回收使用的图片资源
        for (Music item : mDataList) {
            if (item.thumb != null) {
                item.thumb.recycle();
                item.thumb = null;
            }
        }
        mDataList.clear();
        unbindService(mServiceConnection);
        binder.unregisterOnStateChangeListener(mStateChangeListenr);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Music item = mDataList.get(position);
        Log.e("tiem", item.songUri + "");

        if (binder != null) {
            binder.addPlayList(item);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.music_Ona:
                if (binder != null) {
                    binder.playPre();
                }
                break;

            case R.id.music_pause_and_play:
                if (binder != null) {
                    if (!binder.isPlaying()) {
                        binder.play();
                    } else {
                        binder.pause();
                    }
                }
                break;

            case R.id.music_under_the:
                if (binder != null) {
                    binder.playNext();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_list, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.play_list_menu: {
                //响应用户对菜单的点击，显示播放列表
                showPlayList();
            }
            break;

        }

        return true;
    }

    private void showPlayList() {

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //设置对话框的图标
        builder.setIcon(R.mipmap.ic_playlist);
        //设计对话框的显示标题
        builder.setTitle(R.string.play_list);

        //获取播放列表，把播放列表中歌曲的名字取出组成新的列表
        final List<Music> playList = binder.getPlayList();
        final ArrayList<String> data = new ArrayList<String>();
        for (Music music : playList) {
            data.add(music.name);
        }
        ArrayAdapter<String> adapter = null;
        if (data.size() > 0) {
            //播放列表有曲目，显示音乐的名称
            adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, data);
            builder.setAdapter(adapter, null);
        } else {
            //播放列表没有曲目，显示没有音乐
            builder.setMessage(getString(R.string.no_song));
        }
        builder.setSingleChoiceItems(adapter, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                binder.addPlayList(playList.get(which));
                dialog.dismiss();
            }
        });


        //设置该对话框是可以自动取消的，例如当用户在空白处随便点击一下，对话框就会关闭消失
        builder.setCancelable(true);

        //创建并显示对话框
        builder.create().show();
    }

    private ListView.MultiChoiceModeListener mMultiChoiceListener = new AbsListView.MultiChoiceModeListener() {

        @Override
        public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {

        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            //增加进入modal模式后的菜单栏菜单项
            getMenuInflater().inflate(R.menu.music_choice_actionbar, menu);
            enableControlPanel(false);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menu_play: {
                    //获取被选中的音乐项
                    List musicList = new ArrayList<Music>();
                    SparseBooleanArray checkedResult = mlistView.getCheckedItemPositions();
                    for (int i = 0; i < checkedResult.size(); i++) {
                        if (checkedResult.valueAt(i)) {
                            int pos = checkedResult.keyAt(i);
                            Music music = mDataList.get(pos);
                            musicList.add(music);
                        }
                    }
                    //调用MusicService提供的接口，把播放列表保存起来
                    binder.addPlayList(musicList);

                    //退出ListView的modal状态
                    mode.finish();
                }
                break;
            }
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            enableControlPanel(true);
        }

    };

    private void enableControlPanel(boolean enabled) {
        mOn.setEnabled(enabled);
        mPause.setEnabled(enabled);
        mUnder_the.setEnabled(enabled);
        seekBar.setEnabled(enabled);
    }
}
