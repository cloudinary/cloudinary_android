package com.cloudinary;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.text.TextUtils;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class Cloudinary {
	public final static String CF_SHARED_CDN = "d3jpl91pxevbkh.cloudfront.net";
	public final static String AKAMAI_SHARED_CDN = "cloudinary-a.akamaihd.net";
	public final static String SHARED_CDN = AKAMAI_SHARED_CDN;

	private final Map config = new HashMap();

	public Cloudinary(Map config) {
		this.config.putAll(config);
	}

	public Cloudinary(String cloudinaryUrl) {
		initFromUrl(cloudinaryUrl);
	}

	public Cloudinary(Context context) {
		try {
			PackageManager packageManager = context.getPackageManager();
			ApplicationInfo info = packageManager.getApplicationInfo( context.getPackageName(), PackageManager.GET_META_DATA);
			if (info == null|| info.metaData == null) return;
			String cloudinaryUrl = (String) info.metaData.get("CLOUDINARY_URL");
			if (cloudinaryUrl == null) return;
			initFromUrl(cloudinaryUrl);
		} catch (NameNotFoundException e) {
			// No metadata found
		}
	}

	public Url url() {
		return new Url(this);
	}

	public Uploader uploader() {
		return new Uploader(this);
	}

	public String cloudinaryApiUrl(String action, Map options) {
		String cloudinary = asString(options.get("upload_prefix"), asString(this.config.get("upload_prefix"), "https://api.cloudinary.com"));
		String cloud_name = asString(options.get("cloud_name"), asString(this.config.get("cloud_name")));
		if (cloud_name == null)
			throw new IllegalArgumentException("Must supply cloud_name in tag or in configuration");
		String resource_type = asString(options.get("resource_type"), "image");
		return TextUtils.join("/", new String[] { cloudinary, "v1_1", cloud_name, resource_type, action });
	}

	private final static SecureRandom RND = new SecureRandom();

	public String randomPublicId() {
		byte[] bytes = new byte[8];
		RND.nextBytes(bytes);
		return encodeHexString(bytes);
	}

	public String signedPreloadedImage(Map result) {
		try {
			return signedPreloadedImage(new JSONObject(result));
		} catch (JSONException e) {
			throw new IllegalArgumentException("Bad result map");
		}
	}

	public String signedPreloadedImage(JSONObject result) throws JSONException {
		return result.get("resource_type") + "/upload/v" + result.get("version") + "/" + result.get("public_id")
			+ (result.has("format") ? "." + result.get("format") : "") + "#" + result.get("signature");
	}

	public String apiSignRequest(Map<String, Object> paramsToSign, String apiSecret) {
		Collection<String> params = new ArrayList<String>();
		for (Map.Entry<String, Object> param : new TreeMap<String, Object>(paramsToSign).entrySet()) {
			if (param.getValue() instanceof Collection) {
				params.add(param.getKey() + "=" + TextUtils.join(",", (Collection) param.getValue()));
			} else if (param.getValue() instanceof String) {
				String value = (String) param.getValue();
				if (!TextUtils.isEmpty(value)) {
					params.add(param.getKey() + "=" + value);
				}
			}
		}
		String to_sign = TextUtils.join("&", params);
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("Unexpected exception", e);
		}
		byte[] digest = md.digest((to_sign + apiSecret).getBytes());
		return encodeHexString(digest);
	}

	private String encodeHexString(byte[] bytes) {
		StringBuffer hex = new StringBuffer();
		for (byte aByte : bytes) {
			hex.append(Integer.toHexString(0x100 | (((int) aByte) & 0xFF)).substring(1));
		}

		return hex.toString();
	}

	public String privateDownload(String publicId, String format, Map<String, Object> options) throws URISyntaxException {
		String apiKey = Cloudinary.asString(options.get("api_key"), this.getStringConfig("api_key"));
		if (apiKey == null)
			throw new IllegalArgumentException("Must supply api_key");
		String apiSecret = Cloudinary.asString(options.get("api_secret"), this.getStringConfig("api_secret"));
		if (apiSecret == null)
			throw new IllegalArgumentException("Must supply api_secret");
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("public_id", publicId);
		params.put("format", format);
		params.put("attachment", options.get("attachment"));
		params.put("type", options.get("type"));
		for (Iterator iterator = params.values().iterator(); iterator.hasNext();) {
			Object value = iterator.next();
			if (value == null || "".equals(value)) {
				iterator.remove();
			}
		}
		params.put("timestamp", Long.valueOf(System.currentTimeMillis() / 1000L).toString());
		params.put("signature", this.apiSignRequest(params, apiSecret));
		params.put("api_key", apiKey);
		Uri.Builder builder = Uri.parse(cloudinaryApiUrl("download", options)).buildUpon();
		for (Map.Entry<String, Object> param : params.entrySet()) {
			builder.appendQueryParameter(param.getKey(), param.getValue().toString());
		}
		return builder.toString();
	}

	protected void initFromUrl(String cloudinaryUrl) {
		URI cloudinaryUri = URI.create(cloudinaryUrl);
		setConfig("cloud_name", cloudinaryUri.getHost());
		String[] creds = cloudinaryUri.getUserInfo().split(":");
		setConfig("api_key", creds[0]);
		setConfig("api_secret", creds[1]);
		setConfig("private_cdn", !TextUtils.isEmpty(cloudinaryUri.getPath()));
		setConfig("secure_distribution", cloudinaryUri.getPath());
		if (cloudinaryUri.getQuery() != null) {
			for (String param : cloudinaryUri.getQuery().split("&")) {
				String[] keyValue = param.split("=");
				try {
					setConfig(keyValue[0], URLDecoder.decode(keyValue[1], "ASCII"));
				} catch (UnsupportedEncodingException e) {
					throw new RuntimeException("Unexpected exception", e);
				}
			}
		}
	}

	public boolean getBooleanConfig(String key, boolean default_value) {
		return asBoolean(this.config.get(key), default_value);
	}

	public String getStringConfig(String key, String default_value) {
		return asString(this.config.get(key), default_value);
	}

	public String getStringConfig(String key) {
		return asString(this.config.get(key));
	}

	public void setConfig(String key, Object value) {
		this.config.put(key, value);
	}

	public static String asString(Object value) {
		if (value == null) {
			return null;
		} else {
			return value.toString();
		}
	}

	public static String asString(Object value, String defaultValue) {
		if (value == null) {
			return defaultValue;
		} else {
			return value.toString();
		}
	}

	public static List asArray(Object value) {
		if (value == null) {
			return Collections.EMPTY_LIST;
		} else if (value instanceof Object[]) {
			return Arrays.asList((Object[]) value);
		} else if (value instanceof List) {
			return (List) value;
		} else {
			List array = new ArrayList();
			array.add(value);
			return array;
		}
	}

	public static Boolean asBoolean(Object value, Boolean defaultValue) {
		if (value == null) {
			return defaultValue;
		} else if (value instanceof Boolean) {
			return (Boolean) value;
		} else {
			return "true".equals(value);
		}
	}

	public static Map asMap(Object... values) {
		if (values.length % 2 != 0)
			throw new RuntimeException("Usage - (key, value, key, value, ...)");
		Map result = new HashMap(values.length / 2);
		for (int i = 0; i < values.length; i += 2) {
			result.put(values[i], values[i + 1]);
		}
		return result;
	}

	public static Map emptyMap() {
		return Collections.EMPTY_MAP;
	}
}
