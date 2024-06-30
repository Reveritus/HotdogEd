package com.pushkin.hotdoged.fido;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;
import android.widget.Toast;
import com.pushkin.hotdoged.export.Constants;
import com.pushkin.hotdoged.export.Utils;
import java.util.ArrayList;
import java.util.Iterator;

public class LogActivity extends AppCompatActivity {
    /* INFO: Access modifiers changed from: protected */
    @Override // android.support.v7.app.AppCompatActivity, android.support.v4.app.FragmentActivity, android.support.v4.app.BaseFragmentActivityGingerbread, android.app.Activity
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);
        try {
            Intent notificationIntent = new Intent(StartBCReceiver.SYNC_INTENT_NAME);
            notificationIntent.putExtra(Constants.INTENT_EXTRA_SYNCTYPE, 4);
            startService(Utils.createExplicitFromImplicitIntent(this, notificationIntent));
            ArrayList<String> eventArray = (ArrayList) getIntent().getSerializableExtra("data");
            EditText etLog = (EditText) findViewById(R.id.editTextLog);
            etLog.setText("");
            Iterator<String> it = eventArray.iterator();
            while (it.hasNext()) {
                String s = it.next();
                etLog.append(s + "\n\n");
            }
        } catch (Exception e) {
            Toast.makeText(this, "No data found", 0).show();
        }
    }
}
