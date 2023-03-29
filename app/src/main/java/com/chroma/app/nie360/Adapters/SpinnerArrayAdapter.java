package com.chroma.app.nie360.Adapters;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.chroma.app.nie360.Models.Datum;
import com.chroma.app.nie360.R;
import com.chroma.app.nie360.Utils.CustomTextView;

import java.util.List;

public class SpinnerArrayAdapter extends ArrayAdapter<String> {

    private final LayoutInflater mInflater;
    private final Context mContext;
    private final List<Datum> list;
    private final int mResource;

    public SpinnerArrayAdapter(@NonNull Context context, @LayoutRes int resource,
                               @NonNull List objects) {
        super(context, resource, 0, objects);

        mContext = context;
        mInflater = LayoutInflater.from(context);
        mResource = resource;
        list = objects;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView,
                                @NonNull ViewGroup parent) {
        return createItemView(position, convertView, parent);
    }

    @Override
    public @NonNull
    View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createItemView(position, convertView, parent);
    }

    private View createItemView(int position, View convertView, ViewGroup parent) {
        final View view = mInflater.inflate(mResource, parent, false);

        CustomTextView tvEventName = view.findViewById(R.id.tvEventName);
        tvEventName.setText(list.get(position).getEventName());

        return view;
    }
}