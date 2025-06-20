package org.mvplugins.multiverse.portals;

import org.bukkit.Material;
import org.mvplugins.multiverse.core.MultiverseCoreApi;
import org.mvplugins.multiverse.core.config.node.ConfigNode;
import org.mvplugins.multiverse.core.config.node.Node;
import org.mvplugins.multiverse.core.config.node.NodeGroup;
import org.mvplugins.multiverse.core.destination.DestinationsProvider;

import java.util.Objects;

final class MVPortalNodes {

    private final NodeGroup nodes = new NodeGroup();

    private MVPortal portal;
    private DestinationsProvider destinationsProvider;

    MVPortalNodes(MVPortal portal) {
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
            .hidden()
            .aliases("loc")
            .build());

    final ConfigNode<String> world = node(ConfigNode.builder("world", String.class)
            .defaultValue("")
            .hidden()
            .build());

    final ConfigNode<String> destination = node(ConfigNode.builder("destination", String.class)
            .defaultValue("")
            .aliases("dest")
            .suggester((sender, input) -> destinationsProvider.suggestDestinationStrings(sender, input))
            .onSetValue((oldValue, newValue) -> {
                if (!Objects.equals(oldValue, newValue)) {
                    portal.setDestination(newValue);
                }
            })
            .build());

    final ConfigNode<Double> version = node(ConfigNode.builder("version", Double.class)
            .defaultValue(0.0)
            .hidden()
            .build());
}
