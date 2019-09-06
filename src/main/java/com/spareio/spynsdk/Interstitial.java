package com.spareio.spynsdk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class Interstitial extends AppCompatActivity {

    private spynSDK spynSDK;
    private newPackageReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interstitial);

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
        filter.addAction(Intent.ACTION_PACKAGE_DATA_CLEARED);
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addAction(Intent.ACTION_PACKAGE_REPLACED);
        filter.addAction(Intent.ACTION_PACKAGE_RESTARTED);
        filter.addDataScheme("package");

        receiver = new newPackageReceiver();
        registerReceiver(receiver, filter);

        Intent intent = getIntent();
        String dealId = intent.getStringExtra(spynSDK.EXTRA_DEALID);

        spynSDK = new spynSDK.Builder()
                .setDealId(dealId)
                .setLang("en")
                .setContext(this)
                .create();
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

    @Override
    public void onDestroy(){
        unregisterReceiver(receiver);
        super.onDestroy();
    }

    public class newPackageReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("DEBUG"," test for application install/uninstall");
            if (spynSDK.isAppInstalled()) {
                Intent newintent = new Intent(getApplicationContext(), Success.class);
                startActivity(newintent);
            }
        }

    }
}
