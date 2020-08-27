package org.acnt.newsfeed.Home;

import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.acnt.newsfeed.R;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;

public class NewsContentDetails extends AppCompatActivity {

    WebView webView;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_content_details);
        webView = (WebView) findViewById(R.id.webView);

            int SDK_INT = android.os.Build.VERSION.SDK_INT;
            if (SDK_INT > 8)
            {
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                        .permitAll().build();
                StrictMode.setThreadPolicy(policy);
                //your codes here

            }

        Log.d("newsurl-=>",CustomMainActivityAdapter.CustomMainActivityAdapter_news_url.trim());
        startWebView(CustomMainActivityAdapter.CustomMainActivityAdapter_news_url);


    }

    private void startWebView(String url) {

        //Create new webview Client to show progress dialog
        //When opening a url or click on link

        webView.setWebViewClient(new WebViewClient() {
//            ProgressDialog progressDialog;

            //If you will not use this method url links are opeen in new brower not in webview
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            //Show loader on url load
            public void onLoadResource (WebView view, String url) {
                /*if (progressDialog == null) {
                    // in standard case YourActivity.this
                    progressDialog = new ProgressDialog(MainActivity.this);
                    progressDialog.setMessage("Loading...");
                    progressDialog.show();
                }*/
            }
            public void onPageFinished(WebView view, String url) {
              /*  try{
                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                        progressDialog = null;
                    }
                }catch(Exception exception){
                    exception.printStackTrace();
                }*/


            }


        });

        // Javascript inabled on webview

        try {
            Document document = Jsoup.connect(url).get();
            document.getElementsByClass("navbar fixed-top navbar-expand-lg navbar-dark bg-dark fixed-top").remove();
//            document.getElementsByClass("footer").remove();
            WebSettings ws = webView.getSettings();
//            webView.getSettings().setJavaScriptEnabled(true);
            ws.setJavaScriptEnabled(true);
            webView.loadDataWithBaseURL(url, document.toString(), "text/html", "utf-8", "");

        }catch (Exception e){
            e.printStackTrace();
        }


        /*
         String summary = "<html><body>You scored <b>192</b> points.</body></html>";
         webview.loadData(summary, "text/html", null);
         */

        //Load url in webview
//        webView.loadUrl(url);

    }


    // Open previous opened link from history on webview when back button pressed

    @Override
    // Detect when the back button is pressed
    public void onBackPressed() {
        if(webView.canGoBack()) {
            webView.goBack();
        } else {
            // Let the system handle the back button
            super.onBackPressed();
        }
    }


}
