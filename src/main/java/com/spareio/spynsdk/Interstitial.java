package com.spareio.spynsdk;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class Interstitial extends AppCompatActivity {

    private spynSDK spynSDK;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interstitial);

        Intent intent = getIntent();
        String dealId = intent.getStringExtra(spynSDK.EXTRA_DEALID);

        spynSDK = new spynSDK(this, dealId);
    }

    @Override
    public void onBackPressed() {
        spynSDK.reject();
        super.onBackPressed();
    }

    public void acceptOffer(View view) {
        spynSDK.accept();
        launchPlayStore(view);
    }

    public void launchPlayStore(View view) {
        String url = spynSDK.getPlaystoreUrl();
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }
}
