package com.honeybilly.opencvdemo.utils;

/**
 * Created by liqi on 10:34.
 */
public class LocalProcesser {

    private static volatile LocalProcesser instance;

    private LocalProcesser() {
        System.loadLibrary("local-processer");
    }

    public static LocalProcesser getInstance() {
        if (instance == null) {
            synchronized (LocalProcesser.class) {
                if (instance == null) {
                    instance = new LocalProcesser();
                }
            }
        }
        return instance;
    }

    public native void faceDetection(long frameAddress);

    public native boolean initLoad(String haarFilePath);
}
