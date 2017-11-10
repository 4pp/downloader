package com.zsp.filedownloader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by zsp on 2017/11/7.
 */

public class Utils {

    /**
     * 递增序号的新文件名
     *
     * @param count
     * @param filename
     * @return
     */
    public static String createIncreaseFilename(int count, String filename) {

        String name = filename;
        if (count > 1) {
            String sn = "(" + count + ")";
            int i = name.lastIndexOf(".");
            if (i < 0) {
                name += sn;
            } else {
                name = name.substring(0, i) + sn + name.substring(i);
            }
        }
        return name;
    }

    public static File unzip(String zipFileName)
            throws Exception {
        ZipInputStream in = new ZipInputStream(new FileInputStream(zipFileName));
        ZipEntry z;
        String name = "";
        String path = zipFileName.substring(0,zipFileName.lastIndexOf("/"));
        File unzipFile = null;

        while ((z = in.getNextEntry()) != null) {
            name = z.getName();
            Debug.log("mkdir unzipping file: " + name);
            if (z.isDirectory()) {
                // TODO: 2017/11/8 解压目录
//                Debug.log(name + "is a folder");
//                name = name.substring(0, name.length() - 1);
//                File folder = new File(outputDirectory + File.separator + name);
//                folder.mkdirs();
//                counter++;
//                Debug.log("mkdir " + outputDirectory + File.separator + name);
            } else {
                Debug.log(name + "is a normal file");
                //File file = new File(outputDirectory + File.separator + name);
                String rename = zipFileName.substring(zipFileName.lastIndexOf("/")+1,zipFileName.lastIndexOf("."));
                rename += name.substring(name.lastIndexOf("."));
                unzipFile = new File(path + File.separator + rename);
                unzipFile.createNewFile();
                FileOutputStream out = new FileOutputStream(unzipFile);
                int ch;
                byte[] buffer = new byte[1024];
                while ((ch = in.read(buffer)) != -1) {
                    out.write(buffer, 0, ch);
                    out.flush();
                }
                out.close();
            }
        }
        in.close();
        new File(zipFileName).delete();
        return unzipFile;
    }
}
