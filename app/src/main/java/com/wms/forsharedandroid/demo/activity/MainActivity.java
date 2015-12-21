package com.wms.forsharedandroid.demo.activity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.wms.forsharedandroid.demo.R;
import com.wms.forsharedandroid.demo.handler.HandlerMessage;
import com.wms.forsharedandroid.demo.listener.ImageButtonBackgroundSelector;
import com.wms.forsharedandroid.sdk.task.DeleteFileTask;
import com.wms.forsharedandroid.sdk.task.UploadNewFileWithProgressTask;
import com.wms.forsharedandroid.sdk.util.DialogUtil;
import com.wms.forsharedandroid.sdk.util.FileUtil;
import com.wms.forsharedandroid.sdk.util.MessageUtil;

import java.io.File;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements Runnable {

	private UploadProgressHandler uploadProgressHandler = null;
	private ForsharedHandler uploadProcessHandler = null;

	private ProgressBar progressBarUploadVideo = null;
	private TextView textViewFilePath = null;
	private TextView textViewFileUrl = null;
	private TextView textViewProgress = null;
	private ImageButton imageButtonTakeVideo = null;
	private ImageButton imageButtonGallery = null;
	private ImageButton imageButtonUploadVideo = null;
	private ImageButton imageButtonDeleteVideo = null;
	private VideoView videoViewPreview = null;

	private String videoFileName = "";

	UploadNewFileWithProgressTask uploadNewFileWithProgressTask = null;
	DeleteFileTask deleteFileTask = null;

	// Use your own way to safely keep credentials
	private static final String ForsharedLogin = "YOUR_FORSHARED_LOGIN";
	private static final String ForsharedPassword = "YOUR_FORSHARED_PASSWORD";

	// Replace 0 with the ID of your real data folder
	private static final int dataFileFolderId = 0;

	private static Thread thread = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(com.wms.forsharedandroid.demo.R.layout.activity_main);

		textViewFilePath = (TextView) findViewById(R.id.textViewFilePath);
		textViewFileUrl = (TextView) findViewById(R.id.textViewFileUrl);
		textViewProgress = (TextView) findViewById(R.id.textViewProgress);

		videoViewPreview = (VideoView) findViewById(R.id.videoViewPreview);

		imageButtonUploadVideo = (ImageButton) findViewById(R.id.imageButtonUploadVideo);
		imageButtonUploadVideo.setOnClickListener(new ImageButtonUploadVideoOnClickListener());
		imageButtonUploadVideo.setOnTouchListener(new ImageButtonBackgroundSelector());
		imageButtonUploadVideo.setEnabled(false);

		imageButtonDeleteVideo = (ImageButton) findViewById(R.id.imageButtonDeleteVideo);
		imageButtonDeleteVideo.setOnClickListener(new ImageButtonUploadVideoOnClickListener());
		imageButtonDeleteVideo.setOnTouchListener(new ImageButtonBackgroundSelector());
		imageButtonDeleteVideo.setEnabled(false);

		imageButtonTakeVideo = (ImageButton) findViewById(R.id.imageButtonTakeVideo);
		imageButtonTakeVideo.setOnClickListener(new ImageButtonTakeVideoOnClickListener());
		imageButtonTakeVideo.setOnTouchListener(new ImageButtonBackgroundSelector());

		imageButtonGallery = (ImageButton) findViewById(R.id.imageButtonGallery);
		imageButtonGallery.setOnClickListener(new ImageButtonGalleryOnClickListener());
		imageButtonGallery.setOnTouchListener(new ImageButtonBackgroundSelector());

		progressBarUploadVideo = (ProgressBar) findViewById(R.id.progressBarUploadVideo);

		uploadProgressHandler = new UploadProgressHandler();

		thread = new Thread(this);
		thread.start();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode,Intent data) {

		if (requestCode == IntentRequestCodes.TAKE_VIDEO && resultCode == RESULT_OK) {
			File file = new File(videoFileName);
			// In Android 2.2, the file may not be created, therefore we need to check the returned uri.
			if (!file.exists()) {
				if (data.getData() != null) {
					videoFileName = getRealPathFromURI(data.getData());
					file = new File(videoFileName);
				}
				else {
					Toast.makeText(this, getString(R.string.videoNotAvailable), Toast.LENGTH_LONG).show();
					return;
				}
			}
			onVideoReady(file);
		}
		else if (requestCode == IntentRequestCodes.PICK_UP_VIDEO && resultCode == RESULT_OK) {
			Uri selectedVideo = data.getData();
			String[] filePathColumn = { MediaStore.Video.Media.DATA };
			Cursor cursor = getContentResolver().query(selectedVideo, filePathColumn, null, null, null);
			cursor.moveToFirst();
			int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
			String filePath = cursor.getString(columnIndex);
			cursor.close();
			File file = new File(filePath);
			onVideoReady(file);
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	@SuppressWarnings("static-access")
	@Override
	public void run() {
		try {
			do {
				// Let the process rest 1 seconds;
				Thread.sleep(1000);
				// Send a message to the handler
				MessageUtil.sendHandlerMessage(uploadProgressHandler, HandlerMessage.VIDEO_UPLOAD_PROGRESS_UPDATE);
			} while (thread.interrupted() == false);
		}
		catch (InterruptedException e) {

		}

	}

	private class ImageButtonUploadVideoOnClickListener implements ImageButton.OnClickListener {

		@Override
		public void onClick(View v) {
			if(textViewFilePath.getText().toString().equals(getString(R.string.noFileYet))) {
				DialogUtil.showDialog(MainActivity.this, getString(R.string.takeOrSelectVideo));
				return;
			}

			uploadProcessHandler = new ForsharedHandler();
			uploadNewFileWithProgressTask = new UploadNewFileWithProgressTask(MainActivity.this, true, false, R.string.uploadingFile, progressBarUploadVideo,
					uploadProcessHandler, HandlerMessage.FILE_UPLOAD_COMPLETED);
			// params[0] is 4shard login, params[1] is password, params[2] is ID of the folder to upload, params[3] is path of the file to upload
			uploadNewFileWithProgressTask.execute(ForsharedLogin, ForsharedPassword, dataFileFolderId	+ "", videoFileName);
		}

	}

	private class ImageButtonDeleteVideoOnClickListener implements ImageButton.OnClickListener {

		@Override
		public void onClick(View v) {
			if(textViewFilePath.getText().toString().equals(getString(R.string.noFileYet))) {
				DialogUtil.showDialog(MainActivity.this, getString(R.string.takeOrSelectVideo));
				return;
			}

			uploadProcessHandler = new ForsharedHandler();
			deleteFileTask = new DeleteFileTask(MainActivity.this, true, false, R.string.uploadingFile, uploadProcessHandler, HandlerMessage.FILE_DELETE_COMPLETED);
			// params[0] is 4shard login, params[1] is password, params[2] is ID of the folder to upload, params[3] is name of the file to delete
			uploadNewFileWithProgressTask.execute(ForsharedLogin, ForsharedPassword, dataFileFolderId + "", videoFileName);
		}

	}

	private class ForsharedHandler extends Handler {

		int targetMediaFolderResourceId;
		int progressTextResourceId;
		ProgressBar progressBar = null;
		int dataFileFolderId = 0;
		int dataFileNameId = 0;
		int newEntityPosition = 0;
		String userId = "";
		String videoTitle = "";

		public ForsharedHandler() {

		}

		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			if (msg.what == HandlerMessage.FILE_UPLOAD_COMPLETED) {
				// The file has been uploaded. Get the direct download link from the task
				if (uploadNewFileWithProgressTask != null) {
					textViewFileUrl.setText(uploadNewFileWithProgressTask.getFileDownloadLink());
				}
			}
			else if (msg.what == HandlerMessage.FILE_UPDATE_COMPLETED) {
				Toast.makeText(MainActivity.this, getString(R.string.fileUpdated), Toast.LENGTH_LONG).show();
			}
			else if (msg.what == HandlerMessage.FILE_DELETE_COMPLETED) {
				textViewFileUrl.setText(getString(R.string.noUrlYet));
			}
		}

	}

	private class UploadProgressHandler extends Handler {

		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			switch (msg.what) {
				case HandlerMessage.VIDEO_UPLOAD_PROGRESS_UPDATE:
					if (progressBarUploadVideo.getProgress() < 10) {
						textViewProgress.setText(" 0" + progressBarUploadVideo.getProgress() + "%");
					}
					else {
						textViewProgress.setText(" " + progressBarUploadVideo.getProgress() + "%");
					}
					break;
				case HandlerMessage.VIDEO_UPLOAD_COMPLETED:
					Toast.makeText(MainActivity.this, R.string.videoUploadCompleted, Toast.LENGTH_LONG).show();
					break;
				case HandlerMessage.VIDEO_UPLOAD_CANCELLED:
					Toast.makeText(MainActivity.this, R.string.videoUploadCancelled, Toast.LENGTH_LONG).show();
					break;
				default:
					break;
			}
		}

	}

	private File getTempVideoFile() {
		// it will return File like:/mnt/sdcard/com.company.app
		videoFileName = FileUtil.getAppExternalStoragePath(this);

		File file = new File(videoFileName);
		if (!file.exists()) {
			// Create the folder if it does not exist
			file.mkdir();
		}

		videoFileName += "/" + UUID.randomUUID().toString() + ".3gp";

		file = new File(videoFileName);
		return file;
	}

	private class ImageButtonTakeVideoOnClickListener implements ImageButton.OnClickListener {

		@Override
		public void onClick(View v) {
			startTakeVideo();
		}

	}

	private class ImageButtonGalleryOnClickListener implements ImageButton.OnClickListener {

		@Override
		public void onClick(View v) {
			startPickVideo();
		}

	}

	private void startTakeVideo() {
		progressBarUploadVideo.setProgress(0);
		textViewProgress.setText(" 00%");

		Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(getTempVideoFile()));
		intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
		intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 300);
		intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
		startActivityForResult(intent, IntentRequestCodes.TAKE_VIDEO);
	}

	private void startPickVideo() {
		progressBarUploadVideo.setProgress(0);
		textViewProgress.setText(" 00%");

		Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
		try {
			startActivityForResult(intent, IntentRequestCodes.PICK_UP_VIDEO);
		}
		catch (ActivityNotFoundException e) {
			 // On Andriod 2.2, the above method may cause exception due to not finding an activity to handle the intent.
			 // Use the method below instead.
			Intent mediaChooser = new Intent(Intent.ACTION_GET_CONTENT);
			mediaChooser.setType("video/*");
			startActivityForResult(mediaChooser, IntentRequestCodes.PICK_UP_VIDEO);
		}
		catch (SecurityException e) {
			// When picking up videos, there may be an exception:
			// java.lang.SecurityException:
			// Permission Denial:
			// starting Intent { act=android.intent.action.PICK
			// dat=content://media/external/video/media
			// cmp=com.android.music/.VideoBrowserActivity } from ProcessRecord
			// Try another way to start the intent
			intent = new Intent(Intent.ACTION_PICK, null);
			intent.setType("video/*");
			try {
				startActivityForResult(intent, IntentRequestCodes.PICK_UP_VIDEO);
			} catch (Exception ex) {
				DialogUtil.showExceptionAlertDialog(MainActivity.this, getString(R.string.cannotPickUpVideoTitle), getString(R.string.notSupportedOnDevice));
			}
		}
	}

	@SuppressWarnings("deprecation")
	private String getRealPathFromURI(Uri contentUri) {
		String[] proj = { MediaStore.Video.Media.DATA };
		Cursor cursor = managedQuery(contentUri, proj, null, null, null);
		int column_index = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
		cursor.moveToFirst();
		return cursor.getString(column_index);
	}

	private void onVideoReady(File file) {
		MediaController mediaController = new MediaController(this);
		videoFileName = file.getAbsolutePath();
		videoViewPreview.setVisibility(View.VISIBLE);
		videoViewPreview.setVideoPath(videoFileName);
		videoViewPreview.setMediaController(mediaController);
		videoViewPreview.requestFocus();
		videoViewPreview.start();
		videoViewPreview.pause();
		imageButtonUploadVideo.setEnabled(true);
		textViewFilePath.setText(file.getPath());
		Toast.makeText(this, R.string.pressVideoToPreview, Toast.LENGTH_LONG).show();
	}
}
