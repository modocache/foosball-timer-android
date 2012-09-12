package com.modocache.android.foosballtimer;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class FoosballTimerActivity extends Activity {

    static private final int preferencesMenuItemId = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem preferencesMenuItem = menu.add(0,
                                                preferencesMenuItemId,
                                                0,
                                                R.string.pref_menu_item_title);
        preferencesMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == preferencesMenuItemId) {
            startActivity(new Intent(this, TimerPreferenceActivity.class));
            return true;
        }
        return false;
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

        String gameTimeString = PreferenceManager
                                    .getDefaultSharedPreferences(getBaseContext())
                                    .getString("gameTime", "4");
        int gameTime = Integer.parseInt(gameTimeString) * 60;
        GameProgressFragment gameProgressFragment = GameProgressFragment.newInstance(gameTime);
        gameProgressFragment.show(fragmentTransaction, "dialog");
    }
}