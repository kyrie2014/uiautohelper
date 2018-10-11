package com.unisoc.bmte.uiautohelper;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.support.v4.provider.DocumentFile;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.unisoc.bmte.uiautohelper.Common.print;

/**
 * Created by kyrie.liu on 2018/9/6.
 */

public class FileUtils {
    public static String ACTION_OPEN_DOCUMENT_TREE_URL = "ACTION_OPEN_DOCUMENT_TREE";

    public static DocumentFile getDocumentFile(final File file, final boolean isDirectory, Context context) {
        String baseFolder = getExtSdCardFolder(file, context);
        boolean originalDirectory = false;
        if (baseFolder == null) {
            return null;
        }

        String relativePath = null;
        try {
            String fullPath = file.getCanonicalPath();
            if (!baseFolder.equals(fullPath))
                relativePath = fullPath.substring(baseFolder.length() + 1);
            else originalDirectory = true;
        } catch (IOException e) {
            return null;
        } catch (Exception f) {
            originalDirectory = true;
            //continue
        }
        //此处更换你的SharedPreferences
        SharedPreferences lee = context.getSharedPreferences("xxx", 0);
        String uri = lee.getString(ACTION_OPEN_DOCUMENT_TREE_URL, null);
        Uri parse = Uri.parse(uri);

        // start with root of SD card and then parse through document tree.
        DocumentFile document = DocumentFile.fromTreeUri(context, parse);
        if (originalDirectory) return document;
        String[] parts = relativePath.split("\\/");
        for (int i = 0; i < parts.length; i++) {
            DocumentFile nextDocument = document.findFile(parts[i]);
            if (nextDocument == null) {
                if ((i < parts.length - 1) || isDirectory) {
                    if (document.createDirectory(parts[i]) == null) {
                        return null;
                    }
                    nextDocument = document.createDirectory(parts[i]);
                } else {
                    nextDocument = document.createFile("image", parts[i]);
                }
            }
            document = nextDocument;
        }

        return document;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static String getExtSdCardFolder(final File file, Context context) {
        String[] extSdPaths = getExtSdCardPaths(context);
        try {
            for (int i = 0; i < extSdPaths.length; i++) {
                if (file.getCanonicalPath().startsWith(extSdPaths[i])) {
                    return extSdPaths[i];
                }
            }
        } catch (IOException e) {
            return null;
        }
        return null;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static String[] getExtSdCardPaths(Context context) {
        List<String> paths = new ArrayList<String>();
        for (File file : context.getExternalFilesDirs("external")) {
            if (file != null && !file.equals(context.getExternalFilesDir("external"))) {
                int index = file.getAbsolutePath().lastIndexOf("/Android/data");
                if (index < 0) {
                    print("AmazeFileUtils", "Unexpected external file dir: " + file.getAbsolutePath());
                } else {
                    String path = file.getAbsolutePath().substring(0, index);
                    try {
                        path = new File(path).getCanonicalPath();
                    } catch (IOException e) {
                        // Keep non-canonical path.
                    }
                    paths.add(path);
                }
            }
        }
        if (paths.isEmpty()) paths.add("/storage/sdcard0");
        return paths.toArray(new String[0]);
    }
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static boolean delete(File file, boolean isDir, Context mContext) {
        DocumentFile documentFile = getDocumentFile(file, isDir, mContext);
        if(documentFile!=null) {
            documentFile.delete();
        }else {
            //当更换SD卡后，uri值会发生变化，拿到的DocumentFile会为null，而此处在进行一次上面操作既可
        }
        return true;

    }
}
