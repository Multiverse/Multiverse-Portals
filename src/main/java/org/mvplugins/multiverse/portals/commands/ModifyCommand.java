package org.mvplugins.multiverse.portals.commands;

import com.dumptruckman.minecraft.util.Logging;
import org.bukkit.ChatColor;
import org.mvplugins.multiverse.core.command.LegacyAliasCommand;
import org.mvplugins.multiverse.core.locale.message.LocalizableMessage;
import org.mvplugins.multiverse.core.world.WorldManager;
import org.mvplugins.multiverse.core.command.MVCommandIssuer;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandAlias;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandCompletion;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandPermission;
import org.mvplugins.multiverse.external.acf.commands.annotation.Description;
import org.mvplugins.multiverse.external.acf.commands.annotation.Flags;
import org.mvplugins.multiverse.external.acf.commands.annotation.Single;
import org.mvplugins.multiverse.external.acf.commands.annotation.Subcommand;
import org.mvplugins.multiverse.external.acf.commands.annotation.Syntax;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.portals.MVPortal;
import org.mvplugins.multiverse.portals.MultiversePortals;

@Service
class ModifyCommand extends PortalsCommand {

    private final MultiversePortals plugin;
    private final WorldManager worldManager;

    @Inject
    ModifyCommand(@NotNull MultiversePortals plugin, @NotNull WorldManager worldManager) {
        this.plugin = plugin;
        this.worldManager = worldManager;
    }

    @Subcommand("modify")
    @CommandPermission("multiverse.portal.modify")
    @CommandCompletion("@mvportals @portalpropertynames @portalpropertyvalues")
    @Syntax("[portal] <property> <value>")
    @Description("Allows you to modify all values that can be set.")
    public void onModifyCommand(
            MVCommandIssuer issuer,

            @Flags("resolve=issuerAware")
            @Syntax("[portal]")
            @Description("The portal to modify.")
            MVPortal portal,

            @Syntax("<property>")
            @Description("The property to modify.")
            String property,

            @Single
            @Syntax("<value>")
            @Description("The value to set.")
            String value
    ) {
        //todo: remove this in 6.0
        if (property.equalsIgnoreCase("dest") || property.equalsIgnoreCase("destination")) {
            if (value.equalsIgnoreCase("here") && !worldManager.isWorld("here")) {
                Logging.warning("Using 'here' as a destination is deprecated and will be removed in a future version. Use 'e:@here' instead.");
                issuer.sendError("Using 'here' as a destination is deprecated and will be removed in a future version. Use 'e:@here' instead.");
                value = "e:@here";
            }
        }

        String finalValue = value;
        var stringPropertyHandle = portal.getStringPropertyHandle();
        stringPropertyHandle.setPropertyString(issuer.getIssuer(), property, value)
                .onSuccess(ignore -> {
                    this.plugin.savePortalsConfig();
                    issuer.sendMessage(ChatColor.GREEN + "Property " + ChatColor.AQUA + property + ChatColor.GREEN
                            + " of Portal " + ChatColor.YELLOW + portal.getName() + ChatColor.GREEN + " was set to "
                            + ChatColor.AQUA + stringPropertyHandle.getProperty(property).getOrNull());
                }).onFailure(failure -> {
                    issuer.sendError("Property " + ChatColor.AQUA + property + ChatColor.RED + " of Portal "
                            + ChatColor.YELLOW + portal.getName() + ChatColor.RED + " was NOT set to "
                            + ChatColor.AQUA + finalValue);
                    if (failure instanceof LocalizableMessage localizableMessage) {
                        issuer.sendError(localizableMessage.getLocalizableMessage());
                    } else {
                        issuer.sendError(failure.getMessage());
                    }
                });
    }

    @Service
    private final static class LegacyAlias extends ModifyCommand implements LegacyAliasCommand {
        @Inject
        LegacyAlias(MultiversePortals plugin, WorldManager worldManager) {
            super(plugin, worldManager);
        }

        @Override
        @CommandAlias("mvpmodify|mvpm")
        public void onModifyCommand(MVCommandIssuer issuer, MVPortal portal, String property, String value) {
            super.onModifyCommand(issuer, portal, property, value);
        }
    }
}
