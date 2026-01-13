package com.idtfe.services;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class EditorService {
    public String loadFile(String path) {
        try {
            File f = new File(path);
            if (!f.exists()) return null;
            return new String(Files.readAllBytes(Paths.get(path)));
        } catch (IOException e) {
            return null;
        }
    }

    public boolean saveFile(String path, String content) {
        try {
            Files.write(Paths.get(path), content.getBytes());
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
