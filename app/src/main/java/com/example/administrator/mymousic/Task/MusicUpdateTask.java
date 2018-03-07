package com.example.administrator.mymousic.Task;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ListView;

import com.example.administrator.mymousic.Adapter.MusicItemAdapter;
import com.example.administrator.mymousic.Bean.Music;
import com.example.administrator.mymousic.Util.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018-03-02 0002.
 */

public class MusicUpdateTask extends AsyncTask<Object, Music, List<Music>> {
    List<Music> mDataList = new ArrayList<>();
    Context content;
    ListView mlistView;

    public MusicUpdateTask(ListView mlistView, List<Music> mDataList, Context content) {
        this.mDataList = mDataList;
        this.content = content;
        this.mlistView = mlistView;
    }

    @Override
    protected List<Music> doInBackground(Object... params) {

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        String[] searchkKey = new String[]{
                MediaStore.Audio.Media._ID,//检索ID
                MediaStore.Audio.Media.TITLE,//文件标题
                MediaStore.Audio.Media.ALBUM_ID,//专辑ID
                MediaStore.Audio.Media.DATA,//文件存放位置
                MediaStore.Audio.Media.DURATION//播放时长
        };
        String where = MediaStore.Audio.Media.DATA + " like \"%" + "/music" + "%\"";
        String[] keywords = null;
        String sortOrder = MediaStore.Audio.Media.DEFAULT_SORT_ORDER;
        ContentResolver resolver = content.getContentResolver();

        Cursor cursor = resolver.query(uri, searchkKey, where, null, sortOrder);

        if (cursor != null) {
            while (cursor.moveToNext() && !isCancelled()) {
                String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                String id = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
                Uri musicUri = Uri.withAppendedPath(uri, id);
                String name = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                long duration = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
                int albumId = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM_ID));
                Uri albumUri = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), albumId);
                Music data = new Music(name, musicUri, albumUri, duration,0);
                if (uri != null) {
                    ContentResolver res = content.getContentResolver();
                    data.thumb = Utils.createThumbFromUir(res, albumUri);
                }
                //  mDataList.add(data);
                Log.d("cccc", "real music found: " + path);
                publishProgress(data);
            }
            cursor.close();
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Music... values) {
        mDataList.add(values[0]);
        MusicItemAdapter adapter = (MusicItemAdapter) mlistView.getAdapter();
        adapter.notifyDataSetChanged();
    }

   /* @Override
    protected void onPostExecute(List<Music> musics) {
        MusicItemAdapter adapter = new MusicItemAdapter(content, R.layout.mis_list_item, musics);
        mlistView.setAdapter(adapter);
        // adapter.notifyDataSetChanged();
    }*/
}
