package com.modocache.android.foosballtimer;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
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

    static public final String SECONDS_UPDATED_ACTION = "SECONDS_UPDATED_ACTION";
    static public final String GAME_ENDED_ACTION = "GAME_ENDED_ACTION";
    static public final String SECONDS_KEY = "SECONDS_KEY";
    static private final int defaultGameMaximumTime = 4 * 60;
    private Thread gameProgressUpdateThread;
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

        final int gameTime = intent.getIntExtra(FoosballTimerActivity.GAME_TIME_KEY,
                                                defaultGameMaximumTime);
        gameProgressUpdateThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    for (int i = 0; i < gameTime; i++) {
                        Intent broadcastIntent = new Intent();
                        broadcastIntent.setAction(SECONDS_UPDATED_ACTION);
                        broadcastIntent.putExtra(SECONDS_KEY, gameTime - i);
                        getBaseContext().sendBroadcast(broadcastIntent);
                        Thread.sleep(1000);
                    }
                    stopSelf();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        gameProgressUpdateThread.start();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        gameProgressUpdateThread.interrupt();
        gameProgressUpdateThread = null;

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

        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(GAME_ENDED_ACTION);
        getBaseContext().sendBroadcast(broadcastIntent);

        super.onDestroy();
    }
}