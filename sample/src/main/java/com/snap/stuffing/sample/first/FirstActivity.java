package com.snap.stuffing.sample.first;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.TextView;

import com.snap.stuffing.api.DynamicAppManager;
import com.snap.stuffing.sample.R;
import dagger.android.AndroidInjection;

import javax.inject.Inject;

public class FirstActivity extends AppCompatActivity {

    @Inject DynamicAppManager dynamicAppManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidInjection.inject(this);
        setContentView(R.layout.main);

        final TextView descriptionTextView = findViewById(R.id.module_description_text);
        descriptionTextView.setText("First");

        final Button switchButton = findViewById(R.id.switch_app_button);
        switchButton.setOnClickListener(v -> dynamicAppManager.switchToAppFamily("second", true,null));
    }
}
