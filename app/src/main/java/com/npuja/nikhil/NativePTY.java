package com.npuja.nikhil;


public class NativePTY {
    static {
        System.loadLibrary("mylib");
    }

    public native int startShell(String shellPath);
    public native int writeToShell(String input);
    public native String readFromShell();
}