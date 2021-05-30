package com.petpet.c3po.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by artur on 3/11/14.
 */
public class C3POFileUtils {
    public static List<File> traverseDirectory(File file) {
        List<File> result= new ArrayList<File>();
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            List<File> tmp = new ArrayList<File>();
            for ( File f : files ) {
                tmp.addAll(traverseDirectory(f));
            }
            result.addAll(tmp);
        }  else
        {
            result.add(file);
        }
        return result;
    }

}
