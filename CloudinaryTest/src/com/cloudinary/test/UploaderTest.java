package com.cloudinary.test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import android.test.InstrumentationTestCase;
import android.util.Log;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;

public class UploaderTest extends InstrumentationTestCase {

	private Cloudinary cloudinary;
	private static boolean first = true;

	public void setUp() throws Exception {
		this.cloudinary = new Cloudinary(getInstrumentation().getContext());
		if (first) {
			first = false;
			if (cloudinary.config.apiSecret == null) {
				Log.e("UploaderTest", "Please CLOUDINARY_URL in AndroidManifest for Upload test to run");
			}
		}
	}

	protected InputStream getImageStream(String filename) throws IOException {
		return getInstrumentation().getContext().getAssets().open("images/"+filename);
	}
	public void testUpload() throws Exception {
		if (cloudinary.config.apiSecret == null) return;
		JSONObject result = cloudinary.uploader().upload(getImageStream("logo.png"), Cloudinary.emptyMap());
		assertEquals(result.getLong("width"), 241L);
		assertEquals(result.getLong("height"), 51L);
		Map<String, Object> to_sign = new HashMap<String, Object>();
		to_sign.put("public_id", result.getString("public_id"));
		to_sign.put("version", Cloudinary.asString(result.get("version")));
		String expected_signature = cloudinary.apiSignRequest(to_sign, cloudinary.config.apiSecret);
		assertEquals(result.get("signature"), expected_signature);
	}

	public void testUploadUrl() throws Exception {
		if (cloudinary.config.apiSecret == null) return;
		JSONObject result = cloudinary.uploader().upload("http://cloudinary.com/images/logo.png", Cloudinary.emptyMap());
        assertEquals(result.getLong("width"), 241L);
        assertEquals(result.getLong("height"), 51L);
        Map<String, Object> to_sign = new HashMap<String, Object>();
        to_sign.put("public_id", (String) result.get("public_id"));
        to_sign.put("version", Cloudinary.asString(result.get("version")));
		String expected_signature = cloudinary.apiSignRequest(to_sign, cloudinary.config.apiSecret);
        assertEquals(result.get("signature"), expected_signature);
    }

	public void testUploadDataUri() throws Exception {
		if (cloudinary.config.apiSecret == null) return;
    	JSONObject result = cloudinary.uploader().upload("data:image/png;base64,iVBORw0KGgoAA\nAANSUhEUgAAABAAAAAQAQMAAAAlPW0iAAAABlBMVEUAAAD///+l2Z/dAAAAM0l\nEQVR4nGP4/5/h/1+G/58ZDrAz3D/McH8yw83NDDeNGe4Ug9C9zwz3gVLMDA/A6\nP9/AFGGFyjOXZtQAAAAAElFTkSuQmCC", Cloudinary.emptyMap());
        assertEquals(result.getLong("width"), 16L);
        assertEquals(result.getLong("height"), 16L);
        Map<String, Object> to_sign = new HashMap<String, Object>();
        to_sign.put("public_id", (String) result.get("public_id"));
        to_sign.put("version", Cloudinary.asString(result.get("version")));
		String expected_signature = cloudinary.apiSignRequest(to_sign, cloudinary.config.apiSecret);
        assertEquals(result.get("signature"), expected_signature);
    }

	public void testUploadExternalSignature() throws Exception {
		String apiSecret = cloudinary.config.apiSecret;
		if (apiSecret == null) return;
		Map<String,String> config = new HashMap<String,String>();
		config.put("api_key", cloudinary.config.apiKey);
		config.put("cloud_name", cloudinary.config.cloudName);
		
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("timestamp", Long.valueOf(System.currentTimeMillis() / 1000L).toString());
		params.put("signature", this.cloudinary.apiSignRequest(params, apiSecret));
		Cloudinary emptyCloudinary = new Cloudinary(config);
		JSONObject result = emptyCloudinary.uploader().upload(getImageStream("logo.png"), params);
		assertEquals(result.getLong("width"), 241L);
		assertEquals(result.getLong("height"), 51L);
		Map<String, Object> to_sign = new HashMap<String, Object>();
		to_sign.put("public_id", result.getString("public_id"));
		to_sign.put("version", Cloudinary.asString(result.get("version")));
		String expected_signature = cloudinary.apiSignRequest(to_sign, apiSecret);
		assertEquals(result.get("signature"), expected_signature);
	}

	public void testRename() throws Exception {
		if (cloudinary.config.apiSecret == null) return;
		JSONObject result = cloudinary.uploader().upload(getImageStream("logo.png"), Cloudinary.emptyMap());

		cloudinary.uploader().rename(result.getString("public_id"), result.get("public_id") + "2", Cloudinary.emptyMap());

		JSONObject result2 = cloudinary.uploader().upload(getImageStream("favicon.ico"), Cloudinary.emptyMap());
		boolean error_found = false;
		try {
			cloudinary.uploader().rename((String) result2.get("public_id"), result.get("public_id") + "2", Cloudinary.emptyMap());
		} catch (Exception e) {
			error_found = true;
		}
		assertTrue(error_found);
		cloudinary.uploader().rename((String) result2.get("public_id"), result.get("public_id") + "2",
				Cloudinary.asMap("overwrite", Boolean.TRUE));
	}

	public void testExplicit() throws Exception {
		if (cloudinary.config.apiSecret == null) return;
		JSONObject result = cloudinary.uploader()
				.explicit(
						"cloudinary",
						Cloudinary.asMap("eager", Collections.singletonList(new Transformation().crop("scale").width(2.0)), "type",
								"twitter_name"));
		String url = cloudinary.url().type("twitter_name").transformation(new Transformation().crop("scale").width(2.0)).format("png")
				.version(result.get("version")).generate("cloudinary");
		assertEquals(result.getJSONArray("eager").getJSONObject(0).get("url"), url);
	}

	public void testEager() throws Exception {
		if (cloudinary.config.apiSecret == null) return;
		cloudinary.uploader().upload(getImageStream("logo.png"),
				Cloudinary.asMap("eager", Collections.singletonList(new Transformation().crop("scale").width(2.0))));
	}

	public void testHeaders() throws Exception {
		if (cloudinary.config.apiSecret == null) return;
		cloudinary.uploader().upload(getImageStream("logo.png"), Cloudinary.asMap("headers", new String[] { "Link: 1" }));
		cloudinary.uploader().upload(getImageStream("logo.png"), Cloudinary.asMap("headers", Cloudinary.asMap("Link", "1")));
	}

	public void testText() throws Exception {
		if (cloudinary.config.apiSecret == null) return;
		JSONObject result = cloudinary.uploader().text("hello world", Cloudinary.emptyMap());
		assertTrue(result.getInt("width") > 1);
		assertTrue(result.getInt("height") > 1);
	}

	public void testSprite() throws Exception {
		if (cloudinary.config.apiSecret == null) return;
		cloudinary.uploader().upload(getImageStream("logo.png"),
				Cloudinary.asMap("tags", "sprite_test_tag", "public_id", "sprite_test_tag_1"));
		cloudinary.uploader().upload(getImageStream("logo.png"),
				Cloudinary.asMap("tags", "sprite_test_tag", "public_id", "sprite_test_tag_2"));
		JSONObject result = cloudinary.uploader().generate_sprite("sprite_test_tag", Cloudinary.emptyMap());
		assertEquals(2, result.getJSONObject("image_infos").length());
		result = cloudinary.uploader().generate_sprite("sprite_test_tag", Cloudinary.asMap("transformation", "w_100"));
		assertTrue((result.getString("css_url")).contains("w_100"));
		result = cloudinary.uploader().generate_sprite("sprite_test_tag",
				Cloudinary.asMap("transformation", new Transformation().width(100), "format", "jpg"));
		assertTrue((result.getString("css_url")).contains("f_jpg,w_100"));
	}

	public void testMulti() throws Exception {
		if (cloudinary.config.apiSecret == null) return;
		cloudinary.uploader().upload(getImageStream("logo.png"),
				Cloudinary.asMap("tags", "multi_test_tag", "public_id", "multi_test_tag_1"));
		cloudinary.uploader().upload(getImageStream("logo.png"),
				Cloudinary.asMap("tags", "multi_test_tag", "public_id", "multi_test_tag_2"));
		JSONObject result = cloudinary.uploader().multi("multi_test_tag", Cloudinary.emptyMap());
		assertTrue((result.getString("url")).endsWith(".gif"));
		result = cloudinary.uploader().multi("multi_test_tag", Cloudinary.asMap("transformation", "w_100"));
		assertTrue((result.getString("url")).contains("w_100"));
		result = cloudinary.uploader().multi("multi_test_tag",
				Cloudinary.asMap("transformation", new Transformation().width(111), "format", "pdf"));
		assertTrue((result.getString("url")).contains("w_111"));
		assertTrue((result.getString("url")).endsWith(".pdf"));
	}
}
