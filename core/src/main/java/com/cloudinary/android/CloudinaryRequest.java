package com.cloudinary.android;

import com.cloudinary.Transformation;

/**
 * A class that is used for generating a cloudinary resource when creating an image request.
 */
public class CloudinaryRequest {

    private String publicId;
    private Transformation transformation;
    private ResponsiveUrl responsive;

    private CloudinaryRequest(String publicId, Transformation transformation, ResponsiveUrl responsive) {
        this.publicId = publicId;
        this.transformation = transformation;
        this.responsive = responsive;
    }

    /**
     * Get the public id of the cloudinary resource
     */
    public String getPublicId() {
        return publicId;
    }

    /**
     * Get the transformation
     */
    public Transformation getTransformation() {
        return transformation;
    }

    /**
     * Get the responsive preset set for this resource.
     */
    public ResponsiveUrl getResponsive() {
        return responsive;
    }

    /**
     * Builder to construct an instance of {@link CloudinaryRequest}.
     */
    public static class Builder {

        private final String publicId;
        private Transformation transformation;
        private ResponsiveUrl responsive;

        public Builder(String publicId) {
            this.publicId = publicId;
        }

        /**
         * Set a transformation to be used when generating the resource.
         */
        public Builder transformation(Transformation transformation) {
            this.transformation = transformation;
            return this;
        }

        /**
         * Set a responsive url to be used when generating the resource.
         */
        public Builder responsive(ResponsiveUrl responsiveUrl) {
            this.responsive = responsiveUrl;
            return this;
        }

        /**
         * Set a responsive preset to be used when generating the resource.
         */
        public Builder responsive(ResponsiveUrl.Preset responsivePreset) {
            this.responsive = MediaManager.get().responsiveUrl(responsivePreset);
            return this;
        }

        /**
         * @return An instance of {@link CloudinaryRequest}.
         */
        public CloudinaryRequest build() {
            return new CloudinaryRequest(publicId, transformation, responsive);
        }
    }
}