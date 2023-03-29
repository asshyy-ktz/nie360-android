package com.chroma.app.nie360.Activities;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.chroma.app.nie360.Adapters.AdapterBackgrounds;
import com.chroma.app.nie360.Adapters.SpinnerArrayAdapter;
import com.chroma.app.nie360.BaseClasses.BaseActivity;
import com.chroma.app.nie360.Models.EventsResponse;
import com.chroma.app.nie360.NetworkClasses.RetrofitClass;
import com.chroma.app.nie360.R;
import com.chroma.app.nie360.Utils.Constants;
import com.chroma.app.nie360.Utils.CustomEditText;
import com.chroma.app.nie360.Utils.CustomTextView;
import com.chroma.app.nie360.Utils.TinyDB;
import com.codekidlabs.storagechooser.StorageChooser;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import retrofit2.Callback;
import retrofit2.Response;

public class AdminPenal extends BaseActivity {
    ImageView ivBack, ivSave;
    TinyDB tinyDB;
    RadioButton rbMute, rbForeground, rbBackground;
    AppCompatSpinner spinner;
    CustomTextView tvPendingUploads;
    ProgressDialog progressDialog;
    String eventId = "";
    private final static int IMAGE_PICKER_SELECT = 5505;
    private static final int PERMISSION_REQUEST_CODE = 1;
    int position = 0;
    ImageView videoView2, videoView3, videoView4, videoView5, readyImgView;
    ImageView videoView1;
    StorageChooser chooser, chooser1;
    VideoView readyVideoView;
    RelativeLayout readyScreen;
    String p1 = Manifest.permission.CAMERA, p2 = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    CustomEditText etTime;
    String SelectedEventName = "";
    String SelectedEventId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();

        //initializing screen objects with IDs
        init();

        //implementing click listeners
        clickListeners();
    }

    private void init() {
        tinyDB = new TinyDB(AdminPenal.this);

        videoView1 = findViewById(R.id.videoView1);
        videoView2 = findViewById(R.id.videoView2);
        videoView3 = findViewById(R.id.videoView3);
        videoView4 = findViewById(R.id.videoView4);
        videoView5 = findViewById(R.id.videoView5);
        readyScreen = findViewById(R.id.readyScreen);
        readyImgView = findViewById(R.id.readyScreenImgView);
        readyVideoView = findViewById(R.id.readyScreenVideoView);

        readyVideoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Log.e("ERROR", "media player issue: " + mp.toString());

                return true;
            }
        });

        if (!(tinyDB.getString(Constants.SplashBackgroundType).equals(""))) {
            if (tinyDB.getString(Constants.SplashBackgroundType).equalsIgnoreCase("image")) {
                readyVideoView.setVisibility(View.GONE);
                readyImgView.setVisibility(View.VISIBLE);
                Picasso.get().load(tinyDB.getString(Constants.SplashBackground)).into(readyImgView);

            } else {
                readyImgView.setVisibility(View.GONE);
                readyVideoView.setVisibility(View.VISIBLE);
                try {
                    Log.d("URI", "URI IS:" + tinyDB.getString(Constants.SplashBackground));
                    readyVideoView.setVideoURI(Uri.parse(tinyDB.getString(Constants.SplashBackground)));
                    readyVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            readyVideoView.start();
                            mp.setLooping(true);
                            mp.setVolume(0, 0);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        // chooser is a file picker library which gives physical paths of files.
        chooser = new StorageChooser.Builder().filter(StorageChooser.FileType.VIDEO)
                .withActivity(AdminPenal.this)
                .withFragmentManager(getFragmentManager())
                .withMemoryBar(true)
                .allowCustomPath(true)
                .setType(StorageChooser.FILE_PICKER)
                .build();


        chooser.setOnSelectListener(new StorageChooser.OnSelectListener() {
            @Override
            public void onSelect(String path) {
                Log.e("SELECTED_PATH is: ", path);
                if (position == 1) {
                    tinyDB.putString(Constants.Background1, path + "");

                    Bitmap thumb = ThumbnailUtils.createVideoThumbnail(path,
                            MediaStore.Images.Thumbnails.MINI_KIND);
                    BitmapDrawable bitmapDrawable = new BitmapDrawable(thumb);
                    videoView1.setBackgroundDrawable(bitmapDrawable);
                } else if (position == 2) {
                    tinyDB.putString(Constants.Background2, path + "");

                    Bitmap thumb = ThumbnailUtils.createVideoThumbnail(path,
                            MediaStore.Images.Thumbnails.MINI_KIND);
                    BitmapDrawable bitmapDrawable = new BitmapDrawable(thumb);
                    videoView2.setBackgroundDrawable(bitmapDrawable);
                } else if (position == 3) {
                    tinyDB.putString(Constants.Background3, path + "");

                    Bitmap thumb = ThumbnailUtils.createVideoThumbnail(path,
                            MediaStore.Images.Thumbnails.MINI_KIND);
                    BitmapDrawable bitmapDrawable = new BitmapDrawable(thumb);
                    videoView3.setBackgroundDrawable(bitmapDrawable);
                } else if (position == 4) {
                    tinyDB.putString(Constants.Background4, path + "");

                    Bitmap thumb = ThumbnailUtils.createVideoThumbnail(path,
                            MediaStore.Images.Thumbnails.MINI_KIND);
                    BitmapDrawable bitmapDrawable = new BitmapDrawable(thumb);
                    videoView4.setBackgroundDrawable(bitmapDrawable);
                } else if (position == 5) {
                    tinyDB.putString(Constants.Background5, path + "");

                    Bitmap thumb = ThumbnailUtils.createVideoThumbnail(path,
                            MediaStore.Images.Thumbnails.MINI_KIND);
                    BitmapDrawable bitmapDrawable = new BitmapDrawable(thumb);
                    videoView5.setBackgroundDrawable(bitmapDrawable);
                }
            }
        });

        ivBack = findViewById(R.id.ivBack);
        ivSave = findViewById(R.id.ivSave);
        tvPendingUploads = findViewById(R.id.tvPendingUploads);

        rbMute = findViewById(R.id.rbMute);
        rbForeground = findViewById(R.id.rbForeground);
        rbBackground = findViewById(R.id.rbBackground);

        etTime = findViewById(R.id.etTime);
        videoView1 = findViewById(R.id.videoView1);
        videoView2 = findViewById(R.id.videoView2);
        videoView3 = findViewById(R.id.videoView3);
        videoView4 = findViewById(R.id.videoView4);
        videoView5 = findViewById(R.id.videoView5);

        if (!tinyDB.getString(Constants.VideoRecordingLength).isEmpty()) {
            etTime.setText(tinyDB.getString(Constants.VideoRecordingLength) + "");
            etTime.setSelection(etTime.getText().length());
        }

        if (!tinyDB.getString(Constants.Background1).equals("")) {
            try {
                Bitmap thumb = ThumbnailUtils.createVideoThumbnail(tinyDB.getString(Constants.Background1),
                        MediaStore.Images.Thumbnails.MINI_KIND);
                BitmapDrawable bitmapDrawable = new BitmapDrawable(thumb);
                videoView1.setBackgroundDrawable(null);
                videoView1.setBackgroundDrawable(bitmapDrawable);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (!tinyDB.getString(Constants.Background2).equals("")) {
            try {
                Bitmap thumb = ThumbnailUtils.createVideoThumbnail(tinyDB.getString(Constants.Background2),
                        MediaStore.Images.Thumbnails.MINI_KIND);
                BitmapDrawable bitmapDrawable = new BitmapDrawable(thumb);
                videoView2.setBackgroundDrawable(null);
                videoView2.setBackgroundDrawable(bitmapDrawable);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
//
        if (!tinyDB.getString(Constants.Background3).equals("")) {
            try {
                Bitmap thumb = ThumbnailUtils.createVideoThumbnail(tinyDB.getString(Constants.Background3),
                        MediaStore.Images.Thumbnails.MINI_KIND);
                BitmapDrawable bitmapDrawable = new BitmapDrawable(thumb);
                videoView3.setBackgroundDrawable(null);
                videoView3.setBackgroundDrawable(bitmapDrawable);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
//
        if (!tinyDB.getString(Constants.Background4).equals("")) {
            try {
                Bitmap thumb = ThumbnailUtils.createVideoThumbnail(tinyDB.getString(Constants.Background4),
                        MediaStore.Images.Thumbnails.MINI_KIND);
                BitmapDrawable bitmapDrawable = new BitmapDrawable(thumb);
                videoView4.setBackgroundDrawable(null);
                videoView4.setBackgroundDrawable(bitmapDrawable);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (!tinyDB.getString(Constants.Background5).equals("")) {
            try {
                Bitmap thumb = ThumbnailUtils.createVideoThumbnail(tinyDB.getString(Constants.Background5),
                        MediaStore.Images.Thumbnails.MINI_KIND);
                BitmapDrawable bitmapDrawable = new BitmapDrawable(thumb);
                videoView5.setBackgroundDrawable(null);
                videoView5.setBackgroundDrawable(bitmapDrawable);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        spinner = findViewById(R.id.spinner);

        //fetching events from server.
        getEvents("nie360@nashimaging.com", "ny0VQ2xaaDSEGDwRcPIfTFSoOj13TW3M");

        if (tinyDB.getString(Constants.AudioType).isEmpty()) {
            rbBackground.setChecked(true);
            rbForeground.setChecked(false);
            rbMute.setChecked(false);
        } else if (tinyDB.getString(Constants.AudioType).equals(Constants.ForegroundAudio)) {
            rbBackground.setChecked(false);
            rbForeground.setChecked(true);
            rbMute.setChecked(false);
        } else if (tinyDB.getString(Constants.AudioType).equals(Constants.BackgroundAudio)) {
            rbBackground.setChecked(true);
            rbForeground.setChecked(false);
            rbMute.setChecked(false);
        } else {
            rbBackground.setChecked(false);
            rbForeground.setChecked(false);
            rbMute.setChecked(true);
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

                Log.e("timeLength", etTime.getText() + "");

                if (etTime.getText().toString().isEmpty()) {
                    Toast.makeText(AdminPenal.this, "Please enter video recording length", Toast.LENGTH_SHORT).show();
                } else {
                    if (Integer.parseInt(etTime.getText().toString()) > 120 || Integer.parseInt(etTime.getText().toString()) < 10) {
                        Toast.makeText(AdminPenal.this, "Video recording length must be between 10 to 120 seconds", Toast.LENGTH_LONG).show();
                    } else {
                        tinyDB.putString(Constants.VideoRecordingLength, etTime.getText().toString());
                        if (SelectedEventId.isEmpty()) {
                            Toast.makeText(AdminPenal.this, "Please an event", Toast.LENGTH_SHORT).show();
                        } else {
                            finish();
                        }
                    }
                }
            }
        });
        tvPendingUploads.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(AdminPenal.this, PendingVideos.class);
                startActivity(i);
            }
        });

        videoView1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                position = 1;
                permissionAccessVideo();
            }
        });

        videoView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                position = 2;
                permissionAccessVideo();
            }
        });

        videoView3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                position = 3;
                permissionAccessVideo();
            }
        });

        videoView4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                position = 4;
                permissionAccessVideo();
            }
        });

        videoView5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                position = 5;
                permissionAccessVideo();
            }
        });
        readyScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                new AlertDialog.Builder(AdminPenal.this)
                        .setTitle("Choose Media Type")
                        .setMessage("Please select any Media Type here.")
                        .setPositiveButton("Image", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //image
                                permissionAccess();

                            }
                        }).setNegativeButton("Video", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //video

                        permissionAccessReadyVideo();

                    }
                }).show();
            }
        });

        rbForeground.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    tinyDB.putString(Constants.AudioType, Constants.ForegroundAudio);
                    rbBackground.setChecked(false);
                    rbMute.setChecked(false);
                }
            }
        });
        rbBackground.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    tinyDB.putString(Constants.AudioType, Constants.BackgroundAudio);
                    rbForeground.setChecked(false);
                    rbMute.setChecked(false);
                }
            }
        });
        rbMute.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    tinyDB.putString(Constants.AudioType, Constants.MuteAudio);
                    rbBackground.setChecked(false);
                    rbForeground.setChecked(false);
                }
            }
        });
    }

    private void permissionAccess() {
        if (!checkPermission(p1)) {
            Log.e("TAG", p1);
            requestPermission(p1);
        } else if (!checkPermission(p2)) {
            Log.e("TAG", p2);
            requestPermission(p2);
        } else {
            Intent pickIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickIntent.setType("image/*");
            startActivityForResult(pickIntent, IMAGE_PICKER_SELECT);
//            Toast.makeText(CreateJob.this, "All permission granted", Toast.LENGTH_LONG).show();
        }
    }

    private void permissionAccessVideo() {
        if (!checkPermission(p1)) {
            Log.e("TAG", p1);
            requestPermission(p1);
        } else if (!checkPermission(p2)) {
            Log.e("TAG", p2);
            requestPermission(p2);
        } else {
            chooser.show();
        }
    }

    private void permissionAccessReadyVideo() {
        if (!checkPermission(p1)) {
            Log.e("TAG", p1);
            requestPermission(p1);
        } else if (!checkPermission(p2)) {
            Log.e("TAG", p2);
            requestPermission(p2);
        } else {
            Intent pickIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickIntent.setType("video/*");
            startActivityForResult(pickIntent, IMAGE_PICKER_SELECT);
        }
    }

    public void getEvents(final String admin_email, final String key) {
        progressDialog = createProgressDialog(AdminPenal.this, "");

        retrofit2.Call<EventsResponse> call = RetrofitClass.getInstance().getWebRequestsInstance().getEvents(admin_email, key);
        call.enqueue(new Callback<EventsResponse>() {
            @Override
            public void onResponse(retrofit2.Call<EventsResponse> call, final Response<EventsResponse> response) {
                progressDialog.dismiss();

                if (response.body().getStatus().equalsIgnoreCase("ok")) {
                    SpinnerArrayAdapter adapter = new SpinnerArrayAdapter(AdminPenal.this,
                            R.layout.custom_spinner_item, response.body().getData());

                    spinner.setAdapter(adapter);

                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                            eventId = response.body().getData().get(pos).getId() + "";
                            findViewById(R.id.tvHeading).setVisibility(View.GONE);
                            spinner.setVisibility(View.VISIBLE);
                            SelectedEventId = response.body().getData().get(pos).getId() + "";
                            SelectedEventName = response.body().getData().get(pos).getEventName() + "";
                            tinyDB.putString(Constants.SelectedEventId, response.body().getData().get(pos).getId() + "");
                            tinyDB.putString(Constants.SelectedEventName, response.body().getData().get(pos).getEventName() + "");
                            tinyDB.putString(Constants.SelectedEventPosition, pos + "");
                        }

                        public void onNothingSelected(AdapterView<?> parent) {
                            findViewById(R.id.tvHeading).setVisibility(View.VISIBLE);
                            spinner.setVisibility(View.GONE);
                            SelectedEventId = "";
                            SelectedEventName = "";
                            tinyDB.putString(Constants.SelectedEventId, "");
                            tinyDB.putString(Constants.SelectedEventName, "");
                            tinyDB.putString(Constants.SelectedEventPosition, "");
                        }
                    });

                    if ((!tinyDB.getString(Constants.SelectedEventName).isEmpty()) && (!tinyDB.getString(Constants.SelectedEventId).isEmpty()) && (!tinyDB.getString(Constants.SelectedEventPosition).isEmpty())) {
                        spinner.setSelection(Integer.parseInt(tinyDB.getString(Constants.SelectedEventPosition)));

                    }
                }
            }

            @Override
            public void onFailure(retrofit2.Call<EventsResponse> call, Throwable t) {
                t.printStackTrace();
                progressDialog.dismiss();
            }
        });
    }

    private boolean checkPermission(String permission) {
        int result = ContextCompat.checkSelfPermission(AdminPenal.this, permission);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    private void requestPermission(String permission) {

        if (ContextCompat.checkSelfPermission(AdminPenal.this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(AdminPenal.this, new String[]{permission}, PERMISSION_REQUEST_CODE);
        } else {
            //Do the stuff that requires permission...
            Log.e("TAG", "Not say request");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                Log.e("TAG", "val " + grantResults[0]);
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    permissionAccessVideo();
                } else {
                    Toast.makeText(AdminPenal.this, "The app was not allowed permission. Hence, it cannot function properly. Please consider granting it this permission.", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {
            Uri selectedMediaUri = data.getData();


            if (selectedMediaUri.toString().contains("image")) {
                //handle image
                tinyDB.putString(Constants.SplashBackground, selectedMediaUri + "");
                tinyDB.putString(Constants.SplashBackgroundType, "image");
                readyVideoView.setVisibility(View.GONE);
                readyImgView.setVisibility(View.VISIBLE);
                Picasso.get().load(selectedMediaUri).into(readyImgView);

            } else if (selectedMediaUri.toString().contains("video")) {
                //handle video
                try {
                    tinyDB.putString(Constants.SplashBackground, selectedMediaUri + "");
                    tinyDB.putString(Constants.SplashBackgroundType, "video");

                    readyImgView.setVisibility(View.GONE);

                    readyVideoView.setVisibility(View.VISIBLE);
                    readyVideoView.setVideoURI(selectedMediaUri);
                    readyVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            readyVideoView.start();
                            mp.setLooping(true);
                            mp.setVolume(0, 0);
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
