package com.spareio.spynsdk;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class Success extends AppCompatActivity {

    private spynSDK spynSDK;
    private String dealId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_success);

        Intent intent = getIntent();
        dealId = intent.getStringExtra(spynSDK.EXTRA_DEALID);

        spynSDK = new spynSDK(this, dealId);
        if (!spynSDK.isAppInstalled()) {
            Intent intent2 = new Intent(getApplicationContext(), Interstitial.class);
            getApplication().startActivity(intent2);
        }
    }

    public void openSpyn(View view) {
        Intent intent = getApplicationContext().getPackageManager().getLaunchIntentForPackage("com.spare.spyn");
        getApplicationContext().startActivity(intent);
    }
}
