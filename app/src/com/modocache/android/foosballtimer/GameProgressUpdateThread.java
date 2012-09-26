package com.modocache.android.foosballtimer;

import android.content.Context;
import android.content.Intent;

public class GameProgressUpdateThread extends Thread {
    static public final String SECONDS_UPDATED_ACTION = "SECONDS_UPDATED_ACTION";
    static public final String GAME_ENDED_ACTION = "GAME_ENDED_ACTION";
    static public final String SECONDS_KEY = "SECONDS_KEY";

	private static volatile GameProgressUpdateThread sharedThread;

	private Context context = null; 
	private int gameTime = 0;
	
	private GameProgressUpdateThread() {
		super();
	}
	
	private static GameProgressUpdateThread getSharedThread() {
		if (sharedThread == null) {
			synchronized (GameProgressUpdateThread.class) {
				if (sharedThread == null) {
    				sharedThread = new GameProgressUpdateThread();
    			}
			}
		}
		return sharedThread;
	}
	
	public static Boolean isGameInProgress() {
		GameProgressUpdateThread thread = getSharedThread();
		synchronized (thread) {
			return thread.isAlive();
		}
	}
	
	public static void startSharedThread() {
		GameProgressUpdateThread thread = getSharedThread();
		synchronized (thread) {
			if (!thread.isAlive()) {
				thread.start();
			}
		}
	}
	
	public static void stopSharedThread() {
		GameProgressUpdateThread thread = getSharedThread();
		synchronized (thread) {
			if (thread.isAlive()) {
				thread.interrupt();
			}
			thread = null;
			sharedThread = null;
		}
	}
	
	public static void setSharedThreadGameTime(int newGameTime) {
		GameProgressUpdateThread thread = getSharedThread();
		synchronized (thread) {
			if (thread.gameTime == 0) {
				thread.gameTime = newGameTime;
			}
		}
	}
	
	public static void setSharedThreadContext(Context newContext) {
		GameProgressUpdateThread thread = getSharedThread();
		synchronized (thread) {
			if (thread.context == null) {
				thread.context = newContext;
			}
		}
	}
	
	@Override
    public void run() {
        try {
            for (int i = 0; i < gameTime; i++) {
                Intent broadcastIntent = new Intent();
                broadcastIntent.setAction(SECONDS_UPDATED_ACTION);
                broadcastIntent.putExtra(SECONDS_KEY, gameTime - i);
                context.sendBroadcast(broadcastIntent);
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
        	Intent broadcastIntent = new Intent();
            broadcastIntent.setAction(GAME_ENDED_ACTION);
            context.sendBroadcast(broadcastIntent);
            stopSharedThread();
        }
    }
}
