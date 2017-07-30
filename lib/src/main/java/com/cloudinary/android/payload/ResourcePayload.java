package com.cloudinary.android.payload;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;

import java.io.IOException;

/**
 * This class is used to handle uploading of images/videos represented as an Android raw resource id
 */
public class ResourcePayload extends Payload<Integer> {
    static final String URI_KEY = "resource";

    public ResourcePayload(Integer rawResourceId) {
        super(rawResourceId);
    }

    public ResourcePayload(){
    }

    @Override
    public String toUri() {
        return URI_KEY + "://" + data;
    }

    @Override
    void loadData(String encodedData) {
        data = Integer.parseInt(encodedData);
    }

    @Override
    public long getLength(Context context) throws NotFoundException {
        AssetFileDescriptor afd = null;
        long size = -1;
        try {
            afd = context.getResources().openRawResourceFd(data);
            size = afd.getLength();
        }catch (Resources.NotFoundException e){
            throw new NotFoundException("Resource not found");
        } finally {
            if (afd != null) {
                try {
                    afd.close();
                } catch (IOException ignored) {
                }
            }
        }

        return size;
    }

    @Override
    public Object prepare(Context context) throws NotFoundException{
        try {
            return context.getResources().openRawResource(data);
        } catch (Resources.NotFoundException e){
            throw new Resources.NotFoundException(String.format("Resource with id %s does not exists.", data));
        }
    }
}
