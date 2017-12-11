package edu.illinois.finalproject;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Chris Zhu on 12/5/2017.
 */

public class GameActivity extends AppCompatActivity {
    private ImageView photoDisplayView;
    private TextView guessesRemainingTextView;
    private TextView pointsTextView;
    private EditText answerEditText;

    private Random random = new Random();
    private ArrayList<Integer> playedPhotos;
    private String caption;
    private int guesses = 3;
    private int points;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        photoDisplayView = (ImageView) findViewById(R.id.photoDisplayView);
        guessesRemainingTextView = (TextView) findViewById(R.id.guessesTextView);
        pointsTextView = (TextView) findViewById(R.id.pointsTextView);
        answerEditText = (EditText) findViewById(R.id.userGuessEditText);
        loadImage(0);
    }

    /**
     * Takes specified photo from the database and loads it into the ImageView
     */
    private void loadImage(int loadPhotoNumber) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference myRef = database.getReference("captions/image" + loadPhotoNumber);
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                caption = dataSnapshot.getValue(String.class);
                guessesRemainingTextView.setText(caption);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        StorageReference storageReference =
                FirebaseStorage.getInstance()
                        .getReferenceFromUrl("gs://final-project-17c20.appspot.com/images")
                        .child("image" + loadPhotoNumber + ".jpg");

        FirebaseImageLoader firebaseImageLoader = new FirebaseImageLoader();
        Glide.with(this).using(firebaseImageLoader)
                .load(storageReference)
                .signature(new StringSignature(String.valueOf(System.currentTimeMillis())))
                .into(photoDisplayView);
    }
}
