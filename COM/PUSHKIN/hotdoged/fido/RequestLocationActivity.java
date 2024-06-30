package com.pushkin.hotdoged.fido;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import java.util.ArrayList;
import java.util.Collections;

public class RequestLocationActivity extends AppCompatActivity {
    private static final String TAG = "RequestLocationActivity";
    private ListView lvCities;
    private int selCity;
    private Spinner spCountries;
    private final ArrayList<String> countries = new ArrayList<>();
    private final ArrayList<String> cities = new ArrayList<>();

    /* INFO: Access modifiers changed from: protected */
    @Override // android.support.v7.app.AppCompatActivity, android.support.v4.app.FragmentActivity, android.support.v4.app.BaseFragmentActivityGingerbread, android.app.Activity
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_location);
        this.spCountries = (Spinner) findViewById(R.id.spinnerCountry);
        this.lvCities = (ListView) findViewById(R.id.listViewCity);
        fillLocations();
        ArrayAdapter<String> countryAdapter = new ArrayAdapter<>(this, 17367043, this.countries);
        this.spCountries.setAdapter((SpinnerAdapter) countryAdapter);
        this.spCountries.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() { // from class: com.pushkin.hotdoged.fido.RequestLocationActivity.1
            @Override // android.widget.AdapterView.OnItemSelectedListener
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String country = (String) RequestLocationActivity.this.countries.get(position);
                Log.d(RequestLocationActivity.TAG, "Selected country: " + country);
                RequestLocationActivity.this.fillCities(country);
                ArrayAdapter<String> cityAdapter = new ArrayAdapter<>(RequestLocationActivity.this.getContext(), 17367043, RequestLocationActivity.this.cities);
                RequestLocationActivity.this.lvCities.setAdapter((ListAdapter) cityAdapter);
            }

            @Override // android.widget.AdapterView.OnItemSelectedListener
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        this.lvCities.setOnItemClickListener(new AdapterView.OnItemClickListener() { // from class: com.pushkin.hotdoged.fido.RequestLocationActivity.2
            @Override // android.widget.AdapterView.OnItemClickListener
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                RequestLocationActivity.this.selCity = position;
                Log.d(RequestLocationActivity.TAG, "Selected city: " + ((String) RequestLocationActivity.this.cities.get(position)));
                RequestLocationActivity.this.onBackPressed();
            }
        });
    }

    protected Context getContext() {
        return this;
    }

    private void fillLocations() {
        this.countries.clear();
        this.selCity = -1;
        try {
            this.countries.addAll(getIntent().getStringArrayListExtra("countries"));
        } catch (Exception e) {
            Log.e(TAG, "Country list was not transmitted correctly");
        }
        if (this.countries.isEmpty()) {
            this.cities.clear();
            Log.d(TAG, "Country list is empty");
            return;
        }
        Collections.sort(this.countries);
        fillCities(this.countries.get(0));
    }

    /* INFO: Access modifiers changed from: private */
    public void fillCities(String country) {
        Log.d(TAG, "fillCities() called");
        this.cities.clear();
        if (!TextUtils.isEmpty(country)) {
            this.cities.addAll(getIntent().getStringArrayListExtra(country));
            if (this.cities == null) {
                Log.d(TAG, "City list is empty for country " + country);
            } else {
                Collections.sort(this.cities);
            }
        }
    }

    @Override // android.support.v4.app.FragmentActivity, android.app.Activity
    public void onBackPressed() {
        Intent data = new Intent();
        String country = (String) this.spCountries.getSelectedItem();
        String city = (this.cities.isEmpty() || this.selCity < 0) ? null : this.cities.get(this.selCity);
        data.putExtra("country", country);
        data.putExtra("city", city);
        setResult(0, data);
        super.onBackPressed();
    }
}
