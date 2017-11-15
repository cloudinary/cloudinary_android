package com.cloudinary.android.payload;

import android.content.Context;

import com.cloudinary.utils.Base64Coder;

import java.io.File;

/**
 * This class is used to handle uploading of images/videos represented as a {@link java.io.File}
 */
public class FilePayload extends Payload<String> {
    static final String URI_KEY = "file";

    public FilePayload(String filePath) {
        super(filePath);
    }

    public FilePayload() {
    }

    @Override
    public long getLength(Context context) throws PayloadNotFoundException {
        return getFile(context).length();
    }

    @Override
    public Object prepare(Context context) throws PayloadNotFoundException {
        return getFile(context);
    }

    private File getFile(Context context) throws FileNotFoundException {
        File file = new File(data);

        if (!file.exists()) {

            // check if it's a local app file:
            file = context.getFileStreamPath(data);
            if (!file.exists()) {
                throw new FileNotFoundException(String.format("File '%s' does not exist", data));
            }
        }

        return file;
    }

    @Override
    public String toUri() {
        return URI_KEY + "://" + Base64Coder.encodeString(data);
    }

    @Override
    void loadData(String encodedData) {
        data = Base64Coder.decodeString(encodedData);
    }
}
