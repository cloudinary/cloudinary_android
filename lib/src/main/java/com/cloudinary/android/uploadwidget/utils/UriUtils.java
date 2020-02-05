package com.cloudinary.android.uploadwidget.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import androidx.annotation.NonNull;
import android.webkit.MimeTypeMap;

public class UriUtils {

    /**
     * Get the media type of the Uri.
     * @param context Android context.
     * @param uri Uri of a media file.
     * @return The media type of the file.
     */
    public static MediaType getMediaType(Context context, @NonNull Uri uri) {
        String mimeType;
        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            ContentResolver cr = context.getApplicationContext().getContentResolver();
            mimeType = cr.getType(uri);
        } else {
            String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri
                    .toString());
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                    fileExtension.toLowerCase());
        }

        if (mimeType != null) {
            String type = mimeType.split("/")[0];
            switch (type) {
                case "image":
                    return MediaType.IMAGE;
                case "video":
                    return MediaType.VIDEO;
            }
        }

        return null;
    }

    /**
     * Get the first frame of a video
     * @param context Android context.
     * @param uri Uri of the video file.
     * @return First frame of the video.
     */
    public static Bitmap getVideoThumbnail(Context context, Uri uri) {
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(context, uri);

        return  mediaMetadataRetriever.getFrameAtTime(1); // TODO: Get first frame or the default?
    }

}
