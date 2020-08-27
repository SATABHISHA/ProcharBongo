package org.acnt.newsfeed.Config;

import android.view.View;
import android.widget.TextView;

public class Snackbar {
        public Snackbar(String message, View view, int color){
//        int color = Color.parseColor("#ffffff");
//        android.support.design.widget.Snackbar snackbar = android.support.design.widget.Snackbar.make(view, message, 4000);
            com.google.android.material.snackbar.Snackbar snackbar = com.google.android.material.snackbar.Snackbar.make(view,message,4000);
            View sbView = snackbar.getView();
            TextView textView = (TextView) sbView.findViewById(com.google.android.material.R.id.snackbar_text);
            textView.setTextColor(color);
            snackbar.show();

        }
}
