package com.cloudinary.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import android.graphics.Rect;
import android.test.InstrumentationTestCase;
import android.util.Log;

import com.cloudinary.Cloudinary;
import com.cloudinary.Coordinates;
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

	protected InputStream getAssetStream(String filename) throws IOException {
		return getInstrumentation().getContext().getAssets().open(filename);
	}
	public void testUpload() throws Exception {
		if (cloudinary.config.apiSecret == null) return;
		JSONObject result = cloudinary.uploader().upload(getAssetStream("images/logo.png"), Cloudinary.asMap("colors", true));
		assertEquals(result.getLong("width"), 241L);
		assertEquals(result.getLong("height"), 51L);
		assertNotNull(result.get("colors"));
		assertNotNull(result.get("predominant"));
		Map<String, Object> to_sign = new HashMap<String, Object>();
		to_sign.put("public_id", result.getString("public_id"));
		to_sign.put("version", Cloudinary.asString(result.get("version")));
		String expected_signature = cloudinary.apiSignRequest(to_sign, cloudinary.config.apiSecret);
		assertEquals(result.get("signature"), expected_signature);
	}

	public void testUnsignedUpload() throws Exception {
		if (cloudinary.config.apiSecret == null) return;
		JSONObject result = cloudinary.uploader().unsignedUpload(getAssetStream("images/logo.png"), "sample_preset_dhfjhriu", Cloudinary.emptyMap());
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
		JSONObject result = emptyCloudinary.uploader().upload(getAssetStream("images/logo.png"), params);
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
		JSONObject result = cloudinary.uploader().upload(getAssetStream("images/logo.png"), Cloudinary.emptyMap());

		cloudinary.uploader().rename(result.getString("public_id"), result.get("public_id") + "2", Cloudinary.emptyMap());

		JSONObject result2 = cloudinary.uploader().upload(getAssetStream("images/favicon.ico"), Cloudinary.emptyMap());
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
		cloudinary.uploader().upload(getAssetStream("images/logo.png"),
				Cloudinary.asMap("eager", Collections.singletonList(new Transformation().crop("scale").width(2.0))));
	}

	public void testHeaders() throws Exception {
		if (cloudinary.config.apiSecret == null) return;
		cloudinary.uploader().upload(getAssetStream("images/logo.png"), Cloudinary.asMap("headers", new String[] { "Link: 1" }));
		cloudinary.uploader().upload(getAssetStream("images/logo.png"), Cloudinary.asMap("headers", Cloudinary.asMap("Link", "1")));
	}

	public void testText() throws Exception {
		if (cloudinary.config.apiSecret == null) return;
		JSONObject result = cloudinary.uploader().text("hello world", Cloudinary.emptyMap());
		assertTrue(result.getInt("width") > 1);
		assertTrue(result.getInt("height") > 1);
	}

	public void testSprite() throws Exception {
		if (cloudinary.config.apiSecret == null) return;
		cloudinary.uploader().upload(getAssetStream("images/logo.png"),
				Cloudinary.asMap("tags", "sprite_test_tag", "public_id", "sprite_test_tag_1"));
		cloudinary.uploader().upload(getAssetStream("images/logo.png"),
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
		cloudinary.uploader().upload(getAssetStream("images/logo.png"),
				Cloudinary.asMap("tags", "multi_test_tag", "public_id", "multi_test_tag_1"));
		cloudinary.uploader().upload(getAssetStream("images/logo.png"),
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

	public void testUniqueFilename() throws Exception {

		File f = new File(getInstrumentation().getContext().getCacheDir()
				+ "/logo.png");

		InputStream is = getAssetStream("images/logo.png");
		int size = is.available();
		byte[] buffer = new byte[size];
		is.read(buffer);
		is.close();

		FileOutputStream fos = new FileOutputStream(f);
		fos.write(buffer);
		fos.close();

		JSONObject result = cloudinary.uploader().upload(f,
				Cloudinary.asMap("use_filename", true));
		assertTrue(result.getString("public_id").matches("logo_[a-z0-9]{6}"));
		result = cloudinary.uploader().upload(f,
				Cloudinary.asMap("use_filename", true, "unique_filename", false));
		assertEquals(result.getString("public_id"), "logo");
	}

    public void testFaceCoordinates() throws Exception {
	    	//should allow sending face coordinates
	    	Coordinates coordinates = new Coordinates();
	    	Rect rect1 = new Rect(121,31,231,182);
	    	Rect rect2 = new Rect(120,30,229,270);
	    	coordinates.addRect(rect1);
	    	coordinates.addRect(rect2);
	    	JSONObject result = cloudinary.uploader().upload(getAssetStream("images/logo.png"), Cloudinary.asMap("face_coordinates", coordinates, "faces", true));
	    	JSONArray resultFaces = result.getJSONArray("faces");
	    	assertEquals(2, resultFaces.length());
	    	
	    	JSONArray resultCoordinates = resultFaces.getJSONArray(0); 
	    	
	    	assertEquals(rect1.left, resultCoordinates.getInt(0));
	    	assertEquals(rect1.top, resultCoordinates.getInt(1));
	    	assertEquals(rect1.width(), resultCoordinates.getInt(2));
	    	assertEquals(rect1.height(), resultCoordinates.getInt(3));
	    	
	     resultCoordinates = resultFaces.getJSONArray(1); 
	    	
	    	assertEquals(rect2.left, resultCoordinates.getInt(0));
	    	assertEquals(rect2.top, resultCoordinates.getInt(1));
	    	assertEquals(rect2.width(), resultCoordinates.getInt(2));
	    	assertEquals(rect2.height(), resultCoordinates.getInt(3));

    }

    public void testContext() throws Exception {
	    	//should allow sending context
	    	Map context = Cloudinary.asMap("caption", "some caption", "alt", "alternative");
	    cloudinary.uploader().upload(getAssetStream("images/logo.png"), Cloudinary.asMap("context", context));
    }

    
    public void testModerationRequest() throws Exception {
    	//should support requesting manual moderation
    	JSONObject result = cloudinary.uploader().upload(getAssetStream("images/logo.png"),  Cloudinary.asMap("moderation", "manual"));
    	assertEquals("manual", result.getJSONArray("moderation").getJSONObject(0).getString("kind"));
    	assertEquals("pending", result.getJSONArray("moderation").getJSONObject(0).getString("status"));
    }    
    
    public void testRawConvertRequest() {
    	//should support requesting raw conversion
    	try {
    		cloudinary.uploader().upload(getAssetStream("docx.docx"),  Cloudinary.asMap("raw_convert", "illegal", "resource_type", "raw"));
    	} catch(Exception e) {
    		assertTrue(e.getMessage().matches(".*illegal is not a valid.*"));
        }
    }    
    
    public void testCategorizationRequest() {
    	//should support requesting categorization
    	try {
    		cloudinary.uploader().upload(getAssetStream("images/logo.png"),  Cloudinary.asMap("categorization", "illegal"));
    	} catch(Exception e) {
    		assertTrue(e.getMessage().matches(".*illegal is not a valid.*"));
        }
    }    
    
    public void testDetectionRequest() {
    	//should support requesting detection
    	try {
    		cloudinary.uploader().upload(getAssetStream("images/logo.png"),  Cloudinary.asMap("detection", "illegal"));
    	} catch(Exception e) {
    		assertTrue(e.getMessage().matches(".*illegal is not a valid.*"));
        }
    }
        
    public void testAutoTaggingRequest() {
    	//should support requesting auto tagging
    	try {
    		cloudinary.uploader().upload(getAssetStream("images/logo.png"),  Cloudinary.asMap("auto_tagging", 0.5f));
    	} catch(Exception e) {
    		assertTrue(e.getMessage().matches("^Must use(.*)"));
        }
    }    
}
