package com.cloudinary.android.glide_integration;

import android.content.Context;

import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;
import androidx.test.platform.app.InstrumentationRegistry;

import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.ModelLoader;
import com.cloudinary.Transformation;
import com.cloudinary.android.CloudinaryRequest;
import com.cloudinary.android.MediaManager;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.io.InputStream;

import static com.bumptech.glide.request.target.Target.SIZE_ORIGINAL;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;

@RunWith(AndroidJUnit4ClassRunner.class)
public class CloudinaryRequestModelLoaderTest {

    private static final String TEST_CLOUD_NAME = "demo";
    private static final String TEST_PUBLIC_ID = "sample";

    @Rule public MockitoRule mockitoRule = MockitoJUnit.rule();
    @Mock private ModelLoader<GlideUrl, InputStream> urlLoader;
    @Captor private ArgumentCaptor<GlideUrl> captor;

    private CloudinaryRequestModelLoader sut;

    @BeforeClass
    public static void setUp() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        MediaManager.init(context);
        MediaManager.get().getCloudinary().config.cloudName = TEST_CLOUD_NAME;
        MediaManager.get().getCloudinary().config.secure = true;
    }

    @Before
    public void initLoader() {
        sut = new CloudinaryRequestModelLoader(urlLoader);
    }

    @Test
    public void testGeneratedUrl() {
        CloudinaryRequest model = new CloudinaryRequest.Builder(TEST_PUBLIC_ID).build();

        sut.buildLoadData(model, SIZE_ORIGINAL, SIZE_ORIGINAL, new Options());

        String expectedUrl = String.format("https://res.cloudinary.com/%s/image/upload/%s", TEST_CLOUD_NAME, TEST_PUBLIC_ID);
        verify(urlLoader).buildLoadData(captor.capture(), anyInt(), anyInt(), any(Options.class));
        Assert.assertEquals(expectedUrl, captor.getValue().toStringUrl());
    }

    @Test
    public void testGeneratedUrlWithTransformation() {
        CloudinaryRequest model = new CloudinaryRequest.Builder(TEST_PUBLIC_ID)
                .transformation(new Transformation().width(200).height(400))
                .build();

        sut.buildLoadData(model, SIZE_ORIGINAL, SIZE_ORIGINAL, new Options());

        String expectedUrl = String.format("https://res.cloudinary.com/%s/image/upload/h_400,w_200/%s", TEST_CLOUD_NAME, TEST_PUBLIC_ID);
        verify(urlLoader).buildLoadData(captor.capture(), anyInt(), anyInt(), any(Options.class));
        Assert.assertEquals(expectedUrl, captor.getValue().toStringUrl());
    }

    @Test
    public void testGeneratedUrlWithTransformationOption() {
        CloudinaryRequest model = new CloudinaryRequest.Builder(TEST_PUBLIC_ID).build();
        Options options = new Options().set(CloudinaryRequestModelLoader.TRANSFORMATION, new Transformation().width(200).height(400));

        sut.buildLoadData(model, SIZE_ORIGINAL, SIZE_ORIGINAL, options);

        String expectedUrl = String.format("https://res.cloudinary.com/%s/image/upload/h_400,w_200/%s", TEST_CLOUD_NAME, TEST_PUBLIC_ID);
        verify(urlLoader).buildLoadData(captor.capture(), anyInt(), anyInt(), any(Options.class));
        Assert.assertEquals(expectedUrl, captor.getValue().toStringUrl());
    }
}
