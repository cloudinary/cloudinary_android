package com.cloudinary.android;

/**
 * Entry point to dispatch new upload requests.
 */
interface RequestDispatcher {
    /**
     * Dispatch a new upload request. Will get queued and handled based on network and phone policy and time constraints.
     */
    String dispatch(UploadRequest request);
}
