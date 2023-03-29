package com.chroma.app.nie360.Adapters;


import android.annotation.SuppressLint;
import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.VideoView;


import com.chroma.app.nie360.R;
import com.chroma.app.nie360.Utils.CustomTextView;
import com.chroma.app.nie360.Utils.ItemClickListener;
import com.chroma.app.nie360.Utils.TinyDB;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class AdapterBackgrounds extends RecyclerView.Adapter<AdapterBackgrounds.ViewHolder> {
    ArrayList<String> backgrounds = new ArrayList<>(), types = new ArrayList<>();
    Context mContext;
    private long mLastClickTime = 0;
    TinyDB tinyDB;

    public AdapterBackgrounds(ArrayList<String> backgrounds, ArrayList<String> types, Context mContext) {
        this.backgrounds = backgrounds;
        this.types = types;
        this.mContext = mContext;
        tinyDB = new TinyDB(mContext);
        Log.e("ImagesListSize", backgrounds.size() + "");
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemLayoutView = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.item_backgrounds, parent, false);
        return new ViewHolder(itemLayoutView);
    }


    public void setItems(ArrayList<String> backgrounds, ArrayList<String> types) {
        this.backgrounds = backgrounds;
        this.types = types;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.tvBackgroundTitle.setText("Background " + (position + 1));

        if (types.get(position).equalsIgnoreCase("image")) {
            holder.videoView.setVisibility(View.GONE);
            holder.iv.setVisibility(View.VISIBLE);
            Picasso.get().load(backgrounds.get(position)).into(holder.iv);
//            Glide.with(mContext).load(backgrounds.get(position)).into(holder.iv);

        } else {
            holder.iv.setVisibility(View.GONE);
            holder.videoView.setVisibility(View.VISIBLE);
            try {
                holder.videoView.setVideoURI(Uri.parse(backgrounds.get(position)));
                holder.videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        holder.videoView.start();
                        mp.setLooping(true);
                        mp.setVolume(0, 0);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        holder.setClickListener(new ItemClickListener() {
            @Override
            public void onClick(View view, int position, boolean isLongClick) {

            }
        });

    }

    @Override
    public int getItemCount() {

        return (null != backgrounds ? backgrounds.size() : 0);

    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        public ItemClickListener clickListener;
        ImageView iv;
        CustomTextView tvBackgroundTitle;
        VideoView videoView;

        public ViewHolder(View view) {
            super(view);

            view.setTag(view);
            view.setOnClickListener(this);
            view.setOnLongClickListener(this);

            this.iv = view.findViewById(R.id.iv);
            this.tvBackgroundTitle = view.findViewById(R.id.tvBackgroundTitle);
            this.videoView = view.findViewById(R.id.videoView);
        }

        public void setClickListener(ItemClickListener itemClickListener) {
            this.clickListener = itemClickListener;
        }

        @Override
        public void onClick(View view) {
            clickListener.onClick(view, getPosition(), false);
        }

        @Override
        public boolean onLongClick(View view) {
            clickListener.onClick(view, getPosition(), true);
            return true;
        }
    }
}