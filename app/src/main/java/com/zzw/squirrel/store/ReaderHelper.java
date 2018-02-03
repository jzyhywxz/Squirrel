package com.zzw.squirrel.store;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by zzw on 2018/2/1.
 */

public class ReaderHelper {
    private BufferedReader reader = null;

    public void open(File file) {
        if ((file == null) || (!file.exists())) {
            return;
        }

        try {
            if (reader != null) {
                reader.close();
                reader = null;
            }

            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void open(String filename) {
        if (filename != null) {
            open(new File(filename));
        }
    }

    public String readLine() {
        String line = null;
        try {
            if (reader != null) {
                line = reader.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return line;
    }

    public void close() {
        try {
            if (reader != null) {
                reader.close();
                reader = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
