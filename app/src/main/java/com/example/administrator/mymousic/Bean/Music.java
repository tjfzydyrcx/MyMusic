package com.example.administrator.mymousic.Bean;

import android.graphics.Bitmap;
import android.net.Uri;

/**
 * 音乐信息类
 * Created by Administrator on 2018-03-02 0002.
 */

public class Music {


    @Override
    public boolean equals(Object o) {
        Music another = (Music) o;

        //音乐的Uri相同，则说明两者相同
        return another.songUri.equals(this.songUri);
    }

    //歌曲名
    public String name;
    //歌曲uri地址
    public Uri songUri;
    //歌曲封面uri
    public Uri albumUri;
    //歌曲封面图片
    public Bitmap thumb;
    //歌曲播放时长 单位毫秒
    public long duration;

    @Override
    public String toString() {
        return "Music{" +
                "name='" + name + '\'' +
                ", songUri=" + songUri +
                ", albumUri=" + albumUri +
                ", thumb=" + thumb +
                ", duration=" + duration +
                ", playedTime=" + playedTime +
                '}';
    }

    public int getPlayedTime() {
        return playedTime;
    }

    public void setPlayedTime(int playedTime) {
        this.playedTime = playedTime;
    }

    public int playedTime;

    public Music(String name, Uri songUri, Uri albumUri, long duration, int playedTime) {
        this.name = name;
        this.songUri = songUri;
        this.albumUri = albumUri;
        this.duration = duration;
        this.playedTime = playedTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Uri getSongUri() {
        return songUri;
    }

    public void setSongUri(Uri songUri) {
        this.songUri = songUri;
    }

    public Uri getAlbumUri() {
        return albumUri;
    }

    public void setAlbumUri(Uri albumUri) {
        this.albumUri = albumUri;
    }

    public Bitmap getThumb() {
        return thumb;
    }

    public void setThumb(Bitmap thumb) {
        this.thumb = thumb;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }
}
