package com.lichenaut.datapackloader.commands;

import com.lichenaut.datapackloader.DatapackLoader;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DLTPTabCompleter implements TabCompleter {

    private final DatapackLoader plugin;

    public DLTPTabCompleter(DatapackLoader plugin) {this.plugin = plugin;}
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> options = new ArrayList<>();
        if (sender instanceof Player && sender.hasPermission("datapackloader.tp") && args.length == 1) {
            String[] worldNames = new String[plugin.getServer().getWorlds().size()];
            int i = 0;for (World world : plugin.getServer().getWorlds()) {worldNames[i] = world.getName();i++;}
            options.addAll(Arrays.asList(worldNames));
        }
        return options;
    }
}