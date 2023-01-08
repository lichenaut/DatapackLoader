package com.lichenaut.datapackloader.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class DLTabCompleter implements TabCompleter {
    @Override
    public @Nullable List<String> onTabComplete(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) {
        List<String> options = new ArrayList<>();
        if (sender instanceof Player && sender.hasPermission("datapackloader.help") && args.length == 1) {options.add("help");}
        return options;
    }
}