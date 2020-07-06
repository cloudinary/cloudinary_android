package cloudinary.android.sample;

import android.content.Context;
import android.net.Uri;

import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.platform.app.InstrumentationRegistry;

import com.cloudinary.android.sample.R;
import com.cloudinary.android.sample.app.MainActivity;
import com.cloudinary.android.uploadwidget.UploadWidget;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

public class UploadWidgetTest {

    private static File assetFile;
    private static final String TEST_IMAGE = "image.png";

    @BeforeClass
    public static void setup() throws IOException {
        assetFile = assetToFile(TEST_IMAGE);
    }

    @Rule
    public IntentsTestRule<MainActivity> intentsTestRule = new IntentsTestRule<>(MainActivity.class);

    // TODO: Fix UI tests for travis
    @Ignore
    @Test
    public void testUploadWidget() {
        UploadWidget.startActivity(intentsTestRule.getActivity(),
                MainActivity.UPLOAD_WIDGET_REQUEST_CODE,
                new UploadWidget.Options(UploadWidget.Action.START_NOW,
                        Collections.singletonList(Uri.fromFile(assetFile))));
        onView(withId(R.id.crop_action)).perform(click());
        onView(withId(R.id.doneButton)).perform(click());
        onView(withId(R.id.uploadFab)).perform(click());
    }

    private static File assetToFile(String testImage) throws IOException {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        File file = new File(context.getCacheDir() + "/tempFile_" + System.currentTimeMillis());

        file.createNewFile();
        FileOutputStream fos = new FileOutputStream(file);
        InputStream is = getAssetStream(testImage);

        byte[] buffer = new byte[16276];
        int bytesRead;
        while ((bytesRead = is.read(buffer)) != -1) {
            fos.write(buffer, 0, bytesRead);
        }

        fos.flush();
        is.close();

        return file;
    }

    private static InputStream getAssetStream(String filename) throws IOException {
        return InstrumentationRegistry.getInstrumentation().getContext().getAssets().open(filename);
    }
}