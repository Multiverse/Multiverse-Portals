package org.mvplugins.multiverse.portals.listeners;

import java.util.Date;

import com.dumptruckman.minecraft.util.Logging;
import org.mvplugins.multiverse.core.destination.DestinationInstance;
import org.mvplugins.multiverse.core.teleportation.AsyncSafetyTeleporter;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.external.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.portals.PortalPlayerSession;
import org.bukkit.Location;
import org.bukkit.entity.Player;

@Service
public class PlayerListenerHelper {

    private final AsyncSafetyTeleporter safetyTeleporter;

    @Inject
    PlayerListenerHelper(@NotNull AsyncSafetyTeleporter safetyTeleporter) {
        this.safetyTeleporter = safetyTeleporter;
    }

    void stateSuccess(String playerName, String worldName) {
        Logging.fine(String.format(
                "MV-Portals is allowing Player '%s' to use the portal '%s'.",
                playerName, worldName));
    }

    void stateFailure(String playerName, String portalName) {
        Logging.fine(String.format(
                "MV-Portals is DENYING Player '%s' access to use the portal '%s'.",
                playerName, portalName));
    }

    void performTeleport(Player player, Location to, PortalPlayerSession ps, DestinationInstance<?, ?> destination) {
        safetyTeleporter.to(destination).teleport(player)
                .onSuccess(() -> {
                    ps.playerDidTeleport(to);
                    ps.setTeleportTime(new Date());
                    this.stateSuccess(player.getDisplayName(), destination.toString());
                })
                .onFailure(reason -> Logging.fine(
                        "Failed to teleport player '%s' to destination '%s'. Reason: %s",
                        player.getDisplayName(), destination, reason)
                );
    }
}
