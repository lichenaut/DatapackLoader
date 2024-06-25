package com.lichenaut.datapackloader.util;

import com.lichenaut.datapackloader.Main;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import org.apache.logging.log4j.Logger;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@Getter
public class Messager {

    private final Logger logger;
    private final Main main;
    private final BaseComponent[] helpMessage;
    private final BaseComponent[] invalidMessage;
    private final BaseComponent[] invalidTpMessage;
    private final BaseComponent[] invalidWorldMessage;
    private final BaseComponent[] onlyConsoleMessage;
    private final BaseComponent[] onlyPlayerMessage;
    private final BaseComponent[] zipMessage;

    public Messager(Logger logger, Main main) {
        this.logger = logger;
        this.main = main;
        helpMessage = new ComponentBuilder("DatapackLoader documentation: ")
                .color(ChatColor.GRAY)
                .append("https://github.com/lichenaut/DatapackLoader/blob/master/README.md")
                .color(ChatColor.GREEN)
                .create();
        invalidMessage = new ComponentBuilder("Use '/").color(ChatColor.RED)
                .append("dl help").color(ChatColor.GRAY)
                .append("', '/").color(ChatColor.RED)
                .append("dl import ").color(ChatColor.GRAY)
                .append("<").color(ChatColor.RED)
                .append("url").color(ChatColor.GRAY)
                .append(">' in console, or '/")
                .append("dltp ").color(ChatColor.GRAY)
                .append("<").color(ChatColor.RED)
                .append("worldname").color(ChatColor.GRAY)
                .append(">' in game.").color(ChatColor.RED).color(ChatColor.RED).create();
        invalidTpMessage = new ComponentBuilder("Usage: '/").color(ChatColor.RED)
                .append("dltp ").color(ChatColor.GRAY)
                .append("<").color(ChatColor.RED)
                .append("worldname").color(ChatColor.GRAY)
                .append(">'.").color(ChatColor.RED).color(ChatColor.RED).create();
        invalidWorldMessage = new ComponentBuilder("Could not teleport to world. Typo?").color(ChatColor.RED).create();
        onlyConsoleMessage = new ComponentBuilder("Command can only be used by console!").color(ChatColor.RED).create();
        onlyPlayerMessage = new ComponentBuilder("Command can only be used by a player!").color(ChatColor.RED).create();
        zipMessage = new ComponentBuilder("URL must end with a .zip file!").color(ChatColor.RED).create();
    }

    public void sendMsg(CommandSender sender, BaseComponent[] message) {
        if (sender instanceof Player) {
            sender.spigot().sendMessage(message);
            return;
        }

        infoLog(message);
    }

    private void infoLog(BaseComponent[] message) {
        logger.info(new TextComponent(message).toLegacyText().replaceAll("§[0-9a-fA-FklmnoKLMNO]", ""));
    }
}
