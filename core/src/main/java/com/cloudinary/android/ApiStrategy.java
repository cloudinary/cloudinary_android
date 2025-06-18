package com.cloudinary.android;

import java.util.Map;

import com.cloudinary.Api.HttpMethod;
import com.cloudinary.api.ApiResponse;
import com.cloudinary.strategies.AbstractApiStrategy;

public class ApiStrategy extends AbstractApiStrategy {
    @Override
    public ApiResponse callApi(HttpMethod method, String apiUrl, Map<String, ?> params, Map options, String authorizationHeader) throws Exception {
        throw new Exception("Administration API is not supported for mobile applications.");
    }

    @Override
    public ApiResponse callAccountApi(HttpMethod method, String apiUrl, Map<String, ?> params, Map options, String authorizationHeader) throws Exception {
        throw new Exception("Account API is not supported for mobile applications.");
    }
}
