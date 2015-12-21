package com.wms.forsharedandroid.sdk.util;

import android.os.Handler;
import android.os.Message;

public class MessageUtil {

	public static void sendHandlerMessage(Handler handler, int message) {
		// Try to reuse message objects
		Message msg = Message.obtain();
		msg.what = message;
		handler.sendMessage(msg);
	}
}
