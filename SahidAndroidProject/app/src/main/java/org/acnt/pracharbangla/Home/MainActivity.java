package org.acnt.pracharbangla.Home;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

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
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.InstallStateUpdatedListener;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.InstallStatus;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.firebase.FirebaseApp;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.GravityCompat;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.acnt.pracharbangla.Config.Url;
import org.acnt.pracharbangla.Model.NewsFeedModel;
import org.acnt.pracharbangla.R;
import org.acnt.pracharbangla.Video.VideoHomeActivity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.google.android.play.core.install.model.ActivityResult.RESULT_IN_APP_UPDATE_FAILED;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    private AppBarConfiguration mAppBarConfiguration;
    NavigationView navigationView;
    Locale myLocale;
    String currentLanguage = "en", currentLang;
    RecyclerView recycler_view;
    ArrayList<NewsFeedModel> newsFeedModelArrayList = new ArrayList<>();
    LinearLayout ll_recycler;
    TextView tv_nodata;
    public static  CustomMainActivityAdapter customMainActivityAdapter;
    static int popup_flag = 0;
    SharedPreferences sharedPreferences, sharedPreferences_one_time_register;
    ImageButton imgbtn_search, imgbtn_home, imgbtn_add, imgbtn_profile;
    Context context;
    CoordinatorLayout coordinatorLayout;

    //------variable for version update, code starts
    private AppUpdateManager mAppUpdateManager;
    private int RC_APP_UPDATE = 999;
    private int inAppUpdateType;
    private com.google.android.play.core.tasks.Task<AppUpdateInfo> appUpdateInfoTask;
    private InstallStateUpdatedListener installStateUpdatedListener;
    //------variable for version update, code ends

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ll_recycler = findViewById(R.id.ll_recycler);
        tv_nodata = findViewById(R.id.tv_nodata);
        imgbtn_search = findViewById(R.id.imgbtn_search);
        imgbtn_home = findViewById(R.id.imgbtn_home);
        imgbtn_add = findViewById(R.id.imgbtn_add);
        coordinatorLayout=findViewById(R.id.cordinatorLayout);
        imgbtn_profile = findViewById(R.id.imgbtn_profile);

        currentLanguage = getIntent().getStringExtra(currentLang);
        customMainActivityAdapter = new CustomMainActivityAdapter(this,newsFeedModelArrayList);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        /*FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        final View header = navigationView.getHeaderView(0);
        navigationView.setNavigationItemSelectedListener(this);
        set_language_navigation_menu_items();

        //==========Recycler code initializing and setting layoutManager starts======
        recycler_view = findViewById(R.id.recycler_view);
        recycler_view.setHasFixedSize(true);
        recycler_view.setLayoutManager(new LinearLayoutManager(this));
        //==========Recycler code initializing and setting layoutManager ends======


        if(VideoHomeActivity.isConnectingToInternet(MainActivity.this)) {
            loadData("0");
        }else{
            Toast.makeText(getApplicationContext(),"Internet connection seems to be disabled. Please enable it.",Toast.LENGTH_LONG).show();
            startActivity(new Intent(
                    Settings.ACTION_WIRELESS_SETTINGS));
        }

        sharedPreferences = getApplication().getSharedPreferences("HomeDetails", Context.MODE_PRIVATE);
        String type = sharedPreferences.getString("type","");
        if(type != ""){
            if(type.contentEquals("বাংলা")){
                setLocale("ben");
            }else if(type.contentEquals("English")){
                setLocale("en");
            }
        }else if(type == ""){
            if(popup_flag == 0) {
                open_pop_up();
            }
        }

        //----added on 3rd sept,starts
        sharedPreferences_one_time_register = getApplication().getSharedPreferences("RegistrationDetails", Context.MODE_PRIVATE);
        String onetime_register_check = sharedPreferences_one_time_register.getString("onetime_register","");
        if(onetime_register_check == ""){
            register_device_info();
        }
        //----added on 3rd sept,ends

        imgbtn_search.setOnClickListener(this);
        imgbtn_home.setOnClickListener(this);
        imgbtn_add.setOnClickListener(this);
        imgbtn_profile.setOnClickListener(this);


        //-----added on 20th Aug, code starts-------
        // Get token
        context = MainActivity.this;
        FirebaseApp.initializeApp(context);
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnSuccessListener(MainActivity.this, new OnSuccessListener<InstanceIdResult>() {
                    @Override
                    public void onSuccess(InstanceIdResult instanceIdResult) {
                        Log.d("fcm token", instanceIdResult.getToken());
                    }
                });
        // [END retrieve_current_token]
        //-----added on 20th Aug, code ends-------

//        requestMultiplePermissions(); //deed on 30th Aug

        //----added on 20th July for version update, starts----
        mAppUpdateManager = AppUpdateManagerFactory.create(this);
        // Returns an intent object that you use to check for an update.
        appUpdateInfoTask = mAppUpdateManager.getAppUpdateInfo();
        //lambda operation used for below listener
        //For flexible update
        installStateUpdatedListener = installState -> {
            if (installState.installStatus() == InstallStatus.DOWNLOADED) {
                popupSnackbarForCompleteUpdate();
            }
        };
        mAppUpdateManager.registerListener(installStateUpdatedListener);

        //For Flexible
        inAppUpdateType = AppUpdateType.FLEXIBLE;//1
        inAppUpdate();
        //----added on 20th July for version update, ends----


    }


    //-------added on 4th Sept code for version update, starts----


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_APP_UPDATE) {
            //when user clicks update button
            if (resultCode == RESULT_OK) {
                Toast.makeText(MainActivity.this, "App download starts...", Toast.LENGTH_LONG).show();
            } else if (resultCode != RESULT_CANCELED) {
                //if you want to request the update again just call checkUpdate()
                Toast.makeText(MainActivity.this, "App download canceled.", Toast.LENGTH_LONG).show();
            } else if (resultCode == RESULT_IN_APP_UPDATE_FAILED) {
                Toast.makeText(MainActivity.this, "App download failed.", Toast.LENGTH_LONG).show();
            }
        }
    }
    @Override
    protected void onDestroy() {
        mAppUpdateManager.unregisterListener(installStateUpdatedListener);
        super.onDestroy();
    }

    private void inAppUpdate() {

        try {
            // Checks that the platform will allow the specified type of update.
            appUpdateInfoTask.addOnSuccessListener(new com.google.android.play.core.tasks.OnSuccessListener<AppUpdateInfo>() {
                @Override
                public void onSuccess(AppUpdateInfo appUpdateInfo) {
                    if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                            // For a flexible update, use AppUpdateType.FLEXIBLE
                            && appUpdateInfo.isUpdateTypeAllowed(inAppUpdateType)) {
                        // Request the update.

                        try {
                            mAppUpdateManager.startUpdateFlowForResult(
                                    // Pass the intent that is returned by 'getAppUpdateInfo()'.
                                    appUpdateInfo,
                                    // Or 'AppUpdateType.FLEXIBLE' for flexible updates.
                                    inAppUpdateType,
                                    // The current activity making the update request.
                                    MainActivity.this,
                                    // Include a request code to later monitor this update request.
                                    RC_APP_UPDATE);
                        } catch (IntentSender.SendIntentException ignored) {

                        }
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void popupSnackbarForCompleteUpdate() {
        try {
            com.google.android.material.snackbar.Snackbar snackbar =
                    com.google.android.material.snackbar.Snackbar.make(
                            findViewById(R.id.cordinatorLayout),
                            "An update has just been downloaded.\nRestart to update",
                            com.google.android.material.snackbar.Snackbar.LENGTH_INDEFINITE);

            snackbar.setAction("INSTALL", view -> {
                if (mAppUpdateManager != null){
                    mAppUpdateManager.completeUpdate();
                }
            });
            snackbar.setActionTextColor(Color.parseColor("#ffffff"));
            snackbar.show();

        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }

    }
    //-------added on 4th Sept code for version update, ends----

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id){
            case R.id.imgbtn_search:
                open_search_pop_up();
                break;
            case R.id.imgbtn_home:
                loadData("0");
                break;
            case R.id.imgbtn_add:
                startActivity(new Intent(MainActivity.this, VideoHomeActivity.class));
                break;
            case R.id.imgbtn_profile:
                sharedPreferences = getApplication().getSharedPreferences("ProfileDetails", Context.MODE_PRIVATE);
                String name = sharedPreferences.getString("name","");
                if(name == "") {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle(getString(R.string.activity_main_profile_alert_title));
                    builder.setMessage(getString(R.string.activity_main_profile_alert_dialog_message))
                            .setCancelable(false)
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                public void onClick(final DialogInterface dialog, final int id) {
                                    dialog.dismiss();
                                }
                            });
                    final AlertDialog alert = builder.create();
                    alert.show();
                }else{
                    open_profile_popup();
                }
                break;
        }
    }
    //------function to open popup code , starts----
    public void open_search_pop_up(){
        LayoutInflater li = LayoutInflater.from(this);
        final View dialog = li.inflate(R.layout.activity_search_popup, null);
        final EditText ed_search = dialog.findViewById(R.id.ed_search);
        TextView tv_button_search = dialog.findViewById(R.id.tv_button_search);
        TextView tv_button_cancel = dialog.findViewById(R.id.tv_button_cancel);

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setView(dialog);
        alert.setCancelable(false);
        //Creating an alert dialog
        final AlertDialog alertDialog = alert.create();
        alertDialog.show();

        tv_button_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ed_search.getText().toString().trim().isEmpty()){
                    Toast.makeText(getApplicationContext(),"Field cannot be left blank",Toast.LENGTH_LONG).show();
                }else{
                    if(VideoHomeActivity.isConnectingToInternet(MainActivity.this)) {
                        loadSearchData(ed_search.getText().toString());
                    }else{
                        Toast.makeText(getApplicationContext(),"Internet connection seems to be disabled. Please enable it.",Toast.LENGTH_LONG).show();
                        startActivity(new Intent(
                                Settings.ACTION_WIRELESS_SETTINGS));
                    }
                    alertDialog.dismiss();
                }
            }
        });

        tv_button_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });


    }

    @Override
    protected void onRestart() {

        // TODO Auto-generated method stub
        super.onRestart();
        Intent i = new Intent(MainActivity.this, MainActivity.class);  //your class
        startActivity(i);
        finish();

    }

    public void open_pop_up(){
        LayoutInflater li = LayoutInflater.from(this);
        final View dialog = li.inflate(R.layout.activity_settings_popup, null);
        final RadioGroup radioGroup;
        RadioButton radia_id1, radia_id2;
        TextView tv_button_ok, tv_button_cancel;
        final CheckBox chkSignedIn;

        radioGroup = dialog.findViewById(R.id.groupradio);
        radia_id1 = dialog.findViewById(R.id.radia_id1);
        radia_id2 = dialog.findViewById(R.id.radia_id2);
        tv_button_ok = dialog.findViewById(R.id.tv_button_ok);
        tv_button_cancel = dialog.findViewById(R.id.tv_button_cancel);
        chkSignedIn = dialog.findViewById(R.id.chkSignedIn);

        radia_id1.setChecked(true);



        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setView(dialog);
        alert.setCancelable(false);
        //Creating an alert dialog
        final AlertDialog alertDialog = alert.create();
        alertDialog.show();

        tv_button_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int selectedId = radioGroup.getCheckedRadioButtonId();
                if(selectedId!=-1) {
                    RadioButton radioButton = (RadioButton) radioGroup.findViewById(selectedId);
//                    Toast.makeText(getApplicationContext(),radioButton.getText().toString(),Toast.LENGTH_LONG).show();

                    if(radioButton.getText().toString().contentEquals("English")){
                        setLocale("en");
                    }else if(radioButton.getText().toString().contentEquals("বাংলা")){
                        setLocale("ben");
                    }

                    popup_flag = 1;
                    alertDialog.dismiss();
                    if(chkSignedIn.isChecked()){
                        sharedPreferences = getApplication().getSharedPreferences("HomeDetails", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("type", radioButton.getText().toString());
                        editor.commit();
                    }else if(!chkSignedIn.isChecked()){
                        sharedPreferences = getApplication().getSharedPreferences("HomeDetails", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("type", "");
                        editor.commit();
                    }
                    /*if(radioButton.getText().toString().contentEquals("Save")){
                        Toast.makeText(getApplicationContext(),"Saved OD Duty Request",Toast.LENGTH_LONG).show();
                    }else if(radioButton.getText().toString().contentEquals("Submit")){
                        Toast.makeText(getApplicationContext(),"Submitted OD Duty Request",Toast.LENGTH_LONG).show();
                    }*/
                }
            }
        });

        tv_button_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });
    }


    public void open_profile_popup(){
        LayoutInflater li = LayoutInflater.from(this);
        final View dialog = li.inflate(R.layout.activity_profile_popup, null);
        TextView tv_name, tv_email, tv_mobile, tv_button_ok;

        tv_name = dialog.findViewById(R.id.tv_name);
        tv_email = dialog.findViewById(R.id.tv_email);
        tv_mobile = dialog.findViewById(R.id.tv_mobile);
        tv_button_ok = dialog.findViewById(R.id.tv_button_ok);

        sharedPreferences = getApplication().getSharedPreferences("ProfileDetails", Context.MODE_PRIVATE);
        tv_name.setText(sharedPreferences.getString("name",""));
        tv_email.setText(sharedPreferences.getString("email",""));
        tv_mobile.setText(sharedPreferences.getString("mobile",""));

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setView(dialog);
        alert.setCancelable(false);
        //Creating an alert dialog
        final AlertDialog alertDialog = alert.create();
        alertDialog.show();

        tv_button_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               alertDialog.dismiss();
            }
        });
    }
    //------function to open popup code , ends----



    //-------code for menu items, starts
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_settings:
                open_pop_up();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //-------code for menu items, ends

   /* @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }*/

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_local) {
            // Handle the camera action
            if(VideoHomeActivity.isConnectingToInternet(MainActivity.this)) {
                loadData("1");
            }else{
                Toast.makeText(getApplicationContext(),"Internet connection seems to be disabled. Please enable it.",Toast.LENGTH_LONG).show();
                startActivity(new Intent(
                        Settings.ACTION_WIRELESS_SETTINGS));
            }
        }else if(id == R.id.nav_state){
            if(VideoHomeActivity.isConnectingToInternet(MainActivity.this)) {
                loadData("2");
            } else{
                Toast.makeText(getApplicationContext(),"Internet connection seems to be disabled. Please enable it.",Toast.LENGTH_LONG).show();
                startActivity(new Intent(
                        Settings.ACTION_WIRELESS_SETTINGS));
            }
        }else if(id == R.id.nav_country){
            if(VideoHomeActivity.isConnectingToInternet(MainActivity.this)) {
                loadData("3");
            } else{
                Toast.makeText(getApplicationContext(),"Internet connection seems to be disabled. Please enable it.",Toast.LENGTH_LONG).show();
                startActivity(new Intent(
                        Settings.ACTION_WIRELESS_SETTINGS));
            }
        }else if(id == R.id.nav_international){
            if(VideoHomeActivity.isConnectingToInternet(MainActivity.this)) {
                loadData("4");
            }else{
                Toast.makeText(getApplicationContext(),"Internet connection seems to be disabled. Please enable it.",Toast.LENGTH_LONG).show();
                startActivity(new Intent(
                        Settings.ACTION_WIRELESS_SETTINGS));
            }
        }else if(id == R.id.nav_entertainment){
            if(VideoHomeActivity.isConnectingToInternet(MainActivity.this)) {
                loadData("5");
            }else{
                Toast.makeText(getApplicationContext(),"Internet connection seems to be disabled. Please enable it.",Toast.LENGTH_LONG).show();
                startActivity(new Intent(
                        Settings.ACTION_WIRELESS_SETTINGS));
            }
        }else if(id == R.id.nav_sports){
            if(VideoHomeActivity.isConnectingToInternet(MainActivity.this)) {
                loadData("6");
            }else{
                Toast.makeText(getApplicationContext(),"Internet connection seems to be disabled. Please enable it.",Toast.LENGTH_LONG).show();
                startActivity(new Intent(
                        Settings.ACTION_WIRELESS_SETTINGS));
            }
        }else if(id == R.id.nav_job){
            if(VideoHomeActivity.isConnectingToInternet(MainActivity.this)) {
                loadData("7");
            }else{
                Toast.makeText(getApplicationContext(),"Internet connection seems to be disabled. Please enable it.",Toast.LENGTH_LONG).show();
                startActivity(new Intent(
                        Settings.ACTION_WIRELESS_SETTINGS));
            }
        }else if(id == R.id.nav_health){
            if(VideoHomeActivity.isConnectingToInternet(MainActivity.this)) {
                loadData("8");
            }else{
                Toast.makeText(getApplicationContext(),"Internet connection seems to be disabled. Please enable it.",Toast.LENGTH_LONG).show();
                startActivity(new Intent(
                        Settings.ACTION_WIRELESS_SETTINGS));
            }
        }else if(id == R.id.nav_library){
            if(VideoHomeActivity.isConnectingToInternet(MainActivity.this)) {
                loadData("9");
            }else{
                Toast.makeText(getApplicationContext(),"Internet connection seems to be disabled. Please enable it.",Toast.LENGTH_LONG).show();
                startActivity(new Intent(
                        Settings.ACTION_WIRELESS_SETTINGS));
            }
        }else if(id == R.id.nav_editors){
            if(VideoHomeActivity.isConnectingToInternet(MainActivity.this)) {
                loadData("10");
            }else{
                Toast.makeText(getApplicationContext(),"Internet connection seems to be disabled. Please enable it.",Toast.LENGTH_LONG).show();
                startActivity(new Intent(
                        Settings.ACTION_WIRELESS_SETTINGS));
            }
        }else if(id == R.id.nav_advertisement){
            if(VideoHomeActivity.isConnectingToInternet(MainActivity.this)) {
                loadData("11");
            }else{
                Toast.makeText(getApplicationContext(),"Internet connection seems to be disabled. Please enable it.",Toast.LENGTH_LONG).show();
                startActivity(new Intent(
                        Settings.ACTION_WIRELESS_SETTINGS));
            }
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void set_language_navigation_menu_items() {
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        Menu nav_Menu = navigationView.getMenu();
//        setLocale("ben");

//        nav_Menu.findItem(R.id.nav_home).setTitle("Hi");
    }

    public void setLocale(String localeName) {
        /*myLocale = new Locale(localeName);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);
        Intent refresh = new Intent(this, MainActivity.class);
        refresh.putExtra(currentLang, localeName);
        startActivity(refresh);*/

        if (!localeName.equals(currentLanguage)) {
            myLocale = new Locale(localeName);
            Resources res = getResources();
            DisplayMetrics dm = res.getDisplayMetrics();
            Configuration conf = res.getConfiguration();
            conf.locale = myLocale;
            res.updateConfiguration(conf, dm);
            Intent refresh = new Intent(this, MainActivity.class);
            refresh.putExtra(currentLang, localeName);
            startActivity(refresh);
        } else {
//            Toast.makeText(MainActivity.this, "Language already selected!", Toast.LENGTH_SHORT).show();
        }
    }

    //===========Code to get data from api using volley and load data to recycler view, starts==========
    public void loadData(String catId){
        final JSONObject DocumentElementobj = new JSONObject();
        final ProgressDialog loading = ProgressDialog.show(this, "Loading", "Please wait...", true, false);
        try {
            DocumentElementobj.put("catId", catId);
        }catch (JSONException e){
            e.printStackTrace();
        }

        JsonObjectRequest request_json = null;
        String url = Url.BasrUrl +"REST/News/getNews";
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

                                    getResponseData(responseData, loading);

                                   /* if (resobj.getString("status").contentEquals("Success")) {
//                                       Toast.makeText(getApplicationContext(),resobj.getString("message"),Toast.LENGTH_LONG).show();

                                    } else {
                                        Toast.makeText(getApplicationContext(), resobj.getString("message"), Toast.LENGTH_LONG).show();
                                    }*/
                                } catch (JSONException e) {
                                      loading.dismiss();
                                    Toast.makeText(getApplicationContext(),"Internal Server Error",Toast.LENGTH_LONG).show();
                                    e.printStackTrace();
                                }

                            } catch (Exception e) {
                                loading.dismiss();
                                Toast.makeText(getApplicationContext(),"Internal Server Error",Toast.LENGTH_LONG).show();
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    loading.dismiss();
                    Toast.makeText(getApplicationContext(),"Internal Server Error",Toast.LENGTH_LONG).show();
                    VolleyLog.e("Error: ", error.getMessage());
                }
            });
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            requestQueue.add(request_json);
        }catch (JSONException e){
            loading.dismiss();
            Toast.makeText(getApplicationContext(),"Internal Server Error",Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
    public void getResponseData(String response, ProgressDialog loading){
        try {
            if(!newsFeedModelArrayList.isEmpty()){
                newsFeedModelArrayList.clear();
            }
            JSONObject jsonObject = new JSONObject(response);
            Log.d("jsonData-=>",jsonObject.toString());
//            JSONObject jsonObject1 = jsonObject.getJSONObject("response");
            if(jsonObject.getString("status").contentEquals("Success")){
                ll_recycler.setVisibility(View.VISIBLE);
                tv_nodata.setVisibility(View.GONE);
                JSONArray jsonArray = jsonObject.getJSONArray("data");
                for(int i=0; i<jsonArray.length(); i++){
                    JSONObject jsonObject2 = jsonArray.getJSONObject(i);
                    NewsFeedModel newsFeedModel = new NewsFeedModel();

                    newsFeedModel.setId(jsonObject2.getString("id"));
                    newsFeedModel.setPostTitle(jsonObject2.getString("postTitle"));
                    newsFeedModel.setPostImage(Url.BasrUrl+"admin/postimages/"+jsonObject2.getString("postImage"));
//                    newsFeedModel.setPostImage(jsonObject2.getString("postImage"));
                    newsFeedModel.setCategory(jsonObject2.getString("category"));
                    newsFeedModel.setCid(jsonObject2.getString("cid"));
                    newsFeedModel.setPostDetails(jsonObject2.getString("PostDetails"));
                    newsFeedModel.setPostingdate(jsonObject2.getString("postingdate"));
                    newsFeedModel.setUrl(jsonObject2.getString("url"));

                    newsFeedModelArrayList.add(newsFeedModel);

                }
//                recycler_view.setAdapter(new CustomOutdoorListAdapter(OutdoorListActivity.this, outdoorListActivityArrayList));
                if(!newsFeedModelArrayList.isEmpty()) {
                    recycler_view.setAdapter(customMainActivityAdapter);
                }else{
                    ll_recycler.setVisibility(View.GONE);
                    tv_nodata.setVisibility(View.VISIBLE);
                    tv_nodata.setText(getResources().getString(R.string.recycler_list_no_data));
                }
                loading.dismiss();
            }/*else if(jsonObject.getString("status").contentEquals("false")){
                ll_recycler.setVisibility(View.GONE);
                tv_nodata.setVisibility(View.VISIBLE);
                tv_nodata.setText(jsonObject1.getString("message"));
            }*/

        } catch (JSONException e) {
            e.printStackTrace();
            loading.dismiss();
        }
    }

    //===========Code to get data from api and load data to recycler view, ends==========

    //----code to get data from api for searching, starts---------
    public void loadSearchData(String search_data){
        final JSONObject DocumentElementobj = new JSONObject();
        final ProgressDialog loading = ProgressDialog.show(this, "Loading", "Please wait...", true, false);
        try {
            DocumentElementobj.put("searchContain", search_data);
        }catch (JSONException e){
            e.printStackTrace();
        }

        JsonObjectRequest request_json = null;
        String url = Url.BasrUrl +"REST/News/searchNews";
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

                                    getResponseSearchData(responseData, loading);

                                   /* if (resobj.getString("status").contentEquals("Success")) {
//                                       Toast.makeText(getApplicationContext(),resobj.getString("message"),Toast.LENGTH_LONG).show();

                                    } else {
                                        Toast.makeText(getApplicationContext(), resobj.getString("message"), Toast.LENGTH_LONG).show();
                                    }*/
                                } catch (JSONException e) {
                                    loading.dismiss();
                                    e.printStackTrace();
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    VolleyLog.e("Error: ", error.getMessage());
                }
            });
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            requestQueue.add(request_json);
        }catch (JSONException e){
            e.printStackTrace();
        }
    }

    public void getResponseSearchData(String response, ProgressDialog loading){
        try {
            if(!newsFeedModelArrayList.isEmpty()){
                newsFeedModelArrayList.clear();
            }
            JSONObject jsonObject = new JSONObject(response);
            Log.d("jsonData-=>",jsonObject.toString());
//            JSONObject jsonObject1 = jsonObject.getJSONObject("response");
            if(jsonObject.getString("status").contentEquals("Success")){
                ll_recycler.setVisibility(View.VISIBLE);
                tv_nodata.setVisibility(View.GONE);
                JSONArray jsonArray = jsonObject.getJSONArray("data");
                for(int i=0; i<jsonArray.length(); i++){
                    JSONObject jsonObject2 = jsonArray.getJSONObject(i);
                    NewsFeedModel newsFeedModel = new NewsFeedModel();

                    newsFeedModel.setId(jsonObject2.getString("id"));
                    newsFeedModel.setPostTitle(jsonObject2.getString("postTitle"));
                    newsFeedModel.setPostImage(Url.BasrUrl+"admin/postimages/"+jsonObject2.getString("postImage"));
//                    newsFeedModel.setPostImage(jsonObject2.getString("postImage"));
                    newsFeedModel.setCategory(jsonObject2.getString("category"));
                    newsFeedModel.setCid(jsonObject2.getString("cid"));
                    newsFeedModel.setPostDetails(jsonObject2.getString("PostDetails"));
                    newsFeedModel.setPostingdate(jsonObject2.getString("postingdate"));
                    newsFeedModel.setUrl(jsonObject2.getString("url"));

                    newsFeedModelArrayList.add(newsFeedModel);

                }
//                recycler_view.setAdapter(new CustomOutdoorListAdapter(OutdoorListActivity.this, outdoorListActivityArrayList));
                if(!newsFeedModelArrayList.isEmpty()) {
                    recycler_view.setAdapter(customMainActivityAdapter);
                }else{
                    ll_recycler.setVisibility(View.GONE);
                    tv_nodata.setVisibility(View.VISIBLE);
                    tv_nodata.setText(getResources().getString(R.string.recycler_list_no_data));
                }
                loading.dismiss();
            }/*else if(jsonObject.getString("status").contentEquals("false")){
                ll_recycler.setVisibility(View.GONE);
                tv_nodata.setVisibility(View.VISIBLE);
                tv_nodata.setText(jsonObject1.getString("message"));
            }*/

        } catch (JSONException e) {
            e.printStackTrace();
            loading.dismiss();
        }
    }
    //----code to get data from api for searching, ends---------

    boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {
        if(doubleBackToExitPressedOnce)
        {
            //  super.onBackPressed();
            Intent a = new Intent(Intent.ACTION_MAIN);
            a.addCategory(Intent.CATEGORY_HOME);
            a.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(a);

        }
        else {
            doubleBackToExitPressedOnce = true;

            Toast.makeText(getApplicationContext(),"Press BACK once more to exit",Toast.LENGTH_LONG).show();
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;

                }

            }, 2000);
        }


    }

    //-------added for file permission on 7th july, code starts
    private void requestMultiplePermissions() {
        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        // check if all permissions are granted
                        if (report.areAllPermissionsGranted()) {
                            Toast.makeText(getApplicationContext(), "All permissions are granted by user!", Toast.LENGTH_SHORT).show();
                        }

                        // check for permanent denial of any permission
                        if (report.isAnyPermissionPermanentlyDenied()) {
                            // show alert dialog navigating to Settings
                            //openSettingsDialog();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).
                withErrorListener(new PermissionRequestErrorListener() {
                    @Override
                    public void onError(DexterError error) {
                        Toast.makeText(getApplicationContext(), "Some Error! ", Toast.LENGTH_SHORT).show();
                    }
                })
                .onSameThread()
                .check();
    }
    //-------added for file permission on 7th july, code ends


    //-----function to save device id and fcm token using volley, code starts (added on 3rd sept)-------
    public void register_device_info(){
        String android_id = Settings.Secure.getString(getApplicationContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);

        final JSONObject DocumentElementobj = new JSONObject();
        final ProgressDialog loading = ProgressDialog.show(this, "Loading", "Please wait...", true, false);
        try {
            DocumentElementobj.put("device_id", android_id);
            // Get token
            context = MainActivity.this;
            FirebaseApp.initializeApp(context);
            DocumentElementobj.put("fcm_token", FirebaseInstanceId.getInstance().getToken());


        }catch (JSONException e){
            e.printStackTrace();
        }

        Log.d("JsonObjecttoken-=>",DocumentElementobj.toString());
        JsonObjectRequest request_json = null;
        String url = Url.BasrUrl +"REST/News/save_apk_info";
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

                                    if (resobj.getString("status").contentEquals("success")) {
                                        loading.dismiss();
                                        Toast.makeText(getApplicationContext(),"Data saved successfully",Toast.LENGTH_LONG).show();

                                        sharedPreferences_one_time_register = getApplication().getSharedPreferences("RegistrationDetails", Context.MODE_PRIVATE);
                                        SharedPreferences.Editor editor = sharedPreferences_one_time_register.edit();
                                        editor.putString("onetime_register", "true");
                                        editor.commit();

                                    } else {
                                        Toast.makeText(getApplicationContext(), "Unable to register your device", Toast.LENGTH_LONG).show();
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
    //-----function to save device id and fcm token using volley, code ends-------
}