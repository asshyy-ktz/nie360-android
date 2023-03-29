package com.chroma.app.nie360.Activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.chroma.app.nie360.Adapters.AdapterPendingVideos;
import com.chroma.app.nie360.Adapters.AdapterProcessingVideos;
import com.chroma.app.nie360.BaseClasses.BaseActivity;
import com.chroma.app.nie360.Models.ProcessingQueueModel;
import com.chroma.app.nie360.Models.UploadingQueueModel;
import com.chroma.app.nie360.NetworkClasses.RetrofitClass;
import com.chroma.app.nie360.R;
import com.chroma.app.nie360.Utils.Constants;
import com.chroma.app.nie360.Utils.TinyDB;

import java.io.File;
import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Callback;
import retrofit2.Response;

import static com.chroma.app.nie360.Utils.ProcessingService.BROADCAST_ACTION;

public class ProcessingActivity extends BaseActivity {
    ImageView ivBack, ivSave;
    TinyDB tinyDB;
    RecyclerView rvProcessingVideos;
    LinearLayoutManager manager;
    AdapterProcessingVideos adapterProcessingVideos;
    ArrayList<ProcessingQueueModel> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_processing);
        getSupportActionBar().hide();

        init();
        clickListeners();
    }
    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver, new IntentFilter(
                BROADCAST_ACTION));
    }
    private void init() {
        tinyDB = new TinyDB(ProcessingActivity.this);

        ivBack = findViewById(R.id.ivBack);
        ivSave = findViewById(R.id.ivSave);

        list = new ArrayList<>();

        list = tinyDB.getProcessingListObject(Constants.ProcessingList, ProcessingQueueModel.class);

        rvProcessingVideos = findViewById(R.id.rvProcessingVideos);
        manager = new LinearLayoutManager(ProcessingActivity.this);
        if (list.size() > 0) {
            findViewById(R.id.tvNoText).setVisibility(View.GONE);
            rvProcessingVideos.setVisibility(View.VISIBLE);
            adapterProcessingVideos = new AdapterProcessingVideos(list, ProcessingActivity.this);
            rvProcessingVideos.setLayoutManager(manager);
            rvProcessingVideos.setAdapter(adapterProcessingVideos);
        } else {
            findViewById(R.id.tvNoText).setVisibility(View.VISIBLE);
            rvProcessingVideos.setVisibility(View.GONE);
        }


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

                Intent i = new Intent(ProcessingActivity.this, PendingVideos.class);
                startActivity(i);

            }
        });
    }

    public static boolean isValidEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("CALLED", "Intent passed");
            list = tinyDB.getProcessingListObject(Constants.ProcessingList, ProcessingQueueModel.class);
            adapterProcessingVideos = new AdapterProcessingVideos(list, ProcessingActivity.this);
            rvProcessingVideos.setLayoutManager(manager);
            rvProcessingVideos.setAdapter(adapterProcessingVideos);


        }
    };


    @Override
    protected void onStop()
    {
        super.onStop();

    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            if (intent != null) {
                String message = intent.getStringExtra("message");
                Log.e("receiver", "Got message: " + message);
                if (adapterProcessingVideos != null) {
                    adapterProcessingVideos.setItems(tinyDB.getProcessingListObject(Constants.ProcessingList, ProcessingQueueModel.class));
                    adapterProcessingVideos.notifyDataSetChanged();
                } else {
                    rvProcessingVideos = findViewById(R.id.rvProcessingVideos);
                    manager = new LinearLayoutManager(ProcessingActivity.this);
                    adapterProcessingVideos = new AdapterProcessingVideos(tinyDB.getProcessingListObject(Constants.ProcessingList, ProcessingQueueModel.class), ProcessingActivity.this);
                    rvProcessingVideos.setLayoutManager(manager);
                    rvProcessingVideos.setAdapter(adapterProcessingVideos);
                }
            }
        }
    };

    @Override
    public void onDestroy() {
        unregisterReceiver(broadcastReceiver);

        super.onDestroy();
    }
}