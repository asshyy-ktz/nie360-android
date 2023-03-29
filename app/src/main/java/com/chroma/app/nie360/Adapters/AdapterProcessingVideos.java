package com.chroma.app.nie360.Adapters;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chroma.app.nie360.Activities.AdminPenal;
import com.chroma.app.nie360.Activities.PerviewActivity;
import com.chroma.app.nie360.Models.ProcessingQueueModel;
import com.chroma.app.nie360.Models.UploadingQueueModel;
import com.chroma.app.nie360.R;
import com.chroma.app.nie360.Utils.Constants;
import com.chroma.app.nie360.Utils.ItemClickListener;
import com.chroma.app.nie360.Utils.TinyDB;

import java.util.ArrayList;

import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;

public class AdapterProcessingVideos extends RecyclerView.Adapter<AdapterProcessingVideos.ViewHolder> {
    ArrayList<ProcessingQueueModel> list = new ArrayList<>();
    Context mContext;
    private long mLastClickTime = 0;
    TinyDB tinyDB;

    public AdapterProcessingVideos(ArrayList<ProcessingQueueModel> backgrounds, Context mContext) {
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


    public void setItems(ArrayList<ProcessingQueueModel> backgrounds) {
        this.list = backgrounds;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        if (list.get(position).getStatus().equals(Constants.StatusFinished)) {
            holder.ivStatus.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_warning));
            holder.ivReload.setVisibility(View.VISIBLE);
            holder.progress.setVisibility(View.GONE);
            holder.pb.setVisibility(View.VISIBLE);
        } else if (list.get(position).getStatus().equals(Constants.StatusCompleted)) {
            holder.ivStatus.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_checked));
            holder.ivReload.setVisibility(View.GONE);
            holder.progress.setProgressiveStartActivated(false);
            holder.progress.setVisibility(View.GONE);
            holder.pb.setVisibility(View.VISIBLE);
        } else if (list.get(position).getStatus().equals(Constants.StatusProcessing)) {
            holder.ivStatus.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_hourglass));
            holder.ivReload.setVisibility(View.GONE);
            holder.progress.setProgressiveStartActivated(false);
            holder.progress.setVisibility(View.VISIBLE);
            holder.pb.setVisibility(View.GONE);
        } else if (list.get(position).getStatus().equals(Constants.StatusAdded)) {
            holder.ivStatus.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_hourglass));
            holder.ivReload.setVisibility(View.GONE);
            holder.progress.setProgressiveStartActivated(false);
            holder.progress.setVisibility(View.VISIBLE);
            holder.pb.setVisibility(View.GONE);
        }
        Log.d("EMAILS", "Email id is:" + list.get(position).getEmail());
        if (list.get(position).getEmail().isEmpty()){
            holder.email.setText(list.get(position).getPhone().toString());

        }else{
            holder.email.setText(list.get(position).getEmail().toString());

        }


        holder.ivReload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(mContext)
                        .setTitle("Are you sure")
                        .setMessage("Failed processing video will be removed from list.")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                list.remove(position);
                                tinyDB.putProcessingListObject(Constants.ProcessingList, list);
                                Log.e("size", tinyDB.getProcessingListObject(Constants.ProcessingList, ProcessingQueueModel.class).size() + "");
                                notifyDataSetChanged();
                            }
                        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();
            }
        });
        holder.setClickListener(new ItemClickListener() {
            @Override
            public void onClick(View view, int position, boolean isLongClick) {
                if (list.get(position).getStatus().equals(Constants.StatusProcessing)) {
                }
                if (list.get(position).getStatus().equals(Constants.StatusCompleted)) {
                    Intent i = new Intent(mContext, PerviewActivity.class);
                    i.putExtra("vid_url", list.get(position).getOutputPath());
                    Log.e("vid_url", list.get(position).getOutputPath());
                    mContext.startActivity(i);
                }
                if (list.get(position).getStatus().equals(Constants.StatusFinished)) {

                }
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
            this.ivStatus = view.findViewById(R.id.ivStatus);
            this.ivReload = view.findViewById(R.id.ivReload);
            this.progress = view.findViewById(R.id.progress);
            this.rlError = view.findViewById(R.id.rlError);
            this.email = view.findViewById(R.id.email);
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