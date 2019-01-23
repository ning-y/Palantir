package io.ningyuan.palantir.utils;

public class Cpp {
    static {
        System.loadLibrary("cat");
    }

    public native String cat(String filepath);
}
