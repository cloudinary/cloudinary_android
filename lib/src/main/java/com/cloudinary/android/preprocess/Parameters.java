package com.cloudinary.android.preprocess;

/**
 * Video transcoding parameters
 */
public class Parameters {
    public String requestId;
    public String targetFilePath;
    public int width;
    public int height;
    public int targetVideoBitrateKbps;
    public int keyFramesInterval;
    public int frameRate;
    public int targetAudioBitrateKbps;
}
