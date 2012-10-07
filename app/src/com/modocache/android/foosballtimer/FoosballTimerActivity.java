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

    static private final String STATE_CURRENT_SECONDS_KEY = "STATE_CURRENT_SECONDS_KEY";
    static private final String STATE_IN_PROGRESS_KEY = "STATE_IN_PROGRESS_KEY";
    private int currentSeconds;
    private boolean isGameInProgress;

    private Intent timerServiceIntent;
    private TextView secondsTextView;
    private Button toggleGameButton;


    // android.app.Activity Overrides
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        secondsTextView = (TextView) findViewById(R.id.seconds_textview);
        secondsTextView.setText(Integer.toString(0));
        toggleGameButton = (Button) findViewById(R.id.start_game_button);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        IntentFilter secondsUpdatedIntentFilter = new IntentFilter();
        secondsUpdatedIntentFilter.addAction(GameProgressUpdateThread.SECONDS_UPDATED_ACTION);
        registerReceiver(secondsUpdatedIntentReceiver, secondsUpdatedIntentFilter);

        IntentFilter gameEndedIntentFilter = new IntentFilter();
        gameEndedIntentFilter.addAction(GameProgressUpdateThread.GAME_ENDED_ACTION);
        registerReceiver(gameEndedIntentReceiver, gameEndedIntentFilter);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        updateButtonText(GameProgressUpdateThread.isGameInProgress());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        this.currentSeconds = savedInstanceState.getInt(STATE_CURRENT_SECONDS_KEY);
        this.isGameInProgress = savedInstanceState.getBoolean(STATE_IN_PROGRESS_KEY);
        updateButtonText(this.isGameInProgress);
        this.secondsTextView.setText(Integer.toString(this.currentSeconds));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(STATE_CURRENT_SECONDS_KEY, this.currentSeconds);
        outState.putBoolean(STATE_IN_PROGRESS_KEY, this.isGameInProgress);
        super.onSaveInstanceState(outState);
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


    // Public Interface
    public void onStartGameButtonClick(View button) {
        isGameInProgress = GameProgressUpdateThread.isGameInProgress();
        if (isGameInProgress) {
            updateButtonText(false);
            GameProgressUpdateThread.stopSharedThread();
            timerServiceIntent = null;
        } else {
            updateButtonText(true);
            timerServiceIntent = new Intent(this, FoosballTimerService.class);
            String gameTimeString = PreferenceManager
                                        .getDefaultSharedPreferences(getBaseContext())
                                        .getString("gameTime", "4");
            int gameTime = Integer.parseInt(gameTimeString) * 60;
            timerServiceIntent.putExtra(GAME_TIME_KEY, gameTime);
            startService(timerServiceIntent);
        }
    }
    

    // Private Interface
    private void updateButtonText(Boolean isGameInProgress) {
        String text = isGameInProgress ?
                getString(R.string.stop_game_button_text) :
                getString(R.string.start_game_button_text);
        toggleGameButton.setText(text);
    }

    private BroadcastReceiver secondsUpdatedIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            currentSeconds = intent.getIntExtra(GameProgressUpdateThread.SECONDS_KEY, 0);
            secondsTextView.setText(Integer.toString(currentSeconds));
        }
    };

    private BroadcastReceiver gameEndedIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            currentSeconds = 0;
            secondsTextView.setText(Integer.toString(0));
            updateButtonText(false);
        }
    };
}