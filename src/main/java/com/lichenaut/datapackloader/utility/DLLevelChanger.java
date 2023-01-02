package com.lichenaut.datapackloader.utility;

import com.lichenaut.datapackloader.DatapackLoader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DLLevelChanger {

    private final DatapackLoader plugin;

    public DLLevelChanger(DatapackLoader plugin) {this.plugin = plugin;}

    public void changeLevelName() {
        plugin.getLog().info("Altering 'level-name' in 'server.properties' because developer mode is on.");
        plugin.getLog().info("This allows for new worlds to generate after server shutdown.");
        List<String> lines = new ArrayList<>();
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader("server.properties"))) {
            String line;while ((line = bufferedReader.readLine()) != null) {lines.add(line);}
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        for (String string : lines) {
            if (string.startsWith("level-name=")) {
                if (string.equals("level-name=world")) {
                    lines.set(lines.indexOf(string), "level-name=wor1d");
                } else {
                    lines.set(lines.indexOf(string), "level-name=world");
                }
                break;
            }
        }
        try (FileWriter fileWriter = new FileWriter("server.properties")) {
            for (String string : lines) {fileWriter.write(string + "\n");}
        } catch (IOException e) {
            plugin.getLog().severe("IOException: Could not write to 'server.properties'!");
            e.printStackTrace();
        }
    }
}
