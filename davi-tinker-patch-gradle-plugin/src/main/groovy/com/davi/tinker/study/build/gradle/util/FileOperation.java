
package com.davi.tinker.study.build.gradle.util;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileOperation {

    public static final boolean deleteFile(String filePath) {
        if (filePath == null) {
            return true;
        }

        File file = new File(filePath);
        if (file.exists()) {
            return file.delete();
        }
        return true;
    }

    public static final boolean deleteFile(File file) {
        if (file == null) {
            return true;
        }
        if (file.exists()) {
            return file.delete();
        }
        return true;
    }

    /**
     * 是文件或者dir
     */
    public static boolean isLegalFileOrDirectory(String path) {
        if (isLegalFile(path)) {
            return true;
        }
        if (path == null) {
            return false;
        }
        final File file = new File(path);
        return file.exists() && file.isDirectory() && file.canRead();
    }

    /**
     * 是文件
     */
    public static boolean isLegalFile(String path) {
        if (path == null) {
            return false;
        }
        File file = new File(path);
        return file.exists() && file.isFile() && file.length() > 0;
    }

    public static void copyFileUsingStream(File source, File dest) throws IOException {
        FileInputStream is = null;
        FileOutputStream os = null;
        File parent = dest.getParentFile();
        if (parent != null && (!parent.exists())) {
            parent.mkdirs();
        }
        try {
            is = new FileInputStream(source);
            os = new FileOutputStream(dest, false);

            byte[] buffer = new byte[TypedValue.BUFFER_SIZE];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        } finally {
            IOHelper.closeQuietly(os);
            IOHelper.closeQuietly(is);
        }
    }

}
