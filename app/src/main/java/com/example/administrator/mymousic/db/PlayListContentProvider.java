package com.example.administrator.mymousic.db;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.example.administrator.mymousic.db.DBHelper;

public class PlayListContentProvider extends ContentProvider {

    private static final String SCHEME = "content://";

    private static final String PATH_SONGS = "/songs";

    public static final String AUTHORITY = "com.music.PlayListContentProvider";

    //"content://com.anddle.PlayListContentProvider/songs"
    public static final Uri CONTENT_SONGS_URI = Uri.parse(SCHEME + AUTHORITY + PATH_SONGS);
    //声明成员变量，mDBHelper将帮助我们操作SQLite数据库
    private DBHelper mDBHelper;

    public PlayListContentProvider() {
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Implement this to handle requests to delete one or more rows.
        //通过DBHelper获取写数据库的方法
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        //清空playlist_table表，并将删除的数据条数返回
        int count = db.delete(DBHelper.PLAYLIST_TABLE_NAME, selection, selectionArgs);
        return count;
    }

    @Override
    public String getType(Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // TODO: Implement this to handle requests to insert a new row.
        Uri result = null;
        //通过DBHelper获取写数据库的方法
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        //将要数据ContentValues插入到数据库中
        long id = db.insert(DBHelper.PLAYLIST_TABLE_NAME, null, values);

        if (id > 0) {
            //根据返回到id值组合成该数据项对应的Uri地址,
            //假设id为8，那么这个Uri地址类似于content://com.anddle.PlayListContentProvider/songs/8
            result = ContentUris.withAppendedId(CONTENT_SONGS_URI, id);
        }
        return result;
    }

    @Override
    public boolean onCreate() {
        // TODO: Implement this to initialize your content provider on startup.
        mDBHelper = new DBHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        // TODO: Implement this to handle query requests from clients.
        //通过DBHelper获取读数据库的方法
        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        //查询数据库中的数据项
        Cursor cursor = db.query(DBHelper.PLAYLIST_TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);

        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        // TODO: Implement this to handle requests to update one or more rows.
        //通过DBHelper获取写数据库的方法
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        //更新数据库的指定项
        int count = db.update(DBHelper.PLAYLIST_TABLE_NAME, values, selection, selectionArgs);

        return count;
    }
}
