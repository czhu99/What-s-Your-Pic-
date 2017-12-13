package edu.illinois.finalproject;

//unused imports are for commented out (not yet implemented) features
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {
    private static final int CAMERA_REQUEST = 1888;
    //private Uri outPutfileUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        //StrictMode.setVmPolicy(builder.build());

        Button playButton = (Button) findViewById(R.id.playButton);
        runPlayButtonOperations(playButton);

        Button addPicButton = (Button) findViewById(R.id.addPicButton);
        runAddPicButtonOperations(addPicButton);
    }

    /**
     * Launches intent for taking a picture on button press
     *
     * @param addPicButton the button for adding a picture
     */
    private void runAddPicButtonOperations(Button addPicButton) {
        addPicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
                //Intent intent= new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                //File file = new File(Environment.getExternalStorageDirectory(), "MyPhoto.jpg");
                //outPutfileUri = Uri.fromFile(file);
                //intent.putExtra(MediaStore.EXTRA_OUTPUT, outPutfileUri);
                //startActivityForResult(intent, CAMERA_REQUEST);
            }
        });
    }

    /**
     * Launches intent for GameActivity on button press
     *
     * @param playButton the button for playing the game
     */
    private void runPlayButtonOperations(Button playButton) {
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Context context = v.getContext();
                Intent gameIntent = new Intent(context, GameActivity.class);
                context.startActivity(gameIntent);
            }
        });
    }

    /**
     * Creates intent for VerifyPic if camera operation was successful.
     * Sends the photo taken and labels it "Picture"
     *
     * @param requestCode Request code called
     * @param resultCode  Result code given
     * @param data        The intent returned from activity
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            Intent verifyIntent = new Intent(this, VerifyPicActivity.class);
            verifyIntent.putExtra(getString(R.string.picture),
                    (Bitmap) data.getExtras().get(getString(R.string.data)));
            this.startActivity(verifyIntent);

            //File imageFile = new File(Environment.getExternalStorageDirectory(), "MyPhoto.jpg");
            //Bitmap picture =
            // BitmapFactory.decodeFile(Environment.getExternalStorageDirectory() + "/MyPhoto.jpg");

            //Intent verifyIntent = new Intent(this, VerifyPicActivity.class);
            //verifyIntent.putExtra("picture", picture);
            //startActivity(verifyIntent);
        }
    }
}
