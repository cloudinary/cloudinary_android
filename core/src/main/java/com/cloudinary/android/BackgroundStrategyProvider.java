package com.cloudinary.android;

final class BackgroundStrategyProvider {
    private BackgroundStrategyProvider() {}

    static BackgroundRequestStrategy provideStrategy(){
        return new AndroidJobStrategy();
    }
}
