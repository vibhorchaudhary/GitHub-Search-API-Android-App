package com.mapprr.githubsearch.utils;

import android.content.Context;
import android.widget.Toast;

public class SingleToast {

    private static Toast singleToast = null;

    public static void showToast(Context context, String text, int duration) {

        if (singleToast != null)
            singleToast.cancel(); // override current Toast, mate!

        singleToast = Toast.makeText(context, text, duration);
        singleToast.show();
    }


}

