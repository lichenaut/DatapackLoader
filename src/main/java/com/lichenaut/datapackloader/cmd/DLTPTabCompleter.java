package com.lichenaut.datapackloader.cmd;

import com.lichenaut.datapackloader.Main;
import lombok.RequiredArgsConstructor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class DLTPTabCompleter implements TabCompleter {

    private final Main main;

    @Override
    public @Nullable List<String> onTabComplete(@Nonnull CommandSender sender, @Nonnull Command command,
            @Nonnull String label, @Nonnull String[] args) {
        List<String> options = new ArrayList<>();
        if (!(sender instanceof Player) || !(sender.hasPermission("datapackloader.command"))) {
            return options;
        }

        if (args.length == 1 && sender.hasPermission("datapackloader.command.tp")) {
            options.addAll(main.getServer().getWorlds().stream()
                    .map(World::getName)
                    .toList());
        }

        return options;
    }
}