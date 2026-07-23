package org.mvplugins.multiverse.portals.listeners;

import com.dumptruckman.minecraft.util.Logging;
import org.bukkit.Material;
import org.mvplugins.multiverse.core.economy.MVEconomist;
import org.mvplugins.multiverse.core.command.MVCommandIssuer;
import org.mvplugins.multiverse.core.command.MVCommandManager;
import org.mvplugins.multiverse.core.locale.message.Message;
import org.mvplugins.multiverse.core.locale.message.MessageReplacement.Replace;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.portals.MVPortal;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.mvplugins.multiverse.portals.config.PortalsConfig;
import org.mvplugins.multiverse.portals.locale.MVPi18n;

import static org.mvplugins.multiverse.core.locale.message.MessageReplacement.replace;

@Service
final class PortalListenerHelper {

    private final PortalsConfig portalsConfig;
    private final MVEconomist economist;
    private final MVCommandManager commandManager;

    @Inject
    PortalListenerHelper(@NotNull PortalsConfig portalsConfig,
                         @NotNull MVEconomist economist,
                         @NotNull MVCommandManager commandManager) {
        this.portalsConfig = portalsConfig;
        this.economist = economist;
        this.commandManager = commandManager;
    }

    boolean isWithinSameBlock(Location from, Location to) {
        return from.getWorld() == to.getWorld()
                && from.getBlockX() == to.getBlockX()
                && from.getBlockY() == to.getBlockY()
                && from.getBlockZ() == to.getBlockZ();
    }

    void stateSuccess(String playerName, String portalName) {
        Logging.fine(String.format(
                "MV-Portals is allowing Player '%s' to use the portal '%s'.",
                playerName, portalName));
    }

    void stateFailure(String playerName, String portalName) {
        Logging.fine(String.format(
                "MV-Portals is DENYING Player '%s' access to use the portal '%s'.",
                playerName, portalName));
    }

    PortalUseResult checkPlayerCanUsePortal(MVPortal portal, Player player) {
        // If they're using Access and they don't have permission and they're NOT exempt, return, they're not allowed to tp.
        // No longer checking exemption status
        if (portalsConfig.getEnforcePortalAccess() && !player.hasPermission(portal.getPermission())) {
            stateFailure(player.getDisplayName(), portal.getName());
            return PortalUseResult.CANNOT_USE;
        }

        double price = portal.getPrice();
        Material currency = portal.getCurrency();

        // Stop the player if the portal costs and they can't pay
        if (price == 0D || player.hasPermission(portal.getExempt())) {
            return PortalUseResult.FREE_USE;
        }

        if (price > 0D && !economist.isPlayerWealthyEnough(player, price, currency)) {
            MVCommandIssuer issuer = commandManager.getCommandIssuer(player);
            Message message = Message.of(MVPi18n.PORTAL_INSUFFICIENTFUNDS,
                    replace("{price}").with(economist.formatPrice(price, currency)),
                    Replace.NAME.with(portal.getName()));
            player.sendMessage(economist.getNSFMessage(currency, message.formatted(issuer)));
            stateFailure(player.getDisplayName(), portal.getName());
            return PortalUseResult.CANNOT_USE;
        }
        return PortalUseResult.PAID_USE;
    }

    void payPortalEntryFee(MVPortal portal, Player player) {
        economist.payEntryFee(player, portal.getPrice(), portal.getCurrency());
    }

    void sendInvalidFrameMessage(Player player) {
        commandManager.getCommandIssuer(player).sendError(MVPi18n.PORTAL_FRAME_INVALID);
    }

    enum PortalUseResult {
        CANNOT_USE(false, false),
        FREE_USE(true, false),
        PAID_USE(true, true);

        private final boolean canUse;
        private final boolean needToPay;

        PortalUseResult(boolean canUse, boolean needToPay) {
            this.canUse = canUse;
            this.needToPay = needToPay;
        }

        public boolean canUse() {
            return canUse;
        }

        public boolean needToPay() {
            return needToPay;
        }
    }
}
