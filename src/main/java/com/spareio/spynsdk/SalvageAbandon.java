package com.spareio.spynsdk;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class SalvageAbandon extends AppCompatActivity {

    private spynSDK spynSDK;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_salvage_abandon);
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

    public void closeScreen() {
        onBackPressed();
    }

    public void launchPlayStore(View view) {
        findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
        String url = spynSDK.getPlaystoreUrl();
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }
}
