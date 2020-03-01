package com.cloudinary.android;

import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;

import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

@RunWith(AndroidJUnit4ClassRunner.class)
public class UtilsTest {
    @SuppressWarnings("unchecked")
    @Test
    public void testOptionsSerialization() throws IOException, ClassNotFoundException {
        Map<String, Object> options = new HashMap<>();
        options.put("integer", 12);
        options.put("string", "twelve");
        options.put("transformation", new Transformation().angle(30));
        options.put("array", new int[]{1, 2, 3});
        Map<String, Object> newMap = (Map<String, Object>) ObjectUtils.deserialize(ObjectUtils.serialize(options));

        assertEquals(12, newMap.get("integer"));
        assertEquals("twelve", newMap.get("string"));
        Assert.assertEquals(new Transformation().angle(30).generate(), ((Transformation) newMap.get("transformation")).generate());
        assertTrue(Arrays.equals(new int[]{1, 2, 3}, (int[]) newMap.get("array")));
    }
}
