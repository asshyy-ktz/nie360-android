package com.chroma.app.nie360.BaseClasses;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.chroma.app.nie360.Models.ProcessingQueueModel;
import com.chroma.app.nie360.Models.UploadingQueueModel;
import com.chroma.app.nie360.R;
import com.chroma.app.nie360.Utils.Constants;
import com.chroma.app.nie360.Utils.CustomTextView;
import com.chroma.app.nie360.Utils.TinyDB;

import java.util.ArrayList;


public class BaseActivity extends AppCompatActivity {
    public long mLastClickTime = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    public void addFragmentWithBackstack(int containerId, Fragment fragment, String tag) {
        getSupportFragmentManager().beginTransaction().
                add(containerId, fragment, tag)
                .addToBackStack(tag).commit();
    }

    public void addFragment(int containerId, Fragment fragment, String tag) {
        getSupportFragmentManager().beginTransaction()
                .add(containerId, fragment, tag).commit();
    }

    public void replaceFragmentWithBackstack(int containerId, Fragment fragment, String tag) {
        getSupportFragmentManager().beginTransaction().
                replace(containerId, fragment, tag)
                .addToBackStack(tag).commit();
    }

    public void replaceFragment(int containerId, Fragment fragment, String tag) {
        getSupportFragmentManager().beginTransaction()
                .replace(containerId, fragment, tag).commit();
    }

    public Boolean isConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            //we are connected to a network
            return true;
        } else {
            return false;
        }
    }


    public static ProgressDialog createProgressDialog(Context context, String message) {
        CustomTextView text;
        ImageView close;
        final ProgressDialog dialog = new ProgressDialog(context);
        try {
            dialog.show();
        } catch (WindowManager.BadTokenException e) {

        }
        dialog.setCancelable(false);
        dialog.getWindow()
                .setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.progress_dialog);

//         dialog.setMessage("Fetching Events...");
        text = dialog.findViewById(R.id.text);
        close = dialog.findViewById(R.id.cancle);
        text.setText("Fetching Events...");
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.hide();
            }
        });
        return dialog;
    }
}
