package com.example.noratelproject2024.Controls;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Date;

public class Methods {
    public static File createOrGetDirectory(Context context) {
        File myDirectory = new File(context.getExternalFilesDir(null), "C3DSS");
        if (!myDirectory.exists()) {
            myDirectory.mkdirs();
        }
        return myDirectory;
    }

    public void saveToTextFile(Context context, String value, String fileName) {
        try {
            File file = new File(createOrGetDirectory(context), fileName);
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file, true)));
//            out.println(new Date());
            out.println(value);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
