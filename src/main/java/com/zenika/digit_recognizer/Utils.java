package com.zenika.digit_recognizer;

public class Utils {

    public static final int NB_PIXELS = 784;
    public static final int NB_DIGITS = 10;
    public static final int IMAGE_SIZE = 28;

    private static int imgNb = 0;

    public static int getImgNb() {
        return imgNb++;
    }

}
