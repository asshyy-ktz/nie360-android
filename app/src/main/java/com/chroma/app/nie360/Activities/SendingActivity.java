package com.chroma.app.nie360.Activities;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Toast;

import com.chroma.app.nie360.Adapters.SpinnerArrayAdapter;
import com.chroma.app.nie360.BaseClasses.BaseActivity;
import com.chroma.app.nie360.Models.EventsResponse;
import com.chroma.app.nie360.Models.ProcessingQueueModel;
import com.chroma.app.nie360.Models.UploadingQueueModel;
import com.chroma.app.nie360.NetworkClasses.RetrofitClass;
import com.chroma.app.nie360.R;
import com.chroma.app.nie360.Utils.Constants;
import com.chroma.app.nie360.Utils.CustomEditText;
import com.chroma.app.nie360.Utils.ProcessingService;
import com.chroma.app.nie360.Utils.TinyDB;

import java.io.File;
import java.util.ArrayList;

import br.com.sapereaude.maskedEditText.MaskedEditText;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Callback;
import retrofit2.Response;

import static com.chroma.app.nie360.Utils.ProcessingService.BROADCAST_ACTION;

public class SendingActivity extends BaseActivity {
    ImageView ivAdminPenal, ivEmailUnselected, ivEmail, ivPhoneUnselected, ivPhone, ivThumbnail;
    MaskedEditText etPhoneNumber;
    CustomEditText etEmail;
    TinyDB tinyDB;
    String vid_url = "";
    ImageView nextButton;
    public  String currentScreen;
    ProcessingQueueModel model;
    private String first, s, STATUS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sending);
        getSupportActionBar().hide();
        currentScreen = "SendingActivity";
        if (getIntent() != null) {
          model = (ProcessingQueueModel) getIntent().getSerializableExtra("ProcessingModel");
            first = getIntent().getStringExtra(Constants.Video1Path);
            s = getIntent().getStringExtra(Constants.Video2Path);
            STATUS = getIntent().getStringExtra(Constants.ProcessingStatus);
        }
        init();
        clickListeners();
    }
    private void init() {
        tinyDB = new TinyDB(SendingActivity.this);
        nextButton = findViewById(R.id.addbutton);
        ivAdminPenal = findViewById(R.id.ivAdminPenal);
        ivEmailUnselected = findViewById(R.id.ivEmailUnselected);
        ivEmail = findViewById(R.id.ivEmail);
        ivPhoneUnselected = findViewById(R.id.ivPhoneUnselected);
        ivPhone = findViewById(R.id.ivPhone);
        ivThumbnail = findViewById(R.id.ivThumbnail);

        if (!vid_url.isEmpty()) {
            try {
                Bitmap thumb = ThumbnailUtils.createVideoThumbnail(vid_url,
                        MediaStore.Images.Thumbnails.MINI_KIND);
                BitmapDrawable bitmapDrawable = new BitmapDrawable(thumb);
                ivThumbnail.setBackgroundDrawable(bitmapDrawable);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        etEmail = findViewById(R.id.etEmail);
        if (tinyDB.getString(Constants.SendingType).equalsIgnoreCase("")) {
            ivPhoneUnselected.setVisibility(View.GONE);
            ivPhone.setVisibility(View.VISIBLE);
            ivEmail.setVisibility(View.GONE);
            ivEmailUnselected.setVisibility(View.VISIBLE);
            etPhoneNumber.setVisibility(View.VISIBLE);
            etEmail.setVisibility(View.GONE);
            tinyDB.putString(Constants.SendingType, "phone");
        } else {
            if (tinyDB.getString(Constants.SendingType).equalsIgnoreCase("email")) {
                ivPhone.setVisibility(View.GONE);
                ivPhoneUnselected.setVisibility(View.VISIBLE);
                ivEmailUnselected.setVisibility(View.GONE);
                ivEmail.setVisibility(View.VISIBLE);
                etPhoneNumber.setVisibility(View.GONE);
                etEmail.setVisibility(View.VISIBLE);
                tinyDB.putString(Constants.SendingType, "email");
            } else {
                ivPhoneUnselected.setVisibility(View.GONE);
                ivPhone.setVisibility(View.VISIBLE);
                ivEmail.setVisibility(View.GONE);
                ivEmailUnselected.setVisibility(View.VISIBLE);
                etPhoneNumber.setVisibility(View.VISIBLE);
                etEmail.setVisibility(View.GONE);
                tinyDB.putString(Constants.SendingType, "phone");
            }
        }
    }

    private void clickListeners() {
        ivEmailUnselected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ivPhone.setVisibility(View.GONE);
                ivPhoneUnselected.setVisibility(View.VISIBLE);
                ivEmailUnselected.setVisibility(View.GONE);
                ivEmail.setVisibility(View.VISIBLE);

                etPhoneNumber.setVisibility(View.GONE);
                etEmail.setVisibility(View.VISIBLE);
                tinyDB.putString(Constants.SendingType, "email");
            }
        });

        ivPhoneUnselected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ivPhoneUnselected.setVisibility(View.GONE);
                ivPhone.setVisibility(View.VISIBLE);
                ivEmail.setVisibility(View.GONE);
                ivEmailUnselected.setVisibility(View.VISIBLE);

                etPhoneNumber.setVisibility(View.VISIBLE);
                etEmail.setVisibility(View.GONE);

                tinyDB.putString(Constants.SendingType, "phone");
            }
        });


        ivAdminPenal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(SendingActivity.this, AdminPenal.class);
                startActivity(i);
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Boolean shouldMove = false;
                if(tinyDB.getString(Constants.SendingType).equals("email")) {

                    if(etEmail.getText().toString().isEmpty()){
                        Toast.makeText(SendingActivity.this, "Please enter your email.", Toast.LENGTH_SHORT).show();
                    }

                    if(isValidEmail(etEmail.getText())){
                        shouldMove = true;

                    }else{
                        Toast.makeText(SendingActivity.this, "Please correct email format.", Toast.LENGTH_SHORT).show();
                        return;

                    }

                }else{
                    Log.d("Phone number is:" , etPhoneNumber.getText().toString());
                    if (etPhoneNumber.getText().toString().isEmpty() || etPhoneNumber.getText().toString().contains("X")){
                        Toast.makeText(SendingActivity.this, "Please enter your phone number.", Toast.LENGTH_SHORT).show();
                    }else{
                        shouldMove = true;

                    }
                }

                if(shouldMove){
                    model.setEmail(etEmail.getText().toString());
                    model.setPhone(etPhoneNumber.getText().toString());

                    ArrayList<ProcessingQueueModel> list = new ArrayList<>();

                    list.add(model);

                    Log.e("PList: size", list.size() + "");
                    ArrayList<ProcessingQueueModel> list1 = tinyDB.getProcessingListObject(Constants.ProcessingList, ProcessingQueueModel.class);
                    list1.addAll(list);

                    tinyDB.putProcessingListObject(Constants.ProcessingList, list1);
                    Log.e("size", tinyDB.getProcessingListObject(Constants.ProcessingList, ProcessingQueueModel.class).size() + "");


                    Intent intent = new Intent(SendingActivity.this, ProcessingService.class);
                    intent.putExtra(Constants.Video1Path, first + "");
                    intent.putExtra(Constants.Video2Path, s + "");
                    intent.putExtra(Constants.ProcessingStatus, Constants.StatusAdded);
                    intent.putExtra("ProcessingModel", model);

                    Log.e("position", tinyDB.getProcessingListObject(Constants.ProcessingList, ProcessingQueueModel.class).size() - 1 + "");
                    intent.putExtra(Constants.position, tinyDB.getProcessingListObject(Constants.ProcessingList, ProcessingQueueModel.class).size() - 1);
                    startService(intent);

                    Intent intent1 = new Intent(SendingActivity.this, ProcessingActivity.class);
                    startActivity(intent1);

                    finish();
                }else{
                    Toast.makeText(SendingActivity.this, "Missing Field!", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }
    public static boolean isValidEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }

}
