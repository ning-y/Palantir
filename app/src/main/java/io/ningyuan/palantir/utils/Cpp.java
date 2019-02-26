package io.ningyuan.palantir.utils;

public class Cpp {
    static {
        System.loadLibrary("cat");
    }

    public static native String cat(String filepath);
}
