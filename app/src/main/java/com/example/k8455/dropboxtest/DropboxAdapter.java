package com.example.k8455.dropboxtest;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by K8455 on 8.11.2017.
 * Adapter for listview items
 */

public class DropboxAdapter extends ArrayAdapter<String> {

    private Context context;
    private ArrayList<String> items;

    public DropboxAdapter(Context context, ArrayList<String> items){
        super(context,R.layout.rowlayout,R.id.rowtext, items);
        this.context = context;
        this.items = items;
    }

    @Override
    public View getView(int position, View converview, ViewGroup parent){

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.rowlayout,parent,false);

        TextView rowText = (TextView) rowView.findViewById(R.id.rowtext);
        rowText.setText(items.get(position));

        return rowView;

    }

}
