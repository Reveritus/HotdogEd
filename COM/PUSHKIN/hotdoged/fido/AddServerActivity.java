package com.pushkin.hotdoged.fido;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class AddServerActivity extends AppCompatActivity {
    private static final String ADD_SERVER_ACTIVITY_NAME = "com.pushkin.hotdoged.fido.ConfigureServerActivity";

    /* INFO: Access modifiers changed from: protected */
    @Override // android.support.v7.app.AppCompatActivity, android.support.v4.app.FragmentActivity, android.support.v4.app.BaseFragmentActivityGingerbread, android.app.Activity
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_server);
        Button buttonAboutFidonet = (Button) findViewById(R.id.buttonAboutFidonet);
        Button buttonRequestPoint = (Button) findViewById(R.id.buttonRequestPoint);
        Button buttonHavePoint = (Button) findViewById(R.id.buttonHavePoint);
        buttonAboutFidonet.setOnClickListener(new View.OnClickListener() { // from class: com.pushkin.hotdoged.fido.AddServerActivity.1
            @Override // android.view.View.OnClickListener
            public void onClick(View arg0) {
                AddServerActivity.this.aboutFidonet();
            }
        });
        buttonRequestPoint.setOnClickListener(new View.OnClickListener() { // from class: com.pushkin.hotdoged.fido.AddServerActivity.2
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                AddServerActivity.this.requestPoint();
            }
        });
        buttonHavePoint.setOnClickListener(new View.OnClickListener() { // from class: com.pushkin.hotdoged.fido.AddServerActivity.3
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                AddServerActivity.this.havePoint();
            }
        });
    }

    protected void havePoint() {
        Intent intent = new Intent(ADD_SERVER_ACTIVITY_NAME);
        intent.putExtra("serveruri", getIntent().getStringExtra("serveruri"));
        startActivity(intent);
        finish();
    }

    protected void requestPoint() {
        startActivityForResult(new Intent(this, RequestPointActivity.class), 0);
    }

    protected void aboutFidonet() {
        startActivity(new Intent(this, AboutFidonetActivity.class));
    }

    @Override // android.support.v4.app.FragmentActivity, android.app.Activity
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0 && resultCode == 1) {
            finish();
        }
    }
}
