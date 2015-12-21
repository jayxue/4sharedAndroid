package com.wms.forsharedandroid.sdk.task;

import java.io.IOException;
import android.content.Context;
import android.os.Handler;

import com.wms.forsharedandroid.sdk.api.ForsharedUtil;

public class DeleteFileTask extends ForsharedTask {

	public DeleteFileTask(Context context, boolean isProgressDialogRequired, boolean isProgressDialogCancelable,
			int waitingText, Handler handler, int messageToSendUponCompletion) {
		super(context, isProgressDialogRequired, isProgressDialogCancelable, waitingText, null, handler, messageToSendUponCompletion);
	}

	@Override
	protected Void doInBackground(String... params) {
		// params[0] is 4shard login, params[1] is password, params[2] is the containing folder ID, params[3] is name of the file to delete
		try {
			ForsharedUtil.deleteFileFinal(params[0], params[1], Long.valueOf(params[2]), params[3]);
		} catch (NumberFormatException e) {
			errorCode = ErrorCode.PARAM_ERROR;
		} catch (IOException e) {
			errorCode = ErrorCode.NETWORK_ERROR;
		} catch (ServerDataException e) {
			errorCode = ErrorCode.SERVER_ERROR;
		} catch (Exception e) {
			errorCode = ErrorCode.UNKNOWN_ERROR;
		}
		return null;
	}
}
