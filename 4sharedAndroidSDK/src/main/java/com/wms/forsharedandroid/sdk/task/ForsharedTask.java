package com.wms.forsharedandroid.sdk.task;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.view.View;
import android.widget.ProgressBar;

import com.wms.forsharedandroid.sdk.R;
import com.wms.forsharedandroid.sdk.util.DialogUtil;
import com.wms.forsharedandroid.sdk.util.MessageUtil;

public class ForsharedTask extends AsyncTask<String, Void, Void> {

	protected Context context;
	protected String fileNameToDownload = "";

	// Progress dialog
	private boolean isProgressDialogRequired = true;
	private boolean isProgressDialogCancelable = true;
	private int waitingText = 0;
	private ProgressDialog progressDialog = null;

	// Progress bar
	protected ProgressBar progressBar = null;

	// Message handling
	private Handler handler = null;
	private int messageToSendUponCompletion = 0;

	protected String fileDownloadLink = "";

	protected ErrorCode errorCode = ErrorCode.NONE;

	public ForsharedTask(Context context, boolean isProgressDialogRequired, boolean isProgressDialogCancelable,
	                     int waitingText, ProgressBar progressBar, Handler handler, int messageToSendUponCompletion) {
		this.context = context;

		// Progress dialog will be created by the task if required
		this.isProgressDialogRequired = isProgressDialogRequired;
		this.isProgressDialogCancelable = isProgressDialogCancelable;
		this.waitingText = waitingText;

		// Progress bar should be provided by UI if required
		this.progressBar = progressBar;

		this.handler = handler;
		this.messageToSendUponCompletion = messageToSendUponCompletion;
	}

	protected void onPreExecute() {
		if(progressBar != null) {
			// If it's on a screen of showing content, display the progress dialog
			progressBar.setVisibility(View.VISIBLE);
		}
		else if(waitingText != 0 && isProgressDialogRequired) {
			progressDialog = DialogUtil.showWaitingProgressDialog(context, ProgressDialog.STYLE_SPINNER, context.getString(waitingText), isProgressDialogCancelable);
		}
	}

	@Override
	protected Void doInBackground(String... params) {
		return null;
	}

	protected void onPostExecute(Void result) {
		if (progressDialog != null && progressDialog.isShowing()) {
			progressDialog.dismiss();
			progressDialog = null;
		}
		else if(progressBar != null) {
			progressBar.setVisibility(View.GONE);
		}

		if (!errorCode.equals(ErrorCode.NONE)) {
			try {
				if (errorCode.equals(ErrorCode.NETWORK_ERROR)) {
					DialogUtil.showExceptionAlertDialog(context, context.getString(R.string.networkErrorTitle),	context.getString(R.string.networkError));
				} 
				else if (errorCode.equals(ErrorCode.SERVER_ERROR)) {
					DialogUtil.showExceptionAlertDialog(context, context.getString(R.string.serverErrorTitle), context.getString(R.string.serverError));
				}
				else if(errorCode.equals(ErrorCode.DIR_NOT_FOUND_ERROR)) {
					DialogUtil.showExceptionAlertDialog(context, context.getString(R.string.dirNotFoundErrorTitle), context.getString(R.string.dirNotFoundError));
				}
				else if(errorCode.equals(ErrorCode.FILE_NOT_FOUND_ERROR)) {
					DialogUtil.showExceptionAlertDialog(context, context.getString(R.string.fileNotFoundErrorTitle), context.getString(R.string.fileNotFoundError) + " - " + fileNameToDownload);
				}
				else {
					DialogUtil.showExceptionAlertDialog(context, context.getString(R.string.unknownErrorTitle), context.getString(R.string.unknownError));
				}
			} 
			catch (android.view.WindowManager.BadTokenException e) {
				// Window leaked exception: the user has existed from the activity
			}
		} 
		else {
			// Send a handler message if the message has been configured
			if (handler != null && messageToSendUponCompletion > 0)
				MessageUtil.sendHandlerMessage(handler, messageToSendUponCompletion);
		}
	}

	public String getFileDownloadLink() {
		return fileDownloadLink;
	}
}
