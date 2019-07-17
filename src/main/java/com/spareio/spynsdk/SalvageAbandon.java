package com.spareio.spynsdk;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class SalvageAbandon extends AppCompatActivity {

    private spynSDK spynSDK;
    private String dealId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_salvage_abandon);
    }

    @Override
    public void onBackPressed() {
        Intent intent = getIntent();
        dealId = intent.getStringExtra(spynSDK.EXTRA_DEALID);
        spynSDK spynSDK = new spynSDK(this, dealId);
        spynSDK.reject();
        super.onBackPressed();
    }

    public void acceptOffer(View view) {
        Intent intent = getIntent();
        dealId = intent.getStringExtra(spynSDK.EXTRA_DEALID);
        spynSDK spynSDK = new spynSDK(this, dealId);
        spynSDK.accept();
        launchPlayStore(view);
    }

    public void closeOfferOverlay(View view) {
        try {
            Intent intent = new Intent(this, Class.forName("com.spareio.spynpartnerapp.MainActivity"));
            startActivity(intent);
        } catch (Exception e) {
            Log.d("Exception", e.toString());
        }
    }

    public void launchPlayStore(View view) {
        Intent intent = getIntent();
        dealId = intent.getStringExtra(spynSDK.EXTRA_DEALID);
        spynSDK spynSDK = new spynSDK(this, dealId);
        String url = spynSDK.getPlaystoreUrl();
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }
}
