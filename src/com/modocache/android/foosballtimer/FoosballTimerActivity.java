package com.modocache.android.foosballtimer;

import android.os.Bundle;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

public class FoosballTimerActivity extends Activity {

    static private final int gameMaximumTime = 240;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public void onStartGameButtonClick(View button) {
        Toast.makeText(getBaseContext(),
                       getString(R.string.start_game_toast_text),
                       Toast.LENGTH_SHORT).show();

        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            fragmentTransaction.remove(prev);
        }
        fragmentTransaction.addToBackStack(null);

        GameProgressFragment gameProgressFragment = GameProgressFragment.newInstance(gameMaximumTime);
        gameProgressFragment.show(fragmentTransaction, "dialog");
    }
}