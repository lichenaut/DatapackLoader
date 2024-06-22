package com.lichenaut.datapackloader.util;

import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.Logger;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class LevelChanger {

    private final Logger logger;

    public void changeLevelName() throws IOException {
        logger.info("Altering 'level-name' in 'server.properties' because developer mode is on.");
        logger.info("This allows for new worlds to generate after the server starts up again.");
        List<String> lines = new ArrayList<>();
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader("server.properties"))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            throw new IOException("IOException: Could not read 'server.properties'.\n" + e.getMessage());
        }

        for (String line : lines) {
            if (line.startsWith("level-name=")) {
                if (line.equals("level-name=world")) {
                    lines.set(lines.indexOf(line), "level-name=wor1d");
                } else {
                    lines.set(lines.indexOf(line), "level-name=world");
                }

                break;
            }
        }

        try (FileWriter fileWriter = new FileWriter("server.properties")) {
            for (String string : lines) {
                fileWriter.write(string + "\n");
            }
        } catch (IOException e) {
            throw new IOException("IOException: Could not write to 'server.properties'.\n" + e.getMessage());
        }
    }
}
