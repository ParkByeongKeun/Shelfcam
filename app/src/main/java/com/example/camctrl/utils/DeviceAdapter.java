package com.example.camctrl.utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.camctrl.R;

import java.util.ArrayList;

public class DeviceAdapter extends ArrayAdapter<CustomDevice> {

    private ArrayList<CustomDevice> items;
    Context context;

    public DeviceAdapter(Context context, int textViewResourceId, ArrayList<CustomDevice> items) {
        super(context, textViewResourceId, items);
        this.context = context;
        this.items = items;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.vw_list_item, null);
        }
        CustomDevice p = items.get(position);
        if (p != null) {

            ImageView img_title = v.findViewById(R.id.iv_title);
            TextView title1 = v.findViewById(R.id.tvTop2);
            TextView title2 = v.findViewById(R.id.tvTop3);
            TextView title3 = v.findViewById(R.id.tvTop4);
//            RelativeLayout rlEditSerial = v.findViewById(R.id.rlEditSerial);

//            rlEditSerial.setOnClickListener(view -> {
//                if(mItemClickListener != null)
//                    mItemClickListener.onItemClick(position);
//            });
//            img_title.setImageResource(p.getImgName());
//            Log.d("yot132","aa = " + Uri.parse(p.getImgName()));
            Glide.with(this.context).load(p.getImgName()).into(img_title);
            title1.setText(p.getTitle1());
            title2.setText(p.getTitle2());
            title3.setText(p.getTitle3());

        }
        return v;
    }

//    public void setItemClickListener(ItemClickListener a_listener) {
//        Log.d("yot132","a_listener = " + a_listener);
//        mItemClickListener = a_listener;
//    }
}