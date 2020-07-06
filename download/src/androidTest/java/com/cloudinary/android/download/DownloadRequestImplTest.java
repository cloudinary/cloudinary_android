package com.cloudinary.android.download;

import android.widget.ImageView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DownloadRequestImplTest {

    private static String url = "url";

    @Mock
    private ImageView imageView;
    @Mock
    private DownloadRequestBuilderStrategy downloadRequestBuilderStrategy;

    private DownloadRequestImpl sut;

    @Before
    public void setup() {
        sut = new DownloadRequestImpl(downloadRequestBuilderStrategy, imageView);
    }

    @Test
    public void testDownloadStartWithUrl() {
        sut.setSource(url);
        sut.start();

        Mockito.verify(downloadRequestBuilderStrategy, Mockito.times(1)).into(imageView);
    }

    @Test
    public void testDownloadStartRightAfterUrlIsSet() {
        sut.start();
        sut.setSource(url);

        Mockito.verify(downloadRequestBuilderStrategy, Mockito.times(1)).into(imageView);
    }

    @Test
    public void testDownloadDoesNotStartWithoutUrl() {
        sut.start();

        Mockito.verify(downloadRequestBuilderStrategy, Mockito.never()).into(imageView);
    }

    @Test
    public void testDownloadDoesNotStartIfCancelled() {
        sut.setSource(url);
        sut.cancel();
        sut.start();

        Mockito.verify(downloadRequestBuilderStrategy, Mockito.never()).into(imageView);
    }

    @Test
    public void testDownloadStartOnlyOnce() {
        DownloadRequestStrategy downloadRequestStrategy = Mockito.mock(DownloadRequestStrategy.class);
        Mockito.when(downloadRequestBuilderStrategy.into(imageView)).thenReturn(downloadRequestStrategy);

        sut.start();
        sut.setSource(url);
        sut.setSource(url);
        sut.start();

        Mockito.verify(downloadRequestBuilderStrategy, Mockito.times(1)).into(imageView);
    }

}
