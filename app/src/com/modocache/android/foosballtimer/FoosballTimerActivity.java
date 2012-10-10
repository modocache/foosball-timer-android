package com.modocache.android.foosballtimer;

import java.nio.charset.Charset;

import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.nfc.NfcAdapter.OnNdefPushCompleteCallback;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.MenuCompat;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class FoosballTimerActivity extends FragmentActivity
    implements CreateNdefMessageCallback, OnNdefPushCompleteCallback {

    static public final String GAME_TIME_KEY = "GAME_TIME_KEY";
    static private final int preferencesMenuItemId = 1;

    static private final String STATE_CURRENT_SECONDS_KEY = "STATE_CURRENT_SECONDS_KEY";
    static private final String STATE_IN_PROGRESS_KEY = "STATE_IN_PROGRESS_KEY";
    private int currentSeconds;
    private boolean isGameInProgress;
    private Intent timerServiceIntent;

    private static final String PACKAGE_NAME = "com.modocache.android.foosballtimer";
    private static final String NFC_MIME_TYPE = "application/" + PACKAGE_NAME + ".nfc";
    private static final int NFC_MESSAGE_SENT_TAG = 1;
    private NfcAdapter nfcAdapter;
    private Boolean isNfcEnabled;

    private TextView secondsTextView;
    private Button toggleGameButton;


    // android.app.Activity Overrides
    @SuppressLint("NewApi")
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

        setupNfc();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateButtonText(GameProgressUpdateThread.isGameInProgress());

        String action = getIntent().getAction();
        if (this.isNfcEnabled && NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            processNfcIntent(getIntent());
        }
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


    // CreateNdefMessageCallback Interface Methods
    @SuppressLint("NewApi")
    @Override
    public NdefMessage createNdefMessage(NfcEvent event) {
        if (!this.isNfcEnabled) {
            return null;
        }

        byte[] secondsPayload = secondsTextView.getText().toString().getBytes();
        byte[] mimeBytes = NFC_MIME_TYPE.getBytes(Charset.forName("US-ASCII"));
        NdefRecord record = new NdefRecord(NdefRecord.TNF_MIME_MEDIA,
                                          mimeBytes,
                                          new byte[0],
                                          secondsPayload);
        NdefMessage message = new NdefMessage(new NdefRecord[] {
                record,
                NdefRecord.createApplicationRecord(PACKAGE_NAME)
        });
        return message;
    }


    // OnNdefPushCompleteCallback Interface Methods
    @SuppressLint("NewApi")
    @Override
    public void onNdefPushComplete(NfcEvent event) {
        nfcPushCompleteHandler.obtainMessage(NFC_MESSAGE_SENT_TAG).sendToTarget();
    }


    // Public Interface
    public void onStartGameButtonClick(View button) {
        isGameInProgress = GameProgressUpdateThread.isGameInProgress();
        if (isGameInProgress) {
            updateButtonText(false);
            GameProgressUpdateThread.stopSharedThread();
            timerServiceIntent = null;
        } else {
            String gameTimeString = PreferenceManager
                                        .getDefaultSharedPreferences(getBaseContext())
                                        .getString("gameTime", "4");
            int gameTime = Integer.parseInt(gameTimeString) * 60;
            startGame(gameTime);
        }
    }


    // Private Interface
    private void startGame(int gameTime) {
        updateButtonText(true);
        timerServiceIntent = new Intent(this, FoosballTimerService.class);
        timerServiceIntent.putExtra(GAME_TIME_KEY, gameTime);
        startService(timerServiceIntent);
    }

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

    @SuppressLint("NewApi")
    private void setupNfc() {
        if (android.os.Build.VERSION.SDK_INT >= 10) {
            this.nfcAdapter = NfcAdapter.getDefaultAdapter(FoosballTimerActivity.this);
            if (this.nfcAdapter != null) {
                this.nfcAdapter.setNdefPushMessageCallback(this, this);
                this.nfcAdapter.setOnNdefPushCompleteCallback(this, this);
            }
        }

        this.isNfcEnabled = (this.nfcAdapter != null);
    }

    private final Handler nfcPushCompleteHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
            case NFC_MESSAGE_SENT_TAG:
                Toast.makeText(getApplicationContext(),
                               getString(R.string.nfc_push_complete_message),
                               Toast.LENGTH_SHORT).show();
                break;
            }
        };
    };

    @SuppressLint("NewApi")
    private void processNfcIntent(Intent intent) {
        if (!this.isNfcEnabled) {
            return;
        }

        if (this.isGameInProgress) {
            Toast.makeText(FoosballTimerActivity.this,
                           getString(R.string.nfc_game_cannot_sync),
                           Toast.LENGTH_SHORT).show();
            return;
        }

        Parcelable[] rawMessages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        NdefMessage message = (NdefMessage) rawMessages[0];
        String secondsPayload = new String(message.getRecords()[0].getPayload());
        startGame(Integer.parseInt(secondsPayload));
    }
}
