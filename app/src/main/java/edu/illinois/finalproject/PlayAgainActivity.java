package edu.illinois.finalproject;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by Chris Zhu on 12/11/2017.
 */

public class PlayAgainActivity extends AppCompatActivity {
    private int points;
    private int rounds;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_again);

        Intent intent = getIntent();
        String message = intent.getExtras().getString(getString(R.string.guesses));
        points = intent.getExtras().getInt(getString(R.string.points));
        rounds = intent.getExtras().getInt(getString(R.string.rounds));

        TextView messageTextView = (TextView) findViewById(R.id.roundCompleteMessage);
        messageTextView.setText(message);

        Button yesButton = (Button) findViewById(R.id.yesButton);
        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        Button noButton = (Button) findViewById(R.id.noButton);
        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                endGame();
                finish();
            }
        });
    }

    private void endGame() {
        Intent finishGameIntent =
                new Intent(getApplicationContext(), FinishGameActivity.class);
        finishGameIntent.putExtra(getString(R.string.points), points);
        finishGameIntent.putExtra(getString(R.string.rounds), rounds);
        startActivity(finishGameIntent);
    }
}
