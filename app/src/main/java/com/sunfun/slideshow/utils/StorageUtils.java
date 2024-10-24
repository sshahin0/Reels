package com.sunfun.slideshow.utils;

import android.os.Environment;
import android.util.Log;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

public class StorageUtils {
    public final File dataFolder = new File(Environment.getExternalStorageDirectory() + File.separator + ".VideoCropData");
    public final File mainFolder = new File(Environment.getExternalStorageDirectory() + File.separator + "VideoCrop");
    private static volatile StorageUtils StorageUtilsInstance;

    public void createFolder() {
        boolean success = true;
        if (!dataFolder.exists()) {
            success = dataFolder.mkdirs();
        } else {
            deleteDataFolder();
        }

        if (success) {
            Log.d("simul", "Folder creation success");
        } else {
            Log.d("simul", "Folder creation failed");
        }

        if (!mainFolder.exists()) {
            success = mainFolder.mkdirs();
        }

        if (success) {
            Log.d("simul", "Folder creation success");
        } else {
            Log.d("simul", "Folder creation failed");
        }
    }

    public void deleteDataFolder(){
        if (dataFolder.isDirectory())
        {
            String[] children = dataFolder.list();
            assert children != null;
            for (String child : children) {
                try{
                    new File(dataFolder, child).delete();
                } catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        }
    }

    public void moveFile(String inputPath, String outputPath) {
        InputStream in = null;
        OutputStream out = null;
        File inputFile = new File(inputPath);

        try {
            //create output directory if it doesn't exist
            in = new FileInputStream(inputPath);
            out = new FileOutputStream(outputPath);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;

            // write the output file
            out.flush();
            out.close();
            out = null;

        } catch (Exception fnfe1) {
            Log.e("tag", Objects.requireNonNull(fnfe1.getMessage()));
        }

    }

    StorageUtils(){
        if (StorageUtilsInstance != null){
            throw new RuntimeException("Use getInstance() method to get the single instance of this class.");
        }
    }

    public static StorageUtils getInstance() {
        if (StorageUtilsInstance == null) { //if there is no instance available... create new one
            synchronized (VideoInfo.class) {
                if (StorageUtilsInstance == null) StorageUtilsInstance = new StorageUtils();
            }
        }
        return StorageUtilsInstance;
    }

    public void deleteVideo(String path) {
        File file = new File(path);
        if(file.exists())file.delete();
    }
}
