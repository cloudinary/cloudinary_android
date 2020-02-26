package com.cloudinary.android;

import com.cloudinary.Cloudinary;
import com.cloudinary.ProgressCallback;
import com.cloudinary.strategies.AbstractUploaderStrategy;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Map;

public class TestUtils {
    public static AbstractUploaderStrategy replaceWithTimeoutStrategy(Cloudinary cloudinary) throws NoSuchFieldException, IllegalAccessException, IOException {
        return TestUtils.replaceStrategyForIntsance(cloudinary, new AbstractUploaderStrategy() {

            @Override
            public Map callApi(String action, Map<String, Object> params, Map options, Object file, ProgressCallback progressCallback) throws IOException {
                throw new IOException();
            }
        });
    }

    public static AbstractUploaderStrategy replaceStrategyForIntsance(Cloudinary cld, AbstractUploaderStrategy replacement) throws NoSuchFieldException, IllegalAccessException {
        Field uploaderStrategy = Cloudinary.class.getDeclaredField("uploaderStrategy");
        uploaderStrategy.setAccessible(true);
        AbstractUploaderStrategy prev = (AbstractUploaderStrategy) uploaderStrategy.get(cld);
        uploaderStrategy.set(cld, replacement);
        return prev;
    }
}
