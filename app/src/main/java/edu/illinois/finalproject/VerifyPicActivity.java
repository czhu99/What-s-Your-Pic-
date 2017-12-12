package edu.illinois.finalproject;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

/**
 * Created by Chris Zhu on 12/5/2017.
 */

public class VerifyPicActivity extends AppCompatActivity {
    public static final int MIN_CAPTION_LEN = 2;
    public static final int MAX_CAPTION_LEN = 20;

    private boolean emptyReferenceFound = false;
    private int photoNumber;

    private EditText captionEditText;
    private Bitmap picture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_pic);

        captionEditText = (EditText) findViewById(R.id.captionEditText);
        ImageView photoDisplayView = (ImageView) findViewById(R.id.uploadImageView);

        Intent intent = getIntent();
        picture = intent.getParcelableExtra("Picture");
        photoDisplayView.setImageBitmap(picture);


        final Button uploadButton = (Button) findViewById(R.id.uploadButton);
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String caption = captionEditText.getText().toString();
                if (caption.length() < MIN_CAPTION_LEN) {
                    showToast("Caption must be at least two characters in length.");
                } else if (caption.length() > MAX_CAPTION_LEN) {
                    showToast("Caption cannot exceed twenty characters in length.");
                } else {
                    setNextPhotoNumAndUpload();
                    finish();
                }
            }
        });
    }

    private void setNextPhotoNumAndUpload() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        emptyReferenceFound = false;

        DatabaseReference myRef = database.getReference("captions");
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                while (!emptyReferenceFound) {
                    if (dataSnapshot.hasChild("image" + photoNumber)) {
                        photoNumber++;
                    } else {
                        emptyReferenceFound = true;
                    }
                }
                uploadFile(picture);
                uploadCaptionData();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    /**
     * Uploads a photo into the database
     *
     * @param bitmap The photo to be uploaded
     */
    private void uploadFile(Bitmap bitmap) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef =
                storage.getReferenceFromUrl("gs://final-project-17c20.appspot.com/images");
        StorageReference imageReference = storageRef.child("image" + photoNumber + ".jpg");
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] data = byteArrayOutputStream.toByteArray();
        UploadTask uploadTask = imageReference.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                showToast("Upload failed.");
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                showToast("Image uploaded.");
            }
        });
    }

    /**
     * Allows for assignment of a caption for a certain photo into the database.
     * Uses the photo id to pair a caption to a photo.
     */
    private void uploadCaptionData() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference myRef = database.getReference("captions/image" + photoNumber);
        final String caption = captionEditText.getText().toString();
        myRef.setValue(caption);

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void showToast(String message) {
        //outline for toast code segment from
        //https://developer.android.com/guide/topics/ui/notifiers/toasts.html
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, message, duration);
        toast.show();
    }
}
