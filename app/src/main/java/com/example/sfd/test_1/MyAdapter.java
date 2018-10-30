package com.example.sfd.test_1;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by SFD on 2017/12/21.
 */

public class MyAdapter extends SimpleAdapter{

    int count = 0;

    private List<Map<String, Object>> mlist;

    public MyAdapter(Context context, List<? extends Map<String, Object>>data,
                     int resource, String[] from, int[] to){
        super(context, data, resource, from, to);
        mlist = (List<Map<String, Object>>) data;
        if (data == null){
            count = 0;
        }else {
            count = data.size();
        }
    }

    public int getCount(){
        return mlist.size();
    }
    public Object getItem(int pos){
        return pos;
    }
    public long getItemId(int pos){
        return pos;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Map<String, Object> map = mlist.get(position);
//        int image = map.get(""+position);
        View view = super.getView(position, convertView, parent);
//        ImageView imageView = (ImageView) view.findViewById(R.id.ble_image);

        return super.getView(position, convertView, parent);
    }

    //    ListView listView = (ListView) findViewById(R.id.list_1);
//    List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
//    Map<String, Object> map = new HashMap<String, Object>();


}
