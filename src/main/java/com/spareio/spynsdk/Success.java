package com.spareio.spynsdk;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class Success extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_success);
    }

    public void openSpyn(View view) {
        Intent intent = getApplicationContext().getPackageManager().getLaunchIntentForPackage("com.spare.spyn");
        getApplicationContext().startActivity(intent);
    }
}
