package com.cloudinary.android.preprocess;

/**
 * Video transcoding parameters
 */
public class Parameters {
    private String requestId;
    private int width;
    private int height;
    private int targetVideoBitrateKbps;
    private int keyFramesInterval;
    private int frameRate;
    private int targetAudioBitrateKbps;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getTargetVideoBitrateKbps() {
        return targetVideoBitrateKbps;
    }

    public void setTargetVideoBitrateKbps(int targetVideoBitrateKbps) {
        this.targetVideoBitrateKbps = targetVideoBitrateKbps;
    }

    public int getKeyFramesInterval() {
        return keyFramesInterval;
    }

    public void setKeyFramesInterval(int keyFramesInterval) {
        this.keyFramesInterval = keyFramesInterval;
    }

    public int getFrameRate() {
        return frameRate;
    }

    public void setFrameRate(int frameRate) {
        this.frameRate = frameRate;
    }

    public int getTargetAudioBitrateKbps() {
        return targetAudioBitrateKbps;
    }

    public void setTargetAudioBitrateKbps(int targetAudioBitrateKbps) {
        this.targetAudioBitrateKbps = targetAudioBitrateKbps;
    }
}
