package me.ayydxn.orbit.ultralight.platform.clipboard;

import java.util.LinkedHashMap;
import java.util.Map;


public final class ULClipboardData
{
	public static final String TEXT_PLAIN = "text/plain";
	public static final String TEXT_HTML = "text/html";
	public static final String TEXT_URI_LIST = "text/uri-list";

	// Values are either a String or a byte[].
	private final Map<String, Object> entries = new LinkedHashMap<>();

	private String sourceURL = "";

	public ULClipboardData()
	{
	}

	public static ULClipboardData ofText(String text)
	{
		ULClipboardData data = new ULClipboardData();
		data.setText(TEXT_PLAIN, text);

		return data;
	}

	public void setText(String type, String text)
	{
		requireType(type);

		if (text == null)
			throw new IllegalArgumentException("Cannot set a null text value on a ULClipboardData!");

		this.entries.put(type, text);
	}

	public void setBytes(String type, byte[] bytes)
	{
		requireType(type);

		if (bytes == null)
			throw new IllegalArgumentException("Cannot set a null bytes value on a ULClipboardData!");

		this.entries.put(type, bytes.clone());
	}

	public String getText(String type)
	{
		return this.entries.get(type) instanceof String text ? text : null;
	}

	public byte[] getBytes(String type)
	{
		return this.entries.get(type) instanceof byte[] bytes ? bytes.clone() : null;
	}

	public boolean has(String type)
	{
		return this.entries.containsKey(type);
	}

	public boolean isText(String type)
	{
		return this.entries.get(type) instanceof String;
	}

	public void remove(String type)
	{
		this.entries.remove(type);
	}

	public int size()
	{
		return this.entries.size();
	}

	public String[] getTypes()
	{
		return this.entries.keySet().toArray(String[]::new);
	}

	public String getSourceURL()
	{
		return this.sourceURL;
	}

	public void setSourceURL(String sourceURL)
	{
		this.sourceURL = sourceURL != null ? sourceURL : "";
	}

	private static void requireType(String type)
	{
		if (type == null || type.isEmpty())
			throw new IllegalArgumentException("A ULClipboardData type cannot be null or empty!");
	}
}
