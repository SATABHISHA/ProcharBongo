package org.acnt.newsfeed.Home;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.view.GravityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.acnt.newsfeed.Config.Url;
import org.acnt.newsfeed.Model.NewsFeedModel;
import org.acnt.newsfeed.R;
import org.acnt.newsfeed.Video.VideoHomeActivity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;

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
    SharedPreferences sharedPreferences;
    ImageButton imgbtn_search, imgbtn_home, imgbtn_add, imgbtn_profile;
    Context context;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ll_recycler = findViewById(R.id.ll_recycler);
        tv_nodata = findViewById(R.id.tv_nodata);
        imgbtn_search = findViewById(R.id.imgbtn_search);
        imgbtn_home = findViewById(R.id.imgbtn_home);
        imgbtn_add = findViewById(R.id.imgbtn_add);
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
    }

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
}