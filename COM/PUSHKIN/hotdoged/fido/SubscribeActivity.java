package com.pushkin.hotdoged.fido;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;
import com.pushkin.area.Area;
import com.pushkin.area.AreaList;
import com.pushkin.ftn.Link;
import com.pushkin.ftn.LinkOption;
import com.pushkin.ftn.Main;
import com.pushkin.ftn.Netmail;
import com.pushkin.hotdoged.export.HotdogedException;
import com.pushkin.hotdoged.export.ServerEntry;
import com.sun.mail.imap.IMAPStore;
import java.util.Calendar;
import java.util.Iterator;
import jnode.ftn.FtnTools;

public class SubscribeActivity extends AppCompatActivity {
    private static final String TAG = "SubscribeActivity";
    private GroupListAdapter adapter;
    public Context context;
    private EditText etFilter;
    private EditText etNewAreas;
    private Link link;
    private ListView lvGroups;
    private ProgressDialog pd;
    private ServerEntry serverEntry;
    private Uri serverUri;
    private String filter = "";
    int sortOrder = 0;
    boolean showOtherAreas = false;
    boolean sortDescending = false;
    public boolean subscribedFirst = true;

    private class AsyncAreaFetcher extends AsyncTask<String, Object, String> {
        private AsyncAreaFetcher() {
        }

        /* INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public String doInBackground(String... params) {
            try {
                SubscribeActivity.this.adapter = new GroupListAdapter(SubscribeActivity.this.context, SubscribeActivity.this.serverEntry);
                if (SubscribeActivity.this.adapter != null) {
                    return SubscribeActivity.this.adapter.error;
                }
                return null;
            } catch (HotdogedException e) {
                return "Failed to get area list for server " + SubscribeActivity.this.serverEntry.getServerUri() + ": " + e.getLocalizedMessage();
            }
        }

        /* INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public void onPostExecute(String result) {
            if (SubscribeActivity.this.adapter != null) {
                SubscribeActivity.this.adapter.notifyDataSetChanged();
            }
            SubscribeActivity.this.pd.dismiss();
            if (SubscribeActivity.this.adapter != null) {
                SubscribeActivity.this.adapter.setSortOrder(SubscribeActivity.this.sortOrder, SubscribeActivity.this.sortDescending, SubscribeActivity.this.subscribedFirst);
            }
            SubscribeActivity.this.lvGroups.setAdapter((ListAdapter) SubscribeActivity.this.adapter);
            if (result != null) {
                Toast.makeText(SubscribeActivity.this.context, result, 1).show();
                Log.e("AsyncAreaFetcher", result);
            }
            SubscribeActivity.this.adapter.setFilter(SubscribeActivity.this.filter);
            SubscribeActivity.this.etFilter.setText(SubscribeActivity.this.filter);
            super.onPostExecute((AsyncAreaFetcher) result);
        }

        @Override // android.os.AsyncTask
        protected void onPreExecute() {
            SubscribeActivity.this.pd = new ProgressDialog(SubscribeActivity.this.context);
            SubscribeActivity.this.pd.setTitle("Fetching groups");
            SubscribeActivity.this.pd.setMessage("Fetching groups, please wait...");
            SubscribeActivity.this.pd.setCancelable(false);
            SubscribeActivity.this.pd.show();
            super.onPreExecute();
        }
    }

    /* INFO: Access modifiers changed from: protected */
    @Override // android.support.v7.app.AppCompatActivity, android.support.v4.app.FragmentActivity, android.support.v4.app.BaseFragmentActivityGingerbread, android.app.Activity
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_subscribe);
        this.serverUri = Uri.parse(getIntent().getStringExtra("server"));
        this.context = this;
        try {
            this.serverEntry = new ServerEntry(this, this.serverUri);
            Main.info = new Main.SystemInfo(this, this.serverEntry.getUser_name(), ContentFetchService.getLocation(), ContentFetchService.getStationName(this, this.serverEntry), this.serverEntry.getUser_address(), this.serverEntry.getServer_name(), ContentFetchService.NDL, ContentFetchService.DEFAULT_TEARLINE, this.serverEntry);
            this.link = new Link(this.serverEntry.getServer_name());
            Main.info.setLink(this.link);
            this.etFilter = (EditText) findViewById(R.id.editTextFilter);
            this.etNewAreas = (EditText) findViewById(R.id.editTextNewAreas);
            this.lvGroups = (ListView) findViewById(R.id.listViewGroups);
            if (savedInstanceState != null) {
                this.filter = savedInstanceState.getString("filter");
                this.sortOrder = savedInstanceState.getInt("sortorder");
                this.showOtherAreas = savedInstanceState.getBoolean("showotherareas");
                this.sortDescending = savedInstanceState.getBoolean("sortdescending");
                this.subscribedFirst = savedInstanceState.getBoolean("subscribedfirst");
                this.adapter = (GroupListAdapter) getLastCustomNonConfigurationInstance();
                if (this.adapter == null) {
                    new AsyncAreaFetcher().execute(new String[0]);
                } else {
                    this.lvGroups.setAdapter((ListAdapter) this.adapter);
                    this.adapter.setFilter(this.filter);
                    this.etFilter.setText(this.filter);
                }
            } else {
                new AsyncAreaFetcher().execute(new String[0]);
                this.showOtherAreas = TextUtils.isEmpty(this.serverEntry.getAreasurl());
            }
            this.etNewAreas.setVisibility(this.showOtherAreas ? 0 : 8);
            this.etFilter.addTextChangedListener(new TextWatcher() { // from class: com.pushkin.hotdoged.fido.SubscribeActivity.1
                @Override // android.text.TextWatcher
                public void afterTextChanged(Editable arg0) {
                }

                @Override // android.text.TextWatcher
                public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                }

                @Override // android.text.TextWatcher
                public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                    SubscribeActivity.this.adapter.setFilter(arg0.toString());
                }
            });
        } catch (HotdogedException e) {
            Main.SystemInfo.getLogger().log("onCreate", "Failed to load server info: " + e.getMessage());
            Toast.makeText(this, "Failed to load server info: " + e.getMessage(), 1).show();
        }
    }

    /* INFO: Access modifiers changed from: protected */
    @Override // android.support.v7.app.AppCompatActivity, android.support.v4.app.FragmentActivity, android.app.Activity
    public void onSaveInstanceState(Bundle outState) {
        outState.putString("filter", this.etFilter.getText().toString());
        outState.putInt("sortorder", this.sortOrder);
        outState.putBoolean("showotherareas", this.showOtherAreas);
        outState.putBoolean("sortdescending", this.sortDescending);
        outState.putBoolean("subscribedfirst", this.subscribedFirst);
        super.onSaveInstanceState(outState);
    }

    @Override // android.support.v4.app.FragmentActivity
    public Object onRetainCustomNonConfigurationInstance() {
        return this.adapter;
    }

    @Override // android.app.Activity
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.item_other_areas).setChecked(this.showOtherAreas);
        menu.findItem(R.id.item_sort_name).setChecked(this.sortOrder == 0);
        menu.findItem(R.id.item_sort_count).setChecked(this.sortOrder == 2);
        menu.findItem(R.id.item_sort_last).setChecked(this.sortOrder == 1);
        menu.findItem(R.id.item_descending).setChecked(this.sortDescending);
        menu.findItem(R.id.item_subscribed_first).setChecked(this.subscribedFirst);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override // android.app.Activity
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_ok /* 2131493010 */:
                onBackPressed();
                break;
            case R.id.item_cancel /* 2131493011 */:
                Intent intent = new Intent();
                intent.setData(this.serverUri);
                setResult(0, intent);
                finish();
                break;
            case R.id.item_sort_name /* 2131493014 */:
                this.sortOrder = 0;
                sort();
                break;
            case R.id.item_sort_count /* 2131493015 */:
                this.sortOrder = 2;
                sort();
                break;
            case R.id.item_sort_last /* 2131493016 */:
                this.sortOrder = 1;
                sort();
                break;
            case R.id.item_descending /* 2131493017 */:
                this.sortDescending = this.sortDescending ? false : true;
                sort();
                break;
            case R.id.item_subscribed_first /* 2131493018 */:
                this.subscribedFirst = this.subscribedFirst ? false : true;
                sort();
                break;
            case R.id.item_other_areas /* 2131493019 */:
                this.showOtherAreas = this.showOtherAreas ? false : true;
                this.etNewAreas.setVisibility(this.showOtherAreas ? 0 : 8);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void sort() {
        this.adapter.setSortOrder(this.sortOrder, this.sortDescending, this.subscribedFirst);
        this.lvGroups.setAdapter((ListAdapter) this.adapter);
    }

    @Override // android.app.Activity
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_group_subscribe, menu);
        return true;
    }

    @Override // android.support.v4.app.FragmentActivity, android.app.Activity
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.setData(this.serverUri);
        try {
            saveSubscribtion();
            setResult(-1, intent);
        } catch (Exception e) {
            setResult(0, intent);
        }
        super.onBackPressed();
    }

    private void saveSubscribtion() {
        if (this.serverEntry == null) {
            Toast.makeText(this, "Saving subscription failed.", 0).show();
            return;
        }
        String letterText = "";
        AreaList areasTBS = this.adapter.getAreasBySubscriptionStatus(2);
        if (this.showOtherAreas) {
            String areaData = this.etNewAreas.getText().toString().trim();
            if (areaData.length() > 0) {
                String[] areas = areaData.split("\n");
                int length = areas.length;
                int i = 0;
                while (true) {
                    int i2 = i;
                    if (i2 >= length) {
                        break;
                    }
                    String a = areas[i2];
                    if (a.trim().length() > 0) {
                        areasTBS.add(new Area(a.trim(), 0L, 0, null));
                    }
                    i = i2 + 1;
                }
            }
        }
        Iterator<Area> it = areasTBS.iterator();
        while (it.hasNext()) {
            Area area = it.next();
            letterText = letterText + "+" + area.getName() + "\n";
        }
        AreaList areasTBU = this.adapter.getAreasBySubscriptionStatus(3);
        Iterator<Area> it2 = areasTBU.iterator();
        while (it2.hasNext()) {
            Area area2 = it2.next();
            letterText = letterText + "-" + area2.getName() + "\n";
        }
        try {
            subscribeToGroups(areasTBS);
            unsubscribeFromGroups(areasTBU);
            if (letterText.trim().length() > 0) {
                String password = ConfigureServerActivity.getLocalServerPassword(this, this.serverUri.getLastPathSegment());
                try {
                    createNewOutgoingNetmail(this.serverEntry.getUser_name(), this.serverEntry.getUser_address(), LinkOption.BOOLEAN_AREAFIX, this.serverEntry.getServer_name(), password, letterText, this.link);
                    Toast.makeText(this, "Please synchronize to send subscription to boss node.\nSubscribed: " + areasTBS.size() + "\nUnsubscribed: " + areasTBU.size(), 0).show();
                } catch (HotdogedException e) {
                    Main.SystemInfo.getLogger().log("saveSubscribtion", "Failed to send subscription netmail: " + e.getMessage());
                    Toast.makeText(this, "Failed to send subscription netmail: " + e.getMessage(), 0).show();
                }
                Log.d("saveSubscribtion", "Letter text:\n" + letterText);
            }
        } catch (HotdogedException e2) {
            Toast.makeText(this, e2.getLocalizedMessage(), 0).show();
            Log.e(TAG, e2.getLocalizedMessage());
        }
    }

    private void subscribeToGroups(AreaList toAdd) throws HotdogedException {
        ContentValues cv = new ContentValues();
        try {
            Iterator<Area> it = toAdd.iterator();
            while (it.hasNext()) {
                Area area = it.next();
                cv.clear();
                cv.put(IMAPStore.ID_NAME, area.getName().toLowerCase());
                cv.put("grouptype_id", (Integer) 20);
                Uri uri = getContentResolver().insert(Uri.withAppendedPath(this.serverUri, "groups"), cv);
                Main.SystemInfo.getLogger().log("subscribeToGroups()", "Subscribed to: " + area + ", " + uri);
            }
        } catch (Exception e) {
            throw new HotdogedException("Error during subscription: " + e.getLocalizedMessage());
        }
    }

    private void unsubscribeFromGroups(AreaList toDelete) throws HotdogedException {
        try {
            Iterator it = toDelete.iterator();
            while (it.hasNext()) {
                Area area = (Area) it.next();
                if (area.getDbId() >= 0) {
                    Uri uri = Uri.withAppendedPath(this.serverUri, "groups/" + area.getDbId());
                    getContentResolver().delete(uri, null, null);
                } else {
                    Main.SystemInfo.getLogger().log("unsubscribeToGroups()", "Could not unsubscribe from: " + area);
                    toDelete.remove(area);
                }
            }
            Main.SystemInfo.getLogger().log("unsubscribeToGroups()", "Unsubscribed from: " + toDelete.toString());
        } catch (Exception e) {
            throw new HotdogedException("Error during unsubscription: " + e.getLocalizedMessage());
        }
    }

    private void createNewOutgoingNetmail(String fromName, String fromAddr, String toName, String toAddr, String subject, String text, Link link) throws HotdogedException {
        Main.info.setLink(link);
        Netmail mail = new Netmail();
        mail.setDate(Calendar.getInstance().getTime());
        mail.setFromName(fromName);
        mail.setFromFTN(fromAddr);
        mail.setSubject(subject);
        String header = String.format("\u0001MSGID: %s %s\n\u0001PID: %s\n\u0001TID: %s\n", fromAddr, FtnTools.generate8d(), Main.SystemInfo.getPID(), Main.info.getVersion());
        mail.setText(header + "\n" + text + "\n");
        mail.setToName(toName);
        mail.setToFTN(toAddr);
        mail.save(5);
    }
}
