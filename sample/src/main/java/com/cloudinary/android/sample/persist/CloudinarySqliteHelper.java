package com.cloudinary.android.sample.persist;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.Nullable;

import com.cloudinary.android.Logger;
import com.cloudinary.android.sample.model.Resource;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CloudinarySqliteHelper extends SQLiteOpenHelper {
    public static final String ID_COL = "ID";
    public static final int VERSION = 2;
    private static final String TABLE = "images";
    private static final String LOCAL_ID_COL = "localId";
    private static final String PUBLIC_ID_COL = "publicId";
    private static final String RESOURCE_TYPE_COL = "resourceType";
    private static final String REQUEST_ID_COL = "requestId";
    private static final String STATUS_TIMESTAMP_COL = "statusTimestamp";
    private static final String DELETE_TOKEN_COL = "deleteToken";
    private static final String LAST_ERROR_COL = "lastError";
    private static final String LAST_ERROR_DESC_COL = "lastErrorDesc";
    private static final String STATUS_COL = "status";
    private static final String NAME_COL = "name";
    private static final String TAG = CloudinarySqliteHelper.class.getSimpleName();

    public CloudinarySqliteHelper(Context context) {
        super(context, "cloudinary", null, VERSION);
    }

    public static String makeInQueryString(int size) {
        StringBuilder sb = new StringBuilder();
        if (size > 0) {
            sb.append(" IN ( ");
            String placeHolder = "";
            for (int i = 0; i < size; i++) {
                sb.append(placeHolder);
                sb.append("?");
                placeHolder = ",";
            }
            sb.append(" )");
        }
        return sb.toString();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE + " ("
                + ID_COL + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + LOCAL_ID_COL + " TEXT, "
                + PUBLIC_ID_COL + " TEXT,"
                + REQUEST_ID_COL + " TEXT,"
                + RESOURCE_TYPE_COL + " TEXT,"
                + STATUS_COL + " TEXT,"
                + LAST_ERROR_COL + " INTEGER,"
                + LAST_ERROR_DESC_COL + " TEXT, "
                + STATUS_TIMESTAMP_COL + " INTEGER,"
                + NAME_COL + " TEXT,"
                + DELETE_TOKEN_COL + " TEXT);"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        switch (newVersion) {
            case 2:
                db.execSQL("ALTER TABLE " + TABLE + " ADD COLUMN " + NAME_COL + " TEXT;");
        }
    }

    public int setUploadResultParams(String requestId, String publicId, String deleteToken, Resource.UploadStatus status, int lastError, String lastErrorDesc) {
        ContentValues values = new ContentValues();
        values.put(PUBLIC_ID_COL, publicId);
        values.put(DELETE_TOKEN_COL, deleteToken);
        values.put(STATUS_TIMESTAMP_COL, new Date().getTime());
        values.put(STATUS_COL, status.name());
        values.put(LAST_ERROR_COL, lastError);
        values.put(LAST_ERROR_DESC_COL, lastErrorDesc);
        return getWritableDatabase().update(TABLE, values, REQUEST_ID_COL + "=?", new String[]{requestId});
    }

    public boolean insertOrUpdateQueuedResource(String localId, String name, String requestId, String resourceType, Resource.UploadStatus status) {
        ContentValues values = new ContentValues();

        values.put(REQUEST_ID_COL, requestId);
        values.put(NAME_COL, name);
        values.put(RESOURCE_TYPE_COL, resourceType);
        values.put(STATUS_COL, status.name());
        values.put(STATUS_TIMESTAMP_COL, new Date().getTime());

        boolean exists = exists(localId);
        if (exists) {
            int updated = getWritableDatabase().update(TABLE, values, LOCAL_ID_COL + "=?", new String[]{localId});
            Logger.d(TAG, String.format("Setting request id %s for local id %s, updated rows: %d", requestId, localId, updated));
            exists = true;
        } else {
            values.put(LOCAL_ID_COL, localId);
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

    public List<Resource> listAll() {
        Cursor cursor = getReadableDatabase().query(TABLE, null, null, null, null, null, ID_COL);
        List<Resource> res = new ArrayList<>();
        try {
            buildResource(cursor, res);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return res;
    }

    private void buildResource(Cursor cursor, List<Resource> res) {
        if (cursor.moveToFirst()) {
            int localIdIdx = cursor.getColumnIndex(LOCAL_ID_COL);
            int remoteIdIdx = cursor.getColumnIndex(PUBLIC_ID_COL);
            int requestIdIdx = cursor.getColumnIndex(REQUEST_ID_COL);
            int timestampIdx = cursor.getColumnIndex(STATUS_TIMESTAMP_COL);
            int deleteTokenIdx = cursor.getColumnIndex(DELETE_TOKEN_COL);
            int resourceTypeIdx = cursor.getColumnIndex(RESOURCE_TYPE_COL);
            int statusIdx = cursor.getColumnIndex(STATUS_COL);
            int errorIdx = cursor.getColumnIndex(LAST_ERROR_COL);
            int errorDescIdx = cursor.getColumnIndex(LAST_ERROR_DESC_COL);
            int nameIdx = cursor.getColumnIndex(NAME_COL);

            do {
                Resource resource = new Resource();
                resource.setLocalUri(cursor.getString(localIdIdx));
                resource.setCloudinaryPublicId(cursor.getString(remoteIdIdx));
                resource.setRequestId(cursor.getString(requestIdIdx));
                resource.setDeleteToken(cursor.getString(deleteTokenIdx));
                resource.setStatusTimestamp(cursor.isNull(timestampIdx) ? null : new Date(cursor.getLong(timestampIdx)));
                resource.setResourceType(cursor.getString(resourceTypeIdx));
                resource.setStatus(Resource.UploadStatus.valueOf(cursor.getString(statusIdx)));
                resource.setLastError(cursor.getInt(errorIdx));
                resource.setLastErrorDesc(cursor.getString(errorDescIdx));
                resource.setName(cursor.getString(nameIdx));
                res.add(resource);
            } while (cursor.moveToNext());
        }
    }

    public void deleteAllImages() {
        getWritableDatabase().execSQL("DELETE FROM " + TABLE);
    }

    public void delete(String localId) {
        getWritableDatabase().execSQL("DELETE FROM " + TABLE + " WHERE " + LOCAL_ID_COL + "=?", new Object[]{localId});
    }

    public String getLocalUri(String requestId) {
        Cursor cursor = getReadableDatabase().query(TABLE, null, REQUEST_ID_COL + "=?", new String[]{requestId}, null, null, null);
        if (cursor.moveToFirst()) {
            return cursor.getString(cursor.getColumnIndex(LOCAL_ID_COL));
        }

        return null;
    }

    public Resource findByUri(String uri) {
        return findResource(LOCAL_ID_COL, uri);
    }

    @Nullable
    private Resource findResource(String colToSearch, String searchTerm) {
        Cursor cursor = getReadableDatabase().query(TABLE, null, colToSearch + "=?", new String[]{searchTerm}, null, null, null);
        ArrayList<Resource> res = new ArrayList<>(1);
        buildResource(cursor, res);
        return res.size() > 0 ? res.get(0) : null;
    }

    public Resource findByRequestId(String requestId) {
        return findResource(REQUEST_ID_COL, requestId);
    }

    public List<Resource> list(String[] statuses) {
        Cursor cursor = getReadableDatabase().query(TABLE, null, STATUS_COL + makeInQueryString(statuses.length), statuses, null, null, ID_COL);
        List<Resource> res = new ArrayList<>();
        try {
            buildResource(cursor, res);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return res;
    }

    public List<Resource> listAllUploadedAfter(long cutoff) {
        Cursor cursor = getReadableDatabase().query(TABLE, null, STATUS_COL + "=? AND " + STATUS_TIMESTAMP_COL + ">?", new String[]{String.valueOf(Resource.UploadStatus.UPLOADED), String.valueOf(cutoff)}, null, null, ID_COL);
        List<Resource> res = new ArrayList<>();
        try {
            buildResource(cursor, res);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return res;
    }
}
