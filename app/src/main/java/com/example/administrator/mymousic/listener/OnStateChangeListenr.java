package com.example.administrator.mymousic.listener;

import com.example.administrator.mymousic.Bean.Music;

public interface OnStateChangeListenr {

    //用来通知播放进度
    void onPlayProgressChange(Music item);

    //用来通知当前处于播放状态
    void onPlay(Music item);

    //用来通知当前处于暂停或停止状态
    void onPause(Music item);
}