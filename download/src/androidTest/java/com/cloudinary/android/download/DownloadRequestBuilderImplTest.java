package com.cloudinary.android.download;

import android.content.Context;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.test.platform.app.InstrumentationRegistry;

import com.cloudinary.Transformation;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.ResponsiveUrl;
import com.cloudinary.android.download.test.R;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class DownloadRequestBuilderImplTest {

    private static final String TEST_PUBLIC_ID = "sample";
    private static String cloudName;
    private static boolean initialized;

    private DownloadRequestBuilderImpl sut;

    @Mock
    private DownloadRequestBuilderStrategy downloadRequestBuilderStrategy;

    @Mock
    private ImageView imageView;

    @BeforeClass
    public synchronized static void setup() {
        if (!initialized) {
            MediaManager.init(InstrumentationRegistry.getInstrumentation().getTargetContext());
            cloudName = MediaManager.get().getCloudinary().config.cloudName;
            MediaManager.get().getCloudinary().config.secure = true;
            initialized = true;
        }
    }

    @Before
    public void initSut() {
        Context context = InstrumentationRegistry.getInstrumentation().getContext();
        sut = new DownloadRequestBuilderImpl(context, downloadRequestBuilderStrategy);
    }

    @Test
    public void testLoadResource() {
        int resource = R.drawable.old_logo;

        sut.load(resource);
        sut.into(imageView);

        verify(downloadRequestBuilderStrategy, times(1)).load(eq(resource));
        verify(downloadRequestBuilderStrategy, times(1)).into(imageView);
    }

    @Test
    public void testLoadWithRemoteUrl() {
        String remoteUrl = String.format("https://res.cloudinary.com/%s/image/upload/%s", cloudName, TEST_PUBLIC_ID);

        sut.load(remoteUrl);
        sut.into(imageView);

        verify(downloadRequestBuilderStrategy, times(1)).load(eq(remoteUrl));
        verify(downloadRequestBuilderStrategy, times(1)).into(imageView);
    }

    @Test
    public void testLoadWithGeneratedCloudinaryUrlSource() {
        sut.load(TEST_PUBLIC_ID);
        sut.into(imageView);

        String expectedUrl = String.format("https://res.cloudinary.com/%s/image/upload/%s", cloudName, TEST_PUBLIC_ID);
        verify(downloadRequestBuilderStrategy, times(1)).load(eq(expectedUrl));
        verify(downloadRequestBuilderStrategy, times(1)).into(imageView);
    }

    @Test
    public void testLoadWithGeneratedCloudinaryUrlSourceWithTransformation() {
        sut.load(TEST_PUBLIC_ID);
        sut.transformation(new Transformation().width(200).height(400));
        sut.into(imageView);

        String expectedUrl = String.format("https://res.cloudinary.com/%s/image/upload/h_400,w_200/%s", cloudName, TEST_PUBLIC_ID);
        verify(downloadRequestBuilderStrategy, times(1)).load(eq(expectedUrl));
        verify(downloadRequestBuilderStrategy, times(1)).into(imageView);
    }

    @Test
    public void testLoadWithResponsive() {
        Context context = InstrumentationRegistry.getInstrumentation().getContext();
        LinearLayout linearLayout = new LinearLayout(context);
        ImageView imageView = new ImageView(context);
        int width = 200;
        int height = 400;
        linearLayout.layout(0, 0, width, height);
        imageView.layout(0, 0, width, height);
        linearLayout.addView(imageView);

        sut.load(TEST_PUBLIC_ID);
        sut.responsive(ResponsiveUrl.Preset.AUTO_FILL);
        sut.into(imageView);

        String expectedUrl = String.format("https://res.cloudinary.com/%s/image/upload/c_fill,g_auto,h_%d,w_%d/%s", cloudName, height, width, TEST_PUBLIC_ID);
        verify(downloadRequestBuilderStrategy, times(1)).load(eq(expectedUrl));
        verify(downloadRequestBuilderStrategy, times(1)).into(imageView);
    }

    @Test
    public void testRequestBuiltWithPlaceholder() {
        int placeholder = R.drawable.old_logo;

        sut.load(TEST_PUBLIC_ID);
        sut.placeholder(placeholder);
        sut.into(imageView);

        verify(downloadRequestBuilderStrategy, times(1)).placeholder(eq(placeholder));
        verify(downloadRequestBuilderStrategy, times(1)).into(imageView);
    }
}
