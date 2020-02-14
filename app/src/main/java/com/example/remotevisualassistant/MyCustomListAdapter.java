package com.example.remotevisualassistant;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;
import java.util.zip.Inflater;

public class MyCustomListAdapter extends ArrayAdapter<CallLog> {
    Context myContext;
    int resource;
    List<CallLog> logList;

    public MyCustomListAdapter(Context myContext, int resource, List<CallLog> logList){
        super(myContext,resource,logList);
        this.myContext = myContext;
        this.resource = resource;
        this.logList = logList;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(myContext);
        View view = inflater.inflate(resource,null);

        TextView textViewCaller = view.findViewById(R.id.t_caller);
        TextView textViewVolunter = view.findViewById(R.id.t_volunteer);
        TextView textViewDuration = view.findViewById(R.id.t_duration);
        TextView textViewRating = view.findViewById(R.id.t_rating);

        CallLog log = logList.get(position);

        textViewCaller.setText(log.getFrom_name());
        textViewVolunter.setText(log.getTo_name());
        textViewDuration.setText(log.getCall_time());
        textViewRating.setText(log.getRating());

        return view;
    }
}
