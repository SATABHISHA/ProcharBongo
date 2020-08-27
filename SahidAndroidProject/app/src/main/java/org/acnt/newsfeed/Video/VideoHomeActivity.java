package org.acnt.newsfeed.Video;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import org.acnt.newsfeed.Config.CameraUtils;
import org.acnt.newsfeed.Config.FileUtils;
import org.acnt.newsfeed.Config.RealPathUtil;
import org.acnt.newsfeed.Config.Url;
import org.acnt.newsfeed.Home.MainActivity;
import org.acnt.newsfeed.R;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

public class VideoHomeActivity extends AppCompatActivity implements View.OnClickListener{
    // Activity request codes
    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
    private static final int CAMERA_CAPTURE_VIDEO_REQUEST_CODE = 200;

    // key to store image path in savedInstance state
    public static final String KEY_IMAGE_STORAGE_PATH = "image_path";

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    // Bitmap sampling size
    public static final int BITMAP_SAMPLE_SIZE = 8;

    // Gallery directory name to store the images or videos
    public static final String GALLERY_DIRECTORY_NAME = "Hello Camera";

    // Image and Video file extensions
    public static final String IMAGE_EXTENSION = "jpg";
    public static final String VIDEO_EXTENSION = "mp4";

    private static String imageStoragePath;
    public static TextView output;
    Button btn_capture_video, btn_browse, btn_capture_image;
    VideoView videoPreview;
    ImageView imgPreview;
    public static String image_video_to_base64 = "";

    //-----variables for browse files
    private static final int FILE_SELECT_CODE = 0;
    public String file = "";
    Uri contentUri;
    public String file_to_base64="";
    public String file_desc;

    //-----other variables
    EditText ed_email, ed_mobile, ed_title, ed_name;
    TextView txt_submit;
    SharedPreferences sharedPreferences;
    Context context;
    public static String fileType = "";



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_home);

        btn_capture_video = findViewById(R.id.btn_capture_video);
        btn_capture_image = findViewById(R.id.btn_capture_image);
        videoPreview = findViewById(R.id.videoPreview);
        imgPreview = findViewById(R.id.imgPreview);
        btn_browse = findViewById(R.id.btn_browse);

        ed_email = findViewById(R.id.ed_email);
        ed_mobile = findViewById(R.id.ed_mobile);
        ed_title = findViewById(R.id.ed_title);
        ed_name = findViewById(R.id.ed_name);
        txt_submit = findViewById(R.id.txt_submit);


        output = findViewById(R.id.tv_output);
        btn_capture_video.setOnClickListener(this);
        btn_capture_image.setOnClickListener(this);
        btn_browse.setOnClickListener(this);
        txt_submit.setOnClickListener(this);

        // Checking availability of the camera
        if (!CameraUtils.isDeviceSupportCamera(getApplicationContext())) {
            Toast.makeText(getApplicationContext(),
                    "Sorry! Your device doesn't support camera",
                    Toast.LENGTH_LONG).show();
            // will close the app if the device doesn't have camera
            finish();
        }

        // restoring storage image path from saved instance state
        // otherwise the path will be null on device rotation
        restoreFromBundle(savedInstanceState);

        load_sharedPref_data_if_exists();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_capture_video:
                if (CameraUtils.checkPermissions(getApplicationContext())) {
                    captureVideo();
                } else {
                    requestCameraPermission(MEDIA_TYPE_VIDEO);
                }
                break;
            case R.id.btn_capture_image:
                if (CameraUtils.checkPermissions(getApplicationContext())) {
                    captureImage();
                } else {
                    requestCameraPermission(MEDIA_TYPE_IMAGE);
                }
                break;
            case R.id.btn_browse:
                if (CameraUtils.checkPermissions(getApplicationContext())) {
                    output.setVisibility(View.GONE);
                    videoPreview.setVisibility(View.GONE);
                    imgPreview.setVisibility(View.GONE);
                    showFileChooser();
                }else{
                    requestCameraPermission(MEDIA_TYPE_VIDEO);
                }
                break;
            case R.id.txt_submit:
                if(isConnectingToInternet(VideoHomeActivity.this)) {
                    if (ed_name.getText().toString().trim().isEmpty() ||
                            ed_email.getText().toString().trim().isEmpty() ||
                            ed_title.getText().toString().trim().isEmpty() ||
                            ed_mobile.getText().toString().trim().isEmpty()) {
                        Toast.makeText(getApplicationContext(), "Field cannot be left blank", Toast.LENGTH_LONG).show();
                    } else if (!ed_name.getText().toString().trim().isEmpty() ||
                            !ed_email.getText().toString().trim().isEmpty() ||
                            !ed_title.getText().toString().trim().isEmpty() ||
                            !ed_mobile.getText().toString().trim().isEmpty()) {
                        if (image_video_to_base64.trim().contentEquals("")) {
                            Toast.makeText(getApplicationContext(), "Please upload file (image/video)", Toast.LENGTH_LONG).show();
                        } else if (!image_video_to_base64.trim().contentEquals("")) {
                            if (fileType == "video") {
//                            Toast.makeText(getApplicationContext(),"Video",Toast.LENGTH_LONG).show();
                                submit_data(fileType);
                            } else if (fileType == "image") {
//                            Toast.makeText(getApplicationContext(),"Image",Toast.LENGTH_LONG).show();
                                submit_data(fileType);
                            } else {
                                Toast.makeText(getApplicationContext(), "Incorrect file format. Please select any video or image type file", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "Internal Error", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "Internal Error", Toast.LENGTH_LONG).show();
                    }
                }else{
                    Toast.makeText(getApplicationContext(),"No internet connection. Please enable it",Toast.LENGTH_LONG).show();
                }
                break;
            default:
                break;
        }
    }

    /**
     * Restoring store image path from saved instance state
     */
    private void restoreFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(KEY_IMAGE_STORAGE_PATH)) {
                imageStoragePath = savedInstanceState.getString(KEY_IMAGE_STORAGE_PATH);
                if (!TextUtils.isEmpty(imageStoragePath)) {
                    if (imageStoragePath.substring(imageStoragePath.lastIndexOf(".")).equals("." + IMAGE_EXTENSION)) {
                        previewCapturedImage();
                    } else if (imageStoragePath.substring(imageStoragePath.lastIndexOf(".")).equals("." + VIDEO_EXTENSION)) {
                        previewVideo();
                    }
                }
            }
        }
    }


    /**
     * Requesting permissions using Dexter library
     */
    private void requestCameraPermission(final int type) {
        Dexter.withActivity(this)
                .withPermissions(Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.RECORD_AUDIO)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {

                            if (type == MEDIA_TYPE_IMAGE) {
                                // capture picture
                                captureImage();
                            } else {
                                captureVideo();
                            }

                        } else if (report.isAnyPermissionPermanentlyDenied()) {
                            showPermissionsAlert();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }

    /**
     * Capturing Camera Image will launch camera app requested image capture
     */
    private void captureImage() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        File file = CameraUtils.getOutputMediaFile(MEDIA_TYPE_IMAGE);
        if (file != null) {
            imageStoragePath = file.getAbsolutePath();
        }

        Uri fileUri = CameraUtils.getOutputMediaFileUri(getApplicationContext(), file);

        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

        // start the image capture Intent
        startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
    }

    /**
     * Restoring image path from saved instance state
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // get the file url
        imageStoragePath = savedInstanceState.getString(KEY_IMAGE_STORAGE_PATH);
    }

    /**
     * Launching camera app to record video
     */
    private void captureVideo() {
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);

        File file = CameraUtils.getOutputMediaFile(MEDIA_TYPE_VIDEO);
        if (file != null) {
            imageStoragePath = file.getAbsolutePath();
        }

        Uri fileUri = CameraUtils.getOutputMediaFileUri(getApplicationContext(), file);

        // set video quality
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);

        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file

        // start the video capture Intent
        startActivityForResult(intent, CAMERA_CAPTURE_VIDEO_REQUEST_CODE);
    }

    /**
     * Activity result method will be called after closing the camera
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // if the result is capturing Image
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // Refreshing the gallery
                CameraUtils.refreshGallery(getApplicationContext(), imageStoragePath);

                fileType = getFileTypeFromURL(imageStoragePath);
                Log.d("Filetype-=>",fileType);

                image_video_to_base64 = getBase64FromPath(imageStoragePath); //--added on 21st aug

                // successfully captured the image
                // display it in image view
                previewCapturedImage();
            } else if (resultCode == RESULT_CANCELED) {
                // user cancelled Image capture
                Toast.makeText(getApplicationContext(),
                        "User cancelled image capture", Toast.LENGTH_SHORT)
                        .show();
            } else {
                // failed to capture image
                Toast.makeText(getApplicationContext(),
                        "Sorry! Failed to capture image", Toast.LENGTH_SHORT)
                        .show();
            }
        } else if (requestCode == CAMERA_CAPTURE_VIDEO_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // Refreshing the gallery
                CameraUtils.refreshGallery(getApplicationContext(), imageStoragePath);
                String file_to_base64_video = getBase64FromPath(imageStoragePath);

                fileType = getFileTypeFromURL(imageStoragePath);
                Log.d("Filetype-=>",fileType);

                image_video_to_base64 = getBase64FromPath(imageStoragePath); //--added on 21st aug
                Log.d("file_video_tobase64", file_to_base64_video);

                // video successfully recorded
                // preview the recorded video
                previewVideo();
            } else if (resultCode == RESULT_CANCELED) {
                // user cancelled recording
                Toast.makeText(getApplicationContext(),
                        "User cancelled video recording", Toast.LENGTH_SHORT)
                        .show();
            } else {
                // failed to record video
                Toast.makeText(getApplicationContext(),
                        "Sorry! Failed to record video", Toast.LENGTH_SHORT)
                        .show();
            }
        } else if(requestCode == FILE_SELECT_CODE){
            if(resultCode == RESULT_OK){
                // Get the Uri of the selected file
                Uri uri = data.getData();
                contentUri = data.getData();
                Log.d("File Uri: ", uri.toString());
//                    file = uri.toString();
                // Get the path
                String path = null;
                try {
                    path = FileUtils.getPath(this, uri);
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
//                    filePath.setText(uri.toString());

                String file_path = RealPathUtil.getRealPath(VideoHomeActivity.this,uri);
                output.setVisibility(View.VISIBLE);
                output.setText(file_path);
                Log.d("demoTest-=>",file_path);

                fileType = getFileTypeFromURL(file_path);
                Log.d("Filetype-=>",fileType);

                String file_to_base64 = getBase64FromPath(file_path);
                image_video_to_base64 = getBase64FromPath(file_path); //--added on 21st aug
                Log.d("demoTest1Base64-=>",file_to_base64);
//                    Toast.makeText(getApplicationContext(),file_to_base64,Toast.LENGTH_LONG).show();

                //--get file size, code starts
                File file = new File(file_path);
                Double file_size = Double.parseDouble(String.valueOf(file.length()/1024));
                Log.d("filesize-=>",String.valueOf(file_size));
                //--get file size, code ends

                //-----get file extension/name, code starts
                String fileName = file.getName();
                String file_ext = "";
                if(fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0) {
                    file_ext = fileName.substring(fileName.lastIndexOf(".") + 1);
                }
                Log.d("file_ext-=>",file_ext);
                Log.d("file_name-=>",fileName);
            }
        }
    }

    /**
     * Display image from gallery
     */
    private void previewCapturedImage() {
        try {
            // hide video preview
            output.setVisibility(View.GONE);
            videoPreview.setVisibility(View.GONE);

            imgPreview.setVisibility(View.VISIBLE);

            Bitmap bitmap = CameraUtils.optimizeBitmap(BITMAP_SAMPLE_SIZE, imageStoragePath);

            imgPreview.setImageBitmap(bitmap);

        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    /**
     * Displaying video in VideoView
     */
    private void previewVideo() {
        try {
            // hide image preview
            output.setVisibility(View.GONE);
            imgPreview.setVisibility(View.GONE);

            videoPreview.setVisibility(View.VISIBLE);
            videoPreview.setVideoPath(imageStoragePath);
            // start playing
            videoPreview.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Alert dialog to navigate to app settings
     * to enable necessary permissions
     */
    private void showPermissionsAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permissions required!")
                .setMessage("Camera needs few permissions to work properly. Grant them in settings.")
                .setPositiveButton("GOTO SETTINGS", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        CameraUtils.openSettings(VideoHomeActivity.this);
                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();
    }

    //-------function to browse file, starts---
    public void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(
                    Intent.createChooser(intent, "Select a File to Upload"),
                    FILE_SELECT_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            // Potentially direct the user to the Market with a Dialog
            Toast.makeText(this, "Please install a File Manager.",
                    Toast.LENGTH_SHORT).show();
        }
    }
    //-------function to browse file, ends---

    //----code to convert file to base64(5th march 2020), starts-----
    public String getBase64FromPath(String path) {
        String base64 = "";
        try {/*from   w w w .  ja  va  2s  .  c om*/
            File file = new File(path);
            byte[] buffer = new byte[(int) file.length() + 100];
            @SuppressWarnings("resource")
            int length = new FileInputStream(file).read(buffer);
            base64 = Base64.encodeToString(buffer, 0, length,
                    Base64.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return base64;
    }
    //----code to convert file to base64(5th march 2020), ends-----

    //-----function to submit, code starts-----
    public void submit_data(String type){
        String android_id = Settings.Secure.getString(getApplicationContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);

        final JSONObject DocumentElementobj = new JSONObject();
        final ProgressDialog loading = ProgressDialog.show(this, "Loading", "Please wait...", true, false);
        try {
            DocumentElementobj.put("name", ed_name.getText().toString());
            DocumentElementobj.put("email_id", ed_email.getText().toString());
            DocumentElementobj.put("mobile_number", ed_mobile.getText().toString());
            DocumentElementobj.put("video_title", ed_title.getText().toString());
            DocumentElementobj.put("video", image_video_to_base64);
            DocumentElementobj.put("type", type);
            DocumentElementobj.put("device_id", android_id);
        }catch (JSONException e){
            e.printStackTrace();
        }

        Log.d("JsonObject",DocumentElementobj.toString());
        JsonObjectRequest request_json = null;
        String url = Url.BasrUrl +"REST/News/addVideo";
        Log.d("newsurl-=>",url);
        try {
            request_json = new JsonObjectRequest(Request.Method.POST, url, new JSONObject(DocumentElementobj.toString()),
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                //Process os success response
                                JSONObject jsonObj = null;
                                try {
                                    String responseData = response.toString();
                                    String val = "";
                                    JSONObject resobj = new JSONObject(responseData);
                                    Log.d("getData", resobj.toString());

                                    if (resobj.getString("status").contentEquals("Success")) {
                                        loading.dismiss();
                                       Toast.makeText(getApplicationContext(),"Data saved successfully",Toast.LENGTH_LONG).show();

                                        sharedPreferences = getApplication().getSharedPreferences("ProfileDetails", Context.MODE_PRIVATE);
                                        String name = sharedPreferences.getString("name","");
                                        if(name == "") {
                                            SharedPreferences.Editor editor = sharedPreferences.edit();
                                            editor.putString("name", ed_name.getText().toString());
                                            editor.putString("email", ed_email.getText().toString());
                                            editor.putString("mobile", ed_mobile.getText().toString());
                                            editor.commit();
                                        }
                                        Intent intent = new Intent(VideoHomeActivity.this, MainActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);

                                    } else {
                                        Toast.makeText(getApplicationContext(), "Internal Error", Toast.LENGTH_LONG).show();
                                    }
                                } catch (JSONException e) {
                                    loading.dismiss();
                                    Toast.makeText(getApplicationContext(), "Internal Error", Toast.LENGTH_LONG).show();
                                    e.printStackTrace();
                                }

                            } catch (Exception e) {
                                loading.dismiss();
                                Toast.makeText(getApplicationContext(), "Internal Error", Toast.LENGTH_LONG).show();
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    loading.dismiss();
                    if (error instanceof NetworkError) {
                        loading.dismiss();
                        Toast.makeText(getApplicationContext(), "Internal Server Error", Toast.LENGTH_LONG).show();
                    } else if (error instanceof ServerError) {
                        loading.dismiss();
                        Toast.makeText(getApplicationContext(), "Internal Server Error", Toast.LENGTH_LONG).show();
                    } else if (error instanceof AuthFailureError) {
                        loading.dismiss();
                        Toast.makeText(getApplicationContext(), "Internal Error", Toast.LENGTH_LONG).show();
                    } else if (error instanceof ParseError) {
                        loading.dismiss();
                        Toast.makeText(getApplicationContext(), "Internal Error", Toast.LENGTH_LONG).show();
                    } else if (error instanceof NoConnectionError) {
                        loading.dismiss();
                        Toast.makeText(getApplicationContext(), "No Internet Connection. Please check your internet connection.", Toast.LENGTH_LONG).show();
                    } else if (error instanceof TimeoutError) {
                        loading.dismiss();
                        Toast.makeText(getApplicationContext(),
                                "Oops. Session Timeout!",
                                Toast.LENGTH_LONG).show();
                    }else{
                        loading.dismiss();
                        Toast.makeText(getApplicationContext(), "Internal Error", Toast.LENGTH_LONG).show();
                    }
//                    VolleyLog.e("Error: ", error.getMessage());
                }
            });
            request_json.setRetryPolicy(new DefaultRetryPolicy(10000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            requestQueue.add(request_json);
        }catch (JSONException e){
            loading.dismiss();
            Toast.makeText(getApplicationContext(), "Internal Error Occurred", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
    //-----function to submit, code ends-----

    //------function to load data if exists in shared preference, code starts--------
    public void load_sharedPref_data_if_exists(){
        sharedPreferences = getApplication().getSharedPreferences("ProfileDetails", Context.MODE_PRIVATE);
        String name = sharedPreferences.getString("name","");
        if(name != ""){
            ed_name.setText(name);
            ed_name.setClickable(false);
            ed_name.setFocusable(false);
            ed_name.setFocusableInTouchMode(false);

            ed_email.setText(sharedPreferences.getString("email",""));
            ed_email.setClickable(false);
            ed_email.setFocusable(false);
            ed_email.setFocusableInTouchMode(false);

            ed_mobile.setText(sharedPreferences.getString("mobile",""));
            ed_mobile.setClickable(false);
            ed_mobile.setFocusable(false);
            ed_mobile.setFocusableInTouchMode(false);
        }else{
            ed_name.setClickable(true);
            ed_name.setFocusable(true);
            ed_name.setFocusableInTouchMode(true);

            ed_email.setClickable(true);
            ed_email.setFocusable(true);
            ed_email.setFocusableInTouchMode(true);

            ed_mobile.setClickable(true);
            ed_mobile.setFocusable(true);
            ed_mobile.setFocusableInTouchMode(true);
        }
    }
    //------function to load data if exists in shared preference, code ends--------

    //-----function to get url type code starts-------
    private String getFileTypeFromURL(String url){
        String[] splitedArray = url.split("\\.");
        String lastValueOfArray = splitedArray[splitedArray.length-1];
        if(lastValueOfArray.equals("mp4") || lastValueOfArray.equals("flv") || lastValueOfArray.equals("m4a") || lastValueOfArray.equals("3gp") || lastValueOfArray.equals("mkv")){
            return "video";
        }else if(lastValueOfArray.equals("mp3") || lastValueOfArray.equals("ogg")){
            return "audio";
        }else if(lastValueOfArray.equals("jpg") || lastValueOfArray.equals("png") || lastValueOfArray.equals("gif")){
            return "image";
        }else{
            return "others";
        }
    }
    //-----function to get url type code ends-------

    //----function for checking internet connection, code starts---
    public static boolean isConnectingToInternet(Context context)
    {
        ConnectivityManager connectivity =
                (ConnectivityManager) context.getSystemService(
                        Context.CONNECTIVITY_SERVICE);
        if (connectivity != null)
        {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null)
                for (int i = 0; i < info.length; i++)
                    if (info[i].getState() == NetworkInfo.State.CONNECTED)
                    {
                        return true;
                    }
        }
        return false;
    }
    //----function for checking internet connection, code ends---


    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        Intent intent = new Intent(VideoHomeActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
