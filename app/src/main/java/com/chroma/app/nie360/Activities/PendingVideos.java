package com.chroma.app.nie360.Activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.chroma.app.nie360.Adapters.AdapterPendingVideos;
import com.chroma.app.nie360.Adapters.AdapterProcessingVideos;
import com.chroma.app.nie360.BaseClasses.BaseActivity;
import com.chroma.app.nie360.Models.ProcessingQueueModel;
import com.chroma.app.nie360.Models.UploadingQueueModel;
import com.chroma.app.nie360.R;
import com.chroma.app.nie360.Utils.Constants;
import com.chroma.app.nie360.Utils.TinyDB;

import java.util.ArrayList;

import static com.chroma.app.nie360.Utils.ProcessingService.BROADCAST_ACTION;

public class PendingVideos extends BaseActivity {
    ImageView ivBack, ivSave;
    TinyDB tinyDB;
    RecyclerView rvPendingVideos;
    LinearLayoutManager manager;
    AdapterPendingVideos adapterPendingVideos;
    ArrayList<UploadingQueueModel> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pending_videos);
        getSupportActionBar().hide();

        init();
        clickListeners();
    }

    private void init() {
        tinyDB = new TinyDB(PendingVideos.this);

        ivBack = findViewById(R.id.ivBack);
        ivSave = findViewById(R.id.ivSave);

        list = new ArrayList<>();

        list = tinyDB.getUploadingListObject(Constants.UploadingList,UploadingQueueModel.class);


        rvPendingVideos = findViewById(R.id.rvPendingVideos);

        manager = new LinearLayoutManager(PendingVideos.this);
        adapterPendingVideos = new AdapterPendingVideos(list, PendingVideos.this);
        rvPendingVideos.setLayoutManager(manager);
        rvPendingVideos.setAdapter(adapterPendingVideos);

    }
    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver, new IntentFilter(
                BROADCAST_ACTION));
    }
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("CALLED", "Intent passed");
            list = tinyDB.getUploadingListObject(Constants.UploadingList, UploadingQueueModel.class);
            adapterPendingVideos = new AdapterPendingVideos(list, PendingVideos.this);
         int  position = intent.getIntExtra(Constants.position, -1);
            rvPendingVideos.setLayoutManager(manager);
            rvPendingVideos.setAdapter(adapterPendingVideos);


        }
    };
    @Override
    public void onDestroy() {
        unregisterReceiver(broadcastReceiver);

        super.onDestroy();
    }
    private void clickListeners() {
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        ivSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }
}