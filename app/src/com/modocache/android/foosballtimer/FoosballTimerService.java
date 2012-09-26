package com.modocache.android.foosballtimer;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class FoosballTimerService extends Service {
    static private final int defaultGameMaximumTime = 4 * 60;
    private Ringtone ringtone;
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(getBaseContext(),
                getString(R.string.start_game_toast_text),
                Toast.LENGTH_SHORT).show();

        IntentFilter gameEndedIntentFilter = new IntentFilter();
        gameEndedIntentFilter.addAction(GameProgressUpdateThread.GAME_ENDED_ACTION);
        registerReceiver(gameEndedIntentReceiver, gameEndedIntentFilter);

        GameProgressUpdateThread.setSharedThreadContext(getBaseContext());
        final int gameTime = intent.getIntExtra(FoosballTimerActivity.GAME_TIME_KEY,
                								defaultGameMaximumTime);
        GameProgressUpdateThread.setSharedThreadGameTime(gameTime);
        GameProgressUpdateThread.startSharedThread();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Toast.makeText(getBaseContext(),
                       getString(R.string.end_game_toast_text),
                       Toast.LENGTH_SHORT).show();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        if (preferences.getBoolean("isVibrateOn", true)) {
            Vibrator vibrator = (Vibrator) getBaseContext().getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(800);
        }

        if (preferences.getBoolean("isSoundOn", true)) {
            if (ringtone != null) {
                ringtone.stop();
                ringtone = null;
            }

            Uri ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            ringtone = RingtoneManager.getRingtone(getBaseContext(), ringtoneUri);
            ringtone.play();

            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (ringtone != null) {
                        ringtone.stop();
                        ringtone = null;
                    }
                }
            }, 800);
        }

        unregisterReceiver(gameEndedIntentReceiver);
        super.onDestroy();
    }
    
    private BroadcastReceiver gameEndedIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            stopSelf();
        }
    };
}