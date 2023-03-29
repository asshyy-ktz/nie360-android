package com.chroma.app.nie360.Adapters;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.chroma.app.nie360.Activities.SendingActivity;
import com.chroma.app.nie360.Models.ProcessingQueueModel;
import com.chroma.app.nie360.Models.UploadingQueueModel;
import com.chroma.app.nie360.R;
import com.chroma.app.nie360.Utils.Constants;
import com.chroma.app.nie360.Utils.CustomTextView;
import com.chroma.app.nie360.Utils.ItemClickListener;
import com.chroma.app.nie360.Utils.ProcessingService;
import com.chroma.app.nie360.Utils.TinyDB;
import com.chroma.app.nie360.Utils.UploaderService;
import com.squareup.picasso.Picasso;

import java.io.Serializable;
import java.util.ArrayList;

import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;

public class AdapterPendingVideos extends RecyclerView.Adapter<AdapterPendingVideos.ViewHolder> {
    ArrayList<UploadingQueueModel> list = new ArrayList<>();
    Context mContext;
    private long mLastClickTime = 0;
    TinyDB tinyDB;

    public AdapterPendingVideos(ArrayList<UploadingQueueModel> backgrounds, Context mContext) {
        this.list = backgrounds;
        this.mContext = mContext;
        tinyDB = new TinyDB(mContext);
        Log.e("ImagesListSize", backgrounds.size() + "");
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemLayoutView = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.item_videos, parent, false);
        return new ViewHolder(itemLayoutView);
    }


    public void setItems(ArrayList<UploadingQueueModel> backgrounds) {
        this.list = backgrounds;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        holder.ivReload.setImageResource(R.drawable.ic_back);
        Log.d("STATUS:", "STATUS IS: " + list.get(position).getStatus());

        if (list.get(position).getStatus().equals("Failed")) {
        holder.ivStatus.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_warning));
        holder.ivReload.setVisibility(View.VISIBLE);
        holder.progress.setVisibility(View.GONE);
        holder.pb.setVisibility(View.VISIBLE);
        }else if (list.get(position).getStatus().equals("Success")){
            holder.ivStatus.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_checked));
            holder.ivReload.setVisibility(View.GONE);
            holder.progress.setVisibility(View.GONE);
            holder.pb.setVisibility(View.VISIBLE);

        }else if(list.get(position).getStatus().equals("Processing")){
            holder.ivStatus.setImageResource(R.drawable.ic_hourglass);
            holder.ivReload.setVisibility(View.GONE);

            holder.progress.setVisibility(View.VISIBLE);
            holder.pb.setVisibility(View.GONE);

        }
        if (list.get(position).getEmail() != null &&  list.get(position).getEmail().isEmpty()){
            holder.email.setText(list.get(position).getPhone().toString());

        }else{
            holder.email.setText(list.get(position).getEmail().toString());

        }


        holder.ivReload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.ivStatus.setImageResource(R.drawable.ic_hourglass);
                holder.ivReload.setVisibility(View.GONE);

                holder.progress.setVisibility(View.VISIBLE);
                holder.pb.setVisibility(View.GONE);

                Intent intent = new Intent(mContext, UploaderService.class);
                UploadingQueueModel model = new UploadingQueueModel();
                model.setEmail(list.get(position).getEmail());
                model.setPhone(list.get(position).getPhone());
                model.setEventName(list.get(position).getEventName());
                model.setPath(list.get(position).getPath());
                model.setPosition(position);
                intent.putExtra("UploadingModel",  model);

                mContext.startService(intent);

            }
        });

        holder.setClickListener(new ItemClickListener() {
            @Override
            public void onClick(View view, int position, boolean isLongClick) {

            }
        });

    }

    @Override
    public int getItemCount() {

        return (null != list ? list.size() : 0);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        public ItemClickListener clickListener;
        ImageView ivReload, ivStatus;
        SmoothProgressBar progress;
        RelativeLayout rlError;
        ProgressBar pb;
        TextView email;



        public ViewHolder(View view) {
            super(view);

            view.setTag(view);
            view.setOnClickListener(this);
            view.setOnLongClickListener(this);
            this.ivReload = view.findViewById(R.id.ivReload);
            this.ivStatus = view.findViewById(R.id.ivStatus);
            this.email = view.findViewById(R.id.email);

            this.progress = view.findViewById(R.id.progress);
            this.rlError = view.findViewById(R.id.rlError);
            pb = view.findViewById(R.id.pb);
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
    public void refreshBlockOverlay(int position) {
        notifyItemChanged(position);
    }
}