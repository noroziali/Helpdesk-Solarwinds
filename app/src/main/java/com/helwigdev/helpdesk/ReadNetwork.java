package com.helwigdev.helpdesk;

import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.internal.framed.Header;

/**
 * Created by helwig on 10/13/2015.
 */
public class ReadNetwork extends AsyncTask<URL, Void, String> {

    private static final String TAG = "ReadNetwork";
    private final boolean useCookie;
    private final String customCookie;


    public ReadNetwork(int TaskID, RNInterface rnInterface, boolean useCookie, String cookie) {
        this.taskID = TaskID;
        this.rn = rnInterface;
        this.useCookie = useCookie;
        this.customCookie = cookie;
    }

    int taskID;
    RNInterface rn;
    int errType = 0;
    String cookie = null;

    @Override
    protected String doInBackground(URL... params) {

        for (URL url : params) {

            try {

                OkHttpClient client = new OkHttpClient();
                client.followSslRedirects();
                Request request;
                Log.d("OKHTTP",url.toString());
                if(useCookie){
                    Log.d("OKHTTP","Using cookie!");
                    request = new Request.Builder()
                            .url(url)
                            .get()
                            .addHeader("Cookie", customCookie + "")
                            .build();
                } else {
                    request = new Request.Builder()
                            .url(url)
                            .get()
                            .build();
                }

                //get cookie - because the REST api also requires a consistent cookie
                //this feature is undocumented.
                Response response = client.newCall(request).execute();
                String body = response.body().string();
                List<String> headerList = response.headers("Set-Cookie");
                for(String header : headerList) {
                    cookie = header;
                }
                Log.d("OKHTTP",body);
                return body;
            }catch (IOException e) {
                e.printStackTrace();
            }

        }
        return "error";
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        if(cookie != null) rn.setCookie(cookie);
        if(s.equals("error")){
            rn.authErr(errType, taskID);
        } else {
            rn.processResult(s, taskID);
        }
    }
}
