package com.example.tjfmusic;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018-03-05 0005.
 */

public interface OnRecommendListener {
    public void onSuccess(ArrayList<SearchResult> results);

    public void onFail(String results);
}
