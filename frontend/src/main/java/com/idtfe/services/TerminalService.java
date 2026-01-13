package com.idtfe.services;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class TerminalService {
    public String runCommand(String command) {
        StringBuilder out = new StringBuilder();
        try {
            ProcessBuilder pb = new ProcessBuilder();
            // Run via system shell for cross-platform
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                pb.command("cmd.exe", "/c", command);
            } else {
                pb.command("/bin/sh", "-c", command);
            }
            pb.redirectErrorStream(true);
            Process p = pb.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    out.append(line).append(System.lineSeparator());
                }
            }
            p.waitFor();
        } catch (Exception e) {
            out.append("Error: ").append(e.getMessage());
        }
        return out.toString();
    }
}
