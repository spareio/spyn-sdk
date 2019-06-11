package com.spareio.sdklite;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class SpareOffer extends AppCompatActivity {

    private SDKLite spareSdk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spare_offer);

        Intent intent = getIntent();
        String dealId = intent.getStringExtra(SDKLite.EXTRA_DEALID);

        spareSdk = new SDKLite(this, dealId);
    }

    @Override
    public void onBackPressed() {
        spareSdk.reject();
        super.onBackPressed();
    }

    public void acceptOffer(View view) {
        spareSdk.accept();
        finish();
    }
}
