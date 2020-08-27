package org.acnt.newsfeed.Home;

import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.DisplayMetrics;
import android.util.Log;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;

import com.squareup.picasso.Picasso;

import org.acnt.newsfeed.R;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NewsDetails extends AppCompatActivity {
    TextView tv_news_headline, tv_news_body;
    ImageView img_news;
    WebView webview;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_detail);

        tv_news_headline = findViewById(R.id.tv_news_headline);
//        tv_news_body = findViewById(R.id.tv_news_body);
        webview = findViewById(R.id.webview);
        img_news = findViewById(R.id.img_news);
        Picasso.with(this).load(CustomMainActivityAdapter.CustomMainActivityAdapter_image.trim()).fit().into(img_news);
        tv_news_headline.setText(CustomMainActivityAdapter.CustomMainActivityAdapter_news_headline);
       /* tv_news_body.setText(android.text.Html.fromHtml(CustomMainActivityAdapter.CustomMainActivityAdapter_news_body));
        Linkify.addLinks(tv_news_body, Linkify.ALL);*/
        Log.d("test",CustomMainActivityAdapter.CustomMainActivityAdapter_news_body);



      /*  webview.loadData(CustomMainActivityAdapter.CustomMainActivityAdapter_news_body.trim(),"text/html","utf-8");
//        webview.loadUrl("http://www.youtube.com/");
        webview.getSettings().setJavaScriptEnabled(true);
        webview.setWebChromeClient(new WebChromeClient());
        webview.setWebViewClient(new WebViewClient());*/

        webview.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }
        });
        WebSettings webSettings = webview.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webview.loadData(CustomMainActivityAdapter.CustomMainActivityAdapter_news_body, "text/html", "utf-8");

    }
}



