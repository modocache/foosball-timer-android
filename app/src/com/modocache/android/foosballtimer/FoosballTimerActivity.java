package com.modocache.android.foosballtimer;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.MenuCompat;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class FoosballTimerActivity extends FragmentActivity {

    static public final String GAME_TIME_KEY = "GAME_TIME_KEY";
    static private final int preferencesMenuItemId = 1;
    private Intent timerServiceIntent;
    private TextView secondsTextView;
    private Button toggleGameButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        secondsTextView = (TextView) findViewById(R.id.seconds_textview);
        secondsTextView.setText(Integer.toString(0));
        toggleGameButton = (Button) findViewById(R.id.start_game_button);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        IntentFilter secondsUpdatedIntentFilter = new IntentFilter();
        secondsUpdatedIntentFilter.addAction(FoosballTimerService.SECONDS_UPDATED_ACTION);
        registerReceiver(secondsUpdatedIntentReceiver, secondsUpdatedIntentFilter);

        IntentFilter gameEndedIntentFilter = new IntentFilter();
        gameEndedIntentFilter.addAction(FoosballTimerService.GAME_ENDED_ACTION);
        registerReceiver(gameEndedIntentReceiver, gameEndedIntentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(secondsUpdatedIntentReceiver);
        unregisterReceiver(gameEndedIntentReceiver);
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem preferencesMenuItem = menu.add(0,
                                                preferencesMenuItemId,
                                                0,
                                                R.string.pref_menu_item_title);
        MenuCompat.setShowAsAction(preferencesMenuItem,
                                   MenuItem.SHOW_AS_ACTION_IF_ROOM);
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
        String buttonText = toggleGameButton.getText().toString();
        String startText = getString(R.string.start_game_button_text);
        String stopText = getString(R.string.stop_game_button_text);

        if (buttonText.equals(startText)) {
            toggleGameButton.setText(stopText);
            timerServiceIntent = new Intent(this, FoosballTimerService.class);
            String gameTimeString = PreferenceManager
                                        .getDefaultSharedPreferences(getBaseContext())
                                        .getString("gameTime", "4");
            int gameTime = Integer.parseInt(gameTimeString) * 60;
            timerServiceIntent.putExtra(GAME_TIME_KEY, gameTime);
            startService(timerServiceIntent);
        } else {
            toggleGameButton.setText(startText);
            stopService(new Intent(this, FoosballTimerService.class));
            timerServiceIntent = null;
        }
    }

    private BroadcastReceiver secondsUpdatedIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int currentSeconds = intent.getIntExtra(FoosballTimerService.SECONDS_KEY, 0);
            secondsTextView.setText(Integer.toString(currentSeconds));
        }
    };

    private BroadcastReceiver gameEndedIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            secondsTextView.setText(Integer.toString(0));
            toggleGameButton.setText(getString(R.string.start_game_button_text));
        }
    };
}