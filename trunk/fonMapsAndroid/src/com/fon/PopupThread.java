package com.fon;

import android.util.Log;

public class PopupThread extends Thread {
	private static String TAG = PopupThread.class.getName();

	private fonPopupWindow popup;

	private long millis;

	public PopupThread(fonPopupWindow popup) {
		this.popup = popup;
	}

	@Override
	public void run() {
		try {
			Log.i(TAG, "thread is sleeping", null);
			sleep(millis);
			popup.dismiss();
		} catch (InterruptedException e) {}
		Log.i(TAG, "end of sleep", null);
	}

	public void startFor(int milliseconds, fonPopupWindow messageWindow) {
		this.millis = milliseconds;
		this.start();
		try {
			messageWindow.dismiss();
		} catch (Exception e) {
			Log.i(TAG, "excpetion", e);
		}
		// this.stop();
	}
}