package com.advmovile.noote;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class NooteEdit extends Activity {

    private EditText mTitleText;
    private EditText mBodyText;
    private Long mRowId;
    private NooteDbAdapter mDbHelper;
    // ===============
    private TextView dt;
    private EditText mCategory;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private double latitude, longitude;
    private TextView lat, lon;
//    private boolean gps_enabled, network_enabled;

    public void setActivityBackgroundColor(int color) {
        View view = this.getWindow().getDecorView();
        view.setBackgroundColor(color);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_noote_edit);

        setActivityBackgroundColor(Color.rgb(224, 224, 224));

        mDbHelper = new NooteDbAdapter(this);
        mDbHelper.open();

        mTitleText = (EditText) findViewById(R.id.title);
        mBodyText = (EditText) findViewById(R.id.body);
        dt = (TextView) findViewById(R.id.date);
        mCategory = (EditText) findViewById(R.id.category);
        lat = (TextView) findViewById(R.id.latitude);
        lon = (TextView) findViewById(R.id.longitude);

        Button confirmButton = (Button) findViewById(R.id.confirm);

        mRowId = savedInstanceState != null ? savedInstanceState.getLong(NooteDbAdapter.KEY_ROWID) : null;

        if (mRowId == null) {
            Bundle extras = getIntent().getExtras();
            mRowId = extras != null ? extras.getLong(NooteDbAdapter.KEY_ROWID) : null;
        }

        populateFields();

        confirmButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                System.out.println(mTitleText.getText());
                if (mTitleText.getText().toString().equalsIgnoreCase("")) {
                    Toast.makeText(getBaseContext(), "TITLE missing", Toast.LENGTH_SHORT).show();
                } else {



                    setResult(RESULT_OK);
//                    if (ActivityCompat.checkSelfPermission(getBaseContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getBaseContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                        return;
//                    }
//                    locationManager.removeUpdates(locationListener);
                    finish();
                }
            }

        });
    }

    private void populateFields() {
        if (mRowId != null) {
            Cursor note = mDbHelper.fetchNote(mRowId);
            startManagingCursor(note);
            mTitleText.setText(note.getString(note.getColumnIndexOrThrow(NooteDbAdapter.KEY_TITLE)));
            mBodyText.setText(note.getString(note.getColumnIndexOrThrow(NooteDbAdapter.KEY_BODY)));
            dt.setText(note.getString(note.getColumnIndexOrThrow(NooteDbAdapter.KEY_DATE)));
            mCategory.setText(note.getString(note.getColumnIndexOrThrow(NooteDbAdapter.KEY_CATEGORY)));
            lat.setText(String.valueOf(note.getDouble(note.getColumnIndexOrThrow(NooteDbAdapter.KEY_LATITUDE))));
            lon.setText(String.valueOf(note.getDouble(note.getColumnIndexOrThrow(NooteDbAdapter.KEY_LONGITUDE))));
            System.out.println("date"+note.getColumnIndexOrThrow(NooteDbAdapter.KEY_DATE));
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(NooteDbAdapter.KEY_ROWID, mRowId);
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveState();
    }

    @Override
    protected void onResume() {
        super.onResume();
        populateFields();
    }

    private void saveState() {
        String title = mTitleText.getText().toString();
        String body = mBodyText.getText().toString();
        String dt = NooteHelper.formatDT();
        String category = mCategory.getText().toString();
//        double latitude = this.latitude;
//        double longitude = this.longitude;
        String latitude = String.valueOf(0.0);
        String longitude = String.valueOf(0.0);

        if (mRowId == null) {
            long id = mDbHelper.createNote(title, body, dt, category, latitude, longitude);
            if (id > 0) {
                mRowId = id;
            }
        } else {
            mDbHelper.updateNote(mRowId, title, body, dt, category, latitude, longitude);
        }
    }

//    @Override
//    public void onLocationChanged(Location location) {
//        latitude = location.getLatitude();
//        longitude = location.getLongitude();
//
//        lat.setText(String.valueOf(latitude));
//        lon.setText(String.valueOf(longitude));
//    }
//
//    @Override
//    public void onStatusChanged(String s, int i, Bundle bundle) {
//        Log.d("Latitude","status");
//    }
//
//    @Override
//    public void onProviderEnabled(String s) {
//        Log.d("Latitude","Enable");
//    }
//
//    @Override
//    public void onProviderDisabled(String s) {
//        Log.d("Latitude","Disable");
//    }
}
