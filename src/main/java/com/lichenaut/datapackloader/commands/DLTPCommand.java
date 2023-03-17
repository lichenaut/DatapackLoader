package com.lichenaut.datapackloader.commands;

import com.lichenaut.datapackloader.DatapackLoader;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.Objects;

public class DLTPCommand implements CommandExecutor {

    private final DatapackLoader plugin;

    public DLTPCommand(DatapackLoader plugin) {this.plugin = plugin;}

    public void messageSender(CommandSender sender, String message) {
        if (sender instanceof Player) {sender.sendMessage(message);
        } else {plugin.getLog().info(ChatColor.stripColor(message));}
    }

    public void dlTp(CommandSender sender, String worldName) {
        Player player = (Player) sender;
        player.setGameMode(GameMode.SPECTATOR);
        player.teleport(Objects.requireNonNull(Bukkit.getWorld(worldName)).getSpawnLocation());
    }

    @Override
    public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) {
        String invalidMessage = ChatColor.RED + "Invalid usage of '/dltp'. Use '" + ChatColor.GRAY + "/dltp <" + ChatColor.YELLOW + "worldname" + ChatColor.GRAY + ">" + ChatColor.RED +
                "'.";
        String onlyPlayerMessage = ChatColor.RED + "Teleport command can only be used by a player!";
        StringBuilder arguments = new StringBuilder("dltp");
        for (String arg : args) {arguments.append(" ").append(arg);}
        String fakeUnknown = ChatColor.RED + "Unknown or incomplete command, see below for error\n" + ChatColor.RED + ChatColor.UNDERLINE + arguments + ChatColor.RESET +
                ChatColor.RED + ChatColor.ITALIC + "<--[HERE]";

        if (sender instanceof Player) {
            if (!sender.hasPermission("datapackloader.tp")) {messageSender(sender, fakeUnknown);return false;}
            if (args.length != 1) {messageSender(sender, invalidMessage);return false;}
            String[] worldNames = new String[plugin.getServer().getWorlds().size()];
            int i = 0;for (World world : plugin.getServer().getWorlds()) {worldNames[i] = world.getName();i++;}
            for (String worldName : worldNames) {if (args[0].equals(worldName)) {dlTp(sender, worldName);return false;}}
            messageSender(sender, ChatColor.RED + "Could not teleport to world '" + args[0] + "'! Typo?");
        } else {messageSender(sender, onlyPlayerMessage);}
        return false;
    }
}