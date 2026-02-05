package com.hipposideros.hippowavmetadata.service;

import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class GuanoPythonService {

    public void updateGuano(Path wavFile, Map<String, String> updates) {
        List<String> cmd = new ArrayList<>();
        cmd.add("python");
        cmd.add("scripts/guano_update.py");
        cmd.add(wavFile.toString());

        updates.forEach((k, v) -> cmd.add(k + "=" + v));

        run(cmd);
    }

    public void deleteGuano(Path wavFile, List<String> keys) {
        List<String> cmd = new ArrayList<>();
        cmd.add("python");
        cmd.add("scripts/guano_delete.py");
        cmd.add(wavFile.toString());
        cmd.addAll(keys);

        run(cmd);
    }

    private void run(List<String> command) {
        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            Process p = pb.start();
            int code = p.waitFor();

            if (code != 0) {
                throw new RuntimeException("Python GUANO script failed: " + command);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to run Python GUANO script", e);
        }
    }
}
