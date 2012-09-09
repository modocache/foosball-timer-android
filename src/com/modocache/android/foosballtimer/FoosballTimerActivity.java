package com.modocache.android.foosballtimer;

import android.os.Bundle;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

public class FoosballTimerActivity extends Activity {

    static private final int gameMaximumTime = 240;
    static private final int gameProgressDialogIdentifier = 0;
    ProgressDialog gameProgressDialog;
    private Thread gameProgressUpdateThread;

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

    @Override
    @Deprecated
    protected Dialog onCreateDialog(int id) {
        switch (id) {
        case gameProgressDialogIdentifier:
            gameProgressDialog = new ProgressDialog(this);
            gameProgressDialog.setTitle(getString(R.string.game_progress_dialog_title));
            gameProgressDialog.setMessage(getString(R.string.game_progress_dialog_message));
            gameProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            gameProgressDialog.setMax(gameMaximumTime);
            gameProgressDialog.setButton(ProgressDialog.BUTTON_NEGATIVE,
                                         getString(R.string.game_progress_dialog_cancel_button_text),
                                         new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    stopGame();
                }
            });
            return gameProgressDialog;
        default:
            break;
        }
        return null;
    }

    @SuppressWarnings("deprecation")
    public void onStartGameButtonClick(View button) {
        Toast.makeText(getBaseContext(),
                       getString(R.string.start_game_toast_text),
                       Toast.LENGTH_SHORT).show();
        showDialog(gameProgressDialogIdentifier);
        gameProgressDialog.setProgress(0);
        gameProgressUpdateThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    for (int i = 0; i < gameMaximumTime; i++) {
                        Thread.sleep(1000);
                        gameProgressDialog.incrementProgressBy(1);
                    }
                    stopGame();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        gameProgressUpdateThread.start();
    }

    private void stopGame() {
        gameProgressUpdateThread.interrupt();
        gameProgressUpdateThread = null;
        gameProgressDialog.dismiss();

        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getBaseContext(),
                               getString(R.string.end_game_toast_text),
                               Toast.LENGTH_SHORT).show();
            }
        });
    }
}