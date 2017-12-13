package edu.illinois.finalproject;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by Chris Zhu on 12/12/2017.
 */

public class FinishGameActivity extends AppCompatActivity {
    private int points;
    private int rounds;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finish_game);

        TextView messageTextView = (TextView) findViewById(R.id.gameCompleteMessageTextView);

        Intent intent = getIntent();
        points = intent.getExtras().getInt(getString(R.string.points));
        rounds = intent.getExtras().getInt(getString(R.string.rounds));
        messageTextView.setText(getString(R.string.game_completed_you_earned)
            + points + getString(R.string.points_in) + rounds + getString(R.string.rounds_exclam));

        Button mainMenuButton = (Button) findViewById(R.id.mainMenuButton);
        mainMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent list = new Intent(getApplicationContext(), MainActivity.class);
                list.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(list);
                finish();
            }
        });
    }
}
