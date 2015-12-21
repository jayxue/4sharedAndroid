package com.wms.forsharedandroid.sdk.task;

import android.content.Context;
import android.os.Handler;
import android.widget.ProgressBar;

import com.wms.forsharedandroid.sdk.api.ForsharedUtil;

public class UploadNewFileWithProgressTask extends ForsharedTask {

	public UploadNewFileWithProgressTask(Context context, boolean isProgressDialogRequired, boolean isProgressDialogCancelable, int waitingText, ProgressBar progressBar,
	                                     Handler handler, int messageToSendUponCompletion) {
		super(context, isProgressDialogRequired, isProgressDialogCancelable, waitingText, progressBar, handler, messageToSendUponCompletion);
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		progressBar.setProgress(0);
	}

	@Override
	protected Void doInBackground(String... params) {
		// params[0] is 4shard login, params[1] is password, params[2] is ID of the folder to upload file, params[3] is path of the file to upload
		try {
			fileDownloadLink = ForsharedUtil.uploadFileToFolderWithProgress(params[0], params[1], Long.valueOf(params[2]), params[3], progressBar);
		}
		catch (Exception e) {
			errorCode = ErrorCode.UNKNOWN_ERROR;
		}
		return null;
	}

}
