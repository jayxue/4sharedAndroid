/**
 * Upload file to update an existing file 
 */

package com.wms.forsharedandroid.sdk.task;

import java.io.IOException;

import com.wms.forsharedandroid.sdk.api.ForsharedUtil;

import android.content.Context;
import android.os.Handler;

public class UpdateFileTask extends ForsharedTask {

	public UpdateFileTask(Context context, boolean isProgressDialogRequired, int waitingText, Handler handler, int messageToSendUponCompletion) {
		// Do not allow to cancel progress dialog when updating a file
		super(context, isProgressDialogRequired, false, waitingText, null, handler, messageToSendUponCompletion);
	}

	@Override
	protected Void doInBackground(String... params) {
		// params[0] is 4shard login, params[1] is password, params[2] is ID of the folder to upload on server,
		// params[3] is name of the file to update, params[4] is the path of the local file to update
		try {
			ForsharedUtil.updateFile(params[0], params[1], Long.valueOf(params[2]), params[3], params[4]);
		}
		catch (NumberFormatException e) {
			errorCode = ErrorCode.PARAM_ERROR;
		}
		catch (IOException e) {
			errorCode = ErrorCode.NETWORK_ERROR;
		}
		catch (ServerDataException e) {
			errorCode = ErrorCode.SERVER_ERROR;
		}
		catch (Exception e) {
			errorCode = ErrorCode.UNKNOWN_ERROR;
		}
		return null;
	}

}
