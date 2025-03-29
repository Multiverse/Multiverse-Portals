package org.mvplugins.multiverse.portals.commands;

import org.bukkit.ChatColor;
import org.mvplugins.multiverse.core.command.MVCommandIssuer;
import org.mvplugins.multiverse.core.command.MVCommandManager;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandAlias;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandCompletion;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandPermission;
import org.mvplugins.multiverse.external.acf.commands.annotation.Description;
import org.mvplugins.multiverse.external.acf.commands.annotation.Optional;
import org.mvplugins.multiverse.external.acf.commands.annotation.Single;
import org.mvplugins.multiverse.external.acf.commands.annotation.Subcommand;
import org.mvplugins.multiverse.external.acf.commands.annotation.Syntax;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.external.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.portals.MultiversePortals;
import org.mvplugins.multiverse.portals.enums.PortalConfigProperty;

@Service
@CommandAlias("mvp")
public class ConfigCommand extends PortalsCommand {

    private final MultiversePortals plugin;

    @Inject
    ConfigCommand(@NotNull MVCommandManager commandManager, @NotNull MultiversePortals plugin) {
        super(commandManager);
        this.plugin = plugin;
    }

    @CommandAlias("mvpconfig|mvpconf")
    @Subcommand("config|conf")
    @CommandPermission("multiverse.portal.config")
    @CommandCompletion("@portalconfigproperty @empty")
    @Syntax("<property> <value>")
    @Description("Allows you to set Global MV Portals Variables.")
    void onConfigCommand(
            @NotNull MVCommandIssuer issuer,

            @Optional
            @Syntax("<property>")
            @Description("The property to set.")
            PortalConfigProperty property,

            @Optional
            @Single
            @Syntax("<value>")
            @Description("The value to set.")
            String value
    ) {
        if (property == null) {
            String[] allProps = PortalConfigProperty.getAllValues().split(" ");
            StringBuilder currentvals = new StringBuilder();
            for (String prop : allProps) {
                currentvals.append(ChatColor.GREEN);
                currentvals.append(prop);
                currentvals.append(ChatColor.WHITE);
                currentvals.append(" = ");
                currentvals.append(ChatColor.GOLD);
                currentvals.append(this.plugin.getMainConfig().get(prop, "NOT SET"));
                currentvals.append(ChatColor.WHITE);
                currentvals.append(", ");
            }
            issuer.sendMessage(currentvals.substring(0,currentvals.length() - 2));
            return;
        }

        if (value == null) {
            issuer.sendMessage(ChatColor.AQUA + property.name() + ChatColor.WHITE + " has value "
                    + ChatColor.GREEN + this.plugin.getMainConfig().get(property.name().toLowerCase()));
            return;
        }

        if (property.equals(PortalConfigProperty.wand) || property.equals(PortalConfigProperty.portalcooldown)) {
            try {
                this.plugin.getMainConfig().set(property.name(), Integer.parseInt(value));
            } catch (NumberFormatException e) {
                issuer.sendMessage(ChatColor.RED + "Sorry, " + ChatColor.AQUA + property.name() + ChatColor.WHITE + " must be an integer!");
                return;
            }
        } else {
            try {
                this.plugin.getMainConfig().set(property.name().toLowerCase(), Boolean.parseBoolean(value));
            } catch (Exception e) {
                issuer.sendMessage(ChatColor.RED + "Sorry, " + ChatColor.AQUA + property.name() + ChatColor.WHITE + " must be true or false!");
                return;
            }
        }

        if (this.plugin.saveMainConfig()) {
            issuer.sendMessage(ChatColor.GREEN + "SUCCESS!" + ChatColor.WHITE + " Values were updated successfully!");
            this.plugin.reloadConfigs(false);
        } else {
            issuer.sendMessage(ChatColor.RED + "FAIL!" + ChatColor.WHITE + " Check your console for details!");
        }
    }
}
