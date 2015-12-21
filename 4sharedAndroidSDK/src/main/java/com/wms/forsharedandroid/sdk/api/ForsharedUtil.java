package com.wms.forsharedandroid.sdk.api;

import android.widget.ProgressBar;

import com.wms.forsharedandroid.sdk.util.FileUtil;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.List;

@SuppressWarnings("deprecation")
public class ForsharedUtil {

	public static HttpClientWorkingStatus status = HttpClientWorkingStatus.SLEEPING;
	public static HttpClient httpclient = null;
	public static long totalSize = 0;

	public static String uploadFileToFolderWithProgress(String login, String password, long folderId, String filePath, final ProgressBar progressBar) throws Exception {
		String downloadLink = "";
		String directLink = "";

		File file = new File(filePath);

		ForsharedService fss = new ForsharedService(login, password);

		FileInputStream fileInputStream = new FileInputStream(file);

		String sessionKey = fss.createUploadSessionKey(folderId);
		int fileDataCenterID = fss.getNewFileDataCenter();
		String uploadFormUrl = fss.getUploadFormUrl(fileDataCenterID, sessionKey);
		long fileID = fss.uploadStartFile(folderId, file.length(), file.getName());
		status = HttpClientWorkingStatus.UPLOADING;

		httpclient = new DefaultHttpClient();
		HttpContext httpContext = new BasicHttpContext();
		httpclient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
		HttpPost post = new HttpPost(uploadFormUrl);

		CustomMultiPartEntity mpEntity = new CustomMultiPartEntity(
				new CustomMultiPartEntity.ProgressListener() {
					@Override
					public void transferred(long num) {
						if (totalSize > 0) {
							progressBar.setProgress((int) ((num / (float) totalSize) * 100));
						}
					}
				}
		);

		StringBody rfid = new StringBody("" + fileID);
		StringBody rfb = new StringBody("" + 0);
		InputStreamBody isb = new InputStreamBody(new BufferedInputStream(fileInputStream), "FilePart");
		mpEntity.addPart("resumableFileId", rfid);
		mpEntity.addPart("resumableFirstByte", rfb);
		mpEntity.addPart("FilePart", isb);

		totalSize = file.length();

		post.setEntity(mpEntity);

		HttpResponse resp = httpclient.execute(post, httpContext);
		if (resp.getStatusLine().getStatusCode() == 200) {
			downloadLink = fss.getFileDownloadLink(fileID);
			String fileExtension = FileUtil.getFileExtension(filePath);
			directLink = fss.getDirectLink(downloadLink, fileExtension);
		}
		fileInputStream.close();

		httpclient.getConnectionManager().shutdown();

		httpclient = null;

		status = HttpClientWorkingStatus.UPLOAD_COMPLETED;
		return directLink;
	}

	public static void deleteFileFinal(String login, String password, long folderId, String fileName) throws Exception {
		long fileId = ForsharedUtil.getFileIdByName(login, password, folderId, fileName);
		ForsharedService fss = new ForsharedService(login, password);
		fss.deleteFileFinal(fileId);
	}

	public static long getFileIdByName(String login, String password, long dirId, String fileName) throws Exception {
		ForsharedService fss = new ForsharedService(login, password);
		List<AccountItem> items = fss.getItems(dirId);
		long fileId = 0;
		for (AccountItem item : items) {
			if (item.getName().equals(fileName)) {
				fileId = item.getId();
				break; // For sure we can find the file name
			}
		}
		return fileId;
	}

	public static boolean updateFile(String login, String password, long folderIdOnServer, String updateFileName, String localFilePath) throws Exception {
		long updateFileId = getFileIdByName(login, password, folderIdOnServer, updateFileName);
		return updateFile(login, password, folderIdOnServer, updateFileId, localFilePath);
	}

	/**
	 * Update a file on server.
	 *
	 * @param login
	 * @param password
	 * @param folderId Id of the containing folder on server
	 * @param updateFileId Id of the file on server. The corresponding is going to be updated
	 * @param file Full name of the local file that is going to be uploaded
	 * @return Original return: Direct download link of the file.
	 *         As there is no need to get the download link after update (it does HTTP call and consumes time), the return type is changed to boolean.
	 * @throws Exception
	 */
	public static boolean updateFile(String login, String password, long folderId, long updateFileId, String file) throws Exception {
		boolean result = false;

		File f = new File(file);

		ForsharedService fss = new ForsharedService(login, password);

		FileInputStream fileInputStream = new FileInputStream(f);

		String sessionKey = fss.createUploadSessionKey(folderId);
		int fileDataCenterID = fss.getNewFileDataCenter();
		String uploadFormUrl = fss.getUploadFormUrl(fileDataCenterID, sessionKey);
		long fileID = fss.uploadStartFileUpdate(updateFileId, f.length(), f.getName());

		HttpClient client = new DefaultHttpClient();
		HttpPost post = new HttpPost(uploadFormUrl);
		MultipartEntity me = new MultipartEntity();
		StringBody rfid = new StringBody("" + fileID);
		StringBody rfb = new StringBody("" + 0);
		InputStreamBody isb = new InputStreamBody(new BufferedInputStream(fileInputStream), "FilePart");
		me.addPart("resumableFileId", rfid);
		me.addPart("resumableFirstByte", rfb);
		me.addPart("FilePart", isb);

		post.setEntity(me);
		HttpResponse resp = client.execute(post);
		fileInputStream.close();
		if (resp.getStatusLine().getStatusCode() == 200) {
			result = true;
		}

		return result;
	}
}
