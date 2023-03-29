package com.chroma.app.nie360.Utils;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.arthenica.mobileffmpeg.FFmpeg;
import com.chroma.app.nie360.Activities.CaptureVideoScreen;
import com.chroma.app.nie360.Activities.PerviewActivity;
import com.chroma.app.nie360.Models.ProcessingQueueModel;
import com.chroma.app.nie360.Models.UploadingQueueModel;
import com.chroma.app.nie360.NetworkClasses.RetrofitClass;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Callback;
import retrofit2.Response;

import static com.arthenica.mobileffmpeg.FFmpeg.RETURN_CODE_CANCEL;
import static com.arthenica.mobileffmpeg.FFmpeg.RETURN_CODE_SUCCESS;

// Service to form Queue of videos processing in background.

public class ProcessingService extends Service {

    private String first, s, STATUS;
    int position = -1;
    public static final String
            BROADCAST_ACTION = "com.chroma.service";
    public File outputPath;
    public static String timeStamp = "";
    Intent newintent;
    ProcessingQueueModel model;
    TinyDB tinyDB;


    @SuppressLint("WrongConstant")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            first = intent.getStringExtra(Constants.Video1Path);

            s = intent.getStringExtra(Constants.Video2Path);
            STATUS = intent.getStringExtra(Constants.ProcessingStatus);
            position = intent.getIntExtra(Constants.position, -1);
            model = (ProcessingQueueModel) intent.getSerializableExtra("ProcessingModel");


        }
        newintent = new Intent(BROADCAST_ACTION);


        new LongOperation(ProcessingService.this).execute(first, s, STATUS);
        return Service.START_FLAG_REDELIVERY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private class LongOperation extends AsyncTask<String, Integer, String> {
        ProcessingService context;
        ArrayList<ProcessingQueueModel> list = new ArrayList<>();

        public LongOperation(ProcessingService context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            tinyDB = new TinyDB(context);
            list = tinyDB.getProcessingListObject(Constants.ProcessingList, ProcessingQueueModel.class);

        }

        @Override
        protected String doInBackground(String... params) {
            try {
                outputPath = new File(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES), "NIE360/outputs");

                if (!outputPath.exists()) {
                    if (!outputPath.mkdirs()) {

                    }
                }
                timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                Log.e("PList: size", (tinyDB.getProcessingListObject(Constants.ProcessingList, ProcessingQueueModel.class).size()) + "");

                // making query of ffmpeg library. which remove selected color from foreground video and marge with background video.

                final String query;
                if (tinyDB.getString(Constants.AudioType).equals(Constants.BackgroundAudio)) {
                    query = "-y -i " + first + " -i " + s + " -c:v libx264 -preset ultrafast -tune fastdecode -filter_complex [0:v]colorkey=0x" + tinyDB.getString(Constants.backgroundGreenColor) + ":0.3:0.15[ckout];[1:v][ckout]overlay[despill];[despill]despill=green[out] -map [out] -map 1:a -b 3200000 -pix_fmt yuvj420p -c:a libvo_aacenc -s 640:360 -c:a copy " + outputPath + "/" + timeStamp + ".mp4";
                } else if (tinyDB.getString(Constants.AudioType).equals(Constants.ForegroundAudio)) {
                    query = "-y -i " + first + " -i " + s + " -c:v libx264 -preset ultrafast -tune fastdecode -filter_complex [0:v]colorkey=0x" + tinyDB.getString(Constants.backgroundGreenColor) + ":0.3:0.15[ckout];[1:v][ckout]overlay[despill];[despill]despill=green[out] -map [out] -map 0:a -b 3200000 -pix_fmt yuvj420p -c:a libvo_aacenc -s 640:360 -c:a copy " + outputPath + "/" + timeStamp + ".mp4";
                } else {
                    query = "-y -i " + first + " -i " + s + " -c:v libx264 -preset ultrafast -tune fastdecode -filter_complex [0:v]colorkey=0x" + tinyDB.getString(Constants.backgroundGreenColor) + ":0.3:0.15[ckout];[1:v][ckout]overlay[despill];[despill]despill=green[out] -map [out] -pix_fmt yuvj420p -c:a libvo_aacenc " + outputPath + "/" + timeStamp + ".mp4";
                }

                Log.e("query", query);

                Log.e("PList: size", list.size() + "");

                // executing ffmpeg query
                FFmpeg.execute(query);
                int rc = FFmpeg.getLastReturnCode();
                String output = FFmpeg.getLastCommandOutput();
                Log.e("position", position + "");
                if (rc == RETURN_CODE_SUCCESS) {
                    Log.e("Status", "Command execution completed successfully.");
                    list = tinyDB.getProcessingListObject(Constants.ProcessingList, ProcessingQueueModel.class);
                    if (position < list.size()) {
                        list.get(position).setStatus(Constants.StatusCompleted);
                        Log.e("positionStatus", list.get(position).getStatus() + "");
                        list.get(position).setOutputPath((outputPath + "/" + timeStamp + ".mp4") + "");
                        newintent.putExtra("position", position);
                        list.get(position).setEmail(model.getEmail());
                        list.get(position).setPhone(model.getPhone());
                        tinyDB.putProcessingListObject(Constants.ProcessingList, list);
                        sendBroadcast(newintent);
                        sendVideo(list.get(position).getEmail(), list.get(position).getPhone(), list.get(position).getEventName(), list.get(position).getOutputPath(), context);

                    }

                } else if (rc == RETURN_CODE_CANCEL) {

                    Log.e("Status", "Command execution cancelled by user.");
                } else {
                    Log.e("Status", "Command execution failed with rc=" + rc + " and output=" + output);
                    list.get(position).setStatus(Constants.StatusFinished);
                    newintent.putExtra("position", position);
                    tinyDB.putProcessingListObject(Constants.ProcessingList, list);
                    sendBroadcast(newintent);

                }
                Log.e("qury", query + "");

            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            Boolean pendingProcessing = false;
            tinyDB = new TinyDB(context);
            list = tinyDB.getProcessingListObject(Constants.ProcessingList, ProcessingQueueModel.class);
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).getStatus().equals(Constants.StatusAdded)) {
                    pendingProcessing = true;
                    position = i;
                    break;
                }
            }
            if (pendingProcessing) {
                first = list.get(position).getVideo1();
                s = list.get(position).getVideo2();
                new LongOperation(ProcessingService.this).execute(first, s, STATUS);
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

        }
    }

    public void sendVideo(final String emails, final String ph, final String eventName, final String videoUrl, final Context ctx) {


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
                UploadingQueueModel model = new UploadingQueueModel();
                model.setEmail(emails);
                model.setPhone(ph);
                model.setPath(videoUrl);
                model.setEventName(eventName);
                model.setStatus("Success");
                model.setPosition(tinyDB.getUploadingListObject(Constants.UploadingList, UploadingQueueModel.class).size() - 1);
                ArrayList<UploadingQueueModel> modelList = new ArrayList<>();
                modelList.add(model);

                ArrayList<UploadingQueueModel> list = tinyDB.getUploadingListObject(Constants.UploadingList, UploadingQueueModel.class);
                list.addAll(modelList);
                tinyDB.putUploadingListObject(Constants.UploadingList, list);


            }

            @Override
            public void onFailure(retrofit2.Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();

                Log.e("error response", t.getMessage());

                UploadingQueueModel model = new UploadingQueueModel();
                model.setEmail(emails);
                model.setPhone(ph);
                model.setPath(videoUrl);
                model.setEventName(eventName);
                model.setStatus("Failed");
                model.setPosition(tinyDB.getUploadingListObject(Constants.UploadingList, UploadingQueueModel.class).size() - 1);


                ArrayList<UploadingQueueModel> modelList = new ArrayList<>();
                modelList.add(model);

                ArrayList<UploadingQueueModel> list = tinyDB.getUploadingListObject(Constants.UploadingList, UploadingQueueModel.class);
                list.addAll(modelList);
                tinyDB.putUploadingListObject(Constants.UploadingList, list);


                Log.d("SIZE", "List size is:" + list.size());

            }
        });
    }
}