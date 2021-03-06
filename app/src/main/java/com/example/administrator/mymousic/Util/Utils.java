package com.example.administrator.mymousic.Util;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Administrator on 2018-03-02 0002.
 */

public class Utils {

    static public String convertMSecendToTime(long time) {

        SimpleDateFormat mSDF = new SimpleDateFormat("mm:ss");

        Date date = new Date(time);
        String times = mSDF.format(date);

        return times;
    }

    static public Bitmap createThumbFromUir(ContentResolver res, Uri albumUri) {
        InputStream in = null;
        Bitmap bmp = null;
        try {
            in = res.openInputStream(albumUri);
            BitmapFactory.Options sBitmapOptions = new BitmapFactory.Options();
            bmp = BitmapFactory.decodeStream(in, null, sBitmapOptions);
            in.close();
        } catch (FileNotFoundException e) {

        } catch (IOException e) {
            e.printStackTrace();
        }

        return bmp;
    }


}
