package com.example.tjfmusic;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SongsjsoupUtils.getsInstance().getMusicPool();
        SongsjsoupUtils.getsInstance().setListener(new OnRecommendListener() {
            @Override
            public void onSuccess(ArrayList<SearchResult> results) {
                Log.e("song", results.get(0).toString());
            }

            @Override
            public void onFail(String results) {
                Log.e("fail", "失败");
            }
        });
    }
}
