package com.lichenaut.datapackloader.cmd;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CmdUtil {

    public boolean checkDisallowed(CommandSender sender, String permission) {
        return sender instanceof Player && !sender.hasPermission(permission);
    }
}
