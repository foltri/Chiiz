package com.example.folti.chiiz;

import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.w3c.dom.Document;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class PhotoTarget extends ActionBarActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    private Uri fileUri;
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    private static final int CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE = 200;

    private String user_id;
    private LatLng user_location = null;
    private Firebase fireRef;
    private Firebase profileRef;
    private User me;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        user_id = intent.getStringExtra(PeopleNearby.EXTRA_USERID);
        setContentView(R.layout.activity_photo_target);

        ImageView img= (ImageView) findViewById(R.id.imageView);
        img.setImageResource(getResources().getIdentifier(user_id, "drawable", getPackageName()));

        buildGoogleApiClient();
        setUpMapIfNeeded();

        fireRef = new Firebase("https://burning-inferno-7965.firebaseio.com/");
        profileRef = fireRef.child("users/" + user_id);

        me = new User("jasper");

        ValueEventListener users = profileRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot profile) {
                //System.out.println(snapshot.getValue());  //prints "Do you have data? You'll love Firebase."
                user_location = new LatLng(profile.child("position/lat").getValue(Double.class), profile.child("position/lng").getValue(Double.class));
                String user_name = profile.child("name").getValue(String.class);
                ((TextView)findViewById(R.id.textView)).setText("Route to " + user_name);
                setTitle(user_name);
            }

            @Override
            public void onCancelled(FirebaseError error) {
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_photo_target, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        if (id == R.id.action_myphotos) {
            Intent intent = new Intent(PhotoTarget.this, MyPhotos.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void takePicture(View view) {

        // Do something in response to button
        // create Intent to take a picture and return control to the calling application
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE); // create a file to save the image
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file name

        // start the image capture Intent
        startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);

    }

    /** Create a file Uri for saving an image or video */
    private static Uri getOutputMediaFileUri(int type){
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /** Create a File for saving an image or video */
    private static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "Chiiz");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_"+ timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {

                // Image captured and saved to fileUri specified in the Intent
                //Toast.makeText(this, "Image saved to:\n" +fileUri, Toast.LENGTH_LONG).show();

                Intent intent = new Intent(PhotoTarget.this, SentPhoto.class);
                String message = fileUri.toString();
                intent.putExtra("photoPath", fileUri);
                intent.putExtra("user_id", user_id);
                galleryAddPic();

                startActivity(intent);

            } else if (resultCode == RESULT_CANCELED) {
                // User cancelled the image capture
            } else {
                // Image capture failed, advise user
            }
        }

        if (requestCode == CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // Video captured and saved to fileUri specified in the Intent
                Toast.makeText(this, "Video saved to:\n" +
                        data.getData(), Toast.LENGTH_LONG).show();
            } else if (resultCode == RESULT_CANCELED) {
                // User cancelled the video capture
            } else {
                // Video capture failed, advise user
            }
        }
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(fileUri.getPath());
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);

    }

    /** google maps **/

    protected static final String TAG = "location-updates-sample";
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private Marker markers;
    protected GoogleApiClient mGoogleApiClient;
    protected Location mCurrentLocation;
    protected LocationRequest mLocationRequest;
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    GoogleDirection gd;
    Document mDoc;
    private Boolean directionDrawn = false;

    LatLng start = new LatLng(13.744246499553903, 100.53428772836924);
    LatLng end = new LatLng(13.751279688694071, 100.54316081106663);

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        mMap.setMyLocationEnabled(true);

        gd = new GoogleDirection(this);

        start = new LatLng(13.744246499553903, 100.53428772836924);
        end = new LatLng(13.751279688694071, 100.54316081106663);

        gd.setOnDirectionResponseListener(new GoogleDirection.OnDirectionResponseListener() {
            public void onResponse(String status, Document doc, GoogleDirection gd) {
                mDoc = doc;
                mMap.addPolyline(gd.getPolyline(doc, 3, Color.RED));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(start, 15));
                mMap.addMarker(new MarkerOptions().position(start)
                        .icon(BitmapDescriptorFactory.defaultMarker(
                                BitmapDescriptorFactory.HUE_GREEN)));

                mMap.addMarker(new MarkerOptions().position(end)
                        .icon(BitmapDescriptorFactory.fromResource(getResources().getIdentifier("th_" + user_id, "drawable", getPackageName()))));
            }
        });

        if (mCurrentLocation == null) {
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        }


        //markers = mMap.addMarker(new MarkerOptions().position(new LatLng(59.324500, 18.068698)).icon(BitmapDescriptorFactory.fromResource(R.drawable.th_pekka)));
        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(59.324500, 18.068698), 13)); // stockholm
    }

    protected synchronized void buildGoogleApiClient() {

        Log.w(TAG, "building client");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop location updates to save battery, but don't disconnect the GoogleApiClient object.
        stopLocationUpdates();
    }
    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
        if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }


    public void onConnected(Bundle connectionHint) {
        // Provides a simple way of getting a device's location and is well suited for
        // applications that do not require a fine-grained location and that do not need location
        // updates. Gets the best and most recent location currently available, which may be null
        // in rare cases when a location is not available

        if (mCurrentLocation == null) {
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        }
        Log.w(TAG, "connected");
        this.drawDirection();
        this.startLocationUpdates();

    }

    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        mGoogleApiClient.connect();
        Log.i(TAG, "Connection suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    protected void startLocationUpdates() {
       LocationServices.FusedLocationApi.requestLocationUpdates(
                 mGoogleApiClient, mLocationRequest, this);
    }
    protected void stopLocationUpdates() {
        //LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    }

    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        Log.w(TAG, "location changed");

        Firebase locationRef = fireRef.child("users/" + me.getId() + "/position");
        Map<String, String> post1 = new HashMap<String, String>();
        post1.put("lat", ""+location.getLatitude());
        post1.put("lng", ""+location.getLongitude());
        locationRef.setValue(post1);

        this.drawDirection();
        //mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
    }

    public void drawDirection() {
        //Log.w(TAG, "in draw direction b: " + directionDrawn + " l: " + mCurrentLocation.toString());
        if (!directionDrawn && mCurrentLocation != null && user_location != null) {
            Log.w(TAG, "actually drawing");
            start = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
            end = user_location;
            gd.setLogging(true);
            gd.request(start, end, GoogleDirection.MODE_WALKING);
            directionDrawn = true;
        }
    }
}
