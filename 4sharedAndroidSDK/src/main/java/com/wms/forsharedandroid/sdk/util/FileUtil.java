package com.wms.forsharedandroid.sdk.util;

import android.content.Context;
import android.os.Environment;

public class FileUtil {

	/**
	 * Get extension of a file (file name only or full path)
	 * @param file
	 * @return
	 */
	public static String getFileExtension(String file) {
		int lastPointPosition = file.lastIndexOf(".");
		return file.substring(lastPointPosition + 1);
	}

	/**
	 * Return a path of external storage + package name like
	 * /mnt/sdcard/com.company.app
	 *
	 * @param context
	 * @return
	 */
	public static String getAppExternalStoragePath(Context context) {
		return Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + context.getApplicationContext().getPackageName();
	}

}
