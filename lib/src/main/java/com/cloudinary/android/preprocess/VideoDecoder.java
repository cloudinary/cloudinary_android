package com.cloudinary.android.preprocess;

import android.content.Context;
import android.net.Uri;

import com.cloudinary.android.payload.FilePayload;
import com.cloudinary.android.payload.LocalUriPayload;
import com.cloudinary.android.payload.Payload;
import com.cloudinary.android.payload.PayloadNotFoundException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Returns the decoded video uri from a given payload. Payloads must be either {@link LocalUriPayload} or {@link FilePayload}.
 * Note: It doesn't do the actual decoding process.
 */
public class VideoDecoder implements ResourceDecoder<Uri> {

    /**
     * Returns the video uri.
     *
     * @param context Android context.
     * @param payload Payload to extract the resource from
     * @throws PayloadDecodeException if the payload is neither a {@link LocalUriPayload} nor {@link FilePayload}
     * @throws PayloadNotFoundException if the payload's resource cannot be found.
     */
    @Override
    public Uri decode(Context context, Payload payload) throws PayloadDecodeException, PayloadNotFoundException {
        File file;
        if (payload instanceof LocalUriPayload) {
            file = new File(context.getFilesDir() + "/tempFile_" + System.currentTimeMillis());
            InputStream resource = (InputStream) payload.prepare(context);
            OutputStream output = null;
            try {
                output = context.openFileOutput(file.getName(), Context.MODE_PRIVATE);
                byte[] buffer = new byte[4 * 1024]; // or other buffer size
                int read;

                while ((read = resource.read(buffer)) != -1) {
                    output.write(buffer, 0, read);
                }

                output.flush();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            finally {
                try {
                    resource.close();
                    if (output != null) {
                        output.close();
                    }
                } catch (IOException ignored) {
                }
            }
        } else if (payload instanceof FilePayload) {
            file = (File) payload.prepare(context);
        } else {
            throw new PayloadDecodeException();
        }

        return Uri.fromFile(file);
    }

}
