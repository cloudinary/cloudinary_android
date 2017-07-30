package com.cloudinary.android.payload;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

import com.cloudinary.utils.Base64Coder;

import java.io.FileNotFoundException;

public class LocalUriPayload extends Payload<Uri> {
    public static final String[] PROJECTION = {OpenableColumns.SIZE};
    static final String URI_KEY = "uri";

    public LocalUriPayload(Uri data) {
        super(data);
    }

    public LocalUriPayload(){
    }

    @Override
    public String toUri() {
        return URI_KEY + "://" + Base64Coder.encodeString(data.toString());
    }

    @Override
    void loadData(String encodedData) {
        data = Uri.parse(Base64Coder.decodeString(encodedData));
    }

    @Override
    public long getLength(Context context) {
        return fetchFileSizeFromUri(context);
    }

    @Override
    public Object prepare(Context context) throws PayloadNotFoundException {
        try {
            return context.getContentResolver().openInputStream(data);
        } catch (FileNotFoundException e) {
            throw new LocalUriNotFoundException(String.format("Uri %s could not be found", data.toString()));
        }
    }

    private long fetchFileSizeFromUri(Context context) {
        Cursor returnCursor = null;
        long size = 0;

        try {
            returnCursor = context.getContentResolver().query(data, PROJECTION, null, null, null);
            if (returnCursor != null && returnCursor.moveToNext()) {
                int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
                size = returnCursor.getLong(sizeIndex);
            }
        } finally {
            if (returnCursor != null) {
                returnCursor.close();
            }
        }

        return size;
    }
}
