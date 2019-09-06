package com.spareio.spynsdk;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class Success extends AppCompatActivity {

    private spynSDK spynSDK;
    private String dealId;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_success);

        Intent intent = getIntent();
        dealId = intent.getStringExtra(spynSDK.EXTRA_DEALID);

        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

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

        ImageView imageView = (ImageView) findViewById(R.id.partnerLogo);
        imageView.setImageDrawable(getApplicationInfo().loadIcon(getPackageManager()));

        TextView textView = (TextView) findViewById(R.id.successText);
        textView.setText(preferences.getString("successText", ""));
    }

    public void openSpyn(View view) {
        Intent intent = getApplicationContext().getPackageManager().getLaunchIntentForPackage("com.spare.spyn");
        getApplicationContext().startActivity(intent);
    }
}
