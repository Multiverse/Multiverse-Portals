package org.mvplugins.multiverse.portals.commands;

import org.mvplugins.multiverse.core.command.LegacyAliasCommand;
import org.mvplugins.multiverse.core.command.MVCommandIssuer;
import org.mvplugins.multiverse.core.locale.MVCorei18n;
import org.mvplugins.multiverse.core.locale.message.MessageReplacement.Replace;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandAlias;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandCompletion;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandPermission;
import org.mvplugins.multiverse.external.acf.commands.annotation.Description;
import org.mvplugins.multiverse.external.acf.commands.annotation.Optional;
import org.mvplugins.multiverse.external.acf.commands.annotation.Subcommand;
import org.mvplugins.multiverse.external.acf.commands.annotation.Syntax;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.portals.config.PortalsConfig;

@Service
class ConfigCommand extends PortalsCommand {

    private final PortalsConfig portalsConfig;

    @Inject
    ConfigCommand(@NotNull PortalsConfig portalsConfig) {
        this.portalsConfig = portalsConfig;
    }

    @Subcommand("config")
    @CommandPermission("multiverse.portal.config")
    @CommandCompletion("@portalconfigproperties @portalconfigvalues")
    @Syntax("<property> [value]")
    @Description("{@@mv-portals.config.description}")
    void onConfigCommand(
            @NotNull MVCommandIssuer issuer,

            @Optional
            @Syntax("<property>")
            @Description("{@@mv-portals.config.property.description}")
            String property,

            @Optional
            @Syntax("[value]")
            @Description("{@@mv-portals.config.value.description}")
            String value
    ) {
        if (value == null) {
            showConfigValue(issuer, property);
            return;
        }
        updateConfigValue(issuer, property, value);
    }

    private void showConfigValue(MVCommandIssuer issuer, String name) {
        portalsConfig.getStringPropertyHandle().getProperty(name)
                .onSuccess(value -> issuer.sendInfo(MVCorei18n.CONFIG_SHOW_SUCCESS,
                        Replace.NAME.with(name),
                        Replace.VALUE.with(value)))
                .onFailure(e -> issuer.sendError(MVCorei18n.CONFIG_SHOW_ERROR,
                        Replace.NAME.with(name),
                        Replace.ERROR.with(e)));
    }

    private void updateConfigValue(MVCommandIssuer issuer, String name, String value) {
        portalsConfig.getStringPropertyHandle().setPropertyString(issuer.getIssuer(), name, value)
                .onSuccess(ignore -> {
                    portalsConfig.save();
                    issuer.sendInfo(MVCorei18n.CONFIG_SET_SUCCESS,
                            Replace.NAME.with(name),
                            Replace.VALUE.with(value));
                })
                .onFailure(e -> issuer.sendError(MVCorei18n.CONFIG_SET_ERROR,
                        Replace.NAME.with(name),
                        Replace.VALUE.with(value),
                        Replace.ERROR.with(e)));
    }

    @Service
    private final static class LegacyAlias extends ConfigCommand implements LegacyAliasCommand {
        @Inject
        LegacyAlias(PortalsConfig portalsConfig) {
            super(portalsConfig);
        }

        @Override
        @CommandAlias("mvpconfig|mvpconf")
        @Subcommand("config|conf")
        void onConfigCommand(MVCommandIssuer issuer, String property, String value) {
            super.onConfigCommand(issuer, property, value);
        }
    }
}
