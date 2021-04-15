package com.example.a475_proj4;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    SharedPreferences myPreference;
    android.content.SharedPreferences.OnSharedPreferenceChangeListener listener;
    TextView tv;
    ImageView image;
    List<Pet> petList  = new ArrayList<>();
    String prefurl = "pets.json";
    String url = "https://www.pcs.cnu.edu/~kperkins/pets/";
    int numberentries = -1;
    JSONArray jarray;
    class Pet{
        public String name;
        public String file;
        public Pet(String name, String file){
            this.name = name;
            this.file = file;
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ConnectionCheck myCheck = new ConnectionCheck(this);

        myPreference = PreferenceManager.getDefaultSharedPreferences(this);
        listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                if (key.equals("prefURL")) {
                    url = myPreference.getString(key, getString(R.string.URLpet));
                    if(myCheck.isNetworkReachable()) {
                        loadImage(petList.get(0).file);
                    }else{

                        //tv.setText(getString(R.string.unreachable));
                        image.setImageResource(R.drawable.ic_action_name);
                        tv.setVisibility(View.VISIBLE);
                    }
                }
            }
        };
        myPreference.registerOnSharedPreferenceChangeListener(listener);
        if (myCheck.isNetworkReachable()) {
            Download myTask = new Download(prefurl);
            myTask.execute(url);
        }
    }
    private void loadImage(String pic) {
        String extendedUrl = url + prefurl + "/" + pic;
        DownloadImage dImage = new DownloadImage();
        try {
            Bitmap bit = dImage.execute(extendedUrl).get();
            image.setImageBitmap(bit);
        }catch(Exception e){

        }
    }
    public void processJSON(String string) {
        try {
            JSONObject jsonobject = new JSONObject(string);
            String petName = "";
            String fileName = "";

            jarray = jsonobject.getJSONArray("pets");
            for(int i = 0;i< jarray.length(); i++){
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
        private static final String     TAG = "Download";
        private String                  myQuery = "";
        protected int                   statusCode = 0;
        protected String                myURL;
        String data;
        public Download(String url){
            myURL = url;
        }

        @Override
        protected String doInBackground(String... params) {
            myURL = params[0];
            try {
                URL url = new URL( myURL + prefurl);
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
                    if(in != null) {
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
            loadImage("p4.png");
        }

        @Override
        protected void onCancelled() {
            //override to handle this
            super.onCancelled();
        }
    };
    private class DownloadImage extends AsyncTask<String, Void, Bitmap>{
        Bitmap bitmap = null;

        @Override
        protected Bitmap doInBackground(String... strings) {

            URL url;
            HttpURLConnection connect;
            InputStream in;
            try{
                url = new URL(strings[0]);
                connect = (HttpURLConnection) url.openConnection();
                in = connect.getInputStream();
                bitmap = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return bitmap;
        }
        @Override
        protected void onPostExecute(Bitmap result){
            super.onPostExecute(result);
            if(bitmap != null) {
                tv.setVisibility(View.INVISIBLE);
                image.setImageBitmap(result);
            }else{
                tv.setVisibility(View.VISIBLE);
                tv.setText("404!");
                //tvSmall.setText(getString(R.string.text2)+ " " + url);
                image.setImageResource(R.drawable.ic_action_name);
            }
        }
    }

}