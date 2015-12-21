package com.wms.forsharedandroid.sdk.api;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.ksoap2.transport.Transport;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class ForsharedService {

	private String mUsername;
	private String mPassword;

	private static final String SERVICE_URL = "http://api.4shared.com/jax3/DesktopApp";
	private static final String SERVICE_NAMESPACE = "http://api.soap.shared.pmstation.com/";

	/**
	 * Constructs an object for an account
	 *
	 * @param username Username of the account
	 * @param password Password of the account
	 */
	public ForsharedService(String username, String password) {
		mUsername = username;
		mPassword = password;
	}

	/**
	 * Get account's root folder
	 *
	 * @return Item representing the root directory
	 */
	public AccountItem getRoot() throws Exception {
		String soapAction = SERVICE_URL + "/getRoot";
		SoapObject rpc = getRpc("getRoot");
		SoapSerializationEnvelope envelope = getEnvelope(rpc);
		Transport ht = getHttpTransport();
		ht.call(soapAction, envelope);
		return getAccountItem((SoapObject) envelope.getResponse());
	}

	/**
	 * Get contents of a directory
	 *
	 * @param dirId Directory's identifier
	 * @return Array of items representing contents of the directory
	 */
	public List<AccountItem> getItems(long dirId) throws Exception {
		String methodName = "getItems";
		SoapObject rpc = getRpc(methodName);
		rpc.addProperty("arg2", Long.valueOf(dirId));
		SoapSerializationEnvelope envelope = getEnvelope(rpc);
		Transport ht = getHttpTransport();
		ht.call(SERVICE_URL + "/" + methodName, envelope);
		Object result = envelope.getResponse();
		return getAccountItems(result);
	}

	private List<AccountItem> getAccountItems(Object response) {
		if (response == null) {
			return null;
		}

		if (!(response instanceof SoapObject)) {
			return null;
		}

		List<AccountItem> items = new ArrayList<AccountItem>();

		for (int i = 0; i < ((SoapObject) response).getPropertyCount(); i++) {
			Object item = ((SoapObject) response).getProperty(i);
			if (item instanceof SoapObject) {
				items.add(getAccountItem((SoapObject) item));
			}
		}

		return items;
	}

	private SoapObject getRpc(String methodName) {
		SoapObject rpc = new SoapObject(SERVICE_NAMESPACE, methodName);
		rpc.addProperty("arg0", mUsername);
		rpc.addProperty("arg1", mPassword);
		return rpc;
	}

	private SoapSerializationEnvelope getEnvelope(SoapObject rpc) {
		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
		envelope.bodyOut = rpc;
		envelope.encodingStyle = SoapSerializationEnvelope.XSD;
		return envelope;
	}

	private Transport getHttpTransport() {
		Transport ht = new HttpTransportSE(SERVICE_URL);
		ht.setXmlVersionTag("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		ht.debug = true;
		return ht;
	}

	private AccountItem getAccountItem(SoapObject item) {
		return deserialize(new AccountItem(), item);
	}

	private AccountItem deserialize(AccountItem newItem, SoapObject item) {
		Hashtable ht = new Hashtable();
		PropertyInfo propInfo = new PropertyInfo();
		for (int n = 0; n < item.getPropertyCount(); n++) {
			item.getPropertyInfo(n, ht, propInfo);
			newItem.setProperty(propInfo.name, item.getProperty(n));
		}
		return newItem;
	}

	public String createUploadSessionKey(long dirID) throws Exception {
		String methodName = "createUploadSessionKey";
		SoapObject rpc = getRpc(methodName);
		rpc.addProperty("arg2", dirID);
		SoapSerializationEnvelope envelope = getEnvelope(rpc);
		Transport ht = getHttpTransport();
		ht.call(SERVICE_URL + "/" + methodName, envelope);
		Object result = envelope.getResponse();
		return result.toString();
	}

	public int getNewFileDataCenter() throws IOException, XmlPullParserException {
		String methodName = "getNewFileDataCenter";
		SoapObject rpc = getRpc(methodName);
		SoapSerializationEnvelope envelope = getEnvelope(rpc);
		Transport ht = getHttpTransport();
		ht.call(SERVICE_URL + "/" + methodName, envelope);
		Object result = envelope.getResponse();
		return Integer.valueOf(result.toString());
	}

	public String getUploadFormUrl(int dataCenterID, String sessionKey) throws IOException, XmlPullParserException {
		String methodName = "getUploadFormUrl";
		SoapObject rpc = new SoapObject(SERVICE_NAMESPACE, methodName);
		rpc.addProperty("arg0", dataCenterID);
		rpc.addProperty("arg1", sessionKey);
		SoapSerializationEnvelope envelope = getEnvelope(rpc);
		Transport ht = getHttpTransport();
		ht.call(SERVICE_URL + "/" + methodName, envelope);
		Object result = envelope.getResponse();
		return result.toString();
	}

	/**
	 * Get an upload start file Id
	 *
	 * @param folderID folder ID
	 * @param fullSize size of the file
	 * @param fileName Name of the file
	 * @return id of uploaded file
	 * @throws Exception
	 */
	public long uploadStartFile(long folderID, long fullSize, String fileName) throws Exception {
		String methodName = "uploadStartFile";
		SoapObject rpc = getRpc(methodName);
		rpc.addProperty("arg2", folderID); // -1 means root dir
		rpc.addProperty("arg3", fileName);
		rpc.addProperty("arg4", fullSize);
		SoapSerializationEnvelope envelope = getEnvelope(rpc);
		Transport ht = getHttpTransport();
		ht.call(SERVICE_URL + "/" + methodName, envelope);
		String idStr = (envelope.getResponse()).toString();
		return Long.valueOf(idStr);
	}

	/**
	 * Get an upload start file Id for update
	 *
	 * @param updateFileId id of the file to update
	 * @param fullSize size of the file
	 * @param fileName Name of the file
	 * @return id of the file
	 * @throws Exception
	 */
	public long uploadStartFileUpdate(long updateFileId, long fullSize, String fileName) throws Exception {
		String methodName = "uploadStartFileUpdate";
		SoapObject rpc = getRpc(methodName);
		rpc.addProperty("arg2", Long.valueOf(updateFileId)); // -1 means root dir

		// Empty string means don't change existing name. http://www.4shared.com/developer/docs/upload/#uploadStartFileUpdate. Actually, the file name like Comments.txt should be passed.
		// Otherwise the file name will be changed to only "Comments"
		rpc.addProperty("arg3", fileName);
		rpc.addProperty("arg4", Long.valueOf(fullSize));
		SoapSerializationEnvelope envelope = getEnvelope(rpc);
		Transport ht = getHttpTransport();
		ht.call(SERVICE_URL + "/" + methodName, envelope);
		String idStr = (envelope.getResponse()).toString();
		return Long.valueOf(idStr);
	}

	public String getFileDownloadLink(long fileID) throws IOException, XmlPullParserException {
		String methodName = "getFileDownloadLink";
		SoapObject rpc = getRpc(methodName);
		rpc.addProperty("arg2", fileID);
		SoapSerializationEnvelope envelope = getEnvelope(rpc);
		Transport ht = getHttpTransport();
		ht.call(SERVICE_URL + "/" + methodName, envelope);
		Object result = envelope.getResponse();
		return result.toString();
	}

	public String getDirectLink(String link, String fileExtension) throws IOException, XmlPullParserException {
		String methodName = "getDirectLink";
		SoapObject rpc = getRpc(methodName);
		rpc.addProperty("arg2", link);
		SoapSerializationEnvelope envelope = getEnvelope(rpc);
		Transport ht = getHttpTransport();
		ht.call(SERVICE_URL + "/" + methodName, envelope);
		Object response = envelope.getResponse();
		String directLink = response.toString();

		// If the returned link is actually a html page, replace .html with the original file extension
		int htmlPositionInLink = directLink.indexOf(".html");
		if(htmlPositionInLink > 0) {
			// Find extension of the original file
			directLink = directLink.substring(0, htmlPositionInLink + 1) + fileExtension + directLink.substring(htmlPositionInLink + 5);

			// If this is a media file, we need to replace media folder name like "photo" to "download"
			int mediaFolderPosition = directLink.indexOf("photo");
			if(mediaFolderPosition > 0) {
				return directLink.substring(0, mediaFolderPosition) + "download" + directLink.substring(mediaFolderPosition + 5);
			}
			mediaFolderPosition = directLink.indexOf("office");
			if(mediaFolderPosition > 0) {
				return directLink.substring(0, mediaFolderPosition) + "download" + directLink.substring(mediaFolderPosition + 6);
			}
			mediaFolderPosition = directLink.indexOf("video");	// For audio and video
			if(mediaFolderPosition > 0) {
				return directLink.substring(0, mediaFolderPosition) + "download" + directLink.substring(mediaFolderPosition + 5);
			}
		}
		return directLink;
	}

	public void deleteFileFinal(long fileId) throws IOException, XmlPullParserException {
		String methodName = "deleteFileFinal";
		SoapObject rpc = getRpc(methodName);
		rpc.addProperty("arg2", Long.valueOf(fileId));
		SoapSerializationEnvelope envelope = getEnvelope(rpc);
		Transport ht = getHttpTransport();
		ht.call(SERVICE_URL + "/" + methodName, envelope);
	}
}
