package com.cloudinary.android;

import android.util.Log;

import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;
import androidx.test.platform.app.InstrumentationRegistry;

import com.cloudinary.Cloudinary;
import com.cloudinary.Coordinates;
import com.cloudinary.ProgressCallback;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import com.cloudinary.utils.Rectangle;
import com.cloudinary.utils.StringUtils;

import junit.framework.Assert;

import org.cloudinary.json.JSONArray;
import org.cloudinary.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

@RunWith(AndroidJUnit4ClassRunner.class)
public class UploaderTest extends AbstractTest {

    private static Cloudinary cloudinary;
    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @BeforeClass
    public static void setUp() throws Exception {
        String url = Utils.cloudinaryUrlFromContext(InstrumentationRegistry.getInstrumentation().getContext());
        cloudinary = new Cloudinary(url);
        if (StringUtils.isBlank(url)) {
            throw new IllegalArgumentException("UploaderTest - No cloudinary url configured");
        }

        if (!url.startsWith("cloudinary://")){
            throw new IllegalArgumentException("UploaderTest - malformed cloudinary url");
        }
    }

    private File getLargeFile() throws IOException {
        File temp = File.createTempFile("cldupload.test.", "");
        FileOutputStream out = new FileOutputStream(temp);
        int[] header = new int[]{0x42, 0x4D, 0x4A, 0xB9, 0x59, 0x00, 0x00, 0x00, 0x00, 0x00, 0x8A, 0x00, 0x00, 0x00, 0x7C, 0x00, 0x00, 0x00, 0x78, 0x05, 0x00, 0x00, 0x78, 0x05, 0x00, 0x00, 0x01, 0x00, 0x18, 0x00, 0x00, 0x00, 0x00, 0x00, 0xC0, 0xB8, 0x59, 0x00, 0x61, 0x0F, 0x00, 0x00, 0x61, 0x0F, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0xFF, 0x00, 0x00, 0xFF, 0x00, 0x00, 0xFF, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0xFF, 0x42, 0x47, 0x52, 0x73, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x54, 0xB8, 0x1E, 0xFC, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x66, 0x66, 0x66, 0xFC, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0xC4, 0xF5, 0x28, 0xFF, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x04, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        byte[] byteHeader = new byte[138];
        for (int i = 0; i <= 137; i++) byteHeader[i] = (byte) header[i];
        byte[] piece = new byte[10];
        Arrays.fill(piece, (byte) 0xff);
        out.write(byteHeader);
        for (int i = 1; i <= 588000; i++) {
            out.write(piece);
        }
        out.close();
        assertEquals(5880138, temp.length());
        return temp;
    }

    @Test
    public void testUpload() throws Exception {
        if (cloudinary.config.apiSecret == null)
            return;
        JSONObject result = new JSONObject(cloudinary.uploader().upload(getAssetStream(TEST_IMAGE), ObjectUtils.asMap("colors", true)));
        Assert.assertEquals(result.getLong("width"), 241L);
        Assert.assertEquals(result.getLong("height"), 51L);
        assertNotNull(result.get("colors"));
        assertNotNull(result.get("predominant"));
        Map<String, Object> to_sign = new HashMap<String, Object>();
        to_sign.put("public_id", result.getString("public_id"));
        to_sign.put("version", ObjectUtils.asString(result.get("version")));
        String expected_signature = cloudinary.apiSignRequest(to_sign, cloudinary.config.apiSecret);
        Assert.assertEquals(result.get("signature"), expected_signature);
    }

    @Test
    public void testUploadProgressCallback() throws Exception {
        if (cloudinary.config.apiSecret == null)
            return;

        final long totalLength = getAssetFileSize(TEST_IMAGE);
        final long[] totalUploaded = new long[]{0};

        ProgressCallback progressCallback = new ProgressCallback() {
            @Override
            public void onProgress(long bytesUploaded, long totalBytes) {
                totalUploaded[0] += bytesUploaded;
            }
        };

        JSONObject result = new JSONObject(cloudinary.uploader().upload(getAssetStream(TEST_IMAGE), ObjectUtils.asMap("colors", true), progressCallback));

        assertTrue("ProgressCallback was never called", totalUploaded[0] > 0);
        assertEquals("ProgressCallback calls do not sum up to actual file length", totalLength, totalUploaded[0]);

        Assert.assertEquals(result.getLong("width"), 241L);
        Assert.assertEquals(result.getLong("height"), 51L);
        assertNotNull(result.get("colors"));
        assertNotNull(result.get("predominant"));
        Map<String, Object> to_sign = new HashMap<String, Object>();
        to_sign.put("public_id", result.getString("public_id"));
        to_sign.put("version", ObjectUtils.asString(result.get("version")));
        String expected_signature = cloudinary.apiSignRequest(to_sign, cloudinary.config.apiSecret);
        Assert.assertEquals(result.get("signature"), expected_signature);
    }

    @Test
    public void testUnsignedUpload() throws Exception {
        if (cloudinary.config.apiSecret == null)
            return;
        JSONObject result = new JSONObject(cloudinary.uploader().unsignedUpload(getAssetStream(TEST_IMAGE), TEST_PRESET,
                ObjectUtils.emptyMap()));
        Assert.assertEquals(result.getLong("width"), 241L);
        Assert.assertEquals(result.getLong("height"), 51L);
        Map<String, Object> to_sign = new HashMap<String, Object>();
        to_sign.put("public_id", result.getString("public_id"));
        to_sign.put("version", ObjectUtils.asString(result.get("version")));
        Log.d("TestRunner", cloudinary.config.apiSecret);
        String expected_signature = cloudinary.apiSignRequest(to_sign, cloudinary.config.apiSecret);
        Assert.assertEquals(result.get("signature"), expected_signature);
    }

    @Test
    public void testUploadUrl() throws Exception {
        if (cloudinary.config.apiSecret == null)
            return;
        JSONObject result = new JSONObject(cloudinary.uploader().upload("http://cloudinary.com/images/old_logo.png", ObjectUtils.emptyMap()));
        Assert.assertEquals(result.getLong("width"), 241L);
        Assert.assertEquals(result.getLong("height"), 51L);
        Map<String, Object> to_sign = new HashMap<String, Object>();
        to_sign.put("public_id", (String) result.get("public_id"));
        to_sign.put("version", ObjectUtils.asString(result.get("version")));
        String expected_signature = cloudinary.apiSignRequest(to_sign, cloudinary.config.apiSecret);
        Assert.assertEquals(result.get("signature"), expected_signature);
    }

    @Test
    public void testUploadDataUri() throws Exception {
        if (cloudinary.config.apiSecret == null)
            return;
        JSONObject result = new JSONObject(
                cloudinary
                        .uploader()
                        .upload("data:image/png;base64,iVBORw0KGgoAA\nAANSUhEUgAAABAAAAAQAQMAAAAlPW0iAAAABlBMVEUAAAD///+l2Z/dAAAAM0l\nEQVR4nGP4/5/h/1+G/58ZDrAz3D/McH8yw83NDDeNGe4Ug9C9zwz3gVLMDA/A6\nP9/AFGGFyjOXZtQAAAAAElFTkSuQmCC",
                                ObjectUtils.emptyMap()));
        Assert.assertEquals(result.getLong("width"), 16L);
        Assert.assertEquals(result.getLong("height"), 16L);
        Map<String, Object> to_sign = new HashMap<String, Object>();
        to_sign.put("public_id", (String) result.get("public_id"));
        to_sign.put("version", ObjectUtils.asString(result.get("version")));
        String expected_signature = cloudinary.apiSignRequest(to_sign, cloudinary.config.apiSecret);
        Assert.assertEquals(result.get("signature"), expected_signature);
    }

    @Test
    public void testUploadExternalSignature() throws Exception {
        String apiSecret = cloudinary.config.apiSecret;
        if (apiSecret == null)
            return;
        Map<String, String> config = new HashMap<String, String>();
        config.put("api_key", cloudinary.config.apiKey);
        config.put("cloud_name", cloudinary.config.cloudName);

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("timestamp", Long.valueOf(System.currentTimeMillis() / 1000L).toString());
        params.put("signature", this.cloudinary.apiSignRequest(params, apiSecret));
        Cloudinary emptyCloudinary = new Cloudinary(config);
        JSONObject result = new JSONObject(emptyCloudinary.uploader().upload(getAssetStream(TEST_IMAGE), params));
        Assert.assertEquals(result.getLong("width"), 241L);
        Assert.assertEquals(result.getLong("height"), 51L);
        Map<String, Object> to_sign = new HashMap<String, Object>();
        to_sign.put("public_id", result.getString("public_id"));
        to_sign.put("version", ObjectUtils.asString(result.get("version")));
        String expected_signature = cloudinary.apiSignRequest(to_sign, apiSecret);
        Assert.assertEquals(result.get("signature"), expected_signature);
    }

    @Test
    public void testRename() throws Exception {
        if (cloudinary.config.apiSecret == null)
            return;
        JSONObject result = new JSONObject(cloudinary.uploader().upload(getAssetStream(TEST_IMAGE), ObjectUtils.emptyMap()));

        cloudinary.uploader().rename(result.getString("public_id"), result.get("public_id") + "2", ObjectUtils.emptyMap());

        JSONObject result2 = new JSONObject(cloudinary.uploader().upload(getAssetStream("images/favicon.ico"), ObjectUtils.emptyMap()));
        boolean error_found = false;
        try {
            cloudinary.uploader().rename((String) result2.get("public_id"), result.get("public_id") + "2", ObjectUtils.emptyMap());
        } catch (Exception e) {
            error_found = true;
        }
        assertTrue(error_found);
        cloudinary.uploader().rename((String) result2.get("public_id"), result.get("public_id") + "2", ObjectUtils.asMap("overwrite", Boolean.TRUE));
    }

    @Test
    public void testExplicit() throws Exception {
        if (cloudinary.config.apiSecret == null)
            return;
        JSONObject result = new JSONObject(cloudinary.uploader().explicit("sample",
                ObjectUtils.asMap("eager", Collections.singletonList(new Transformation().crop("scale").width(2.0)), "type", "upload")));
        String url = cloudinary.url().transformation(new Transformation().crop("scale").width(2.0)).format("jpg")
                .version(result.get("version")).generate("sample");
        Assert.assertEquals(url, result.getJSONArray("eager").getJSONObject(0).get("url"));
    }

    @Test
    public void testEager() throws Exception {
        if (cloudinary.config.apiSecret == null)
            return;
        cloudinary.uploader().upload(getAssetStream(TEST_IMAGE),
                ObjectUtils.asMap("eager", Collections.singletonList(new Transformation().crop("scale").width(2.0))));
    }

    @Test
    public void testUploadAsync() throws Exception {
        if (cloudinary.config.apiSecret == null)
            return;
        JSONObject result = new JSONObject(cloudinary.uploader().upload(getAssetStream(TEST_IMAGE),
                ObjectUtils.asMap("transformation", new Transformation().crop("scale").width(2.0), "async", true)));
        Assert.assertEquals(result.getString("status"), "pending");
    }

    @Test
    public void testHeaders() throws Exception {
        if (cloudinary.config.apiSecret == null)
            return;
        cloudinary.uploader().upload(getAssetStream(TEST_IMAGE), ObjectUtils.asMap("headers", new String[]{"Link: 1"}));
        cloudinary.uploader().upload(getAssetStream(TEST_IMAGE), ObjectUtils.asMap("headers", ObjectUtils.asMap("Link", "1")));
    }

    @Test
    public void testText() throws Exception {
        if (cloudinary.config.apiSecret == null)
            return;
        JSONObject result = new JSONObject(cloudinary.uploader().text("hello world", ObjectUtils.emptyMap()));
        assertTrue(result.getInt("width") > 1);
        assertTrue(result.getInt("height") > 1);
    }

    @Test
    public void testSprite() throws Exception {
        if (cloudinary.config.apiSecret == null)
            return;
        final String sprite_test_tag = String.format("sprite_test_tag_%d", new java.util.Date().getTime());
        cloudinary.uploader().upload(getAssetStream(TEST_IMAGE), ObjectUtils.asMap("tags", sprite_test_tag, "public_id", "sprite_test_tag_1"));
        cloudinary.uploader().upload(getAssetStream(TEST_IMAGE), ObjectUtils.asMap("tags", sprite_test_tag, "public_id", "sprite_test_tag_2"));
        JSONObject result = new JSONObject(cloudinary.uploader().generateSprite(sprite_test_tag, ObjectUtils.emptyMap()));
        Assert.assertEquals(2, result.getJSONObject("image_infos").length());
        result = new JSONObject(cloudinary.uploader().generateSprite(sprite_test_tag, ObjectUtils.asMap("transformation", "w_100")));
        assertTrue((result.getString("css_url")).contains("w_100"));
        result = new JSONObject(cloudinary.uploader().generateSprite(sprite_test_tag,
                ObjectUtils.asMap("transformation", new Transformation().width(100), "format", "jpg")));
        assertTrue((result.getString("css_url")).contains("f_jpg,w_100"));
    }

    @Test
    public void testMulti() throws Exception {
        if (cloudinary.config.apiSecret == null)
            return;
        cloudinary.uploader().upload(getAssetStream(TEST_IMAGE), ObjectUtils.asMap("tags", "multi_test_tag", "public_id", "multi_test_tag_1"));
        cloudinary.uploader().upload(getAssetStream(TEST_IMAGE), ObjectUtils.asMap("tags", "multi_test_tag", "public_id", "multi_test_tag_2"));
        JSONObject result = new JSONObject(cloudinary.uploader().multi("multi_test_tag", ObjectUtils.emptyMap()));
        assertTrue((result.getString("url")).endsWith(".gif"));
        result = new JSONObject(cloudinary.uploader().multi("multi_test_tag", ObjectUtils.asMap("transformation", "w_100")));
        assertTrue((result.getString("url")).contains("w_100"));
        result = new JSONObject(cloudinary.uploader().multi("multi_test_tag", ObjectUtils.asMap("transformation", new Transformation().width(111), "format", "pdf")));
        assertTrue((result.getString("url")).contains("w_111"));
        assertTrue((result.getString("url")).endsWith(".pdf"));
    }

    @Test
    public void testUniqueFilename() throws Exception {
        if (cloudinary.config.apiSecret == null)
            return;

        File f = new File(InstrumentationRegistry.getInstrumentation().getContext().getCacheDir() + "/old_logo.png");

        InputStream is = getAssetStream(TEST_IMAGE);
        int size = is.available();
        byte[] buffer = new byte[size];
        is.read(buffer);
        is.close();

        FileOutputStream fos = new FileOutputStream(f);
        fos.write(buffer);
        fos.close();

        JSONObject result = new JSONObject(cloudinary.uploader().upload(f, ObjectUtils.asMap("use_filename", true)));
        assertTrue(result.getString("public_id").matches("old_logo_[a-z0-9]{6}"));
        result = new JSONObject(cloudinary.uploader().upload(f, ObjectUtils.asMap("use_filename", true, "unique_filename", false)));
        Assert.assertEquals(result.getString("public_id"), "old_logo");
    }

    @Test
    public void testFaceCoordinates() throws Exception {
        // should allow sending face coordinates
        if (cloudinary.config.apiSecret == null)
            return;
        Coordinates coordinates = new Coordinates();
        Rectangle rect1 = new Rectangle(121, 31, 110, 51);
        Rectangle rect2 = new Rectangle(120, 30, 109, 51);
        coordinates.addRect(rect1);
        coordinates.addRect(rect2);
        JSONObject result = new JSONObject(cloudinary.uploader().upload(getAssetStream(TEST_IMAGE), ObjectUtils.asMap("face_coordinates", coordinates, "faces", true)));
        JSONArray resultFaces = result.getJSONArray("faces");
        Assert.assertEquals(2, resultFaces.length());

        JSONArray resultCoordinates = resultFaces.getJSONArray(0);

        Assert.assertEquals(rect1.x, resultCoordinates.getInt(0));
        Assert.assertEquals(rect1.y, resultCoordinates.getInt(1));
        Assert.assertEquals(rect1.width, resultCoordinates.getInt(2));
        Assert.assertEquals(rect1.height, resultCoordinates.getInt(3));

        resultCoordinates = resultFaces.getJSONArray(1);

        Assert.assertEquals(rect2.x, resultCoordinates.getInt(0));
        Assert.assertEquals(rect2.y, resultCoordinates.getInt(1));
        Assert.assertEquals(rect2.width, resultCoordinates.getInt(2));
        Assert.assertEquals(rect2.height, resultCoordinates.getInt(3));

    }

    @Test
    public void testContext() throws Exception {
        // should allow sending context
        if (cloudinary.config.apiSecret == null)
            return;
        @SuppressWarnings("rawtypes")
        Map context = ObjectUtils.asMap("caption", "some caption", "alt", "alternative");
        cloudinary.uploader().upload(getAssetStream(TEST_IMAGE), ObjectUtils.asMap("context", context));
    }

    @Test
    public void testModerationRequest() throws Exception {
        // should support requesting manual moderation
        if (cloudinary.config.apiSecret == null)
            return;
        JSONObject result = new JSONObject(cloudinary.uploader().upload(getAssetStream(TEST_IMAGE), ObjectUtils.asMap("moderation", "manual")));
        Assert.assertEquals("manual", result.getJSONArray("moderation").getJSONObject(0).getString("kind"));
        Assert.assertEquals("pending", result.getJSONArray("moderation").getJSONObject(0).getString("status"));
    }

    @Test
    public void testRawConvertRequest() {
        // should support requesting raw conversion
        if (cloudinary.config.apiSecret == null)
            return;
        try {
            cloudinary.uploader().upload(getAssetStream("docx.docx"), ObjectUtils.asMap("raw_convert", "illegal", "resource_type", "raw"));
        } catch (Exception e) {
            assertEquals("Raw convert is invalid", e.getMessage());
        }
    }

    @Test
    public void testCategorizationRequest() {
        String errorMessage = "";
        // should support requesting categorization
        if (cloudinary.config.apiSecret == null)
            return;
        try {
            cloudinary.uploader().upload(getAssetStream(TEST_IMAGE), ObjectUtils.asMap("categorization", "illegal"));
        } catch (Exception e) {
            errorMessage = e.getMessage();
        }

        assertTrue(errorMessage.contains("Categorization item illegal is not valid"));
    }

    @Test
    public void testDetectionRequest() {
        // should support requesting detection
        if (cloudinary.config.apiSecret == null)
            return;
        try {
            cloudinary.uploader().upload(getAssetStream(TEST_IMAGE), ObjectUtils.asMap("detection", "illegal"));
        } catch (Exception e) {
            assertTrue(e.getMessage().matches(".*(Illegal value|not a valid|invalid).*"));
        }
    }

    @Test
    public void testFilenameOption() throws Exception {
        JSONObject result = new JSONObject(cloudinary.uploader().upload(getAssetStream(TEST_IMAGE), ObjectUtils.asMap("filename", "emanelif")));
        Assert.assertEquals("emanelif", result.getString("original_filename"));
    }

    @Test
    public void testComplexFilenameOption() throws Exception {
        String complexFilename = "Universal Image Loader @#&=+-_.,!()~'%20.png";
        JSONObject result = new JSONObject(cloudinary.uploader().upload(getAssetStream(TEST_IMAGE), ObjectUtils.asMap("filename", complexFilename)));
        complexFilename = complexFilename.replace(".png", "");

        Assert.assertEquals(complexFilename, result.getString("original_filename"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testUploadLarge() throws Exception {
        // support uploading large files
        if (cloudinary.config.apiSecret == null)
            return;

        File temp = File.createTempFile("cldupload.test.", "");
        FileOutputStream out = new FileOutputStream(temp);
        int[] header = new int[]{0x42, 0x4D, 0x4A, 0xB9, 0x59, 0x00, 0x00, 0x00, 0x00, 0x00, 0x8A, 0x00, 0x00, 0x00, 0x7C, 0x00, 0x00, 0x00, 0x78, 0x05, 0x00, 0x00, 0x78, 0x05, 0x00, 0x00, 0x01, 0x00, 0x18, 0x00, 0x00, 0x00, 0x00, 0x00, 0xC0, 0xB8, 0x59, 0x00, 0x61, 0x0F, 0x00, 0x00, 0x61, 0x0F, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0xFF, 0x00, 0x00, 0xFF, 0x00, 0x00, 0xFF, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0xFF, 0x42, 0x47, 0x52, 0x73, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x54, 0xB8, 0x1E, 0xFC, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x66, 0x66, 0x66, 0xFC, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0xC4, 0xF5, 0x28, 0xFF, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x04, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        byte[] byteHeader = new byte[138];
        for (int i = 0; i <= 137; i++) byteHeader[i] = (byte) header[i];
        byte[] piece = new byte[10];
        Arrays.fill(piece, (byte) 0xff);
        out.write(byteHeader);
        for (int i = 1; i <= 588000; i++) {
            out.write(piece);
        }
        out.close();
        assertEquals(5880138, temp.length());

        JSONObject resource = new JSONObject(cloudinary.uploader().uploadLarge(temp, ObjectUtils.asMap("resource_type", "raw", "chunk_size", 5243000)));
        Assert.assertEquals("raw", resource.getString("resource_type"));

        resource = new JSONObject(cloudinary.uploader().uploadLarge(temp, ObjectUtils.asMap("chunk_size", 5243000)));
        Assert.assertEquals("image", resource.getString("resource_type"));
        Assert.assertEquals(1400L, resource.getLong("width"));
        Assert.assertEquals(1400L, resource.getLong("height"));

        resource = new JSONObject(cloudinary.uploader().uploadLarge(temp, ObjectUtils.asMap("chunk_size", 5880138)));
        Assert.assertEquals("image", resource.getString("resource_type"));
        Assert.assertEquals(1400L, resource.getLong("width"));
        Assert.assertEquals(1400L, resource.getLong("height"));
    }

    @Test
    public void testUploadLargeResume() throws Exception {
        // support uploading large files

        File temp = File.createTempFile("cldupload.test.", "");
        FileOutputStream out = new FileOutputStream(temp);
        int[] header = new int[]{0x42, 0x4D, 0x4A, 0xB9, 0x59, 0x00, 0x00, 0x00, 0x00, 0x00, 0x8A, 0x00, 0x00, 0x00, 0x7C, 0x00, 0x00, 0x00, 0x78, 0x05, 0x00, 0x00, 0x78, 0x05, 0x00, 0x00, 0x01, 0x00, 0x18, 0x00, 0x00, 0x00, 0x00, 0x00, 0xC0, 0xB8, 0x59, 0x00, 0x61, 0x0F, 0x00, 0x00, 0x61, 0x0F, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0xFF, 0x00, 0x00, 0xFF, 0x00, 0x00, 0xFF, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0xFF, 0x42, 0x47, 0x52, 0x73, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x54, 0xB8, 0x1E, 0xFC, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x66, 0x66, 0x66, 0xFC, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0xC4, 0xF5, 0x28, 0xFF, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x04, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        byte[] byteHeader = new byte[138];
        for (int i = 0; i <= 137; i++) byteHeader[i] = (byte) header[i];
        byte[] piece = new byte[10];
        Arrays.fill(piece, (byte) 0xff);
        out.write(byteHeader);
        for (int i = 1; i <= 2 * 588000; i++) {
            out.write(piece);
        }
        out.close();
        int expectedSize = 2 * 5880000 + 138;
        Assert.assertEquals(expectedSize, temp.length());

        String uniqueUploadId = cloudinary.randomPublicId();

        // fail right after 1 chunk:
        final int bufferSize = 5243000;
        try {
            cloudinary.uploader().uploadLarge(temp, ObjectUtils.asMap("resource_type", "raw"), bufferSize, 0, uniqueUploadId, new ProgressCallback() {
                @Override
                public void onProgress(long bytesUploaded, long totalBytes) {
                    if (bytesUploaded > bufferSize) {
                        throw new RuntimeException();
                    }
                }
            });
        } catch (Exception ignored) {
        }

        // fail again after second chunk (starting after 1 chunk):
        try {
            cloudinary.uploader().uploadLarge(temp, ObjectUtils.asMap("resource_type", "raw"), bufferSize, bufferSize, uniqueUploadId, new ProgressCallback() {
                @Override
                public void onProgress(long bytesUploaded, long totalBytes) {
                    if (bytesUploaded > 2 * bufferSize) {
                        throw new RuntimeException();
                    }
                }
            });
        } catch (Exception ignored) {
        }

        // finish it up - start upload from 3rd chunk (so skip 2 * buffer size):
        Map resource = cloudinary.uploader().uploadLarge(temp, ObjectUtils.asMap("resource_type", "raw"), bufferSize, 2 * bufferSize, uniqueUploadId, null);
        assertEquals(expectedSize, resource.get("bytes"));
    }

    @Test
    public void testUploadLargeProgressCallback() throws Exception {
        // support uploading large files
        if (cloudinary.config.apiSecret == null)
            return;


        File temp = getLargeFile();
        final CountDownLatch signal = new CountDownLatch(1);
        final long totalLength = temp.length();

        ProgressCallback progressCallback = new ProgressCallback() {
            @Override
            public void onProgress(long bytesUploaded, long totalBytes) {
                if (bytesUploaded == totalLength) {
                    signal.countDown();
                }
            }
        };
        JSONObject resource = new JSONObject(cloudinary.uploader().uploadLarge(temp, ObjectUtils.asMap("resource_type", "raw", "chunk_size", 5243000), progressCallback));

        signal.await(120, TimeUnit.SECONDS);
        assertEquals(signal.getCount(), 0);

        Assert.assertEquals("raw", resource.getString("resource_type"));

        resource = new JSONObject(cloudinary.uploader().uploadLarge(temp, ObjectUtils.asMap("chunk_size", 5243000)));
        Assert.assertEquals("image", resource.getString("resource_type"));
        Assert.assertEquals(1400L, resource.getLong("width"));
        Assert.assertEquals(1400L, resource.getLong("height"));

        resource = new JSONObject(cloudinary.uploader().uploadLarge(temp, ObjectUtils.asMap("chunk_size", 5880138)));
        Assert.assertEquals("image", resource.getString("resource_type"));
        Assert.assertEquals(1400L, resource.getLong("width"));
        Assert.assertEquals(1400L, resource.getLong("height"));
    }

    // This test is not reliable enough for uploads, timeout behaviour is too unpredictable
    //    @Test(expected = SocketTimeoutException.class)
    public void testConnectTimeout() throws IOException {
        cloudinary.uploader().unsignedUpload(getAssetStream(TEST_IMAGE), TEST_PRESET, ObjectUtils.asMap("connect_timeout", 1));
    }

    @Test(expected = SocketTimeoutException.class)
    public void testReadTimeout() throws IOException {
        cloudinary.uploader().unsignedUpload(getAssetStream(TEST_IMAGE), TEST_PRESET, ObjectUtils.asMap("read_timeout", 1));
    }
}
