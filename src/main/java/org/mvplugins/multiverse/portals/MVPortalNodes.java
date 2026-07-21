package org.mvplugins.multiverse.portals;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.mvplugins.multiverse.core.config.node.ConfigNode;
import org.mvplugins.multiverse.core.config.node.Node;
import org.mvplugins.multiverse.core.config.node.NodeGroup;
import org.mvplugins.multiverse.core.exceptions.MultiverseException;
import org.mvplugins.multiverse.core.locale.message.Message;
import org.mvplugins.multiverse.external.vavr.control.Try;
import org.mvplugins.multiverse.portals.action.ActionHandler;
import org.mvplugins.multiverse.portals.action.ActionHandlerProvider;
import org.mvplugins.multiverse.portals.locale.MVPi18n;
import org.mvplugins.multiverse.portals.utils.MultiverseRegion;

import java.util.Collections;
import java.util.List;

final class MVPortalNodes {

    private final NodeGroup nodes = new NodeGroup();

    private MultiversePortals plugin;
    private MVPortal portal;
    private ActionHandlerProvider actionHandlerProvider;

    MVPortalNodes(MultiversePortals plugin, MVPortal portal) {
        this.plugin = plugin;
        this.portal = portal;
        this.actionHandlerProvider = plugin.getServiceLocator().getService(ActionHandlerProvider.class);
    }

    NodeGroup getNodes() {
        return nodes;
    }

    private <N extends Node> N node(N node) {
        nodes.add(node);
        return node;
    }

    final ConfigNode<Material> currency = node(ConfigNode.builder("currency", Material.class)
            .defaultValue(Material.AIR)
            .aliases("curr")
            .build());

    final ConfigNode<Double> price = node(ConfigNode.builder("price", Double.class)
            .defaultValue(0.0)
            .build());

    final ConfigNode<Boolean> safeTeleport = node(ConfigNode.builder("safe-teleport", Boolean.class)
            .defaultValue(true)
            .aliases("safe")
            .build());

    final ConfigNode<Boolean> teleportNonPlayers = node(ConfigNode.builder("teleport-non-players", Boolean.class)
            .defaultValue(false)
            .aliases("telenonplayers")
            .build());

    final ConfigNode<String> owner = node(ConfigNode.builder("owner", String.class)
            .defaultValue("")
            .build());

    final ConfigNode<String> location = node(ConfigNode.builder("location", String.class)
            .defaultValue("")
            .aliases("loc")
            .suggester((sender, input) -> {
                if (sender instanceof Player player && plugin.getPortalSession(player).getSelectedRegion() != null) {
                    return List.of("@selected-region");
                }
                return Collections.emptyList();
            })
            .stringParser((sender, input, type) -> {
                if (input.equals("@selected-region")) {
                    if (!(sender instanceof Player player)) {
                        return Try.failure(new MultiverseException(
                                Message.of(MVPi18n.PORTALCONFIG_LOCATION_PLAYERSONLY)));
                    }
                    MultiverseRegion region = plugin.getPortalSession(player).getSelectedRegion();
                    if (region == null) {
                        return Try.failure(new MultiverseException(
                                Message.of(MVPi18n.PORTALCONFIG_LOCATION_SELECTIONREQUIRED)));
                    }
                    return Try.success(region.toString());
                }
                PortalLocation portalLocation = PortalLocation.parseLocation(input);
                if (!portalLocation.isValidLocation()) {
                    return Try.failure(new MultiverseException(
                            Message.of(MVPi18n.PORTALCONFIG_LOCATION_INVALID)));
                }
                return Try.success(portalLocation.toString());
            })
            .onSetValue((oldValue, newValue) -> portal.setPortalLocationInternal(PortalLocation.parseLocation(newValue)))
            .build());

    final ConfigNode<String> actionType = node(ConfigNode.builder("action-type", String.class)
            .suggester(input -> actionHandlerProvider.getAllHandlerTypeNames())
            .defaultValue("multiverse-destination")
            .build());

    final ConfigNode<String> action = node(ConfigNode.builder("action", String.class)
            .defaultValue("")
            .aliases("destination", "dest")
            .suggester((sender, input) -> actionHandlerProvider.getHandlerType(portal.getActionType())
                    .map(actionHandlerType -> actionHandlerType.suggestActions(sender, input))
                    .getOrElse(Collections.emptyList()))
            .stringParser((sender, input, type) ->
                    Try.of(() -> actionHandlerProvider.getHandlerType(portal.getActionType())
                            .mapAttempt(actionHandlerType -> actionHandlerType.parseHandler(sender, input))
                            .map(ActionHandler::serialise)
                            .getOrThrow(failure ->
                                    new MultiverseException(failure.getFailureMessage()))))
            .build());

    final ConfigNode<Boolean> checkDestinationSafety = node(ConfigNode.builder("check-destination-safety", Boolean.class)
            .defaultValue(true)
            .build());

    final ConfigNode<Double> version = node(ConfigNode.builder("version", Double.class)
            .defaultValue(0.0)
            .hidden()
            .build());
}
