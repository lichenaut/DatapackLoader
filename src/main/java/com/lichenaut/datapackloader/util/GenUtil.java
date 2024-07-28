package com.lichenaut.datapackloader.util;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GenUtil {

    public boolean checkDisallowed(CommandSender sender, String permission) {
        return sender instanceof Player && !sender.hasPermission(permission);
    }

    public boolean isFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.scheduler.AsyncScheduler");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
