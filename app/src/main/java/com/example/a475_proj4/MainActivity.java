package com.example.a475_proj4;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    SharedPreferences myPreference;
    android.content.SharedPreferences.OnSharedPreferenceChangeListener listener;
    List<Pet> petList = new ArrayList<>();
    String prefurl = "pets.json";
    String url = "https://www.pcs.cnu.edu/~kperkins/pets/";
    int numberentries = -1;
    JSONArray jarray;
    ViewPager2 vp;
    ViewPager2_Adapter csa;

    class Pet {
        public String name;
        public String file;

        public Pet(String name, String file) {
            this.name = name;
            this.file = file;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //setSupportActionBar((Toolbar)findViewById(R.id.toolbar1));
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        ConnectionCheck myCheck = new ConnectionCheck(this);


        myPreference = PreferenceManager.getDefaultSharedPreferences(this);
        listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                if (key.equals("prefURL")) {
                    url = myPreference.getString(key, getString(R.string.URLpet));
                    if (myCheck.isNetworkReachable()) {
                        //loadImage(petList.get(0).file);
                       downloadJson();
                    } else {
                        petList.clear();
                        vp = findViewById(R.id.view_pager);
                        csa = new ViewPager2_Adapter(MainActivity.this);
                        vp.setAdapter(csa);
                        csa.notifyDataSetChanged();
                        //tv.setText(getString(R.string.unreachable));
                    }
                }
            }
        };
        myPreference.registerOnSharedPreferenceChangeListener(listener);
        if (myCheck.isNetworkReachable()) {
           downloadJson();
        }
    }

    public void downloadJson(){
        Download myTask = new Download(url);
        myTask.execute(prefurl);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu) {
            startActivity(new Intent(this, PreferenceSettings.class));
        }

        return super.onOptionsItemSelected(item);
    }
    /*private void loadImage(String pic) {
        String extendedUrl = url + pic;
        DownloadImage dImage = new DownloadImage();
        try {
            Bitmap bit = dImage.execute(extendedUrl).get();
            image.setImageBitmap(bit);
        } catch (Exception e) {

        }
    }*/

    public void processJSON(String string) {
        try {
            JSONObject jsonobject = new JSONObject(string);
            String petName = "";
            String fileName = "";

            jarray = jsonobject.getJSONArray("pets");
            for (int i = 0; i < jarray.length(); i++) {
                JSONObject jobj = jarray.getJSONObject(i);
                petName = jobj.get("name") + "";
                fileName = jobj.get("file") + "";
                Pet tmp = new Pet(petName, fileName);
                petList.add(tmp);
            }
            numberentries = jarray.length();


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private class Download extends AsyncTask<String, Void, String> {
        private static final String TAG = "Download";
        private String myQuery = "";
        protected int statusCode = 0;
        protected String myURL;
        String data;

        public Download(String url) {
            myURL = url;
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL(myURL + params[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                BufferedReader in = null;
                try {
                    connection.connect();

                    statusCode = connection.getResponseCode();
                    if (statusCode / 100 != 2) {
                        Log.e(TAG, "Error-connection.getResponseCode returned "
                                + Integer.toString(statusCode));
                        return null;
                    }

                    in = new BufferedReader(new InputStreamReader(connection.getInputStream()), 8096);
                    String myData;
                    StringBuffer sb = new StringBuffer();

                    while ((myData = in.readLine()) != null) {
                        sb.append(myData);
                    }
                    return sb.toString();

                } finally {
                    if (in != null) {
                        in.close();
                    }
                    connection.disconnect();
                }
            } catch (Exception exc) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            processJSON(result);
            csa = new ViewPager2_Adapter(MainActivity.this);
            vp = findViewById(R.id.view_pager);
            vp.setAdapter(csa);
            csa.notifyDataSetChanged();
            //loadImage("p4.png");
        }

        @Override
        protected void onCancelled() {
            //override to handle this
            super.onCancelled();
        }
    }


    public class ViewPager2_Adapter extends RecyclerView.Adapter {
        private final Context ctx;
        private final LayoutInflater li;

        class PagerViewHolder extends RecyclerView.ViewHolder {
            private static final int UNINITIALIZED = -1;
            ImageView iv;
            TextView tv;
            int position = UNINITIALIZED;
            public PagerViewHolder(@NonNull View itemView) {
                super(itemView);
                iv = (ImageView) itemView.findViewById(R.id.imageView1);
                tv = (TextView) itemView.findViewById(R.id.tvSmall);
            }
        }

        private class GetImage extends AsyncTask<String, Void, Bitmap> {
            private PagerViewHolder myVh;
            private int original_position;
            private static final String     TAG = "ImageDownloadTask";
            private static final int        DEFAULTBUFFERSIZE = 50;
            private static final int        NODATA = -1;
            private int                     statusCode=0;
            private String                  url = "https://www.pcs.cnu.edu/~kperkins/pets/";

            public GetImage(PagerViewHolder myVh) {
                this.myVh = myVh;
                this.original_position = myVh.position;
            }

            @Override
            protected Bitmap doInBackground(String... params) {
                try {
                    Thread.sleep(1000);
                    java.net.URL url1 = new URL(params[0]);

                    // this does no network IO
                    HttpURLConnection connection = (HttpURLConnection) url1.openConnection();

                    // this opens a connection, then sends GET & headers
                    connection.connect();

                    statusCode = connection.getResponseCode();

                    if (statusCode / 100 != 2) {
                        Log.e(TAG, "Error-connection.getResponseCode returned "
                                + Integer.toString(statusCode));
                        return null;
                    }

                    InputStream is = connection.getInputStream();
                    BufferedInputStream bis = new BufferedInputStream(is);

                    // the following buffer will grow as needed
                    ByteArrayOutputStream baf = new ByteArrayOutputStream(DEFAULTBUFFERSIZE);
                    int current = 0;

                    // wrap in finally so that stream bis is sure to close
                    try {
                        while ((current = bis.read()) != NODATA) {
                            baf.write((byte) current);
                        }

                        // convert to a bitmap
                        byte[] imageData = baf.toByteArray();
                        return BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
                    } finally {
                        // close resource no matter what exception occurs
                        bis.close();
                    }
                } catch (Exception exc) {
                    return null;
                }
            }

            @Override
            protected void onPostExecute(Bitmap param) {
                if (this.myVh.position == this.original_position) {
                    myVh.iv.setImageBitmap(param);
                } else
                    Toast.makeText(ViewPager2_Adapter.this.ctx, "YIKES! Recycler view reused, my result is useless", Toast.LENGTH_SHORT).show();
            }


        }


        public ViewPager2_Adapter(Context ctx) {
            this.ctx = ctx;
            li = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = li.inflate(R.layout.swipe, parent, false);
            return new PagerViewHolder(view);   //the new one
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            PagerViewHolder viewHolder = (PagerViewHolder) holder;
            ConnectionCheck check = new ConnectionCheck(MainActivity.this);
            if(!check.isNetworkReachable() || petList.isEmpty()){
                viewHolder.iv.setImageResource(R.drawable.ic_action_name);
            }
            viewHolder.tv.setText(petList.get(position).name);
            viewHolder.position = position;
            GetImage myTask = new GetImage(viewHolder);
            myTask.execute(new String[]{url + petList.get(position).file});
        }

        @Override
        public int getItemCount() {
            return petList.size();
        }
    }
}