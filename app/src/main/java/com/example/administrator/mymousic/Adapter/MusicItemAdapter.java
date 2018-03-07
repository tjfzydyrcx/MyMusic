package com.example.administrator.mymousic.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.administrator.mymousic.Bean.Music;
import com.example.administrator.mymousic.R;
import com.example.administrator.mymousic.Util.Utils;

import java.util.List;

public class MusicItemAdapter extends BaseAdapter {

    private List<Music> mData;
    private LayoutInflater mInflater;
    private int mResource;
    private Context mContext;

    public MusicItemAdapter() {

    }

    public MusicItemAdapter(Context context, int resId, List<Music> data) {
        mContext = context;
        mData = data;
        mInflater = LayoutInflater.from(context);
        mResource = resId;
    }

    @Override
    public int getCount() {
        return mData != null ? mData.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return mData != null ? mData.get(position) : null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mInflater.inflate(mResource, parent, false);
        }

        Music item = mData.get(position);

        TextView title = convertView.findViewById(R.id.music_title);
        title.setText(item.name);

        TextView createTime = convertView.findViewById(R.id.music_duration);

        //调用辅助函数转换时间格式
        String times = Utils.convertMSecendToTime(item.duration);
        createTime.setText(times);

        ImageView thumb = convertView.findViewById(R.id.music_thumb);
        if (thumb != null) {
            if (item.thumb != null) {
                thumb.setImageBitmap(item.thumb);
            } else {
                thumb.setImageResource(R.mipmap.ic_launcher_round);
            }
        }

        return convertView;
    }

}
 