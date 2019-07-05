package ir.gfpishro.geosuiteandroidprivateusers.Helpers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Formatter;

import ir.gfpishro.geosuiteandroidprivateusers.Models.Keys;

public class FileUtils {
    private static void writeFile(File file, String data) throws Exception {
//        try (FileOutputStream stream = new FileOutputStream(file)) { stream.write(data.getBytes()); }
        try (Writer out = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(file), StandardCharsets.UTF_8))) {
            out.write(data);
        }
    }

    private static String readFile(File file) throws Exception {
        int length = (int) file.length();
        byte[] bytes = new byte[length];
        try (FileInputStream in = new FileInputStream(file)) {
            in.read(bytes);
        }
        return new String(bytes);
    }

//    public static boolean deleteFile(File file) throws SecurityException => file.delete();

    @SuppressLint("DefaultLocale")
    public static void writeInsideCSV(File file, int key, String value) throws Exception {
//        value = value.replaceAll(",", "");
        String fileContent = "";
        try {
            fileContent = readFile(file);
        } catch (Exception ignored) {
        }
        int index = fileContent.indexOf(key + ",");
        if (index != -1) {
            int lastIndex = fileContent.indexOf("\n", index);
            lastIndex = lastIndex == -1 ? fileContent.length() - 1 : lastIndex + 1;
            String rp = fileContent.substring(index, lastIndex);
            fileContent = fileContent.replace(rp, "");
        }
        fileContent += String.format("%d, %s\n", key, value);
        writeFile(file, fileContent);
    }

    public static String loadLineInMapCodeCsv(File file, int key) {
        try {
            String fileContent = FileUtils.readFile(file);
            int index = fileContent.indexOf(key + ",");
            if (index == -1) throw new Exception();
            int lastIndex = fileContent.indexOf("\n", index);
            if (lastIndex == -1) lastIndex = fileContent.length() - 1;
            return fileContent.substring(index, lastIndex);
        } catch (Exception e) {
            return "";
        }
    }

    public static File getMapCodeFile(int id, String type) {
        String directory = Environment.getExternalStorageDirectory() + Keys.mapsFolder + Keys.mapCodeFolder;
        getFolderCreateIfNotExist(directory);
        return new File(directory, Keys.mapCodeCsvFileName(id, type));
    }

    public static boolean doesExistsAndBiggerThan(File file, int megaBytes) {
        return file.exists() && file.length() > megaBytes * Math.pow(10, 6);
    }

    public static File getFolderCreateIfNotExist(String path) {
        File folder = new File(path);
        if (!folder.exists()) folder.mkdirs();
        return folder;
    }

    public static void openVideoFile(final Context context, final Uri videoFile) {
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
        intent.setDataAndType(videoFile, "video/*");
        context.startActivity(intent);
    }

    public static String fileSha1(final File file) throws Exception {
        final MessageDigest messageDigest = MessageDigest.getInstance("SHA1");
        try (InputStream is = new BufferedInputStream(new FileInputStream(file))) {
            final byte[] buffer = new byte[1024];
            for (int read; (read = is.read(buffer)) != -1; ) messageDigest.update(buffer, 0, read);
        }
        // Convert the byte to hex format
        try (Formatter formatter = new Formatter()) {
            for (final byte b : messageDigest.digest()) {
                formatter.format("%02x", b);
            }
            return formatter.toString();
        }
    }
}
