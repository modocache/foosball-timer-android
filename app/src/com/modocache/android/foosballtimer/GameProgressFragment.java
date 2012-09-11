package com.modocache.android.foosballtimer;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Vibrator;
import android.widget.Toast;
import android.app.DialogFragment;

public class GameProgressFragment extends DialogFragment {

    static private final int defaultGameMaximumTime = 240;
    static ProgressDialog gameProgressDialog;
    private Thread gameProgressUpdateThread;

    static GameProgressFragment newInstance(int gameMaximumTime) {
        GameProgressFragment fragment = new GameProgressFragment();
        Bundle args = new Bundle();
        args.putInt("gameMaximumTime", gameMaximumTime);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        gameProgressDialog = new ProgressDialog(getActivity());
        gameProgressDialog.setTitle(getString(R.string.game_progress_dialog_title));
        gameProgressDialog.setMessage(getString(R.string.game_progress_dialog_message));
        gameProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        gameProgressDialog.setMax(getArguments().getInt("gameMaximumTime", defaultGameMaximumTime));
        gameProgressDialog.setButton(ProgressDialog.BUTTON_NEGATIVE,
                                     getString(R.string.game_progress_dialog_cancel_button_text),
                                     new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dismiss();
            }
        });
        return gameProgressDialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        gameProgressDialog.setProgress(0);
        gameProgressUpdateThread = new Thread(new Runnable() {
            @Override
            public void run() {
                int gameMaximumTime = getArguments().getInt("gameMaximumTime", defaultGameMaximumTime);
                try {
                    for (int i = gameProgressDialog.getProgress(); i < gameMaximumTime; i++) {
                        Thread.sleep(1000);
                        gameProgressDialog.incrementProgressBy(1);
                    }
                    dismiss();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        gameProgressUpdateThread.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        gameProgressUpdateThread.interrupt();
        gameProgressUpdateThread = null;
        dismiss();

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Context context = getActivity().getBaseContext();
                Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

                vibrator.vibrate(800);
                Toast.makeText(context,
                               getString(R.string.end_game_toast_text),
                               Toast.LENGTH_SHORT).show();
            }
        });
    }
}
