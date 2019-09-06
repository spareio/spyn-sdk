package com.spareio.spynsdk;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class StartScreen extends AppCompatActivity {

    private spynSDK spynSDK;
    private String dealId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_screen);

        Intent intent = getIntent();
        dealId = intent.getStringExtra(spynSDK.EXTRA_DEALID);

        spynSDK = new spynSDK.Builder()
                .setIcon(getApplicationInfo().loadIcon(getPackageManager()))
                .setDealId(dealId)
                .setLang("en")
                .setContext(this)
                .create();
        if (!spynSDK.isAppInstalled()) {
            Intent intent2 = new Intent(getApplicationContext(), Interstitial.class);
            getApplication().startActivity(intent2);
        }
    }

    public void openSpyn(View view) {
        Log.d("Message", "Spyn should open");
    }
}
