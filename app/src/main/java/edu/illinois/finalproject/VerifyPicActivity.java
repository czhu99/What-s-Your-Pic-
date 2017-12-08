package edu.illinois.finalproject;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

/**
 * Created by Chris Zhu on 12/5/2017.
 */

public class VerifyPicActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verifypic);

        Intent intent = getIntent();
        Bitmap picture = intent.getParcelableExtra("Picture");

        ImageView photoDisplayView = (ImageView) findViewById(R.id.uploadImageView);
        photoDisplayView.setImageBitmap(picture);
    }
}
