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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.firebase.client.Firebase;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;


public class SentPhoto extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sent_photo);

        //Firebase init
        Firebase.setAndroidContext(this);
        Firebase fireRef = new Firebase("https://sweltering-torch-662.firebaseio.com/chiiz");

        Intent intent = getIntent();
        Uri path = intent.getParcelableExtra("photoPath");
        //Toast.makeText(this, "Video saved to:\n" + path.toString(), Toast.LENGTH_LONG).show();

        ImageView iv = (ImageView) findViewById(R.id.sentPhoto);

        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), path);
        } catch (IOException e) {
            e.printStackTrace();    //it seems like the image is not saved..
        }
        iv.setImageBitmap(bitmap);

        //Uploading image to firebase
        ByteArrayOutputStream bYtE = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bYtE);
        //bmp.recycle();
        byte[] byteArray = bYtE.toByteArray();
        String imageFile = Base64.encodeToString(byteArray, Base64.DEFAULT);

        Map<String, String> post1 = new HashMap<String, String>();
        post1.put("name", "Pekka");
        post1.put("img", imageFile);
        fireRef.push().setValue(post1);
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
