/**
 * Game create for competition on http://annimon.com
 *
 * Copyright (C) 2012, 2013 NaikSoftware
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package ua.naiksoftware.chars;

import android.app.*;
import android.os.*;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;
import java.io.*;
import java.security.*;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
//import filelog.Log;

public class Sender extends AsyncTask<Integer, Void, Boolean> {

    private static final String tag = "Sender";
    private String err = "";
    private AlertDialog dialog;
    public static final String URL = "http://eof-cms.h2m.ru/game.php";// game.php заменяем нужным скриптом
    private Activity target;

    public Sender(Activity activity) {
        target = activity;
    }

    /**
     *
     * @param <b>p</b> score and time
     * @return true if ok, else false
     */
    @Override
    protected Boolean doInBackground(Integer... p) {
        Log.d(tag, "doInBacckground begin");
        int score = p[0];
        int time = p[1];
        String model = Build.MANUFACTURER + " " + Build.MODEL;
        String devId = Build.FINGERPRINT;
        if (model == null || model.equals("")) {
            model = "Unknown";
        }
        Log.d(tag, "doInBackground: data set: devId=" + (devId == null ? "null" : devId));
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(URL.replace("game", "chars"));
        Log.d(tag, "doInBackground: httppost created");

        HttpResponse response = null;
        try {
            // Add my data
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(7);
            nameValuePairs.add(new BasicNameValuePair("score", String.valueOf(score)));
            nameValuePairs.add(new BasicNameValuePair("time", String.valueOf(time)));
            nameValuePairs.add(new BasicNameValuePair("l", MainActivity.login));
            nameValuePairs.add(new BasicNameValuePair("p", MainActivity.pass));
            nameValuePairs.add(new BasicNameValuePair("model", model));
            nameValuePairs.add(new BasicNameValuePair("devid", devId));
            nameValuePairs.add(new BasicNameValuePair("valid", md5(MainActivity.login + MainActivity.pass + MainActivity.login.length())));
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            Log.d(tag, "doInBackground: form set, pre execute httpclient");
            // Execute HTTP Post Request
            response = httpclient.execute(httppost);
            Log.d(tag, "doInBackground: executed httpclient");

        } catch (ClientProtocolException e) {
            Log.e(tag, "doInBackground: protocol exception", e);
            err = e.getMessage().replace("eof-cms.h2m.ru", "annimon.com");// security :)
            return false;
        } catch (IOException e) {
            Log.d(tag, "doInBackground: IOException", e);
            err = e.getMessage().replace("eof-cms.h2m.ru", "annimon.com");// security :)
            return false;
        }
        Log.d(tag, "doInBackground: all right (end)");
        return true;
    }

    @Override
    protected void onPreExecute() {
        Log.d(tag, "onPreExecute begin");

        ProgressBar progressBar = new ProgressBar(EngineActivity.CONTEXT);
        progressBar.setIndeterminate(true);

        AlertDialog.Builder builder = new AlertDialog.Builder(EngineActivity.CONTEXT);
        builder.setCustomTitle(progressBar);
        builder.setIcon(android.R.drawable.ic_menu_upload);
        builder.setMessage(R.string.send_record);
        builder.setCancelable(false);
        dialog = builder.create();
        dialog.show();
        Log.d(tag, "onPreExecute end");
    }

    @Override
    protected void onPostExecute(Boolean result) {
        Log.d(tag, "onPostExecute begin with " + result);
        try {
            if (result) {
                Toast toast = Toast.makeText(EngineActivity.CONTEXT, R.string.send_ok, Toast.LENGTH_LONG);
                toast.show();
            } else {
                Toast toast = Toast.makeText(EngineActivity.CONTEXT, err, Toast.LENGTH_LONG);
                toast.show();
            }
            dialog.dismiss();
            target.finish();
        } catch (Exception e) {
            Log.e("Sender", "onPostExecute exception", e);
        }
    }

    /* MD5 hash of string*/
    public static final String md5(final String s) {
        Log.d(tag, "md5 begin");
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();
            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < messageDigest.length; i++) {
                String h = Integer.toHexString(0xFF & messageDigest[i]);
                while (h.length() < 2) {
                    h = "0" + h;
                }
                hexString.append(h);
            }
            Log.d(tag, "md5 end with hash=" + hexString);
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            Log.e(tag, "md5 algorithm excepton", e);
            e.printStackTrace();
        }
        Log.d(tag, "md5 end with empty hash");
        return "";
    }
}
