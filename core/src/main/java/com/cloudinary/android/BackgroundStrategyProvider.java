package com.cloudinary.android;

class BackgroundStrategyProvider {
    static BackgroundRequestStrategy provideStrategy(){
        return new AndroidJobStrategy();
    }
}
