package com.pushkin.hotdoged.fido;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

public class MessageActivity extends AppCompatActivity {
    private TextView tvMessage;

    /* INFO: Access modifiers changed from: protected */
    @Override // android.support.v7.app.AppCompatActivity, android.support.v4.app.FragmentActivity, android.support.v4.app.BaseFragmentActivityGingerbread, android.app.Activity
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        this.tvMessage = (TextView) findViewById(R.id.textViewMessage);
        String message = getIntent().getStringExtra("message");
        this.tvMessage.setMovementMethod(new ScrollingMovementMethod());
        this.tvMessage.setText(message);
    }
}
