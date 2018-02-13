package com.akshaysadarangani.tabbd;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;


/**
 * Created by Akshay on 8/4/2017.
 */

public class WebsiteAdapter extends RecyclerView.Adapter<WebsiteAdapter.MyViewHolder> {
    private List<Website> websiteList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView url;
        private ImageView del;

        private MyViewHolder(View view) {
            super(view);
            url = view.findViewById(R.id.url);
            del = view.findViewById(R.id.deleteIcon);
        }
    }

    public WebsiteAdapter(List<Website> websiteList) {
        this.websiteList = websiteList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.website_list_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        Website website = websiteList.get(position);
        holder.url.setText(website.url);
        final int pos = position;
        // Delete website
        holder.del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference myRef = database.getReference("websites");
                String wid = websiteList.get(pos).wid;
                myRef.child(wid).removeValue();
                websiteList.remove(pos);
                notifyItemRemoved(pos);
                notifyItemRangeChanged(pos, websiteList.size());
            }
        });
        // Open link in browser
        holder.url.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = websiteList.get(pos).url;
                if (!url.startsWith("http://") && !url.startsWith("https://"))
                    url = "http://" + url;
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                view.getContext().startActivity(browserIntent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return websiteList.size();
    }
}


