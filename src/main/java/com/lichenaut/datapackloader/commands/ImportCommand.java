package com.lichenaut.datapackloader.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class ImportCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {//check for only console
            System.out.println("equivalent to this plugin's help command");
        } else if (args.length == 1) {
            System.out.println("just did import with no field");//check if arg is really 'import'
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("import") && args[1].contains(".")) {
                System.out.println("success?!");
            }
        }
        return false;
    }
}