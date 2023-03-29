package com.chroma.app.nie360.Activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaMetadataRetriever;
import android.media.MediaRecorder;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.arthenica.mobileffmpeg.Config;
import com.arthenica.mobileffmpeg.FFmpeg;
import com.arthenica.mobileffmpeg.MediaInformation;
import com.chroma.app.nie360.BaseClasses.BaseActivity;
import com.chroma.app.nie360.Models.ProcessingQueueModel;
import com.chroma.app.nie360.R;
import com.chroma.app.nie360.Utils.CameraPreview;
import com.chroma.app.nie360.Utils.Constants;
import com.chroma.app.nie360.Utils.CustomTextView;
import com.chroma.app.nie360.Utils.ProcessingService;
import com.chroma.app.nie360.Utils.TinyDB;
import com.codekidlabs.storagechooser.StorageChooser;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.arthenica.mobileffmpeg.FFmpeg.RETURN_CODE_CANCEL;
import static com.arthenica.mobileffmpeg.FFmpeg.RETURN_CODE_SUCCESS;

public class CaptureVideoScreen extends BaseActivity implements View.OnClickListener {
    private Camera mCamera;
    private CameraPreview mPreview;
    private boolean isRecording = false;
    private MediaRecorder mediaRecorder;
    String TAG = "CaptureVideoScreen";
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    ImageView buttonConfirm, buttonCancel;
    ImageView captureButton;
    public File outputPath;
    String p1 = Manifest.permission.CAMERA, p2 = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    private final static int IMAGE_PICKER_SELECT = 5505;
    private static final int PERMISSION_REQUEST_CODE = 1;
    String first = null, s = null;
    public static String file_name = "";
    Button videoView;
    CustomTextView timer;
    CountDownTimer countDownTimer;
    Boolean recorded = false;
    public static String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    StorageChooser chooser;
    String path1 = null, path2 = null;
    ImageView ivAdminPenal;
    LinearLayout llPicker;
    TinyDB tinyDB;
    Boolean isVideoView1HaveVideo, isVideoView2HaveVideo, isVideoView3HaveVideo, isVideoView4HaveVideo, isVideoView5HaveVideo;
    CircleImageView videoView1, videoView2, videoView3, videoView4, videoView5;
    private static final int FOCUS_AREA_SIZE = 300;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_capture_video_screen);
        getSupportActionBar().hide();

        tinyDB = new TinyDB(CaptureVideoScreen.this);

        mCamera = getCameraInstance();
        mPreview = new CameraPreview(this, mCamera);
        timer = findViewById(R.id.timer);

        videoView = findViewById(R.id.videoView);
        ivAdminPenal = findViewById(R.id.ivAdminPenal);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);

        mPreview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    focusOnTouch(event);
                }
                return true;
            }
        });

        init();
        clickListeneres();
    }

    private void focusOnTouch(MotionEvent event) {
        try {
            if (mCamera != null) {
                Camera.Parameters parameters = mCamera.getParameters();
                if (parameters.getMaxNumMeteringAreas() > 0) {
                    Log.i(TAG, "fancy !");
                    Rect rect = calculateFocusArea(event.getX(), event.getY());

                    parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                    List<Camera.Area> meteringAreas = new ArrayList<Camera.Area>();
                    meteringAreas.add(new Camera.Area(rect, 800));
                    parameters.setFocusAreas(meteringAreas);

                    mCamera.setParameters(parameters);
                    mCamera.autoFocus(mAutoFocusTakePictureCallback);
                } else {
                    mCamera.autoFocus(mAutoFocusTakePictureCallback);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Rect calculateFocusArea(float x, float y) {
        int left = clamp(Float.valueOf((x / mPreview.getWidth()) * 2000 - 1000).intValue(), FOCUS_AREA_SIZE);
        int top = clamp(Float.valueOf((y / mPreview.getHeight()) * 2000 - 1000).intValue(), FOCUS_AREA_SIZE);

        return new Rect(left, top, left + FOCUS_AREA_SIZE, top + FOCUS_AREA_SIZE);
    }

    private int clamp(int touchCoordinateInCameraReper, int focusAreaSize) {
        int result;
        if (Math.abs(touchCoordinateInCameraReper) + focusAreaSize / 2 > 1000) {
            if (touchCoordinateInCameraReper > 0) {
                result = 1000 - focusAreaSize / 2;
            } else {
                result = -1000 + focusAreaSize / 2;
            }
        } else {
            result = touchCoordinateInCameraReper - focusAreaSize / 2;
        }
        return result;
    }

    private Camera.AutoFocusCallback mAutoFocusTakePictureCallback = new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            if (success) {
                // do something...
                Log.i("tap_to_focus", "success!");
            } else {
                // do something...
                Log.i("tap_to_focus", "fail!");
            }
        }
    };

    private void init() {
        chooser = new StorageChooser.Builder().filter(StorageChooser.FileType.VIDEO)
                .withActivity(CaptureVideoScreen.this)
                .withFragmentManager(getFragmentManager())
                .withMemoryBar(true)
                .allowCustomPath(true)
                .setType(StorageChooser.FILE_PICKER)
                .build();

        videoView1 = findViewById(R.id.videoView1);
        videoView2 = findViewById(R.id.videoView2);
        videoView3 = findViewById(R.id.videoView3);
        videoView4 = findViewById(R.id.videoView4);
        videoView5 = findViewById(R.id.videoView5);
        videoView1.setVisibility(View.VISIBLE);


        if (!tinyDB.getString(Constants.Background1).equals("")) {
            try {
                Bitmap thumb = ThumbnailUtils.createVideoThumbnail(tinyDB.getString(Constants.Background1),
                        MediaStore.Images.Thumbnails.MINI_KIND);
                BitmapDrawable bitmapDrawable = new BitmapDrawable(thumb);
                videoView1.setImageDrawable(bitmapDrawable);
                isVideoView1HaveVideo = true;

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            isVideoView1HaveVideo = false;
            videoView1.setImageResource(R.drawable.ic_launcher_background);
        }
        if (!tinyDB.getString(Constants.Background2).equals("")) {
            try {
                Bitmap thumb = ThumbnailUtils.createVideoThumbnail(tinyDB.getString(Constants.Background2),
                        MediaStore.Images.Thumbnails.MINI_KIND);
                BitmapDrawable bitmapDrawable = new BitmapDrawable(thumb);
                videoView2.setImageDrawable(bitmapDrawable);
                isVideoView2HaveVideo = true;


            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            isVideoView2HaveVideo = false;

        }
        if (!tinyDB.getString(Constants.Background3).equals("")) {
            try {
                Bitmap thumb = ThumbnailUtils.createVideoThumbnail(tinyDB.getString(Constants.Background3),
                        MediaStore.Images.Thumbnails.MINI_KIND);
                BitmapDrawable bitmapDrawable = new BitmapDrawable(thumb);
                videoView3.setImageDrawable(bitmapDrawable);
                isVideoView3HaveVideo = true;

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            isVideoView3HaveVideo = false;

        }
        if (!tinyDB.getString(Constants.Background4).equals("")) {
            try {
                Bitmap thumb = ThumbnailUtils.createVideoThumbnail(tinyDB.getString(Constants.Background4),
                        MediaStore.Images.Thumbnails.MINI_KIND);
                BitmapDrawable bitmapDrawable = new BitmapDrawable(thumb);
                videoView4.setImageDrawable(bitmapDrawable);
                isVideoView4HaveVideo = true;

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            isVideoView4HaveVideo = false;

        }
        if (!tinyDB.getString(Constants.Background5).equals("")) {
            try {
                Bitmap thumb = ThumbnailUtils.createVideoThumbnail(tinyDB.getString(Constants.Background5),
                        MediaStore.Images.Thumbnails.MINI_KIND);
                BitmapDrawable bitmapDrawable = new BitmapDrawable(thumb);
                videoView5.setImageDrawable(bitmapDrawable);
                isVideoView5HaveVideo = true;

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            isVideoView5HaveVideo = false;
        }
        buttonConfirm = findViewById(R.id.button_confirm);
        buttonCancel = findViewById(R.id.button_cancel);
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRecording) {
                    Toast.makeText(CaptureVideoScreen.this, "Please stop recording first!", Toast.LENGTH_SHORT).show();
                } else {
                    finish();
                }
            }
        });
        outputPath = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "NIE360/outputs");

        // Create the storage directory if it does not exist
        if (!outputPath.exists()) {
            if (!outputPath.mkdirs()) {

            }
        }
        videoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                permissionAccessVideo();
            }
        });
        chooser.setOnSelectListener(new StorageChooser.OnSelectListener() {
            @Override
            public void onSelect(String path) {
                Log.e("SELECTED_PATH", path);
                s = path;
            }
        });

        llPicker = findViewById(R.id.llPicker);
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

    private boolean checkPermission(String permission) {
        int result = ContextCompat.checkSelfPermission(CaptureVideoScreen.this, permission);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    private void requestPermission(String permission) {
        if (ContextCompat.checkSelfPermission(CaptureVideoScreen.this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(CaptureVideoScreen.this, new String[]{permission}, PERMISSION_REQUEST_CODE);
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
                    Toast.makeText(CaptureVideoScreen.this, "The app was not allowed permission. Hence, it cannot function properly. Please consider granting it this permission.", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    private static Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    public String generatePath(Uri uri, Context context) {
        String filePath = null;
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        if (isKitKat) {
            filePath = generateFromKitkat(uri, context);
        }
        if (filePath != null) {
            return filePath;
        }

        Cursor cursor = context.getContentResolver().query(uri, new String[]{MediaStore.MediaColumns.DATA}, null, null, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                filePath = cursor.getString(columnIndex);
            }
            cursor.close();
        }
        return filePath == null ? uri.getPath() : filePath;
    }

    @TargetApi(19)
    private String generateFromKitkat(Uri uri, Context context) {
        String filePath = null;
        if (DocumentsContract.isDocumentUri(context, uri)) {
            String wholeID = DocumentsContract.getDocumentId(uri);

            String id = wholeID.split(":")[1];

            String[] column = {MediaStore.Video.Media.DATA};
            String sel = MediaStore.Video.Media._ID + "=?";

            Cursor cursor = context.getContentResolver().
                    query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                            column, sel, new String[]{id}, null);


            int columnIndex = cursor.getColumnIndex(column[0]);

            if (cursor.moveToFirst()) {
                filePath = cursor.getString(columnIndex);
            }

            cursor.close();
        }
        return filePath;
    }

    private void clickListeneres() {
        captureButton = findViewById(R.id.button_capture);
        captureButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (tinyDB.getString(Constants.VideoRecordingLength).isEmpty()) {
                            Toast.makeText(CaptureVideoScreen.this, "Video recording length is not defined. Go to Admin Penal and save video recording length.", Toast.LENGTH_LONG).show();
                        } else {
                            if (isRecording) {
                                                         // stop recording and release camera
                                mediaRecorder.stop();  // stop the recording
                                releaseMediaRecorder(); // release the MediaRecorder object
                                mCamera.lock();         // take camera access back from MediaRecorder

                                // inform the user that recording has stopped
                                captureButton.setImageDrawable(getResources().getDrawable(R.drawable.start_stop));
                                buttonCancel.setImageDrawable(getResources().getDrawable(R.drawable.discard_true));
                                if (recorded) {
                                    buttonConfirm.setImageDrawable(getResources().getDrawable(R.drawable.ok));
                                }
                                isRecording = false;
                                countDownTimer.cancel();
                                first = getOutputMediaFileUri(MEDIA_TYPE_VIDEO).toString();
                                Log.e("foregroundPath", first);
                                llPicker.setVisibility(View.VISIBLE);
                                releaseMediaRecorder();       // if you are using MediaRecorder, release it first
                                releaseCamera();
                            } else {
                                // initialize video camera
                                if (prepareVideoRecorder()) {
                                    // Camera is available and unlocked, MediaRecorder is prepared,
                                    // now you can start recording
                                    mediaRecorder.start();
                                    tinyDB.putString(Constants.backgroundGreenColor, "");
                                    // inform the user that recording has started
                                    captureButton.setImageDrawable(getResources().getDrawable(R.drawable.capture_selected));
                                    buttonCancel.setImageDrawable(getResources().getDrawable(R.drawable.discard));
                                    buttonConfirm.setImageDrawable(getResources().getDrawable(R.drawable.ok_unselected));
                                    isRecording = true;
                                    recorded = true;
                                    countDownTimer = new CountDownTimer(Integer.parseInt(tinyDB.getString(Constants.VideoRecordingLength)) * 1000, 1000) {

                                        public void onTick(long millisUntilFinished) {
                                            timer.setText(new SimpleDateFormat("mm:ss").format(new Date(millisUntilFinished)));
                                        }

                                        public void onFinish() {
                                            if (isRecording) {
                                                // stop recording and release camera
                                                mediaRecorder.stop();  // stop the recording
                                                releaseMediaRecorder(); // release the MediaRecorder object
                                                mCamera.lock();         // take camera access back from MediaRecorder

                                                // inform the user that recording has stopped
                                                captureButton.setImageDrawable(getResources().getDrawable(R.drawable.start_stop));
                                                buttonCancel.setImageDrawable(getResources().getDrawable(R.drawable.discard_true));
                                                if (recorded) {
                                                    buttonConfirm.setImageDrawable(getResources().getDrawable(R.drawable.ok));
                                                }
                                                isRecording = false;
                                                countDownTimer.cancel();
                                                first = getOutputMediaFileUri(MEDIA_TYPE_VIDEO).toString();
                                                Log.e("foregroundPath", first);
                                                llPicker.setVisibility(View.VISIBLE);
                                                releaseMediaRecorder();       // if you are using MediaRecorder, release it first
                                                releaseCamera();
                                            }
                                        }
                                    }.start();
                                } else {
                                    // prepare didn't work, release the camera
                                    releaseMediaRecorder();
                                    // inform user
                                }
                            }
                        }
                    }
                }
        );

        ivAdminPenal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(CaptureVideoScreen.this, AdminPenal.class);
                startActivity(i);
            }
        });
        buttonConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRecording) {
                    Toast.makeText(CaptureVideoScreen.this, "Please stop recording first!", Toast.LENGTH_SHORT).show();
                } else {
                    if (first != null) {
                        if (s == null) {
                            Toast.makeText(CaptureVideoScreen.this, "Please select background video before processing!", Toast.LENGTH_SHORT).show();
                        } else {

                            if (tinyDB.getString(Constants.backgroundGreenColor).isEmpty()) {
                                Toast.makeText(CaptureVideoScreen.this, "Select Alpha color first to start merging process", Toast.LENGTH_SHORT).show();
                            } else {
                                Boolean processing = false;
                                ArrayList<ProcessingQueueModel> list = tinyDB.getProcessingListObject(Constants.ProcessingList, ProcessingQueueModel.class);
                                for (int i = 0; i < list.size(); i++) {
                                    if (list.get(i).getStatus().equals(Constants.StatusProcessing)) {
                                        processing = true;
                                        break;
                                    }
                                }
                                if (processing) {
                                    Log.e("size", tinyDB.getProcessingListObject(Constants.ProcessingList, ProcessingQueueModel.class).size() + "");
                                    ProcessingQueueModel modell = new ProcessingQueueModel();
                                    modell.setVideo1(first);
                                    modell.setVideo2(s);
                                    modell.setStatus(Constants.StatusAdded);
                                    modell.setOutputPath("empty");
                                    modell.setEventName(tinyDB.getString(Constants.SelectedEventName));

                                    Log.e("size", tinyDB.getProcessingListObject(Constants.ProcessingList, ProcessingQueueModel.class).size() + "");

                                    Intent intent1 = new Intent(CaptureVideoScreen.this, SendingActivity.class);
                                    intent1.putExtra("ProcessingModel", modell);
                                    startActivity(intent1);
                                    finish();
                                } else {
                                    ProcessingQueueModel modell = new ProcessingQueueModel();
                                    modell.setVideo1(first);
                                    modell.setVideo2(s);
                                    modell.setStatus(Constants.StatusProcessing);
                                    modell.setOutputPath("empty");
                                    modell.setEventName(tinyDB.getString(Constants.SelectedEventName));

                                    Intent intent1 = new Intent(CaptureVideoScreen.this, SendingActivity.class);
                                    intent1.putExtra("ProcessingModel", modell);
                                    intent1.putExtra(Constants.Video1Path, first + "");
                                    intent1.putExtra(Constants.Video2Path, s + "");
                                    intent1.putExtra(Constants.ProcessingStatus, Constants.StatusAdded);

                                    startActivity(intent1);
                                    finish();
                                }
                            }
                        }
                    } else {
                        Toast.makeText(CaptureVideoScreen.this, "Please record video first before processing!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });


        llPicker.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public void onClick(View v) {
                if (!file_name.isEmpty()) {
                    final ImageView ivColor, ivSelect, image;
                    final Dialog dialog = new Dialog(CaptureVideoScreen.this);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setContentView(R.layout.thumbnail_dialog);
                    dialog.setCancelable(false);

                    ivColor = dialog.findViewById(R.id.iv_color);
                    ivSelect = dialog.findViewById(R.id.ivSelect);
                    image = dialog.findViewById(R.id.image);
                    if (tinyDB.getString(Constants.backgroundGreenColor).isEmpty()) {

                    } else {
                        ivColor.setBackgroundColor(Color.parseColor("#" + tinyDB.getString(Constants.backgroundGreenColor)));
                    }
                    try {
                        final Bitmap bitmap = createThumbnailFromPath(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/NIE360/" + file_name).getAbsolutePath(), MediaStore.Images.Thumbnails.FULL_SCREEN_KIND);
                        Log.e("path", Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/NIE360/" + file_name).getAbsolutePath());
                        if (bitmap != null) {
                            image.setImageBitmap(bitmap);
                            Log.e("bitmap", bitmap.getByteCount() + "");
                            image.setOnTouchListener(new View.OnTouchListener() {
                                @Override
                                public boolean onTouch(View v, MotionEvent event) {

                                    int x = (int) event.getX();
                                    int y = (int) event.getY();

                                    int redValue;
                                    int blueValue;
                                    int greenValue;
                                    String hex;

                                    switch (event.getAction()) {
                                        case MotionEvent.ACTION_DOWN:
                                            ivColor.setBackgroundColor(
                                                    getProjectedColor((ImageView) v, bitmap, x, y));
                                            redValue = Color.red(getProjectedColor((ImageView) v, bitmap, x, y));
                                            blueValue = Color.blue(getProjectedColor((ImageView) v, bitmap, x, y));
                                            greenValue = Color.green(getProjectedColor((ImageView) v, bitmap, x, y));
                                            Log.e("RGB", redValue + ", " + greenValue + ", " + blueValue);
                                            hex = String.format("%02x%02x%02x", redValue, greenValue, blueValue);
                                            Log.e("HEX", hex);
                                            tinyDB.putString(Constants.backgroundGreenColor, hex);
                                            break;
                                        case MotionEvent.ACTION_MOVE:
                                            ivColor.setBackgroundColor(
                                                    getProjectedColor((ImageView) v, bitmap, x, y));
                                            redValue = Color.red(getProjectedColor((ImageView) v, bitmap, x, y));
                                            blueValue = Color.blue(getProjectedColor((ImageView) v, bitmap, x, y));
                                            greenValue = Color.green(getProjectedColor((ImageView) v, bitmap, x, y));
                                            Log.e("RGB", redValue + ", " + greenValue + ", " + blueValue);
                                            hex = String.format("%02x%02x%02x", redValue, greenValue, blueValue);
                                            Log.e("HEX", hex);
                                            tinyDB.putString(Constants.backgroundGreenColor, hex);
                                            break;
                                        case MotionEvent.ACTION_UP:
                                            ivColor.setBackgroundColor(
                                                    getProjectedColor((ImageView) v, bitmap, x, y));
                                            redValue = Color.red(getProjectedColor((ImageView) v, bitmap, x, y));
                                            blueValue = Color.blue(getProjectedColor((ImageView) v, bitmap, x, y));
                                            greenValue = Color.green(getProjectedColor((ImageView) v, bitmap, x, y));
                                            Log.e("RGB", redValue + ", " + greenValue + ", " + blueValue);
                                            hex = String.format("%02x%02x%02x", redValue, greenValue, blueValue);
                                            Log.e("HEX", hex);
                                            tinyDB.putString(Constants.backgroundGreenColor, hex);
                                            break;
                                    }
                                    return true;
                                }
                            });
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    }
                    ivSelect.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (tinyDB.getString(Constants.backgroundGreenColor).isEmpty()) {
                                Toast.makeText(CaptureVideoScreen.this, "Please select background color.", Toast.LENGTH_SHORT).show();
                            } else {
                                dialog.dismiss();
                            }
                        }
                    });
                    dialog.show();
                } else {
                    Toast.makeText(CaptureVideoScreen.this, "Please record video first to select background color.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        videoView1.setOnClickListener(this);
        videoView2.setOnClickListener(this);
        videoView3.setOnClickListener(this);
        videoView4.setOnClickListener(this);
        videoView5.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        if (v instanceof ImageView) {
            CircleImageView imageView = (CircleImageView) v;

            if (imageView == videoView1) {
                if (isVideoView1HaveVideo) {
                    imageView.setBorderWidth(8);
                    imageView.setBorderColor(getResources().getColor(R.color.colorAccent));
                    videoView2.setBorderWidth(0);
                    videoView3.setBorderWidth(0);
                    videoView4.setBorderWidth(0);
                    videoView5.setBorderWidth(0);
                    s = tinyDB.getString(Constants.Background1);
                    Log.e("backgroundPath", tinyDB.getString(Constants.Background1));
                } else {
                    Toast.makeText(this, "Please select this background video from Admin Panel first.", Toast.LENGTH_LONG).show();
                }
            } else if (imageView == videoView2) {
                if (isVideoView2HaveVideo) {
                    imageView.setBorderWidth(8);
                    imageView.setBorderColor(getResources().getColor(R.color.colorAccent));
                    videoView1.setBorderWidth(0);
                    videoView3.setBorderWidth(0);
                    videoView4.setBorderWidth(0);
                    videoView5.setBorderWidth(0);
                    s = tinyDB.getString(Constants.Background2);
                    Log.e("backgroundPath", tinyDB.getString(Constants.Background2));
                } else {
                    Toast.makeText(this, "Please select this background video from Admin Panel first.", Toast.LENGTH_LONG).show();
                }
            }
            if (imageView == videoView3) {
                if (isVideoView3HaveVideo) {
                    imageView.setBorderWidth(8);
                    imageView.setBorderColor(getResources().getColor(R.color.colorAccent));
                    videoView1.setBorderWidth(0);
                    videoView2.setBorderWidth(0);
                    videoView4.setBorderWidth(0);
                    videoView5.setBorderWidth(0);
                    s = tinyDB.getString(Constants.Background3);
                    Log.e("backgroundPath", tinyDB.getString(Constants.Background3));
                } else {
                    Toast.makeText(this, "Please select this background video from Admin Panel first.", Toast.LENGTH_LONG).show();
                }
            } else if (imageView == videoView4) {
                if (isVideoView4HaveVideo) {
                    imageView.setBorderWidth(8);
                    imageView.setBorderColor(getResources().getColor(R.color.colorAccent));
                    videoView1.setBorderWidth(0);
                    videoView3.setBorderWidth(0);
                    videoView2.setBorderWidth(0);
                    videoView5.setBorderWidth(0);
                    s = tinyDB.getString(Constants.Background4);
                    Log.e("backgroundPath", tinyDB.getString(Constants.Background4));
                } else {
                    Toast.makeText(this, "Please select this background video from Admin Panel first.", Toast.LENGTH_LONG).show();
                }
            } else if (imageView == videoView5) {
                if (isVideoView5HaveVideo) {
                    imageView.setBorderWidth(8);
                    imageView.setBorderColor(getResources().getColor(R.color.colorAccent));
                    videoView1.setBorderWidth(0);
                    videoView3.setBorderWidth(0);
                    videoView4.setBorderWidth(0);
                    videoView2.setBorderWidth(0);
                    s = tinyDB.getString(Constants.Background5);
                    Log.e("backgroundPath", tinyDB.getString(Constants.Background5));
                } else {
                    Toast.makeText(this, "Please select this background video from Admin Panel first.", Toast.LENGTH_LONG).show();
                }
            }
            // do what you want with imageView
        }

    }

    private int getProjectedColor(ImageView iv, Bitmap bm, int x, int y) {
        if (x < 0 || y < 0 || x > (iv.getWidth() - 1) || y > (iv.getHeight() - 1)) {
            //outside ImageView
            return 0;
        } else {
            int projectedX = (int) ((double) x * ((double) bm.getWidth() / (double) iv.getWidth()));
            int projectedY = (int) ((double) y * ((double) bm.getHeight() / (double) iv.getHeight()));


            return bm.getPixel(projectedX, projectedY);
        }
    }

    public Bitmap createThumbnailFromPath(String filePath, int type) {
        return ThumbnailUtils.createVideoThumbnail(filePath, type);
    }

    public static Bitmap retriveVideoFrameFromVideo(String videoPath)
            throws Throwable {
        Bitmap bitmap = null;
        MediaMetadataRetriever mediaMetadataRetriever = null;
        try {
            mediaMetadataRetriever = new MediaMetadataRetriever();
            if (Build.VERSION.SDK_INT >= 14)
                mediaMetadataRetriever.setDataSource(videoPath, new HashMap<String, String>());
            else
                mediaMetadataRetriever.setDataSource(videoPath);

            bitmap = mediaMetadataRetriever.getFrameAtTime(1, MediaMetadataRetriever.OPTION_CLOSEST);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Throwable(
                    "Exception in retriveVideoFrameFromVideo(String videoPath)"
                            + e.getMessage());

        } finally {
            if (mediaMetadataRetriever != null) {
                mediaMetadataRetriever.release();
            }
        }
        return bitmap;
    }

    private static File getOutputMediaFile(int type) {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "NIE360");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("NIE360", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        Log.e("timeStemp", timeStamp + "");
//        String time[] = timeStamp.split("_");
        File mediaFile;
        file_name = "VID" + timeStamp + ".mp4";
        Log.e("file_name", file_name + "");

        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_" + timeStamp + ".jpg");
        } else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID" + timeStamp + ".mp4");
        } else {
            return null;
        }
        Log.e("file_name", mediaFile.getName() + "");

        return mediaFile;
    }

    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
            c.setDisplayOrientation(90);
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
            e.printStackTrace();
        }
        return c; // returns null if camera is unavailable
    }

    private boolean prepareVideoRecorder() {

        mCamera = getCameraInstance();
        mediaRecorder = new MediaRecorder();
        // Step 1: Unlock and set camera to MediaRecorder
        mCamera.unlock();
        mediaRecorder.setCamera(mCamera);
        mediaRecorder.setOrientationHint(90);

        // Step 2: Set sources
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
        mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_720P));
        // Step 4: Set output file
        mediaRecorder.setOutputFile(getOutputMediaFile(MEDIA_TYPE_VIDEO).toString());
        // Step 5: Set the preview output
        mediaRecorder.setPreviewDisplay(mPreview.getHolder().getSurface());
        // Step 6: Prepare configured MediaRecorder
        try {
            mediaRecorder.prepare();
        } catch (IllegalStateException e) {
            Log.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }
        return true;
    }


    @Override
    protected void onPause() {
        super.onPause();
        // release the camera immediately on pause event

        releaseMediaRecorder();
        releaseCamera();
    }

    private void releaseMediaRecorder() {
        if (mediaRecorder != null) {
            mediaRecorder.reset();   // clear recorder configuration
            mediaRecorder.release(); // release the recorder object
            mediaRecorder = null;
            mCamera.lock();           // lock camera for later use
        }
    }

    private void releaseCamera() {
        if (mCamera != null) {
//            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }

    private String getRealPathFromURI(Uri contentURI) {
        String result;
        Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) { // Source is Dropbox or other similar local file path
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {
            Uri selectedMediaUri = data.getData();
            s = generatePath(selectedMediaUri, CaptureVideoScreen.this);

            Log.e("backgroundPath", s + "");
        }
    }

    private class FetchInformations extends AsyncTask<String, Integer, String> {
        ProgressDialog progressDialog = createProgressDialog(CaptureVideoScreen.this, "Please wait.\nFetching video details. it can take while");
        boolean execption = false;

        @Override
        protected String doInBackground(String... params) {
            try {
                MediaInformation info = FFmpeg.getMediaInformation(first);
                Log.e("mediaInfo1", info.getPath());
                MediaInformation info2 = FFmpeg.getMediaInformation(s);
                Log.e("mediaInfo2", info2.getPath());

                path1 = info.getPath();
                path2 = info2.getPath();
            } catch (NullPointerException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                execption = true;
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (execption) {
                Toast.makeText(CaptureVideoScreen.this, "File dos'nt exists!", Toast.LENGTH_SHORT).show();
                progressDialog.hide();
            } else {
                progressDialog.hide();
            }
        }

        @Override
        protected void onPreExecute() {
            progressDialog.show();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }
    }

    private class LongOperation extends AsyncTask<String, Integer, String> {
        ProgressDialog progressDialog = createProgressDialog(CaptureVideoScreen.this, "Please wait\nNIE 360 is making you a video it can take a while.");
        String error = "";

        @Override
        protected String doInBackground(String... params) {
            try {
                final String query;
                if (tinyDB.getString(Constants.AudioType).equals(Constants.BackgroundAudio)) {
                    query = "-y -i " + s + " -i " + first + " -preset ultrafast -filter_complex [1:v]colorkey=0x" + tinyDB.getString(Constants.backgroundGreenColor) + ":0.3:0.15[ckout];[0:v][ckout]overlay[despill];[despill]despill=green[out] -map [out] -map 0:a -b 3200000 -vcodec mpeg4 -s 640:360 -c:a copy " + outputPath + "/" + timeStamp + ".mp4";
                } else if (tinyDB.getString(Constants.AudioType).equals(Constants.ForegroundAudio)) {
                    query = "-y -i " + s + " -i " + first + " -preset ultrafast -filter_complex [1:v]colorkey=0x" + tinyDB.getString(Constants.backgroundGreenColor) + ":0.3:0.15[ckout];[0:v][ckout]overlay[despill];[despill]despill=green[out] -map [out] -map 0:a -b 3200000 -vcodec mpeg4 -s 640:360 -c:a copy " + outputPath + "/" + timeStamp + ".mp4";
                } else {
                    query = "-y -i " + s + " -i " + first + " -preset ultrafast -filter_complex [1:v]colorkey=0x" + tinyDB.getString(Constants.backgroundGreenColor) + ":0.3:0.15[ckout];[0:v][ckout]overlay[despill];[despill]despill=green[out] -map [out] -vcodec mpeg4 " + outputPath + "/" + timeStamp + ".mp4";
                }


                Log.e("query", query);

                FFmpeg.execute(query);
                int rc = FFmpeg.getLastReturnCode();
                String output = FFmpeg.getLastCommandOutput();

                if (rc == RETURN_CODE_SUCCESS) {
                    Log.e("Status", "Command execution completed successfully.");
                    Intent i = new Intent(CaptureVideoScreen.this, PerviewActivity.class);
                    i.putExtra("vid_url", timeStamp + ".mp4");
                    Log.e("vid_url", timeStamp + ".mp4");
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                    finish();
                } else if (rc == RETURN_CODE_CANCEL) {
                    Log.e("Status", "Command execution cancelled by user.");
                } else {
                    Log.e("Status", "Command execution failed with rc=" + rc + " and output=" + output);
                    error = "Failed processing with RC = " + rc;
                }
                publishProgress();
                Log.e("qury", query + "");

            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            progressDialog.hide();
            if (!error.isEmpty()) {
                Toast.makeText(CaptureVideoScreen.this, error + "", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onPreExecute() {
            progressDialog.show();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (videoView1 != null && videoView2 != null && videoView3 != null && videoView4 != null && videoView5 != null) {
            if (!tinyDB.getString(Constants.Background1).equals("")) {
                try {
                    Bitmap thumb = ThumbnailUtils.createVideoThumbnail(tinyDB.getString(Constants.Background1),
                            MediaStore.Images.Thumbnails.MINI_KIND);
                    BitmapDrawable bitmapDrawable = new BitmapDrawable(thumb);
                    videoView1.setImageDrawable(bitmapDrawable);
                    isVideoView1HaveVideo = true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                isVideoView1HaveVideo = false;

                videoView1.setImageResource(R.drawable.ic_launcher_background);
            }
            if (!tinyDB.getString(Constants.Background2).equals("")) {
                try {
                    Bitmap thumb = ThumbnailUtils.createVideoThumbnail(tinyDB.getString(Constants.Background2),
                            MediaStore.Images.Thumbnails.MINI_KIND);
                    BitmapDrawable bitmapDrawable = new BitmapDrawable(thumb);
                    videoView2.setImageDrawable(bitmapDrawable);
                    isVideoView2HaveVideo = true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                isVideoView2HaveVideo = false;
            }
            if (!tinyDB.getString(Constants.Background3).equals("")) {
                try {
                    Bitmap thumb = ThumbnailUtils.createVideoThumbnail(tinyDB.getString(Constants.Background3),
                            MediaStore.Images.Thumbnails.MINI_KIND);
                    BitmapDrawable bitmapDrawable = new BitmapDrawable(thumb);
                    videoView3.setImageDrawable(bitmapDrawable);
                    isVideoView3HaveVideo = true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                isVideoView3HaveVideo = false;
            }
            if (!tinyDB.getString(Constants.Background4).equals("")) {
                try {
                    Bitmap thumb = ThumbnailUtils.createVideoThumbnail(tinyDB.getString(Constants.Background4),
                            MediaStore.Images.Thumbnails.MINI_KIND);
                    BitmapDrawable bitmapDrawable = new BitmapDrawable(thumb);
                    videoView4.setImageDrawable(bitmapDrawable);
                    isVideoView4HaveVideo = true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                isVideoView4HaveVideo = false;
            }
            if (!tinyDB.getString(Constants.Background5).equals("")) {
                try {
                    Bitmap thumb = ThumbnailUtils.createVideoThumbnail(tinyDB.getString(Constants.Background5),
                            MediaStore.Images.Thumbnails.MINI_KIND);
                    BitmapDrawable bitmapDrawable = new BitmapDrawable(thumb);
                    videoView5.setImageDrawable(bitmapDrawable);
                    isVideoView5HaveVideo = true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                isVideoView5HaveVideo = false;
            }
        }
    }
}
