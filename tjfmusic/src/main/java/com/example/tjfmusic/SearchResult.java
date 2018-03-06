package com.example.tjfmusic;

import java.io.Serializable;

public class SearchResult implements Serializable {
    private static final long serialVersionUID = 0X00000001L;
    private String musicName;
    private String url;
    private String artist;
    private String album;

    @Override
    public String toString() {
        return "SearchResult{" +
                "musicName='" + musicName + '\'' +
                ", url='" + url + '\'' +
                ", artist='" + artist + '\'' +
                ", album='" + album + '\'' +
                '}';
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getMusicName() {
        return musicName;
    }

    public void setMusicName(String musicName) {
        this.musicName = musicName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }
}