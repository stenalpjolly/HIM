package com.example.user.cloudmessenger;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by LEKHA VARGHESE on 18-04-2015.
 */
public class CustomList extends ArrayAdapter<RowItem> {

    Context context;

    public CustomList(Context context, int resourceId,
                      List<RowItem> items) {
        super(context, resourceId, items);
        this.context = context;
    }

    /*private view holder class*/
    private class ViewHolder {
        ImageView imageView;
        TextView txtTitle;
        TextView txtDate;
        TextView txtDur;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        RowItem rowItem = getItem(position);

        LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.adapter_layout, null);
            holder = new ViewHolder();
            holder.txtDate = (TextView) convertView.findViewById(R.id.date);
            holder.txtTitle = (TextView) convertView.findViewById(R.id.title);
            holder.txtDur=(TextView)convertView.findViewById(R.id.dur);
            holder.imageView = (ImageView) convertView.findViewById(R.id.icon);
            convertView.setTag(holder);
        } else
            holder = (ViewHolder) convertView.getTag();
        SimpleDateFormat sd=new SimpleDateFormat("dd:mm:yyyy hh:mm");
        String dat = sd.format(rowItem.getDate());
        holder.txtDate.setText(dat);
        holder.txtTitle.setText(rowItem.getTitle());
        holder.txtDur.setText(rowItem.getDuration());
        holder.imageView.setImageResource(rowItem.getImageId());

        return convertView;
    }
}
