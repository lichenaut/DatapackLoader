package com.lichenaut.datapackloader.commands;

import com.lichenaut.datapackloader.DatapackLoader;
import com.lichenaut.datapackloader.urlimport.DLURLImporter;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URL;

public class DLCommand implements CommandExecutor {

    private final DatapackLoader plugin;

    public DLCommand(DatapackLoader plugin) {this.plugin = plugin;}

    public void messageSender(CommandSender sender, String message) {
        if (sender instanceof Player) {sender.sendMessage(message);
        } else {plugin.getLog().info(ChatColor.stripColor(message));}
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        String helpMessage = ChatColor.YELLOW + "Please read the 'README.txt' file in this plugin's folder!\n" + ChatColor.GRAY +
                "- Move DatapackLoader.jar to the server's 'plugins' folder, then start the server.\n" +
                "- Configure 'config.yml' in the newly-generated 'DatapackLoader' folder to your preferences.\n" +
                "- There are four different methods of getting datapacks into the 'Datapacks' folder:\n" +
                "    - Dragging and dropping by hand.\n    - Pasting file URLs into 'config.yml'.\n    - Enabling 'starter-datapack' in 'config.yml'.\n" +
                "    - Pasting a URL into the '/dl import <" + ChatColor.YELLOW + "url" + ChatColor.GRAY + ">' console command.\n" +
                "- Restart the server with '/stop', then start it.\nURL file's type should be '.zip'.";
        String invalidMessage = ChatColor.RED + "Invalid usage of '/dl'. Use '" + ChatColor.GRAY + "/dl help" + ChatColor.RED + "', or use '" + ChatColor.GRAY + "/dl import <" +
                ChatColor.YELLOW + "url" + ChatColor.GRAY + ">" + ChatColor.RED + "' in console.";
        String onlyConsoleMessage = ChatColor.RED + "Import command can only be used by console!";
        String zipMessage = ChatColor.RED + "[DatapackLoader] URL must end with a .zip file!";
        StringBuilder arguments = new StringBuilder("dl");
        if (args.length != 0) {for (String arg : args) {arguments.append(" ").append(arg);}}
        String fakeUnknown = ChatColor.RED + "Unknown or incomplete command, see below for error\n" + ChatColor.RED + ChatColor.UNDERLINE + arguments + ChatColor.RESET +
                ChatColor.RED + ChatColor.ITALIC + "<--[HERE]";

        if (sender instanceof Player && !sender.hasPermission("datapackloader.help")) {messageSender(sender, fakeUnknown);return false;}
        if (args.length == 0) {messageSender(sender, invalidMessage);return false;}
        if (args[0].equals("help")) {messageSender(sender, helpMessage);return false;
        } else if (args[0].equals("import")) {
            if (sender instanceof Player && !sender.isOp()) {messageSender(sender, fakeUnknown);return false;}
            if (sender instanceof Player) {messageSender(sender, onlyConsoleMessage);return false;}
            if (args.length != 2) {messageSender(sender, invalidMessage);return false;}
            if (!args[1].endsWith(".zip")) {messageSender(sender, zipMessage);return false;}
            try {
                URL url = new URL(args[1]);
                new DLURLImporter(plugin).importUrl(url);
                plugin.getLog().info("Success! Stop and start the server to apply changes.");
            } catch (IOException e) {
                plugin.getLog().severe("IOException: Could not import datapacks from URL '" + args[1] + "'!");
                e.printStackTrace();
            }
        } else {messageSender(sender, invalidMessage);}
        return false;
    }
}
