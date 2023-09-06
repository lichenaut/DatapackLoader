package com.lichenaut.datapackloader.commands;

import com.lichenaut.datapackloader.DatapackLoader;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

public class DLTPCommand implements CommandExecutor {

    private final DatapackLoader plugin;

    public DLTPCommand(DatapackLoader plugin) {this.plugin = plugin;}

    public void messageSender(CommandSender sender, String message) {
        if (sender instanceof Player) sender.sendMessage(message); else plugin.getLog().info(ChatColor.stripColor(message));
    }

    public void dlTp(CommandSender sender, World world) {
        Player player = (Player) sender;
        player.setGameMode(GameMode.SPECTATOR);
        player.teleport(world.getSpawnLocation());
    }

    @Override
    public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) {
        String invalidMessage = ChatColor.RED + "Invalid usage of '/dltp'. Use '" + ChatColor.GRAY + "/dltp <" + ChatColor.YELLOW + "worldname" + ChatColor.GRAY + ">" + ChatColor.RED + "'.";
        String onlyPlayerMessage = ChatColor.RED + "Teleport command can only be used by a player!";

        if (!(sender instanceof Player)) {messageSender(sender, onlyPlayerMessage);return false;}

        if (!sender.hasPermission("datapackloader.tp")) return false;

        if (args.length != 1) {messageSender(sender, invalidMessage);return false;}

        for (World world : plugin.getServer().getWorlds()) if (args[0].equalsIgnoreCase(world.getName())) {dlTp(sender, world);return true;}

        messageSender(sender, ChatColor.RED + "Could not teleport to world '" + args[0] + "'! Typo?");
        return false;
    }
}