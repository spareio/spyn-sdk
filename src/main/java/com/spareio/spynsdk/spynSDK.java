package com.spareio.spynsdk;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.provider.Settings;
import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;

public class spynSDK {

    private Context mContext;
    private String machineId = "";
    private String baseUrl = "https://x.devspare.io/api/v1/launcher/workers/";
    private String dealId = "";
    private JSONObject worker = null;

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    public static final String EXTRA_DEALID = "com.spareio.spareiodemopartnerapp.extra.DEALID";

    public static final String AUTHORITY = "com.spareio.spynSDK.provider";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    public spynSDK(Context context, String dealId) {
        mContext = context;
        setMachineId();
        this.dealId = dealId;
        preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        editor = preferences.edit();
        if (getWorker() == null) {
            register();
        } else {
            showStatusMessage("Device already registered\nworker: " + getMachineId());
        }
    }

    public void salvageAbandon() {
        Intent intent = new Intent(mContext, SalvageAbandon.class);
        mContext.startActivity(intent);
    }

    public void offerSpyn() {
        offer();
        loadSpynData();
        getWorkerFromAPI(true);
    }

    public void showStatusMessage(String message) {
        Intent intent = new Intent();
        intent.setAction("com.example.ACTION_UPDATE_STATUS");
        intent.putExtra("message", message);
        mContext.sendBroadcast(intent);
    }

    // Check if Spyn is installed on the device
    public boolean isAppInstalled() {
        String packageName = "com.spare.spyn";
        try {
            mContext.getPackageManager().getApplicationInfo(packageName, 0);
            return true;
        }
        catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    // Fetch the machineId
    public String getMachineId() {
        return machineId;
    }

    // Fetch the worker payload
    public JSONObject getWorker() {
        JSONObject workerObj;
        try {
            return new JSONObject(preferences.getString("worker", null));
        } catch (Exception e) {
            Log.d("Exception", e.toString());
            return null;
        }
    }

    // Fetch the playstore URL
    public String getPlaystoreUrl() {
        worker = getWorker();
        String app_url;
        try {
             app_url = worker.get("app_url").toString();
        } catch (JSONException e) {
            Log.d("Exception", e.toString());
            app_url = "";
        }

        return app_url;
    }

    // Fetch secret
    public String getSecret() {
        worker = getWorker();
        String secret;
        try {
            secret = worker.get("secret").toString();
        } catch (Exception e) {
            Log.d("Exception", e.toString());
            secret = "";
        }

        return secret;
    }

    // Set the machineId
    private void setMachineId() {
        String uuidString = Settings.Secure.getString(mContext.getContentResolver(), Settings.Secure.ANDROID_ID);
        machineId = UUID.nameUUIDFromBytes(uuidString.getBytes()).toString();
    }

    // Perform device registration
    public void register() {
        RequestQueue queue = Volley.newRequestQueue(mContext);

        JSONObject json = getRegisterVars(this.dealId);
        JsonObjectRequest jsonobj = new JsonObjectRequest(Request.Method.POST, baseUrl, json,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            // Do something here once we have the response
                            Log.d("Registration",  response.toString());
                            editor.putString("worker", response.toString());
                            editor.commit();
                            worker = response;
                            insertPartnerRecord();
                            showStatusMessage("Registered \nworker: " + getMachineId());
                        } catch (Exception e) {
                            Log.d("Exception", "Failed to register");
                            Log.d("Exception", e.toString());
                            showStatusMessage("Registration succeeded but an error has occurred");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String json;

                        NetworkResponse response = error.networkResponse;
                        if(response != null && response.data != null){
                            switch(response.statusCode){
                                case 400: case 412: case 404:
                                    json = new String(response.data);
                                    Log.w("Volley Error", json);
                                    showStatusMessage("Device already registered\nworker: " + getMachineId());
                                    break;
                            }
                        }
                    }
                }
        ){};
        queue.add(jsonobj);
    }

    // Fetch the worker object from the API
    public void getWorkerFromAPI(final Boolean doOffer) {
        String url = baseUrl + getMachineId() + "?secret=" + getSecret();
        RequestQueue queue = Volley.newRequestQueue(mContext);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        editor.putString("worker", response);
                        editor.commit();
                        if (doOffer) {
                            String[] eventsArray = getEvents();
                            if (Arrays.asList(eventsArray).contains("activated")) {
                                Log.d("Message", "it is activated");
                            } else {
                                Log.d("Message", "it is not activated");
                            }

                            if (Arrays.asList(eventsArray).contains("activated")) {
                                Log.d("Status", "It's activated");
                                Intent intent = new Intent(mContext, Success.class);
                                mContext.startActivity(intent);
                            } else if (Arrays.asList(eventsArray).contains("accepted")) {
                                if (!isAppInstalled()) {
                                    // Show interstitial
                                    Log.d("Status", "Accepted and not installed");
                                    Intent intent = new Intent(mContext, Interstitial.class);
                                    intent.putExtra(EXTRA_DEALID, dealId);
                                    mContext.startActivity(intent);
                                } else {
                                    Log.d("Status", "Accepted and installed");
                                    Intent intent = mContext.getPackageManager().getLaunchIntentForPackage("com.spare.spyn");
                                    mContext.startActivity(intent);
                                }
                            } else {
                                Log.d("Status", "else");
                                Intent intent = new Intent(mContext, Interstitial.class);
                                intent.putExtra(EXTRA_DEALID, dealId);
                                mContext.startActivity(intent);
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String json;

                        NetworkResponse response = error.networkResponse;
                        if(response != null && response.data != null){
                            switch(response.statusCode){
                                case 400: case 412: case 404:
                                    json = new String(response.data);
                                    Log.w("Volley Error", json);
                                    break;
                            }
                        }
                    }
                }
        );
        queue.add(stringRequest);
    }

    // Make Offered call
    public void offer() {
        String url = baseUrl + getMachineId() + "/offered/?secret=" + getSecret();
        RequestQueue queue = Volley.newRequestQueue(mContext);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        Log.d("Offered", "Spare has been offered");
                        showStatusMessage("Offer has been shown to the user");
                        getWorkerFromAPI(false);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String json;

                        NetworkResponse response = error.networkResponse;
                        if(response != null && response.data != null){
                            switch(response.statusCode){
                                case 400: case 412: case 404:
                                    json = new String(response.data);
                                    Log.w("Volley Error", json);
                                    break;
                            }
                        }
                    }
                }
        );
        queue.add(stringRequest);
    }

    // Make Accepted call
    public void accept() {
        String url = baseUrl + getMachineId() + "/accepted/?secret=" + getSecret();
        RequestQueue queue = Volley.newRequestQueue(mContext);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        Log.d("Accepted", "Spare has been accepted");
                        showStatusMessage("Offer has been accepted the user");
                        getWorkerFromAPI(false);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String json;

                        NetworkResponse response = error.networkResponse;
                        if(response != null && response.data != null){
                            switch(response.statusCode){
                                case 400: case 412: case 404:
                                    json = new String(response.data);
                                    Log.w("Volley Error", json);
                                    break;
                            }
                        }
                    }
                }
        );
        queue.add(stringRequest);
    }

    // Make Rejected call
    public void reject() {
        String url = baseUrl + getMachineId() + "/rejected/?secret=" + getSecret();
        RequestQueue queue = Volley.newRequestQueue(mContext);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        Log.d("Accepted", "Spare has been rejected");
                        showStatusMessage("Offer has been rejected by the user");
                        getWorkerFromAPI(false);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String json;

                        NetworkResponse response = error.networkResponse;
                        if(response != null && response.data != null){
                            switch(response.statusCode){
                                case 400: case 412: case 404:
                                    json = new String(response.data);
                                    Log.w("Volley Error", json);
                                    break;
                            }
                        }
                    }
                }
        );
        queue.add(stringRequest);
    }

    public JSONObject getRegisterVars(String dealId) {
        JSONObject params = new JSONObject();

        try {
            params.put("os_release", android.os.Build.VERSION.RELEASE);
            params.put("udid", getMachineId());
            params.put("country_code", mContext.getResources().getConfiguration().locale.getCountry());
            params.put("device_manufacturer", Build.MANUFACTURER);
            params.put("device_model", Build.MODEL);
            params.put("device_cpu", getCpu());
            params.put("device_ram", getRam());
            params.put("device_rom", getRom());
            params.put("os_system", "Android");
            params.put("os_tag", "Tag");
            params.put("search_engine", "");
            params.put("browser", getBrowser(mContext));
            params.put("deal", dealId);
        } catch (JSONException e) {
            Log.w("error", e.toString());
        }

        return params;
    }

    private String getCpu() {
        String cpuName = "";
        Map<String, String> map = new HashMap<String, String>();
        try {
            Scanner s = new Scanner(new File("/proc/cpuinfo"));
            while (s.hasNextLine()) {
                String[] vals = s.nextLine().split(": ");
                if (vals.length > 1) map.put(vals[0].trim(), vals[1].trim());
            }
            cpuName = map.get("vendor_id") + " " + map.get("model name");
        } catch (Exception e) {Log.e("getCpuInfoMap",Log.getStackTraceString(e));}

        return cpuName;
    }

    private String getRam() {
        Runtime runtime = Runtime.getRuntime();
        final long memory=runtime.totalMemory();
        return Long.toString(memory);
    }

    private String getRom() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSizeLong();
        long totalBlocks = stat.getBlockCountLong();
        return Long.toString(totalBlocks * blockSize);
    }

    private String getBrowser(Context mContext) {
        Intent browserIntent = new Intent("android.intent.action.VIEW", Uri.parse("http://"));
        ResolveInfo resolveInfo = mContext.getPackageManager().resolveActivity(browserIntent, PackageManager.MATCH_DEFAULT_ONLY);
        return resolveInfo.activityInfo.packageName;
    }

    private String[] getEvents() {
        String[] events;
        try {
            JSONArray eventsJson = getWorker().getJSONArray("events");
            events = new String[eventsJson.length()];
            for (int i = 0 ; i < eventsJson.length(); i++) {
                events[i] = eventsJson.getJSONObject(i).getString("event");
            }

            return events;
        } catch(Exception e) {
            Log.d("Exception", e.toString());
        }

        return new String[0];
    }

    private void insertPartnerRecord(){
        ContentValues values = new ContentValues();
        values.put(spynSDK.SpynPartnerEntry.COLUMN_WORKER,getWorker().toString());

        Uri uri = mContext.getContentResolver().insert(spynSDK.SpynPartnerEntry.CONTENT_URI,values);
    }

    private void loadSpynData() {
        String workerValue;
        Cursor cursor = mContext.getContentResolver()
                .query(spynSDK.SpynPartnerEntry.CONTENT_URI,null,null,null,null);

        try {
            if (cursor.moveToFirst()) {
                try {
                    workerValue = cursor.getString(cursor.getColumnIndex("worker"));
                    Log.d("Worker", workerValue);
                } catch (Exception e) {
                    Log.d("Exception in load", e.toString());
                }
            } else {
                Log.d("loadSpynData", "No data");
            }
        } catch (Exception e) {
            Log.d("Exception", e.toString());
        }
    }

    // Put the workerId in shared resources
    public static final class SpynPartnerEntry implements BaseColumns {
        public static final String TABLE_NAME = "SpynPartner";
        public static final String COLUMN_WORKER = "worker";
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(TABLE_NAME).build();

        public static Uri buildTodoUriWithId(long id) {
            return CONTENT_URI.buildUpon()
                    .appendPath(Long.toString(id))
                    .build();
        }
    }
}
