package org.mvplugins.multiverse.portals;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.mvplugins.multiverse.core.MultiverseCoreApi;
import org.mvplugins.multiverse.core.config.node.ConfigNode;
import org.mvplugins.multiverse.core.config.node.Node;
import org.mvplugins.multiverse.core.config.node.NodeGroup;
import org.mvplugins.multiverse.core.destination.DestinationInstance;
import org.mvplugins.multiverse.core.destination.DestinationsProvider;
import org.mvplugins.multiverse.core.exceptions.MultiverseException;
import org.mvplugins.multiverse.external.vavr.control.Try;
import org.mvplugins.multiverse.portals.utils.MultiverseRegion;

import java.util.Collections;
import java.util.List;

final class MVPortalNodes {

    private final NodeGroup nodes = new NodeGroup();

    private MultiversePortals plugin;
    private MVPortal portal;
    private DestinationsProvider destinationsProvider;

    MVPortalNodes(MultiversePortals plugin, MVPortal portal) {
        this.plugin = plugin;
        this.portal = portal;
        this.destinationsProvider = MultiverseCoreApi.get().getDestinationsProvider();
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
                        return Try.failure(new MultiverseException("You can only use '@selected-region' as a player."));
                    }
                    MultiverseRegion region = plugin.getPortalSession(player).getSelectedRegion();
                    if (region == null) {
                        return Try.failure(new MultiverseException("You must select a region first. See `/mvp wand` for more info."));
                    }
                    return Try.success(region.toString());
                }
                PortalLocation portalLocation = PortalLocation.parseLocation(input);
                if (!portalLocation.isValidLocation()) {
                    return Try.failure(new MultiverseException("Invalid location format. The portal location must be in the format `WORLD:X,Y,Z:X,Y,Z`."));
                }
                return Try.success(portalLocation.toString());
            })
            .onSetValue((oldValue, newValue) -> portal.setPortalLocationInternal(PortalLocation.parseLocation(newValue)))
            .build());

    final ConfigNode<String> destination = node(ConfigNode.builder("destination", String.class)
            .defaultValue("")
            .aliases("dest")
            .suggester((sender, input) -> destinationsProvider.suggestDestinationStrings(sender, input))
            .stringParser((sender, input, type) -> destinationsProvider.parseDestination(sender, input)
                    .map(DestinationInstance::toString)
                    .toTry())
            .build());

    final ConfigNode<Boolean> checkDestinationSafety = node(ConfigNode.builder("check-destination-safety", Boolean.class)
            .defaultValue(true)
            .build());

    final ConfigNode<Double> version = node(ConfigNode.builder("version", Double.class)
            .defaultValue(0.0)
            .hidden()
            .build());
}
