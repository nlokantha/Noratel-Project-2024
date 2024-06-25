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
import java.text.SimpleDateFormat;
import java.util.Date;

public class Methods {
    public static File createOrGetDirectory(Context context) {
        File myDirectory = new File(context.getExternalFilesDir(null), "C3DSS");
        if (!myDirectory.exists()) {
            myDirectory.mkdirs();
        }
        return myDirectory;
    }

    public void saveToTextFile(Context context, String value) {
        try {

            deleteOldFiles(context);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            String currentDate = sdf.format(new Date());
            String fileName = currentDate + ".txt";

            File file = new File(createOrGetDirectory(context), fileName);
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file, true)));
//            out.println(new Date());
            out.println(value);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void deleteOldFiles(Context context) {
        File directory = createOrGetDirectory(context);
        File[] files = directory.listFiles();
        if (files != null) {
            long currentTime = System.currentTimeMillis();
            long oneMonthInMillis = 30L * 24 * 60 * 60 * 1000;

            for (File file : files) {
                if (currentTime - file.lastModified() > oneMonthInMillis) {
                    file.delete();
                }
            }
        }
    }
}
