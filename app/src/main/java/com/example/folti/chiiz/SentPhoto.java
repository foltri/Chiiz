package com.example.folti.chiiz;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.maps.model.LatLng;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class SentPhoto extends ActionBarActivity {

    private String user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sent_photo);

        Intent intent = getIntent();
        Uri path = intent.getParcelableExtra("photoPath");
        user_id = intent.getStringExtra("user_id");

        //Toast.makeText(this, "Video saved to:\n" + path.toString(), Toast.LENGTH_LONG).show();

        //Firebase init
        Firebase.setAndroidContext(this);
        Firebase fireRef = new Firebase("https://burning-inferno-7965.firebaseio.com/photos/" + user_id);

        Firebase userRef = new Firebase("https://burning-inferno-7965.firebaseio.com/users/" + user_id);

        ValueEventListener users = userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot profile) {
                //System.out.println(snapshot.getValue());  //prints "Do you have data? You'll love Firebase."

                SimpleDateFormat s = new SimpleDateFormat("dd MMMM yyyy 'at' kk:mm");
                String format = s.format(new Date());
                ((TextView)findViewById(R.id.textView)).setText("Photo of " + profile.child("name").getValue(String.class) + ", " + format);
                setTitle(profile.child("name").getValue(String.class)+"'s photo");
            }

            @Override
            public void onCancelled(FirebaseError error) {
            }
        });

        ImageView iv = (ImageView) findViewById(R.id.sentPhoto);

        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        iv.setImageBitmap(bitmap);

        // resize bitmap
        //bitmap =  Bitmap.createScaledBitmap(bitmap, 120, 120, false)

        //Uploading image to firebase
        ByteArrayOutputStream bYtE = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.WEBP, 60, bYtE);
        //bmp.recycle();
        byte[] byteArray = bYtE.toByteArray();
        String imageFile = Base64.encodeToString(byteArray, Base64.DEFAULT);

        Map<String, String> post1 = new HashMap<String, String>();
        post1.put("timestamp", ""+(System.currentTimeMillis() / 1000L));
        post1.put("dataURL", imageFile);
        post1.put("encoding", "data:image/webp;base64");
        fireRef.push().setValue(post1);
    }

    @Override
    public Intent getSupportParentActivityIntent() {
        Intent intent = new Intent(SentPhoto.this, PhotoTarget.class);
        intent.putExtra(PeopleNearby.EXTRA_USERID, user_id);
        return intent;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_sent_photo, menu);
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
            Intent intent = new Intent(SentPhoto.this, MyPhotos.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void OKbuttonClicked(View view) {
        Intent intent = new Intent(SentPhoto.this, PeopleNearby.class);
        startActivity(intent);
    }


}
