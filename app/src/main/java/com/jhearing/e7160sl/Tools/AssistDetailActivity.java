package com.jhearing.e7160sl.Tools;

import androidx.appcompat.app.AppCompatActivity;
import com.jhearing.e7160sl.R;
import com.jhearing.e7160sl.model.AssistantInfo;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;

public class AssistDetailActivity extends AppCompatActivity {
    private TextView assistantTv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assist_detail);
        assistantTv =   findViewById(R.id.assistant_tv);
        Intent intent = getIntent();
        if (intent != null){
            AssistantInfo data = intent.getParcelableExtra("data");

            if (!TextUtils.isEmpty(data.getContent())){
                assistantTv.setText(data.getContent());
            }
        }
    }
}
