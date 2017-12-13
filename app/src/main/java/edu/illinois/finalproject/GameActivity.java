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
    public static final int MAX_GUESSES = 3;
    public static final int MID_GUESSES = 2;
    public static final int MAX_POINTS = 3;
    public static final int MID_POINTS = 2;


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
    private int numPhotosInDatabase = 0;
    private int numPhotosLoaded = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        photoDisplayView = (ImageView) findViewById(R.id.photoDisplayView);
        guessesRemainingTextView = (TextView) findViewById(R.id.guessesTextView);
        pointsTextView = (TextView) findViewById(R.id.pointsTextView);

        guessesRemainingTextView.setText(getString(R.string.guesses_remaining) + guessesRemaining);
        pointsTextView.setText(getString(R.string.total_points) + Integer.toString(points));

        getRandomUnusedPhotoAndLoadData();

        answerEditText = (EditText) findViewById(R.id.userGuessEditText);
        answerEditText.setFocusableInTouchMode(true);
        answerEditText.requestFocus();
        answerEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KEYCODE_ENTER && event.getAction() == ACTION_DOWN) {
                    makeGuess();
                    InputMethodManager inputMethodManager =
                            (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    return true;
                }
                return false;
            }
        });
    }

    /**
     * Finds a random photo number that has not been seen yet and reads the data in for that photo
     */
    private void getRandomUnusedPhotoAndLoadData() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference(getString(R.string.captions));
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                while (dataSnapshot.hasChild(getString(R.string.image) + numPhotosInDatabase)) {
                    numPhotosInDatabase++;
                }
                while (true) {
                    if (numPhotosLoaded == numPhotosInDatabase) { //when all photos have been played
                        showToast(getString(R.string.no_unplayed_rmng));
                        //launches game end activity when no more pictures
                        Intent finishGameIntent =
                                new Intent(getApplicationContext(), FinishGameActivity.class);
                        finishGameIntent.putExtra(getString(R.string.points), points);
                        finishGameIntent.putExtra(getString(R.string.rounds), numPhotosLoaded);
                        startActivity(finishGameIntent);

                        finish();
                        return;
                    }
                    int randomNumber = random.nextInt(numPhotosInDatabase);
                    if (dataSnapshot.hasChild(getString(R.string.image) + randomNumber) &&
                            !playedPhotos.contains(randomNumber)) {
                        playedPhotos.add(randomNumber);
                        nextNumber = randomNumber;
                        loadImageAndCaption(nextNumber);
                        numPhotosLoaded++;
                        return;
                    }
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

            //used to determine if user is told they won in number of "tries" or one "try"
            String triesString = getString(R.string.tries_play_again);
            if (totalGuessesMade == MAX_GUESSES) {
                points++;
            } else if (totalGuessesMade == MID_GUESSES) {
                points += MID_POINTS;
            } else {
                points += MAX_POINTS;
                triesString = getString(R.string.try_play_again);
            }
            pointsTextView.setText(getString(R.string.total_pts) + points);

            if (numPhotosLoaded != numPhotosInDatabase) {
                //launches the "play again" screen with "guessed right" message
                launchPlayAgainIntent(getString(R.string.you_guessed_right_in)
                        + totalGuessesMade + triesString);
            }

            guessesRemaining = MAX_GUESSES;
            guessesRemainingTextView.setText(getString(R.string.guesses_rmng) + guessesRemaining);
            answerEditText.setText("");
            getRandomUnusedPhotoAndLoadData();
        } else { //when the guess is incorrect
            //when the user has run out of tries and there are still unused photos
            if (guessesRemaining == 0 && numPhotosLoaded != numPhotosInDatabase) {
                //launches the "play again" screen with "did not guess right" message
                launchPlayAgainIntent(getString(R.string.did_not_guess_right)
                        + caption + getString(R.string.quote_try_again));
                guessesRemaining = MAX_GUESSES;
                answerEditText.setText("");
                getRandomUnusedPhotoAndLoadData();
            } else { //when the user has tries remaining
                guessesRemainingTextView.setText(getString(R.string.guesses_rmng) + guessesRemaining);
                showToast(getString(R.string.incorrect_try_again));
            }
        }
    }

    /**
     * Launches a PlayAgainActivity with appropriate message
     *
     * @param message
     */
    private void launchPlayAgainIntent(String message) {
        Intent againIntent = new Intent(this, PlayAgainActivity.class);
        againIntent.putExtra(getString(R.string.points), points);
        againIntent.putExtra(getString(R.string.rounds), numPhotosLoaded);
        againIntent.putExtra(getString(R.string.guesses), message);
        startActivity(againIntent);
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
