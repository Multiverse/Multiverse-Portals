package org.mvplugins.multiverse.portals.commands;

import com.dumptruckman.minecraft.util.Logging;
import org.mvplugins.multiverse.core.command.LegacyAliasCommand;
import org.mvplugins.multiverse.core.locale.message.LocalizableMessage;
import org.mvplugins.multiverse.core.locale.message.MessageReplacement.Replace;
import org.mvplugins.multiverse.core.world.WorldManager;
import org.mvplugins.multiverse.core.command.MVCommandIssuer;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandAlias;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandCompletion;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandPermission;
import org.mvplugins.multiverse.external.acf.commands.annotation.ConsumesRest;
import org.mvplugins.multiverse.external.acf.commands.annotation.Description;
import org.mvplugins.multiverse.external.acf.commands.annotation.Flags;
import org.mvplugins.multiverse.external.acf.commands.annotation.Subcommand;
import org.mvplugins.multiverse.external.acf.commands.annotation.Syntax;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.portals.MVPortal;
import org.mvplugins.multiverse.portals.MultiversePortals;
import org.mvplugins.multiverse.portals.locale.MVPi18n;

import static org.mvplugins.multiverse.core.locale.message.MessageReplacement.replace;

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
    @Description("{@@mv-portals.modify.description}")
    public void onModifyCommand(
            MVCommandIssuer issuer,

            @Flags("resolve=issuerAware")
            @Syntax("[portal]")
            @Description("{@@mv-portals.modify.portal.description}")
            MVPortal portal,

            @Syntax("<property>")
            @Description("{@@mv-portals.modify.property.description}")
            String property,

            @ConsumesRest
            @Syntax("<value>")
            @Description("{@@mv-portals.modify.value.description}")
            String value
    ) {
        //todo: remove this in 6.0
        if (property.equalsIgnoreCase("dest") || property.equalsIgnoreCase("destination")) {
            if (value.equalsIgnoreCase("here") && !worldManager.isWorld("here")) {
                Logging.warning("Using 'here' as a destination is deprecated and will be removed in a future version. Use 'e:@here' instead.");
                issuer.sendError(MVPi18n.MODIFY_HEREDEPRECATED);
                value = "e:@here";
            }
        }

        String finalValue = value;
        var stringPropertyHandle = portal.getStringPropertyHandle();
        stringPropertyHandle.setPropertyString(issuer.getIssuer(), property, value)
                .onSuccess(ignore -> {
                    if (!this.plugin.savePortalsConfig()) {
                        issuer.sendError(MVPi18n.MODIFY_SAVEFAILED);
                        return;
                    }
                    issuer.sendInfo(MVPi18n.MODIFY_SUCCESS,
                            Replace.NAME.with(property),
                            replace("{portal}").with(portal.getName()),
                            Replace.VALUE.with(stringPropertyHandle.getProperty(property).getOrNull()));
                    if (property.equalsIgnoreCase("action-type")) {
                        issuer.sendError(MVPi18n.MODIFY_ACTIONTYPEWARNING);
                    }
                }).onFailure(failure -> {
                    issuer.sendError(MVPi18n.MODIFY_FAILURE,
                            Replace.NAME.with(property),
                            replace("{portal}").with(portal.getName()),
                            Replace.VALUE.with(finalValue));
                    if (failure instanceof LocalizableMessage localizableMessage) {
                        issuer.sendError(localizableMessage.getLocalizableMessage());
                    } else {
                        issuer.sendError(MVPi18n.GENERIC_ERROR_DETAILS, Replace.ERROR.with(failure.getMessage()));
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
