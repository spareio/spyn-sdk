package com.spareio.spynsdk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class SalvageAbandon extends AppCompatActivity {

    private spynSDK spynSDK;
    private String dealId;
    private newPackageReceiver receiver;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_salvage_abandon);

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
        filter.addAction(Intent.ACTION_PACKAGE_DATA_CLEARED);
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addAction(Intent.ACTION_PACKAGE_REPLACED);
        filter.addAction(Intent.ACTION_PACKAGE_RESTARTED);
        filter.addDataScheme("package");

        receiver = new SalvageAbandon.newPackageReceiver();
        registerReceiver(receiver, filter);

        Intent intent = getIntent();
        dealId = intent.getStringExtra(spynSDK.EXTRA_DEALID);

        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        spynSDK = new spynSDK.Builder()
                .setIcon(getApplicationInfo().loadIcon(getPackageManager()))
                .setDealId(dealId)
                .setLang("en")
                .setContext(this)
                .create();

        if (spynSDK.isAppInstalled()) {
            Intent intent2 = new Intent(getApplicationContext(), Success.class);
            getApplication().startActivity(intent2);
        }

        ImageView imageView = (ImageView) findViewById(R.id.partnerLogo);
        imageView.setImageDrawable(getApplicationInfo().loadIcon(getPackageManager()));

        TextView textView = (TextView) findViewById(R.id.whoopsText);
        textView.setText(preferences.getString("whoopsText", ""));
    }

    @Override
    public void onBackPressed() {
        Intent intent = getIntent();
        dealId = intent.getStringExtra(spynSDK.EXTRA_DEALID);
        spynSDK = new spynSDK.Builder()
                .setIcon(getApplicationInfo().loadIcon(getPackageManager()))
                .setDealId(dealId)
                .setLang("en")
                .setContext(this)
                .create();
        spynSDK.reject();
        super.onBackPressed();
    }

    public void acceptOffer(View view) {
        Intent intent = getIntent();
        dealId = intent.getStringExtra(spynSDK.EXTRA_DEALID);
        spynSDK = new spynSDK.Builder()
                .setIcon(getApplicationInfo().loadIcon(getPackageManager()))
                .setDealId(dealId)
                .setLang("en")
                .setContext(this)
                .create();
        spynSDK.accept();
        launchPlayStore(view);
    }

    public void closeOfferOverlay(View view) {
        try {
            Intent intent = new Intent(this, Class.forName("com.spareio.hotspotshieldpartnerapp.MainActivity"));
            startActivity(intent);
        } catch (Exception e) {
            Log.d("Exception", e.toString());
        }
    }

    public void launchPlayStore(View view) {
        Intent intent = getIntent();
        dealId = intent.getStringExtra(spynSDK.EXTRA_DEALID);
        spynSDK = new spynSDK.Builder()
                .setIcon(getApplicationInfo().loadIcon(getPackageManager()))
                .setDealId(dealId)
                .setLang("en")
                .setContext(this)
                .create();
        String url = spynSDK.getPlaystoreUrl();
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    public class newPackageReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (spynSDK.isAppInstalled()) {
                Intent newintent = new Intent(getApplicationContext(), Success.class);
                startActivity(newintent);
            }
        }

    }
}
