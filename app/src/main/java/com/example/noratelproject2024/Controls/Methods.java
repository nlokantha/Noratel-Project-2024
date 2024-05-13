package com.example.noratelproject2024.Controls;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class Methods {
    public static File createOrGetDirectory(Context context) {
        File myDirectory = new File(context.getExternalFilesDir(null), "C3DSS");

        if (!myDirectory.exists()) {
            myDirectory.mkdirs();
        }
        return myDirectory;
    }

}
