package com.mapprr.githubsearch.utils;

import java.io.IOException;

public class ConnectionUtils {

    public static boolean isConnected() {
        String command = "ping -c 1 google.com";
        try {
            return (Runtime.getRuntime().exec(command).waitFor() == 0);
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
            return false;
        }
    }

}
