package edu.illinois.finalproject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

import static android.view.KeyEvent.ACTION_DOWN;
import static android.view.KeyEvent.KEYCODE_ENTER;

/**
 * Created by Chris Zhu on 12/5/2017.
 */

public class GameActivity extends AppCompatActivity {
    public static final int MAX_PHOTOS = 100;
    public static final int MAX_GUESSES = 3;

    private ImageView photoDisplayView;
    private TextView guessesRemainingTextView;
    private TextView pointsTextView;
    private EditText answerEditText;

    private Random random = new Random();
    private ArrayList<Integer> playedPhotos = new ArrayList<>();
    private int nextNumber;
    private String caption;

    private int guessesRemaining = MAX_GUESSES;
    private int points = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        photoDisplayView = (ImageView) findViewById(R.id.photoDisplayView);
        guessesRemainingTextView = (TextView) findViewById(R.id.guessesTextView);
        pointsTextView = (TextView) findViewById(R.id.pointsTextView);

        guessesRemainingTextView.setText(getString(R.string.guesses_remaining) + guessesRemaining);
        pointsTextView.setText(getString(R.string.total_points) + Integer.toString(points));

        getRandomUnusedNumberAndLoadData();

        answerEditText = (EditText) findViewById(R.id.userGuessEditText);
        answerEditText.setFocusableInTouchMode(true);
        answerEditText.requestFocus();
        answerEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KEYCODE_ENTER && event.getAction() == ACTION_DOWN) {
                    makeGuess();
                    InputMethodManager imm =
                            (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    return true;
                }
                return false;
            }
        });
    }

    /**
     * Finds a random photo number that has not been seen yet and reads the data in for that photo
     */
    private void getRandomUnusedNumberAndLoadData() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference(getString(R.string.captions));
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int counter = 0;
                while (true) {
                    if (counter > 100) {
                        showToast(getString(R.string.no_unplayed_rmng));
                        finish();
                        return;
                    }
                    int number = random.nextInt(MAX_PHOTOS);
                    if (dataSnapshot.hasChild(getString(R.string.image) + number) &&
                            !playedPhotos.contains(number)) {
                        playedPhotos.add(number);
                        nextNumber = number;
                        loadImageAndCaption(nextNumber);
                        return;
                    }
                    counter++;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     * Takes specified photo and it image from the database and loads it into the ImageView
     */
    private void loadImageAndCaption(int loadPhotoNumber) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference myRef =
                database.getReference(getString(R.string.captions_sub_image) + loadPhotoNumber);
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                caption = dataSnapshot.getValue(String.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        StorageReference storageReference =
                FirebaseStorage.getInstance()
                        .getReferenceFromUrl(getString(R.string.storage_directory_fb))
                        .child(getString(R.string.image) + loadPhotoNumber
                                + getString(R.string.dot_jpg));

        FirebaseImageLoader firebaseImageLoader = new FirebaseImageLoader();
        Glide.with(this).using(firebaseImageLoader)
                .load(storageReference)
                .signature(new StringSignature(String.valueOf(System.currentTimeMillis())))
                .into(photoDisplayView);
    }

    /**
     * Takes the text input from the user and uses it as a guess for the current caption
     */
    public void makeGuess() {
        guessesRemaining--;
        String guess = answerEditText.getText().toString();
        if (guess.equalsIgnoreCase(caption)) { //when the guess is correct
            int totalGuessesMade = MAX_GUESSES - guessesRemaining;

            //used to determine if user is told number of "tries" or one "try"
            String triesString = getString(R.string.tries_play_again);
            if (totalGuessesMade == 3) {
                points += 1;
            } else if (totalGuessesMade == 2) {
                points += 2;
            } else {
                points += 3;
                triesString = getString(R.string.try_play_again);
            }
            pointsTextView.setText(getString(R.string.total_pts) + points);


            //launches the "play again" screen with appropriate information in the message
            Intent againIntent = new Intent(this, PlayAgainActivity.class);
            againIntent.putExtra(getString(R.string.guesses),
                    getString(R.string.you_guessed_right_in)
                            + totalGuessesMade + triesString);
            startActivity(againIntent);

            guessesRemaining = MAX_GUESSES;
            guessesRemainingTextView.setText(getString(R.string.guesses_rmng) + guessesRemaining);
            answerEditText.setText("");
            getRandomUnusedNumberAndLoadData();
        } else { //when the guess is incorrect
            if (guessesRemaining == 0) { //when the user has run out of tries
                //launches the "play again" screen with appropriate information in the message
                Intent playAgainIntent = new Intent(this, PlayAgainActivity.class);
                playAgainIntent.putExtra(getString(R.string.guesses),
                        getString(R.string.did_not_guess_right));
                startActivity(playAgainIntent);
                guessesRemaining = MAX_GUESSES;
                answerEditText.setText("");
                getRandomUnusedNumberAndLoadData();
            } else { //when the user has tries remaining
                guessesRemainingTextView.setText(getString(R.string.guesses_rmng) + guessesRemaining);
                showToast(getString(R.string.incorrect_try_again));
            }
        }
    }

    public void showToast(String message) {
        //outline for toast code segment from
        //https://developer.android.com/guide/topics/ui/notifiers/toasts.html
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, message, duration);
        toast.show();
    }
}
