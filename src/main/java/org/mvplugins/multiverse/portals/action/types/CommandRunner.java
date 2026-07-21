package org.mvplugins.multiverse.portals.action.types;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.mvplugins.multiverse.core.utils.REPatterns;
import org.mvplugins.multiverse.core.locale.message.Message;
import org.mvplugins.multiverse.external.acf.locales.MessageKeyProvider;
import org.mvplugins.multiverse.portals.locale.MVPi18n;

import static org.mvplugins.multiverse.core.locale.message.MessageReplacement.replace;

abstract class CommandRunner {

    static CommandRunner fromString(String command) {
        String[] split = REPatterns.COLON.split(command, 2);
        return switch ((split.length == 2) ? split[0] : "") {
            case "op" -> new Op(command, split[1]);
            case "console" -> new Console(command, split[1]);
            default -> new Self(command, command);
        };
    }

    final String rawCmd;
    private final String cmdStr;
    private final MessageKeyProvider descriptionKey;

    private CommandRunner(String rawCmd, String cmdStr, MessageKeyProvider descriptionKey) {
        this.rawCmd = rawCmd;
        this.cmdStr = cmdStr;
        this.descriptionKey = descriptionKey;
    }

    void runCommand(CommandSender sender) {
        runCommand(sender, parseCmdStrPlaceholders(sender));
    }

    String parseCmdStrPlaceholders(CommandSender sender) {
        String parsedCmd = cmdStr;
        if (sender instanceof Entity entity) {
            parsedCmd = parsedCmd.replace("%world%", entity.getWorld().getName());
        }
        if (sender instanceof OfflinePlayer player) {
            parsedCmd = parsedCmd.replace("%player%", String.valueOf(player.getName()));
            if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                parsedCmd = PlaceholderAPI.setPlaceholders(player, parsedCmd);
            }
        }
        return parsedCmd;
    }

    Message actionDescription(CommandSender sender) {
        return Message.of(descriptionKey,
                replace("{command}").with(parseCmdStrPlaceholders(sender)));
    }

    protected abstract void runCommand(CommandSender sender, String cmd);

    private static class Self extends CommandRunner {

        private Self(String rawCmd, String cmdStr) {
            super(rawCmd, cmdStr, MVPi18n.ACTION_COMMAND_SELF);
        }

        @Override
        protected void runCommand(CommandSender sender, String cmd) {
            Bukkit.dispatchCommand(sender, cmd);
        }
    }

    private static class Op extends CommandRunner {

        private Op(String rawCmd, String cmdStr) {
            super(rawCmd, cmdStr, MVPi18n.ACTION_COMMAND_OPERATOR);
        }

        @Override
        protected void runCommand(CommandSender sender, String cmd) {
            boolean shouldRemoveOp = false;
            if (!sender.isOp()) {
                sender.setOp(true);
                shouldRemoveOp = true;
            }
            Bukkit.dispatchCommand(sender, cmd);
            if (shouldRemoveOp) {
                sender.setOp(false);
            }
        }
    }

    private static class Console extends CommandRunner {

        private Console(String rawCmd, String cmdStr) {
            super(rawCmd, cmdStr, MVPi18n.ACTION_COMMAND_CONSOLE);
        }

        @Override
        protected void runCommand(CommandSender sender, String cmd) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
        }
    }
}
