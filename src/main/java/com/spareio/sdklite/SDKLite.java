package com.spareio.sdklite;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;

public class SDKLite {

    private Context mContext;
    private String machineId = "";
    private String deviceStatus = "";
    private String baseUrl = "https://x.devspare.io/api/v1/launcher/workers/";
    private String playstoreUrl = "";

    public SDKLite(Context context) {
        mContext = context;
        setMachineId();
        getWorker();
    }


    // Set the machineId
    private void setMachineId() {
        String uuidString = Settings.Secure.getString(mContext.getContentResolver(), Settings.Secure.ANDROID_ID);
        machineId = UUID.nameUUIDFromBytes(uuidString.getBytes()).toString();
    }

    // Fetch the machineId
    public String getMachineId() {
        return machineId;
    }

    public String getDeviceStatus() { return deviceStatus; }

    public String getPlaystoreUrl() { return playstoreUrl; }

    public void launchSpareOffer() {
        Intent intent = new Intent(mContext, SpareOffer.class);
        mContext.startActivity(intent);
    }

    private void setDeviceStatus(String newStatus) {
        deviceStatus = newStatus;
        Intent intent = new Intent();
        intent.setAction("com.example.ACTION_UPDATE_STATUS");
        intent.putExtra("status", newStatus);
        mContext.sendBroadcast(intent);
    }

    // Perform device registration
    public void register(String dealId) {
        RequestQueue queue = Volley.newRequestQueue(mContext);

        JSONObject json = getRegisterVars(dealId);
        Log.d("Register vars", json.toString());
        JsonObjectRequest jsonobj = new JsonObjectRequest(Request.Method.POST, baseUrl, json,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            // Do something here once we have the response
                            Log.d("Response", response.toString());
                            setDeviceStatus(response.getString("state"));
                            playstoreUrl = response.getString("app_url");
                        } catch (Exception e) {
                            Log.d("Exception", "Failed to register");
                            Log.d("Exception", e.toString());
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
        ){};
        queue.add(jsonobj);
    }

    // Make Offered call
    public void offer() {
        String url = baseUrl + getMachineId() + "/offered/";
        RequestQueue queue = Volley.newRequestQueue(mContext);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        setDeviceStatus("offered");
                        Log.d("Offered", "Spare has been offered");
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
                                    Log.w("Volley Error", json.toString());
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
        String url = baseUrl + getMachineId() + "/accepted/";
        RequestQueue queue = Volley.newRequestQueue(mContext);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        setDeviceStatus("accepted");
                        Log.d("Accepted", "Spare has been accepted");
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
        String url = baseUrl + getMachineId() + "/rejected/";
        RequestQueue queue = Volley.newRequestQueue(mContext);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        setDeviceStatus("rejected");
                        Log.d("Accepted", "Spare has been accepted");
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

    // Get device status
    private void getWorker() {
        String url = baseUrl + getMachineId();
        RequestQueue queue = Volley.newRequestQueue(mContext);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        try {
                            JSONObject json = new JSONObject(response);
                            setDeviceStatus(json.getString("state"));
                        } catch (JSONException e) {
                            Log.d("Exception", e.toString());
                        }
                        Log.d("Response", response);
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
                                    setDeviceStatus("unregistered");
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
}
