package com.jhearing.e7160sl.Tools;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.jhearing.e7160sl.R;
import com.jhearing.e7160sl.model.AssistantInfo;

public class OptionsActivity extends AppCompatActivity {
    private TextView assistantTv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

    }
}
