package com.lichenaut.datapackloader.cmd;

import com.lichenaut.datapackloader.Main;
import com.lichenaut.datapackloader.util.GenUtil;
import com.lichenaut.datapackloader.util.Messager;
import lombok.RequiredArgsConstructor;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class DLTPCmd implements CommandExecutor {

    private static CompletableFuture<Void> commandFuture = CompletableFuture.completedFuture(null);
    private final GenUtil genUtil;
    private final Main main;
    private final Messager messager;

    @Override
    public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label,
            @Nonnull String[] args) {
        if (genUtil.checkDisallowed(sender, "datapackloader.command")) {
            return true;
        }

        if (!(sender instanceof Player)) {
            commandFuture = commandFuture
                    .thenAcceptAsync(processed -> messager.sendMsg(sender, messager.getOnlyPlayerMessage()));
            return true;
        }

        if (!sender.hasPermission("datapackloader.command.tp")) {
            return true;
        }

        if (args.length != 1) {
            commandFuture = commandFuture
                    .thenAcceptAsync(processed -> messager.sendMsg(sender, messager.getInvalidMessage()));
            return false;
        }

        for (World world : main.getServer().getWorlds()) {
            if (args[0].equalsIgnoreCase(world.getName())) {
                Player player = (Player) sender;
                player.setGameMode(GameMode.SPECTATOR);

                if (genUtil.isFolia()) {
                    player.teleportAsync(world.getSpawnLocation());
                } else {
                    player.teleport(world.getSpawnLocation());
                }
                return true;
            }
        }

        commandFuture = commandFuture
                .thenAcceptAsync(processed -> messager.sendMsg(sender, messager.getInvalidWorldMessage()));
        return false;
    }
}