package com.example.mosaic.forrent;

import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.support.v7.widget.CardView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class ScrollingActivity extends AppCompatActivity {



    private List<JSONObject> mDataset = new ArrayList<JSONObject>();
    RecyclerView mRecyclerView;
    LinearLayoutManager mLayoutManager;
    private ViewAdapter mAdapter;

    public void register(){
        try
        {
            final FormPoster formPoster = new FormPoster(new URL("http://news-thwala.rhcloud.com/reg"));
            formPoster.add("data", "data");
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try
                    {
                        InputStream is = formPoster.post();
                        BufferedReader rd = new BufferedReader(new InputStreamReader(is));

                        final StringBuilder response = new StringBuilder(); // or StringBuffer if not Java 5+
                        String line;
                        while ((line = rd.readLine()) != null)
                        {
                            response.append(line);
                            response.append('\r');
                        }
                        rd.close();
                        final String objString = response.toString();
                        try {
                            JSONObject jsonObject = new JSONObject(objString);
                            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ScrollingActivity.this);
                            sp.edit().putString("uid",jsonObject.getString("uid")).apply();

                        }catch (JSONException jse){
                            Log.e("JSONE",jse.toString());
                        }
                        Log.e("News",objString);
                    } catch (final IOException ioe)
                    {
                        Log.e("ERROR",ioe.toString());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(ScrollingActivity.this,"Check network",Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            });
            thread.start();
        }
        catch (MalformedURLException me)
        {
            Log.e("NetWork Exception", me.toString());
        }
    }

    public void getHouses(){
        // Write a message to the database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("message");

        myRef.setValue("Hello, World!");
    }


    public boolean isRegistered(){
        boolean registered=false;
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        registered=sp.contains("uid");
        return registered;
    }


    private void getNews(){
        try
        {
            final FormPoster formPoster = new FormPoster(new URL("http://news-thwala.rhcloud.com/"));
            String uid = PreferenceManager.getDefaultSharedPreferences(this).getString("uid","Unregistered");
            formPoster.add("uid",uid);
            formPoster.add("data", "data");

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try
                    {
                        InputStream is = formPoster.post();
                        BufferedReader rd = new BufferedReader(new InputStreamReader(is));

                        final StringBuilder response = new StringBuilder(); // or StringBuffer if not Java 5+
                        String line;
                        while ((line = rd.readLine()) != null)
                        {
                            response.append(line);
                            response.append('\r');
                        }
                        rd.close();
                        final String objString = response.toString();
                        try {
                            JSONArray array = new JSONArray(objString);
                            mDataset.clear();
                            for (int i=0;i<array.length();i++){
                                mDataset.add(array.getJSONObject(i));
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mAdapter.notifyDataSetChanged();
                                }
                            });

                        }catch (JSONException jse){
                            Log.e("JSONE",jse.toString());
                        }
                        Log.e("News",objString);
                    } catch (final IOException ioe)
                    {
                        Log.e("ERROR",ioe.toString());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(ScrollingActivity.this,"Check network",Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            });
            thread.start();
        }
        catch (MalformedURLException me)
        {
            Log.e("NetWork Exception", me.toString());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getHouses();
        setContentView(R.layout.activity_scrolling);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getNews();
                Snackbar.make(view, "Refreshed", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getNews();
        if(!isRegistered()){
            register();
        }
        mRecyclerView = (RecyclerView)findViewById(R.id.booking_rv);
        mAdapter=new ViewAdapter();
        mRecyclerView.setAdapter(mAdapter);
        mLayoutManager=new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_scrolling, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        if(id==R.id.feedback){
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            LayoutInflater inflater = (LayoutInflater)getSystemService( Context.LAYOUT_INFLATER_SERVICE );
            final View v = inflater.inflate(R.layout.feedback_layout, null);
            final AppCompatEditText et = (AppCompatEditText)v.findViewById(R.id.feedbacket);
            builder.setView(v)
                    .setTitle("Please Enter Feedback")
                    .setPositiveButton("send", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            sendFeedBack(et.getText().toString());
                        }
                    })
                    .create().show();
        }
        return super.onOptionsItemSelected(item);
    }

    public void sendFeedBack(String _f){
        try
        {
            final FormPoster formPoster = new FormPoster(new URL("http://news-thwala.rhcloud.com/feedback"));
            String uid = PreferenceManager.getDefaultSharedPreferences(this).getString("uid","Unregistered");
            formPoster.add("uid",uid);
            formPoster.add("message",_f);
            formPoster.add("data", "data");

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try
                    {
                        InputStream is = formPoster.post();
                        BufferedReader rd = new BufferedReader(new InputStreamReader(is));

                        final StringBuilder response = new StringBuilder(); // or StringBuffer if not Java 5+
                        String line;
                        while ((line = rd.readLine()) != null)
                        {
                            response.append(line);
                            response.append('\r');
                        }
                        rd.close();
                        final String objString = response.toString();
                        try {
                            JSONArray array = new JSONArray(objString);
                            mDataset.clear();
                            for (int i=0;i<array.length();i++){
                                mDataset.add(array.getJSONObject(i));
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mAdapter.notifyDataSetChanged();
                                }
                            });

                        }catch (JSONException jse){
                            Log.e("JSONE",jse.toString());
                        }
                        Log.e("News",objString);
                    } catch (final IOException ioe)
                    {
                        Log.e("ERROR",ioe.toString());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(ScrollingActivity.this,"Check network",Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            });
            thread.start();
        }
        catch (MalformedURLException me)
        {
            Log.e("NetWork Exception", me.toString());
        }
    }

    class ViewAdapter  extends RecyclerView.Adapter<ViewAdapter.ViewHolder>{



        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            CardView v = (CardView) LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.content_scrolling, parent, false);
            // set the view's size, margins, paddings and layout parameters

            ViewHolder vh = new ViewHolder(v);
            return vh;
        }

        @Override
        public int getItemCount() {
            return mDataset.size();
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            try {
                holder.titleTv.setText(mDataset.get(position).getString("head"));
                holder.summaryTv.setText(mDataset.get(position).getString("body"));
                holder.storyTv.setText(mDataset.get(position).getString("story"));
                holder.more.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        holder.more.setText(holder.storyTv.getVisibility()==View.VISIBLE?"more":"less");
                        holder.storyTv.setVisibility(holder.storyTv.getVisibility()==View.VISIBLE?View.GONE:View.VISIBLE);
                    }
                });
            }catch (JSONException jse){
                Log.e("ERROR",jse.toString());
            }
        }

        class ViewHolder extends RecyclerView.ViewHolder{
            public TextView titleTv;
            public TextView summaryTv;
            public AppCompatTextView storyTv;
            public AppCompatTextView more;

            public ViewHolder(CardView cv){
                super(cv);
                titleTv=(TextView) cv.findViewById(R.id.booker_tv);
                summaryTv=(TextView) cv.findViewById(R.id.booker_details_tv);
                storyTv=(AppCompatTextView)cv.findViewById(R.id.storytv);
                more = (AppCompatTextView)cv.findViewById(R.id.book_btn);
            }
        }
    }

    class QueryString
    {
        private StringBuilder query = new StringBuilder();
        public QueryString() {
        }
        public synchronized void add(String name, String value) {
            query.append('&');
            encode(name, value);
        }
        private synchronized void encode(String name, String value) {
            try {
                query.append(URLEncoder.encode(name, "UTF-8"));
                query.append('=');
                query.append(URLEncoder.encode(value, "UTF-8"));
            } catch (UnsupportedEncodingException ex) {
                throw new RuntimeException("Broken VM does not support UTF-8");
            }
        }
        public synchronized String getQuery() {
            return query.toString();
        }
        @Override
        public String toString() {
            return getQuery();
        }
    }

    class FormPoster
    {
        //Always Override For QUserListner Interface



        private URL url;
        // from Chapter 5, Example 5-8
        private QueryString query = new QueryString();

        public FormPoster(URL url) {
            if (!url.getProtocol().toLowerCase().startsWith("http")) {
                throw new IllegalArgumentException(
                        "Posting only works for http URLs");
            }
            this.url = url;
        }

        public void add(String name, String value) {
            query.add(name, value);
        }

        public URL getURL() {
            return this.url;
        }

        public InputStream post() throws IOException {
// open the connection and prepare it to POST

            HttpURLConnection uc = (HttpURLConnection)url.openConnection();
            uc.setDoOutput(true);
            try {
                OutputStreamWriter out
                        = new OutputStreamWriter(uc.getOutputStream(), "UTF-8");
// The POST line, the Content-type header,
// and the Content-length headers are sent by the URLConnection.
// We just need to send the data
                out.write(query.toString());
                out.write("\r\n");
                out.flush();
            }

            catch (UnknownHostException e){}
// Return the response
            return uc.getInputStream();
        }
    }
}
