package com.cloudinary.android;

public class BackgroundStrategyProvider {
    static BackgroundRequestStrategy provideStrategy(){
        return new AndroidJobStrategy();
    }
}
