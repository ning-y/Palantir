package io.ningyuan.palantir.utils;

import android.util.Log;

public class Cpp {
    static {
        System.loadLibrary("cat");
    }

    public static native String cat(String filepath);
}
