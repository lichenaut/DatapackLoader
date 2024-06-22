package com.lichenaut.datapackloader.cmd;

import com.lichenaut.datapackloader.Main;
import com.lichenaut.datapackloader.url.URLImporter;
import com.lichenaut.datapackloader.util.Messager;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.Logger;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class DLCommand implements CommandExecutor {

    private static CompletableFuture<Void> commandFuture = CompletableFuture.completedFuture(null);
    private final CmdUtil cmdUtil;
    private final String datapacksFolderPath;
    private final Logger logger;
    private final Main main;
    private final Messager messager;
    private final String separator;

    @Override
    public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label,
            @Nonnull String[] args) {
        if (cmdUtil.checkDisallowed(sender, "datapackloader.command")) {
            return true;
        }

        if (args[0].equals("help")) {
            if (cmdUtil.checkDisallowed(sender, "datapackloader.command.help")) {
                return true;
            }

            commandFuture = commandFuture.thenAcceptAsync(processed -> messager.sendMsg(sender, messager.getHelpMessage()));
            return true;
        } else if (args[0].equals("import")) {
            if (sender instanceof Player) {
                commandFuture = commandFuture
                        .thenAcceptAsync(processed -> messager.sendMsg(sender, messager.getOnlyConsoleMessage()));
                return true;
            }

            if (args.length != 2) {
                commandFuture = commandFuture
                        .thenAcceptAsync(processed -> messager.sendMsg(sender, messager.getInvalidMessage()));
                return true;
            }

            if (!args[1].endsWith(".zip")) {
                commandFuture = commandFuture
                        .thenAcceptAsync(processed -> messager.sendMsg(sender, messager.getZipMessage()));
                return false;
            }

            commandFuture = commandFuture
                    .thenAcceptAsync(processed -> {
                        try {
                            URL url = new URI(args[1]).toURL();
                            new URLImporter(logger, main, separator).importUrl(datapacksFolderPath, url);
                            logger.info("Success! Stop and start the server to apply changes.");
                        } catch (IOException e) {
                            throw new RuntimeException("IOException: Failed to import datapack.", e);
                        } catch (URISyntaxException e) {
                            throw new RuntimeException("URISyntaxException: Invalid URL.", e);
                        }
                    })
                    .exceptionally(e -> {
                        logger.error(e.getMessage());
                        return null;
                    });
            return true;
        }

        commandFuture = commandFuture
                .thenAcceptAsync(processed -> messager.sendMsg(sender, messager.getInvalidMessage()));
        return true;
    }
}
