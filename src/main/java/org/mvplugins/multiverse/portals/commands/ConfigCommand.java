package org.mvplugins.multiverse.portals.commands;

import org.mvplugins.multiverse.core.command.LegacyAliasCommand;
import org.mvplugins.multiverse.core.command.MVCommandIssuer;
import org.mvplugins.multiverse.core.locale.MVCorei18n;
import org.mvplugins.multiverse.core.locale.message.MessageReplacement;
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
    @Description("Allows you to set Global MV Portals Variables.")
    void onConfigCommand(
            @NotNull MVCommandIssuer issuer,

            @Optional
            @Syntax("<property>")
            @Description("The property to set or get info of.")
            String property,

            @Optional
            @Syntax("[value]")
            @Description("The value to set.")
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
                .onSuccess(value -> issuer.sendMessage(MVCorei18n.CONFIG_SHOW_SUCCESS,
                        MessageReplacement.Replace.NAME.with(name),
                        MessageReplacement.Replace.VALUE.with(value)))
                .onFailure(e -> issuer.sendMessage(MVCorei18n.CONFIG_SHOW_ERROR,
                        MessageReplacement.Replace.NAME.with(name),
                        MessageReplacement.Replace.ERROR.with(e)));
    }

    private void updateConfigValue(MVCommandIssuer issuer, String name, String value) {
        portalsConfig.getStringPropertyHandle().setPropertyString(name, value)
                .onSuccess(ignore -> {
                    portalsConfig.save();
                    issuer.sendMessage(MVCorei18n.CONFIG_SET_SUCCESS,
                            MessageReplacement.Replace.NAME.with(name),
                            MessageReplacement.Replace.VALUE.with(value));
                })
                .onFailure(e -> issuer.sendMessage(MVCorei18n.CONFIG_SET_ERROR,
                        MessageReplacement.Replace.NAME.with(name),
                        MessageReplacement.Replace.VALUE.with(value),
                        MessageReplacement.Replace.ERROR.with(e)));
    }

    @Service
    private final static class LegacyAlias extends ConfigCommand implements LegacyAliasCommand {
        @Inject
        LegacyAlias(PortalsConfig portalsConfig) {
            super(portalsConfig);
        }

        @Override
        @CommandAlias("mvpconfig|mvpconf")
        @Subcommand("conf")
        void onConfigCommand(MVCommandIssuer issuer, String property, String value) {
            super.onConfigCommand(issuer, property, value);
        }
    }
}
