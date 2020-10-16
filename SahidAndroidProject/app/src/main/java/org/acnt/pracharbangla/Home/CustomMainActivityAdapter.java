package org.acnt.pracharbangla.Home;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.acnt.pracharbangla.Model.NewsFeedModel;
import org.acnt.pracharbangla.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class CustomMainActivityAdapter extends RecyclerView.Adapter<CustomMainActivityAdapter.MyViewHolder> {
    public LayoutInflater inflater;
    public static ArrayList<NewsFeedModel> newsFeedModelArrayList;
    private Context context;
    public static String CustomMainActivityAdapter_image, CustomMainActivityAdapter_news_headline, CustomMainActivityAdapter_news_body, CustomMainActivityAdapter_news_url;

//    public static ProgressDialog loading;
//    public static TextView tv_download;


    public CustomMainActivityAdapter(Context ctx, ArrayList<NewsFeedModel> newsFeedModelArrayList){

        inflater = LayoutInflater.from(ctx);
        this.newsFeedModelArrayList = newsFeedModelArrayList;
    }
    @Override
    public CustomMainActivityAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.activity_main_custom_row, parent, false);
        CustomMainActivityAdapter.MyViewHolder holder = new CustomMainActivityAdapter.MyViewHolder(view);
        context = parent.getContext();
        return holder;
    }

    int row_index;
    @Override
    public void onBindViewHolder(CustomMainActivityAdapter.MyViewHolder holder, final int position) {
        holder.itemView.setTag(newsFeedModelArrayList.get(position));
//        holder.tv_od_status.setText(newsFeedModelArrayList.get(position).getOd_status());

        holder.tv_headline.setText(newsFeedModelArrayList.get(position).getPostTitle());
//        holder.tv_body.setText(newsFeedModelArrayList.get(position).getPostDetails());
//        holder.tv_body.setText(android.text.Html.fromHtml(newsFeedModelArrayList.get(position).getPostDetails()).toString());
//        holder.tv_posting_date.setText("Posted on "+newsFeedModelArrayList.get(position).getPostingdate());
        Picasso.with(context).load(newsFeedModelArrayList.get(position).getPostImage().trim()).networkPolicy(NetworkPolicy.NO_CACHE).fit().into(holder.img_view);
//        Picasso.with(context).load(Url.BasrUrl+"admin/postimages/"+newsFeedModelArrayList.get(position).getPostImage()).into(holder.img_view);
//        Picasso.with(context).load("http://news.webolaf.com/admin/postimages/a0023c09173ba55038dccca3240d8002.jpg").into(holder.img_view);

        DateFormat inputFormat = new SimpleDateFormat("dd/MM/yyyy");
        DateFormat outputFormat = new SimpleDateFormat("dd-MMM-yyyy");

       /* String inputTextFromDate = outDoorListModelArrayList.get(position).getFrom_date();
        String inputTextToDate = outDoorListModelArrayList.get(position).getTo_date();

        Date dateFromDate = null, dateToate = null;
        try {
            dateFromDate = inputFormat.parse(inputTextFromDate);
            dateToate = inputFormat.parse(inputTextToDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if(Double.parseDouble(outDoorListModelArrayList.get(position).getTotal_days())>1) {
            holder.tv_od_date.setText(outputFormat.format(dateFromDate) + " To  " + outputFormat.format(dateToate));
        }else{
            holder.tv_od_date.setText(outputFormat.format(dateFromDate));
        }*/


       holder.relative_layout.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               row_index = position;
               notifyDataSetChanged();
               CustomMainActivityAdapter_image = newsFeedModelArrayList.get(position).getPostImage();
               CustomMainActivityAdapter_news_headline = newsFeedModelArrayList.get(position).getPostTitle();
               CustomMainActivityAdapter_news_body = newsFeedModelArrayList.get(position).getPostDetails();
               CustomMainActivityAdapter_news_url = newsFeedModelArrayList.get(position).getUrl();
               Intent intent = new Intent(context,NewsContentDetails.class);
               context.startActivity(intent);
           }
       });
        if(row_index == position){
            holder.tv_headline.setTextColor(Color.parseColor("#CC0000"));
        }else{
            holder.tv_headline.setTextColor(Color.parseColor("#46B11F"));
        }
    }

    @Override
    public int getItemCount() {
        return newsFeedModelArrayList.size();
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView tv_headline, tv_posting_date;
        ImageView img_view;
        RelativeLayout relative_layout;



        public MyViewHolder(final View itemView) {
            super(itemView);
            tv_headline = itemView.findViewById(R.id.tv_headline);
//            tv_posting_date = itemView.findViewById(R.id.tv_posting_date);
            img_view = itemView.findViewById(R.id.img_view);
            relative_layout = itemView.findViewById(R.id.relative_layout);

            /*relative_layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final int position = getAdapterPosition();
                   *//* OutdoorListActivity.new_create_yn = 0;
                    od_request_id = outDoorListModelArrayList.get(position).getOd_request_id();
                    Intent i = new Intent(context, OutDoorRequestActivity.class);
                    context.startActivity(i);*//*
                    CustomMainActivityAdapter_image = newsFeedModelArrayList.get(position).getPostImage();
                    CustomMainActivityAdapter_news_headline = newsFeedModelArrayList.get(position).getPostTitle();
                    CustomMainActivityAdapter_news_body = newsFeedModelArrayList.get(position).getPostDetails();
                    CustomMainActivityAdapter_news_url = newsFeedModelArrayList.get(position).getUrl();

//                    Intent intent = new Intent(context,NewsDetails.class);
                    Intent intent = new Intent(context,NewsContentDetails.class);
//                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);

                }
            });*/


        }


    }

}
