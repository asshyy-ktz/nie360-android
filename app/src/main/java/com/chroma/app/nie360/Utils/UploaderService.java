package com.chroma.app.nie360.Utils;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.view.View;

import com.chroma.app.nie360.Models.UploadingQueueModel;
import com.chroma.app.nie360.NetworkClasses.RetrofitClass;

import java.io.File;
import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Callback;
import retrofit2.Response;

// Service to form Queue of videos after processing of video.
public class UploaderService extends Service {


    UploadingQueueModel model;
    TinyDB tinydb;
    ArrayList<UploadingQueueModel> list;
    int position = -1;
    public static final String
            BROADCAST_ACTION = "com.chroma.service";
    Intent newintent;
    UploaderService context;


    @SuppressLint("WrongConstant")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            model = (UploadingQueueModel) intent.getSerializableExtra("UploadingModel");

        }

        newintent = new Intent(BROADCAST_ACTION);

        tinydb = new TinyDB(getApplicationContext());


        new UploadFile(UploaderService.this).execute();
        // TBD
        return Service.START_FLAG_REDELIVERY;       //        return Service.START_FLAG_REDELIVERY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private class UploadFile extends AsyncTask<String, Integer, String> {

        public UploadFile(UploaderService activity) {
        }

        @Override
        protected String doInBackground(String... url) {
            list.get(model.getPosition()).setStatus("Processing");
            tinydb.putUploadingListObject(Constants.UploadingList, list);

            sendVideo(model.getEmail(), model.getPhone(), model.getEventName(), model.getPath(), model.getPosition());
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            list = tinydb.getUploadingListObject(Constants.UploadingList, UploadingQueueModel.class);

        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

        }

        @Override
        protected void onCancelled(String s) {
            super.onCancelled(s);
//            dialog.dismiss();
            Log.e("progress", "Canceled");
        }
    }

    public void sendVideo(final String emails, final String ph, final String eventName, final String videoUrl, final int pos) {


        RequestBody admin_email, key, media_type, email, phone, event_name;


        admin_email = RequestBody.create(MediaType.parse("multipart/form-data"), "nie360@nashimaging.com");
        key = RequestBody.create(MediaType.parse("multipart/form-data"), "ny0VQ2xaaDSEGDwRcPIfTFSoOj13TW3M");
        media_type = RequestBody.create(MediaType.parse("multipart/form-data"), "video");

        email = RequestBody.create(MediaType.parse("multipart/form-data"), emails);
        phone = RequestBody.create(MediaType.parse("multipart/form-data"), ph);
        event_name = RequestBody.create(MediaType.parse("multipart/form-data"), eventName);


        MultipartBody.Part videoParts;

        File file1 = new File(String.valueOf(videoUrl));

        videoParts = MultipartBody.Part.createFormData("media", file1.getName(), RequestBody.create(MediaType.parse("video/*"), file1));

        retrofit2.Call<ResponseBody> call = RetrofitClass.getInstance().getWebRequestsInstance().sendVideo(admin_email, event_name, key, media_type, videoParts, email, phone);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(retrofit2.Call<ResponseBody> call, final Response<ResponseBody> response) {

                Log.e("response", response.toString());

                list.get(pos).setStatus("Success");
                tinydb.putUploadingListObject(Constants.UploadingList, list);
                newintent.putExtra("pos", pos);
                sendBroadcast(newintent);

            }

            @Override
            public void onFailure(retrofit2.Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();

                Log.e("error response", t.getMessage());
                list.get(pos).setStatus("Failed");
                tinydb.putUploadingListObject(Constants.UploadingList, list);
                newintent.putExtra("pos", pos);

                sendBroadcast(newintent);

            }
        });
    }
}