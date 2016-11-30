package com.advmovile.noote;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationListener;

import java.text.DateFormat;
import java.util.Date;

import static android.content.ContentValues.TAG;


public class NooteEdit extends Activity implements
        View.OnClickListener, ConnectionCallbacks, OnConnectionFailedListener, LocationListener
        ,ActivityCompat.OnRequestPermissionsResultCallback {

    static final int TAKE_AVATAR_CAMERA_REQUEST = 1;
    static final int TAKE_AVATAR_GALLERY_REQUEST =2;

    private EditText mTitleText;
    private EditText mBodyText;
    private Long mRowId;
    private NooteDbAdapter mDbHelper;
    // ===============
    private TextView dt;
    private EditText mCategory;
    private TextView textViewLat, textViewLng;
    private String stringLat;
    private String stringLng;

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    protected Location mCurrentLocation;
    protected String mLastUpdateTime;
    protected Boolean mRequestingLocationUpdates;

    // ===== add photo
    private ImageButton newPhoto;
//    private ImageView photo;
    private byte[] photoBytes;




    public void setActivityBackgroundColor(int color) {
        View view = this.getWindow().getDecorView();
        view.setBackgroundColor(color);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_noote_edit);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        mRequestingLocationUpdates = false;
        mLastUpdateTime = "";

        setActivityBackgroundColor(Color.rgb(224, 224, 224));

        mDbHelper = new NooteDbAdapter(this);
        mDbHelper.open();

        mTitleText = (EditText) findViewById(R.id.title);
        mBodyText = (EditText) findViewById(R.id.body);
        dt = (TextView) findViewById(R.id.date);
        mCategory = (EditText) findViewById(R.id.category);
        textViewLat = (TextView) findViewById(R.id.latitude);
        textViewLng = (TextView) findViewById(R.id.longitude);

        newPhoto = (ImageButton) findViewById(R.id.photoButton);
//        photo = (ImageView) findViewById(R.id.photo);

        Button confirmButton = (Button) findViewById(R.id.confirm);
        confirmButton.setOnClickListener(this);

        Button mapButton = (Button) findViewById(R.id.map);
        mapButton.setOnClickListener(this);

        // ===============
        mRowId = savedInstanceState != null ? savedInstanceState.getLong(NooteDbAdapter.KEY_ROWID) : null;
        // ===============


        if (mRowId == null) {
            Bundle extras = getIntent().getExtras();
            mRowId = extras != null ? extras.getLong(NooteDbAdapter.KEY_ROWID) : null;
        }

        populateFields();
        buildGoogleApiClient();

//        newPhoto.setOnClickListener(new ChooseCameraListener());
        newPhoto.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Intent pickPhoto = new Intent(Intent.ACTION_PICK);
                pickPhoto.setType("image/*");
                startActivityForResult(Intent.createChooser(pickPhoto, "Choose a picture to use as your avatar!"), TAKE_AVATAR_GALLERY_REQUEST);

                return true;
            }
        });

    }

//    @Override
//    public boolean onLongClick(View view) {
//        Intent pickPhoto = new Intent(Intent.ACTION_PICK);
//        pickPhoto.setType("image/*");
//        startActivityForResult(Intent.createChooser(pickPhoto, "Choose a picture to use as your avatar!"), TAKE_AVATAR_GALLERY_REQUEST);
//
//        return true;
//    }

//    public class ChooseCameraListener implements View.OnClickListener {
//
//        @Override
//        public void onClick(View v) {
//
//            Intent pictureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
//            startActivityForResult(Intent.createChooser(
//                    pictureIntent, "Take your photo"), TAKE_AVATAR_CAMERA_REQUEST);
//        }
//    }


//    private class ChooseGalleryListener implements View.OnLongClickListener {
//
//        @Override
//        public boolean onLongClick(View v)
//        {
//            Intent pickPhoto = new Intent(Intent.ACTION_PICK);
//            pickPhoto.setType("image/*");
//            startActivityForResult(Intent.createChooser(pickPhoto, "Choose a picture to use as your avatar!"), TAKE_AVATAR_GALLERY_REQUEST);
//
//            return true;
//        }
//    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case TAKE_AVATAR_CAMERA_REQUEST:

//                if (resultCode == Activity.RESULT_CANCELED)
//                {
//// Avatar camera mode was canceled.
//                }
//                else if (resultCode == Activity.RESULT_OK)
//                {
//// Avatar camera executed ok
//                    Bitmap cameraPic =(Bitmap) data.getExtras().get("data");
//                    if (cameraPic != null)
//                    {
//                        try
//                        {
//// Get URI from bitmap
//                            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
//                            cameraPic.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
//                            String path = MediaStore.Images.Media.insertImage(this.getContentResolver(), cameraPic, "Title", null);
//
//// Convert to a byte array we can access in saveState()
//
//// set ImageView to cameraPic
//                            imgPhoto.setImageBitmap(cameraPic);
//                        }
//                        catch (Exception e)
//                        {
//// error saving the image
//                        }
//                    }
//                }
            case TAKE_AVATAR_GALLERY_REQUEST:
                if (resultCode == Activity.RESULT_CANCELED) {
                    // Avatar gallery request mode was canceled.
                } else if (resultCode == Activity.RESULT_OK) {
                    //Get image picked
                    Uri photoUri = data.getData();


                    if (photoUri != null) {
                        try {
                            newPhoto.setImageURI(photoUri);
                            Bitmap galleryPic = BitmapFactory.decodeStream(getContentResolver().openInputStream(photoUri));
                            photoBytes = NooteHelper.getImageBytes(galleryPic);
                        } catch (Exception e) {

                        }
                    }
                }
        }


    }




    private synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(3000);
        mLocationRequest.setFastestInterval(3000 / 2);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    }

    protected void startLocationUpdates() {
        // The final argument to {@code requestLocationUpdates()} is a LocationListener
        // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    protected void stopLocationUpdates() {
        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.

        // The final argument to {@code requestLocationUpdates()} is a LocationListener
        // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();

        super.onStop();
    }

    /**
     * Runs when a GoogleApiClient object successfully connects.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "Connected to GoogleApiClient");

        // If the initial location was never previously requested, we use
        // FusedLocationApi.getLastLocation() to get it. If it was previously requested, we store
        // its value in the Bundle and check for it in onCreate(). We
        // do not request it again unless the user specifically requests location updates by pressing
        // the Start Updates button.
        //
        // Because we cache the value of the initial location in the Bundle, it means that if the
        // user launches the activity,
        // moves to a new location, and then changes the device orientation, the original location
        // is displayed as the activity is re-created.
        if (mCurrentLocation == null) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
            stringLat = String.valueOf(mCurrentLocation.getLatitude());
            stringLng = String.valueOf(mCurrentLocation.getLongitude());
        }

        // If the user presses the Start Updates button before GoogleApiClient connects, we set
        // mRequestingLocationUpdates to true (see startUpdatesButtonHandler()). Here, we check
        // the value of mRequestingLocationUpdates and if it is true, we start location updates.
        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    /**
     * Callback that fires when the location changes.
     */
    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    private void populateFields() {
        if (mRowId != null) {
            Cursor note = mDbHelper.fetchNote(mRowId);
            startManagingCursor(note);
            mTitleText.setText(note.getString(note.getColumnIndexOrThrow(NooteDbAdapter.KEY_TITLE)));
            mBodyText.setText(note.getString(note.getColumnIndexOrThrow(NooteDbAdapter.KEY_BODY)));
            dt.setText(note.getString(note.getColumnIndexOrThrow(NooteDbAdapter.KEY_DATE)));
            mCategory.setText(note.getString(note.getColumnIndexOrThrow(NooteDbAdapter.KEY_CATEGORY)));
            newPhoto.setImageBitmap(NooteHelper.getImage(note.getBlob(note.getColumnIndexOrThrow(NooteDbAdapter.KEY_PHOTO))));
            textViewLat.setText(String.valueOf(note.getDouble(note.getColumnIndexOrThrow(NooteDbAdapter.KEY_LATITUDE))));
            textViewLng.setText(String.valueOf(note.getDouble(note.getColumnIndexOrThrow(NooteDbAdapter.KEY_LONGITUDE))));
//            System.out.println("date"+note.getColumnIndexOrThrow(NooteDbAdapter.KEY_DATE));
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mRowId != null) {
            outState.putLong(NooteDbAdapter.KEY_ROWID, mRowId);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //noinspection MissingPermission
//        locationManager.removeUpdates(locationListener);
        if (mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
            startLocationUpdates();
        }
        populateFields();

    }

    private void saveState() {
        String title = mTitleText.getText().toString();
        String body = mBodyText.getText().toString();
        String dt = NooteHelper.formatDT();
        String category = mCategory.getText().toString();
//        String textViewLat = this.textViewLat.getText().toString();
//        String textViewLng = this.textViewLng.getText().toString();

        String lat = stringLat;
        String lng = stringLng;

        if (mRowId == null) {
            long id = mDbHelper.createNote(title, body, dt, category, lat, lng, photoBytes);
            if (id > 0) {
                mRowId = id;
            }
        } else {
            mDbHelper.updateNote(mRowId, title, body, dt, category, lat, lng, photoBytes);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.confirm:

                if (mTitleText.getText().toString().equalsIgnoreCase("")) {
                    Toast.makeText(getBaseContext(), "TITLE missing", Toast.LENGTH_SHORT).show();
                } else {
                    saveState();
                    setResult(RESULT_OK);
                    finish();
                }
                break;
            case R.id.map:
                if (textViewLat.getText().toString() == null && textViewLng.getText().toString() == null) {
                    Toast.makeText(getApplicationContext(), "save note first", Toast.LENGTH_LONG).show();
                } else {
                    double lat = Double.valueOf(textViewLat.getText().toString());
                    double lng = Double.valueOf(textViewLng.getText().toString());

                    Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                    intent.putExtra("latitude", lat);
                    intent.putExtra("longitude", lng);
                    intent.putExtra("title", mTitleText.getText().toString());
                    startActivity(intent);
                }
                break;
            default:
                break;
        }
    }

}
