package com.akshaysadarangani.tabbd;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ConnectivityReceiver.ConnectivityReceiverListener {

    String url, userID, pasteData;
    SwipeRefreshLayout mSwipeRefreshLayout;
    private List<Website> websiteList = new ArrayList<>();
    private WebsiteAdapter mAdapter;
    private ProgressBar progressBar;
    RecyclerView recyclerView;
    FirebaseDatabase database;
    DatabaseReference myRef;
    TextView tip;

    final static String CONNECTIVITY_ACTION = "android.net.conn.CONNECTIVITY_CHANGE";
    IntentFilter intentFilter;
    ConnectivityReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize elements
        Intent intent = getIntent();
        final String userName = intent.getStringExtra("userName");
        userID = intent.getStringExtra("uid");
        final String extra = intent.getStringExtra("extra");
        mSwipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        progressBar =  findViewById(R.id.progressBar);
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("websites");
        //myRef.keepSynced(true);
        tip = findViewById(R.id.connectivityTip);

        // Connection receiver setup
        intentFilter = new IntentFilter();
        intentFilter.addAction(CONNECTIVITY_ACTION);
        receiver = new ConnectivityReceiver();

        // Welcome Snackbar
        View parentLayout = findViewById(android.R.id.content);
        Snackbar snackbar = Snackbar.make(parentLayout, "Welcome back " + userName + "!", Snackbar.LENGTH_LONG);
        // Changing action button text color
        View sbView = snackbar.getView();
        TextView textView = sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.parseColor("#ff4081"));
        snackbar.show();

        // Load into recycler view
        recyclerView = findViewById(R.id.recycler_view);
        mAdapter = new WebsiteAdapter(websiteList);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

        // Pull down to refresh
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                checkConnection();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });

        FloatingActionButton myFab = findViewById(R.id.fab);
        myFab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                popupDialog(null);
            }
        });

        FloatingActionButton fabLogout = findViewById(R.id.fab2);
        fabLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoginManager.getInstance().logOut();
                FirebaseAuth.getInstance().signOut();
                finish();
            }
        });
        if(extra != null) {
            popupDialog(extra);
        }

    }

    // Method to check connection status
    private void checkConnection() {
        boolean isConnected = ConnectivityReceiver.isConnected();
        showStatus(isConnected);
    }

    // Showing the status at the bottom of the screen
    private void showStatus(boolean isConnected) {
        String message;
        TextView cnt = findViewById(R.id.connectivity);
        if (isConnected) {
            message = "CONNECTED";
            cnt.setTextColor(Color.parseColor("#00c853"));
        } else {
            message = "OFFLINE";
            String hint = "Pull down from top to try reconnecting";
            tip.setText(hint);
            tip.setTextColor(Color.parseColor("#d50000"));
            cnt.setTextColor(Color.parseColor("#d50000"));
        }
        cnt.setText(message);
        if(mAdapter.getItemCount() > 0) {
            websiteList.clear();
            mAdapter.notifyDataSetChanged();
        }
        loadWebsites();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // register connection status listener
        MyApplication.getInstance().setConnectivityListener(this);
        registerReceiver(receiver, intentFilter);
    }

    @Override
    public void onStop () {
        unregisterReceiver(receiver);
        super.onStop();
    }

    /**
     * Callback will be triggered when there is change in
     * network connection
     */
    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        showStatus(isConnected);
    }

    public void loadWebsites() {
        recyclerView.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        Query query = myRef.orderByChild("creator").equalTo(userID);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                progressBar.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                if(ConnectivityReceiver.isConnected()) {
                    Calendar rightNow = Calendar.getInstance();
                    String date = "Last sync: " + rightNow.getTime();
                    tip.setText(date);
                    tip.setTextColor(Color.parseColor("#00c853"));
                }
            }

            public void onCancelled(DatabaseError dbError) {
                progressBar.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        });

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(mAdapter.getItemCount() > 0) {
                    websiteList.clear();
                    mAdapter.notifyDataSetChanged();
                }
                if(dataSnapshot.exists()) {
                    for(DataSnapshot d : dataSnapshot.getChildren()) {
                        String wid = d.getKey();
                        String url = d.child("url").getValue(String.class);
                        Website w = new Website(wid, url, userID);
                        websiteList.add(w);
                        mAdapter.notifyItemInserted(websiteList.size() - 1);
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        TextView tip = findViewById(R.id.connectivityTip);
        if(ConnectivityReceiver.isConnected()) {
            String data = "Sync in progress";
            tip.setText(data);
            tip.setTextColor(Color.parseColor("#00c853"));
        }
    }

    public void popupDialog(String sharedUrl) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(" ");
        View mView = View.inflate(getApplicationContext(), R.layout.custom_input_dialog, null);
        // Set up the input
        final EditText input = mView.findViewById(R.id.userInputDialog); //new EditText(getApplicationContext());
        final Button paste = mView.findViewById(R.id.paste);
        builder.setView(mView);
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        pasteData = null;
        // Examines the item on the clipboard. If getText() does not return null, the clip item contains the
        // text. Assumes that this application can only handle one item at a time.
        ClipData clip = clipboard.getPrimaryClip();
        ClipData.Item item;
        if (clip != null) {
            // Gets the first item from the clipboard data
            item = clip.getItemAt(0);
            pasteData = item.getText().toString();
        }
        // Gets the clipboard as text.
        // If the string contains data, then the paste operation is done
        paste.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (pasteData != null) {
                    input.setText(pasteData);
                    input.setSelection(input.getText().length());
                }
                else
                    Toast.makeText(MainActivity.this, "No text in clipboard", Toast.LENGTH_LONG).show();
            }
        });

        if(sharedUrl != null)
            input.setText(sharedUrl);
        input.setSelection(input.getText().length());

        // Set up the fab
        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                url = input.getText().toString().trim();
                if(url.isEmpty()) {
                    dialog.cancel();
                    return;
                }
                Website website = new Website(userID, url);
                // Write to the database
                String wID = myRef.push().getKey();
                myRef.child(wID).setValue(website);

            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }
}