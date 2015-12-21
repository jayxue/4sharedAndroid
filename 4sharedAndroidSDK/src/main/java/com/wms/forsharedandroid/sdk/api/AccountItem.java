/**
 * 4shared account item, representing a file or a directory description data.
 * Implementation includes network serialization functionality.
 */
package com.wms.forsharedandroid.sdk.api;

import org.ksoap2.serialization.SoapPrimitive;

public class AccountItem {

	private long id;
	private String name;
	private boolean directory;

	public AccountItem() {
	}

	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public boolean isDirectory() {
		return directory;
	}

	public String toString() {
		return directory ? "[" + name + "]" : name;
	}

	public void setProperty(String propName, Object value) {
		if (value == null) {
			return;
		}
		if (value instanceof SoapPrimitive) {
			value = ((SoapPrimitive) value).toString();
		}

		if (propName.equals("id")) {
			if (value instanceof Long) {
				id = ((Long) value).longValue();
			}
			else {
				id = Long.parseLong(value.toString());
			}
		}
		if (propName.equals("directory")) {
			directory = convertBoolean(value);
		}
		else if (propName.equals("name")) {
			name = convertString(value);
		}
	}

	private boolean convertBoolean(Object val) {
		if (val == null) {
			return false;
		}
		else {
			return val.toString().toLowerCase().equals("true");
		}
	}

	private String convertString(Object str) {
		if (!(str instanceof String)) {
			return null;
		}

		if (((String) str).equals("String{}")) {
			return "";
		}
		else {
			return (String) str;
		}
	}
}
