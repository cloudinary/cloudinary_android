package com.cloudinary.android.sample.persist;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.cloudinary.android.Logger;
import com.cloudinary.android.sample.model.Image;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CloudinarySqliteHelper extends SQLiteOpenHelper {
    private static final String TABLE = "images";
    private static final String LOCAL_ID_COL = "localId";
    private static final String PUBLIC_ID_COL = "publicId";
    private static final String WIDTH_COL = "width";
    private static final String HEIGHT_COL = "height";
    private static final String REQUEST_ID_COL = "requestId";
    private static final String INSERT_TIMESTAMP = "insertTimestamp";
    private static final String UPLOAD_TIMESTAMP_COL = "uploadTimestamp";
    private static final String DELETE_TOKEN_COL = "deleteToken";
    private static final String TAG = CloudinarySqliteHelper.class.getSimpleName();

    public CloudinarySqliteHelper(Context context) {
        super(context, "cloudinary", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE + " ("
                + LOCAL_ID_COL + " TEXT, "
                + PUBLIC_ID_COL + " TEXT,"
                + REQUEST_ID_COL + " TEXT,"
                + WIDTH_COL + " INTEGER,"
                + HEIGHT_COL + " INTEGER,"
                + INSERT_TIMESTAMP + " INTEGER,"
                + UPLOAD_TIMESTAMP_COL + " INTEGER,"
                + DELETE_TOKEN_COL + " TEXT);"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public int setUploadResultParams(String requestId, String publicId, int width, int height, String deleteToken, Date timestamp) {
        ContentValues values = new ContentValues();
        values.put(PUBLIC_ID_COL, publicId);
        values.put(DELETE_TOKEN_COL, deleteToken);
        values.put(UPLOAD_TIMESTAMP_COL, timestamp.getTime());
        values.put(WIDTH_COL, width);
        values.put(HEIGHT_COL, height);
        return getWritableDatabase().update(TABLE, values, REQUEST_ID_COL + "=?", new String[]{requestId});
    }

    public boolean insertNewImage(String localId, String requestId) {
        boolean exists = false;
        ContentValues values = new ContentValues();
        values.put(REQUEST_ID_COL, requestId);
        if (exists(localId)) {
            int updated = getWritableDatabase().update(TABLE, values, LOCAL_ID_COL + "=?", new String[]{localId});
            Logger.d(TAG, String.format("Setting request id %s for local id %s, updated rows: %d", requestId, localId, updated));
            exists = true;
        } else {
            values.put(LOCAL_ID_COL, localId);
            values.put(INSERT_TIMESTAMP, new Date().getTime());
            getWritableDatabase().insert(TABLE, null, values);
        }

        return exists;
    }

    public boolean exists(String localId) {
        Cursor query = getReadableDatabase().query(TABLE, null, LOCAL_ID_COL + "=?", new String[]{localId}, null, null, null);
        boolean exists = query.moveToFirst();
        query.close();
        return exists;
    }

    public List<Image> readImages() {
        Cursor cursor = getReadableDatabase().query(TABLE, null, null, null, null, null, INSERT_TIMESTAMP + " desc");
        List<Image> res = new ArrayList<>();
        try {
            if (cursor.moveToFirst()) {
                int localIdIdx = cursor.getColumnIndex(LOCAL_ID_COL);
                int remoteIdIdx = cursor.getColumnIndex(PUBLIC_ID_COL);
                int requestIdIdx = cursor.getColumnIndex(REQUEST_ID_COL);
                int timestampIdx = cursor.getColumnIndex(UPLOAD_TIMESTAMP_COL);
                int deleteTokenIdx = cursor.getColumnIndex(DELETE_TOKEN_COL);
                int widthIdx = cursor.getColumnIndex(WIDTH_COL);
                int heightIdx = cursor.getColumnIndex(HEIGHT_COL);

                do {
                    Image image = new Image();
                    image.setLocalUri(cursor.getString(localIdIdx));
                    image.setCloudinaryPublicId(cursor.getString(remoteIdIdx));
                    image.setRequestId(cursor.getString(requestIdIdx));
                    image.setDeleteToken(cursor.getString(deleteTokenIdx));
                    image.setUploadTimestamp(new Date(cursor.getInt(timestampIdx)));
                    image.setWidth(cursor.getInt(widthIdx));
                    image.setHeight(cursor.getInt(heightIdx));
                    res.add(image);
                } while (cursor.moveToNext());

            }
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return res;
    }

    public void deleteAllImages() {
        getWritableDatabase().execSQL("DELETE FROM " + TABLE);
    }
}
