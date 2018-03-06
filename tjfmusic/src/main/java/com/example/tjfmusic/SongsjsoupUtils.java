package com.example.tjfmusic;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by Administrator on 2018-03-05 0005.
 */

public class SongsjsoupUtils {

    private static final String URL = "http://music.baidu.com/top/new/?pst=shouyeTop";

    private static SongsjsoupUtils sInstance;

    public static SongsjsoupUtils getsInstance() {
        if (sInstance == null) {
            sInstance = new SongsjsoupUtils();
        }
        return sInstance;
    }

    private ThreadPoolExecutor poolExecutor;

    private OnRecommendListener mListener;

    private Handler mHandle = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case ConstantsUtils.SUCCESS:
                    mListener.onSuccess((ArrayList<SearchResult>) msg.obj);
                    break;
                case ConstantsUtils.FAIL:
                    mListener.onFail("无歌曲");
                    break;
            }
        }
    };


    @SuppressLint("HandlerLeak")
    private SongsjsoupUtils() {
        poolExecutor = new
                ThreadPoolExecutor(5, 10, 1,
                TimeUnit.MINUTES, new
                LinkedBlockingQueue<Runnable>(1));
    }

    public SongsjsoupUtils setListener(OnRecommendListener listener) {
        this.mListener = listener;
        return this;
    }

    public void getMusicPool() {
        poolExecutor.execute(new Runnable() {
            @Override
            public void run() {
                ArrayList<SearchResult> results = getMusicList();
                if (results == null) {
                    mHandle.sendEmptyMessage(ConstantsUtils.FAIL);
                    return;
                }
                mHandle.obtainMessage(ConstantsUtils.SUCCESS, results).sendToTarget();
            }
        });
    }

    private ArrayList<SearchResult> getMusicList() {
        try {
            Document doc = Jsoup.connect(URL).userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36"
                    + " (KHTML, like Gecko) Chrome/42.0.2311.22 Safari/537.36").timeout(60 * 1000).get();
            Elements songstitles = doc.select("span.song-title");
            Elements artists = doc.select("span.author_list");
            ArrayList<SearchResult> searchResults = new ArrayList<>();
            Log.e("doc",doc.toString());
            for (int i = 0; i < songstitles.size(); i++) {
                SearchResult searchResult = new SearchResult();
                Elements urls = songstitles.get(i).getElementsByTag("a");
                String headurl="http://music.baidu.com";
                searchResult.setUrl(headurl+urls.get(0).attr("href"));
                searchResult.setMusicName(urls.get(0).text());

                Elements artisElements = artists.get(i).getElementsByTag("a");
                searchResult.setArtist(artisElements.get(0).text());
                searchResult.setAlbum("最新推荐");
                searchResults.add(searchResult);

            }
            return searchResults;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
