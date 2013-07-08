package com.cloudinary;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class Uploader {
	private final Cloudinary cloudinary;

	public Uploader(Cloudinary cloudinary) {
		this.cloudinary = cloudinary;
	}
	static final String[] BOOLEAN_UPLOAD_OPTIONS = new String[] {"backup", "exif", "faces", "colors", "image_metadata", "use_filename", "eager_async", "invalidate", "discard_original_filename"};

	public Map<String, Object> buildUploadParams(Map options) {
        if (options == null) options = Cloudinary.emptyMap();
		Map<String, Object> params = new HashMap<String, Object>();
		Object transformation = options.get("transformation");
		if (transformation != null) {
			if (transformation instanceof Transformation) {
				transformation = ((Transformation) transformation).generate();
			}
			params.put("transformation", transformation.toString());
		}
		params.put("public_id", (String) options.get("public_id"));
		params.put("callback", (String) options.get("callback"));
		params.put("format", (String) options.get("format"));
		params.put("type", (String) options.get("type"));
		for (String attr : BOOLEAN_UPLOAD_OPTIONS) {
			Boolean value = Cloudinary.asBoolean(options.get(attr), null);
			if (value != null)
				options.put(attr, value.toString());			
		}
		params.put("eager", buildEager((List<Transformation>) options.get("eager")));
		params.put("headers", buildCustomHeaders(options.get("headers")));
		params.put("notification_url", (String) options.get("notification_url"));
		params.put("eager_notification_url", (String) options.get("eager_notification_url"));
		params.put("tags", TextUtils.join(",", Cloudinary.asArray(options.get("tags"))));
		return params;
	}

	public JSONObject upload(Object file, Map options) throws IOException {
        if (options == null) options = Cloudinary.emptyMap();
		Map<String, Object> params = buildUploadParams(options);
		return callApi("upload", params, options, file);
	}

	public JSONObject destroy(String publicId, Map options) throws IOException {
        if (options == null) options = Cloudinary.emptyMap();
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("type", (String) options.get("type"));
		params.put("public_id", publicId);
		params.put("invalidate", Cloudinary.asBoolean(options.get("invalidate"), false).toString());			
		return callApi("destroy", params, options, null);
	}

	public JSONObject rename(String fromPublicId, String toPublicId, Map options) throws IOException {
        if (options == null) options = Cloudinary.emptyMap();
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("type", (String) options.get("type"));
		params.put("overwrite", Cloudinary.asBoolean(options.get("overwrite"), false).toString());			
		params.put("from_public_id", fromPublicId);
		params.put("to_public_id", toPublicId);
		return callApi("rename", params, options, null);
	}

	public JSONObject explicit(String publicId, Map options) throws IOException {
        if (options == null) options = Cloudinary.emptyMap();
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("public_id", publicId);
		params.put("callback", (String) options.get("callback"));
		params.put("type", (String) options.get("type"));
		params.put("eager", buildEager((List<Transformation>) options.get("eager")));
		params.put("headers", buildCustomHeaders(options.get("headers")));
		params.put("tags", TextUtils.join(",", Cloudinary.asArray(options.get("tags"))));
		return callApi("explicit", params, options, null);
	}

	public JSONObject generate_sprite(String tag, Map options) throws IOException {
        if (options == null) options = Cloudinary.emptyMap();
		Map<String, Object> params = new HashMap<String, Object>();
		Object transParam = options.get("transformation");
		Transformation transformation = null;
		if (transParam instanceof Transformation) {
			transformation = new Transformation((Transformation) transParam);
		} else if (transParam instanceof String) {
			transformation = new Transformation().rawTransformation((String) transParam);
		} else {
			transformation = new Transformation();
		}
		String format = (String) options.get("format");
		if (format != null) {
			transformation.fetchFormat(format);
		}
		params.put("transformation", transformation.generate());
		params.put("tag", tag);
		params.put("notification_url", (String) options.get("notification_url"));
		params.put("async", Cloudinary.asBoolean(options.get("async"), false).toString());			
		return callApi("sprite", params, options, null);
	}

	public JSONObject multi(String tag, Map options) throws IOException {
        if (options == null) options = Cloudinary.emptyMap();
		Map<String, Object> params = new HashMap<String, Object>();
		Object transformation = options.get("transformation");
		if (transformation != null) {
			if (transformation instanceof Transformation) {
				transformation = ((Transformation) transformation).generate();
			}
			params.put("transformation", transformation.toString());
		}
		params.put("tag", tag);
		params.put("notification_url", (String) options.get("notification_url"));
		params.put("format", (String) options.get("format"));
		params.put("async", Cloudinary.asBoolean(options.get("async"), false).toString());			
		return callApi("multi", params, options, null);
	}

	public JSONObject explode(String public_id, Map options) throws IOException {
        if (options == null) options = Cloudinary.emptyMap();
		Map<String, Object> params = new HashMap<String, Object>();
		Object transformation = options.get("transformation");
		if (transformation != null) {
			if (transformation instanceof Transformation) {
				transformation = ((Transformation) transformation).generate();
			}
			params.put("transformation", transformation.toString());
		}
		params.put("public_id", public_id);
		params.put("notification_url", (String) options.get("notification_url"));
		params.put("format", (String) options.get("format"));
		return callApi("explode", params, options, null);
	}
	
	// options may include 'exclusive' (boolean) which causes clearing this tag
	// from all other resources
	public JSONObject addTag(String tag, String[] publicIds, Map options) throws IOException {
        if (options == null) options = Cloudinary.emptyMap();
		boolean exclusive = Cloudinary.asBoolean(options.get("exclusive"), false);
		String command = exclusive ? "set_exclusive" : "add";
		return callTagsApi(tag, command, publicIds, options);
	}

	public JSONObject removeTag(String tag, String[] publicIds, Map options) throws IOException {
        if (options == null) options = Cloudinary.emptyMap();
		return callTagsApi(tag, "remove", publicIds, options);
	}

	public JSONObject replaceTag(String tag, String[] publicIds, Map options) throws IOException {
        if (options == null) options = Cloudinary.emptyMap();
		return callTagsApi(tag, "replace", publicIds, options);
	}

	public JSONObject callTagsApi(String tag, String command, String[] publicIds, Map options) throws IOException {
        if (options == null) options = Cloudinary.emptyMap();
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("tag", tag);
		params.put("command", command);
		params.put("type", (String) options.get("type"));
		params.put("public_ids", Arrays.asList(publicIds));
		return callApi("tags", params, options, null);
	}

	private final static String[] TEXT_PARAMS = { "public_id", "font_family", "font_size", "font_color", "text_align", "font_weight",
			"font_style", "background", "opacity", "text_decoration" };

	public JSONObject text(String text, Map options) throws IOException {
        if (options == null) options = Cloudinary.emptyMap();
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("text", text);
		for (String param : TEXT_PARAMS) {
			params.put(param, Cloudinary.asString(options.get(param)));
		}
		return callApi("text", params, options, null);
	}

	public JSONObject callApi(String action, Map<String, Object> params, Map options, Object file) throws IOException {
        if (options == null) options = Cloudinary.emptyMap();
		boolean returnError = Cloudinary.asBoolean(options.get("return_error"), false);
		String apiKey = Cloudinary.asString(options.get("api_key"), this.cloudinary.config.apiKey);
		if (apiKey == null)
			throw new IllegalArgumentException("Must supply api_key");

	    if (options.containsKey("signature") && options.containsKey("timestamp")) {
			params.put("timestamp", options.get("timestamp"));
			params.put("signature", options.get("signature"));
			params.put("api_key", apiKey);
	    } else {	    
			String apiSecret = Cloudinary.asString(options.get("api_secret"), this.cloudinary.config.apiSecret);
			if (apiSecret == null)
				throw new IllegalArgumentException("Must supply api_secret");
			params.put("timestamp", Long.valueOf(System.currentTimeMillis() / 1000L).toString());
			params.put("signature", this.cloudinary.apiSignRequest(params, apiSecret));
			params.put("api_key", apiKey);
	    }

		String apiUrl = cloudinary.cloudinaryApiUrl(action, options);
		MultipartUtility multipart = new MultipartUtility(apiUrl, "UTF-8", cloudinary.randomPublicId());

		// Remove blank parameters
		for (Map.Entry<String, Object> param : params.entrySet()) {
			if (param.getValue() instanceof String || param.getValue() instanceof Integer) { 
				String value = Cloudinary.asString(param.getValue());
				if (!TextUtils.isEmpty(value)) {
					multipart.addFormField(param.getKey(), value);
				}
			} else if (param.getValue() instanceof Collection) {
				for (Object value : (Collection) param.getValue()) {
					multipart.addFormField(param.getKey()+"[]", Cloudinary.asString(value));					
				}
			}
		}

		if (file instanceof String && !((String) file).matches("https?:.*|s3:.*|data:[^;]*;base64,([a-zA-Z0-9/+\n=]+)")) {
			file = new File((String) file);
		}
		if (file instanceof File) {
			multipart.addFilePart("file", (File) file);
		} else if (file instanceof String) {
			multipart.addFormField("file", (String) file);
		} else if (file instanceof InputStream) {
			multipart.addFilePart("file", (InputStream) file);
		}
		HttpURLConnection connection = multipart.execute();
		int code; 
		try {
			code = connection.getResponseCode();
		} catch (IOException e) {
			if (e.getMessage().equals("No authentication challenges found")) {
				// Android trying to be clever...
				code = 401;
			} else {
				throw e;
			}
		}
		InputStream responseStream = code >= 400 ? connection.getErrorStream() : connection.getInputStream(); 
		String responseData = readFully(responseStream);
		connection.disconnect();

		if (code != 200 && code != 400 && code != 500) {
			throw new RuntimeException("Server returned unexpected status code - " + code + " - " + responseData);
		}

		JSONObject result;
		try {
			result = new JSONObject(responseData);
			if (result.has("error")) {
				JSONObject error = result.getJSONObject("error");
				if (returnError) {
					error.put("http_code", code);
				} else {
					throw new RuntimeException(error.getString("message"));
				}
			}
			return result;
		} catch (JSONException e) {
			throw new RuntimeException("Invalid JSON response from server " + e.getMessage());
		}
	}
	
	protected static String readFully(InputStream in) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int length = 0;
		while ((length = in.read(buffer)) != -1) {
			baos.write(buffer, 0, length);
		}
		return new String(baos.toByteArray());
	}

	protected String buildEager(List<? extends Transformation> transformations) {
		if (transformations == null) {
			return null;
		}
		List<String> eager = new ArrayList<String>();
		for (Transformation transformation : transformations) {
			List<String> single_eager = new ArrayList<String>();
			String transformationString = transformation.generate();
			if (!TextUtils.isEmpty(transformationString)) {
				single_eager.add(transformationString);
			}
			if (transformation instanceof EagerTransformation) {
				EagerTransformation eagerTransformation = (EagerTransformation) transformation;
				if (!TextUtils.isEmpty(eagerTransformation.getFormat())) {
					single_eager.add(eagerTransformation.getFormat());
				}
			}
			eager.add(TextUtils.join("/", single_eager));
		}
		return TextUtils.join("|", eager);
	}

	protected String buildCustomHeaders(Object headers) {
		if (headers == null) {
			return null;
		} else if (headers instanceof String) {
			return (String) headers;
		} else if (headers instanceof Object[]) {
			return TextUtils.join("\n", (Object[]) headers) + "\n";
		} else {
			Map<String, String> headersMap = (Map<String, String>) headers;
			StringBuilder builder = new StringBuilder();
			for (Map.Entry<String, String> header : headersMap.entrySet()) {
				builder.append(header.getKey()).append(": ").append(header.getValue()).append("\n");
			}
			return builder.toString();
		}
	}
}
