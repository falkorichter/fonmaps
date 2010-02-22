package com.fon;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.PopupWindow;
import android.widget.TextView;

public class fonPopupWindow extends PopupWindow implements OnClickListener {

	private TextView messageText;

	public fonPopupWindow(Context context) {
		super(context);

		messageText = new TextView(context);
		messageText.setText("");
		this.setContentView(messageText);
		this.setFocusable(true);
	}

	public void setText(String text) {
		messageText.setText(text);
	}

	public void addText(String text) {
		messageText.setText(getText() + "\n" + text);
	}

	public String getText() {
		return (String) messageText.getText();
	}

	public void onClick(View v) {
		Log.i("fonMaps", "PopUpclicked", null);
		this.dismiss();

	}

}
