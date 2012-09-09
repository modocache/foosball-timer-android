package com.modocache.android.foosballtimer.test;

import com.modocache.android.foosballtimer.FoosballTimerActivity;

import android.app.Activity;
import android.test.ActivityInstrumentationTestCase2;
import android.view.KeyEvent;
import android.widget.Button;

public class FoosballTimerActivityTest extends ActivityInstrumentationTestCase2<FoosballTimerActivity> {
	private Activity activity;
	private Button startGameButton;
	
	public FoosballTimerActivityTest() {
		super(FoosballTimerActivity.class);
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		setActivityInitialTouchMode(false);
		activity = getActivity();
		startGameButton = (Button) activity.findViewById(
				com.modocache.android.foosballtimer.R.id.start_game_button);
	}
	
	public void testTapStartGameButton() {
		assertTrue("Start game button should be clickable when the activity starts.",
				   startGameButton.isClickable());
		assertNull("Progress dialog should not be shown before clicking button.",
				   activity.getFragmentManager().findFragmentByTag("dialog"));

		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				startGameButton.requestFocus();
			}
		});
		this.sendKeys(KeyEvent.KEYCODE_DPAD_CENTER);
		assertNotNull("Progress dialog should be shown after clicking button.",
					  activity.getFragmentManager().findFragmentByTag("dialog"));
		
		this.sendKeys(KeyEvent.KEYCODE_BACK);
		assertNull("Progress dialog should not be shown after clicking button.",
				   activity.getFragmentManager().findFragmentByTag("dialog"));
	}
}
