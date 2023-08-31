package com.example.camctrl.utils;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.camctrl.R;

import java.util.ArrayList;

public class RecordListViewAdapter extends BaseAdapter {

    private ArrayList<ListViewItem> listViewItemList = new ArrayList<ListViewItem>() ;

    public RecordListViewAdapter() {
    }

    @Override
    public int getCount() {
        return listViewItemList.size() ;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Context context = parent.getContext();
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.listview_record_item, parent, false);
        }
//        ImageView iconImageView = convertView.findViewById(R.id.imageView1);
//        ImageView moreIconImageView = convertView.findViewById(R.id.imageView2);

        TextView titleTextView = convertView.findViewById(R.id.tvVelocity);
        TextView tvCam = convertView.findViewById(R.id.tvCam);
//        TextView dateTextView = convertView.findViewById(R.id.tvDate);
        ListViewItem listViewItem = listViewItemList.get(position);
//        Drawable logoDrawable = context.getResources().getDrawable(listViewItem.getLogoIconId());
//        iconImageView.setImageDrawable(logoDrawable);
        titleTextView.setText(listViewItem.getTitle());
        tvCam.setText(listViewItem.getSerial());
//        dateTextView.setText(listViewItem.getSerial());
//        tvBlock.setText("경고! 출입을 제한합니다.");
//        if(listViewItem.getIsOn()) {
//            titleTextView.setTextColor(Color.BLACK);
//            dateTextView.setTextColor(Color.GRAY);
//            tvBlock.setVisibility(View.GONE);
//        }else {
//            titleTextView.setTextColor(Color.RED);
//            dateTextView.setTextColor(Color.RED);
//            tvBlock.setVisibility(View.VISIBLE);
//        }
//        moreIconImageView.setOnClickListener(v -> {
//            Log.d("yot132","more icon onClick");
//        });
        return convertView;
    }
    @Override
    public long getItemId(int position) {
        return position ;
    }

    @Override
    public Object getItem(int position) {
        return listViewItemList.get(position) ;
    }

    public void addItem(String title,String serial) {
        ListViewItem item = new ListViewItem();
        item.setTitle(title);
        item.setTitleSerial(serial);
        listViewItemList.add(item);
    }

    public void clear() {
        listViewItemList.clear();
        this.notifyDataSetChanged();
    }
}