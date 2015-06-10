package com.steelgirderdev.spotifystreamer.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.steelgirderdev.spotifystreamer.R;

import java.util.List;

/**
 * Source http://stackoverflow.com/questions/2265661/how-to-use-arrayadaptermyclass
 * Generic Adapter
 */
public abstract class GenericArrayAdapter<T, C> extends ArrayAdapter<T> {

    // Vars
    private LayoutInflater mInflater;
    C mContext;

    public GenericArrayAdapter(C context, List<T> objects) {
        super((Activity) context, 0, objects);
        mContext = context;
        init(context);
    }

    // Headers
    public abstract void drawRow(TextView textView, ImageView imageView, T object);

    private void init(C context) {
        this.mInflater = LayoutInflater.from((Activity) context);
    }

    @Override public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder vh;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_item_artist_withpic, parent, false);
            vh = new ViewHolder(convertView);
            convertView.setTag(vh);
        } else {
            vh = (ViewHolder) convertView.getTag();
        }

        drawRow(vh.textView, vh.imageView, getItem(position));

        return convertView;
    }

    static class ViewHolder {

        TextView textView;
        ImageView imageView;

        private ViewHolder(View rootView) {
            textView = (TextView) rootView.findViewById(R.id.list_item_artist_withpic_textview_name);
            imageView = (ImageView) rootView.findViewById(R.id.list_item_artist_withpic_imageview);
        }
    }
}
