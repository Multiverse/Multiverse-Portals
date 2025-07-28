package org.mvplugins.multiverse.portals.config;

import org.bukkit.Material;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.core.config.node.ConfigHeaderNode;
import org.mvplugins.multiverse.core.config.node.ConfigNode;
import org.mvplugins.multiverse.core.config.node.ListConfigNode;
import org.mvplugins.multiverse.core.config.node.Node;
import org.mvplugins.multiverse.core.config.node.NodeGroup;
import org.mvplugins.multiverse.core.config.node.serializer.NodeSerializer;
import org.mvplugins.multiverse.core.utils.MaterialConverter;
import org.mvplugins.multiverse.portals.MultiversePortals;

import java.util.ArrayList;

@Service
final class PortalsConfigNodes {

    private final NodeGroup nodes = new NodeGroup();

    NodeGroup getNodes() {
        return nodes;
    }

    private <N extends Node> N node(N node) {
        nodes.add(node);
        return node;
    }

    private final ConfigHeaderNode portalCreationHeader = node(ConfigHeaderNode.builder("portal-creation")
            .comment("#-------------------------------------------------------------------------------------------------------#")
            .comment("#                                                                                                       #")
            .comment("#          __  __ _   _ _  _____ _____   _____ ___  ___ ___   ___  ___  ___ _____ _   _    ___          #")
            .comment("#         |  \\/  | | | | ||_   _|_ _\\ \\ / / __| _ \\/ __| __| | _ \\/ _ \\| _ \\_   _/_\\ | |  / __|         #")
            .comment("#         | |\\/| | |_| | |__| |  | | \\ V /| _||   /\\__ \\ _|  |  _/ (_) |   / | |/ _ \\| |__\\__ \\         #")
            .comment("#         |_|  |_|\\___/|____|_| |___| \\_/ |___|_|_\\|___/___| |_|  \\___/|_|_\\ |_/_/ \\_\\____|___/         #")
            .comment("#                                                                                                       #")
            .comment("#                                                                                                       #")
            .comment("#               WIKI:        https://mvplugins.org/portals/fundamentals/basic-usage/                    #")
            .comment("#               DISCORD:     https://discord.gg/NZtfKky                                                 #")
            .comment("#               BUG REPORTS: https://github.com/Multiverse/Multiverse-Portals/issues                    #")
            .comment("#                                                                                                       #")
            .comment("#                                                                                                       #")
            .comment("#           New options are added to this file automatically. If you manually made changes              #")
            .comment("#           to this file while your server is running, please run `/mvp reload` command.                #")
            .comment("#                                                                                                       #")
            .comment("#-------------------------------------------------------------------------------------------------------#")
            .comment("")
            .comment("")
            .build());

    // todo: node serialization for materials
    final ConfigNode<Material> wandMaterial = node(ConfigNode.builder("portal-creation.wand-material", Material.class)
            .comment("The item used to select a region to create a mvportal. Run `/mv wand` to start selection.")
            .comment("This will be ignore if worldedit is used for selection instead.")
            .defaultValue(Material.WOODEN_PICKAXE)
            .name("wand-material")
            .serializer(new NodeSerializer<>() {
                @Override
                public Material deserialize(Object o, Class<Material> aClass) {
                    return MaterialConverter.stringToMaterial(String.valueOf(o));
                }

                @Override
                public Object serialize(Material material, Class<Material> aClass) {
                    return material.name().toLowerCase();
                }
            })
            .build());

    final ConfigNode<Boolean> bucketFilling = node(ConfigNode.builder("portal-creation.bucket-filling", Boolean.class)
            .comment("")
            .comment("If enabled, water and lava bucket can be used to fill a mvportal.")
            .defaultValue(true)
            .name("bucket-filling")
            .onSetValue((oldValue, newValue) -> MultiversePortals.bucketFilling = newValue)
            .build());

    // todo: node serialization for materials
    final ListConfigNode<Material> frameMaterials = node(ListConfigNode.listBuilder("portal-creation.frame-materials", Material.class)
            .comment("")
            .comment("The allowed materials used to create the frame of the portal.")
            .comment("If the list is empty, all materials are allowed.")
            .defaultValue(ArrayList::new)
            .name("frame-materials")
            .itemSerializer(new NodeSerializer<>() {
                @Override
                public Material deserialize(Object o, Class<Material> aClass) {
                    return MaterialConverter.stringToMaterial(String.valueOf(o));
                }

                @Override
                public Object serialize(Material material, Class<Material> aClass) {
                    return material.name().toLowerCase();
                }
            })
            .onSetValue((oldValue, newValue) -> MultiversePortals.FrameMaterials = newValue)
            .build());

    final ConfigNode<Boolean> clearOnRemove = node(ConfigNode.builder("portal-creation.clear-on-remove", Boolean.class)
            .comment("")
            .comment("If enabled, nether/end fills will be cleared when the portal is removed.")
            .comment("This keeps us from leaving behind portal blocks (which would take an unsuspecting player to the nether when trying to use the mvportal)")
            .defaultValue(false)
            .name("clear-on-remove")
            .aliases("clearonremove")
            .onSetValue((oldValue, newValue) -> MultiversePortals.ClearOnRemove = newValue)
            .build());

    private final ConfigHeaderNode portalUsageHeader = node(ConfigHeaderNode.builder("portal-usage")
            .comment("")
            .comment("")
            .build());

    final ConfigNode<Boolean> enforcePortalAccess = node(ConfigNode.builder("portal-usage.enforce-portal-access", Boolean.class)
            .comment("If enabled, players will not be able to teleport to mvportals they do not have access to.")
            .comment("The permission node is: `multiverse.portal.access.<portal-name>`")
            .defaultValue(true)
            .name("enforce-portal-access")
            .aliases("enforceportalaccess")
            .onSetValue((oldValue, newValue) -> MultiversePortals.EnforcePortalAccess = newValue)
            .build());

    final ConfigNode<Integer> portalCooldown = node(ConfigNode.builder("portal-usage.portal-cooldown", Integer.class)
            .comment("")
            .comment("The time (in milliseconds) a player must wait between using a mvportal.")
            .defaultValue(1000)
            .name("portal-cooldown")
            .aliases("portalcooldown")
            .build());

    final ConfigNode<Boolean> portalsDefaultToNether = node(ConfigNode.builder("portal-usage.portals-default-to-nether", Boolean.class)
            .comment("")
            .comment("If enabled, when a mvportal with nether fill that is unusable, either due to invalid destination or lack of permissions,")
            .comment("will fallback and behave as a normal nether portal teleporting between the nether world.")
            .defaultValue(false)
            .name("portals-default-to-nether")
            .aliases("portalsdefaulttonether")
            .build());

    final ConfigNode<Boolean> netherAnimation = node(ConfigNode.builder("portal-usage.nether-animation", Boolean.class)
            .comment("")
            .comment("If enabled, the nether blobbing animation will be played before the player is teleported.")
            .comment("Note: This does not work if the player is in creative mode due to server software limitations.")
            .defaultValue(true)
            .name("nether-animation")
            .onSetValue((oldValue, newValue) -> MultiversePortals.NetherAnimation = newValue)
            .build());

    final ConfigNode<Boolean> teleportVehicles = node(ConfigNode.builder("portal-usage.teleport-vehicles", Boolean.class)
            .comment("")
            .comment("If enabled, mvportals will teleport all vehicles along with its passengers when the vehicle enters the portal.")
            .comment("Vehicles are usually boats, minecarts, pigs and horses.")
            .defaultValue(false)
            .name("teleport-vehicles")
            .aliases("teleportvehicles")
            .onSetValue((oldValue, newValue) -> MultiversePortals.TeleportVehicles = newValue)
            .build());

    final ConfigNode<Boolean> useOnMove = node(ConfigNode.builder("portal-usage.use-on-move", Boolean.class)
            .comment("")
            .comment("If enabled, player movement will be tracked to determine if the player has entered a portal.")
            .comment("Disabling this will cause mvportals without nether or end fill to not work.")
            .comment("Only disable this if all your portals have nether or end fill and want to slight enhance performance.")
            .defaultValue(true)
            .name("use-on-move")
            .aliases("useonmove")
            .onSetValue((oldValue, newValue) -> MultiversePortals.UseOnMove = newValue)
            .build());

    final ConfigNode<Double> version = node(ConfigNode.builder("version", Double.class)
            .comment("")
            .comment("")
            .comment("This just signifies the version number so we can see what version of config you have.")
            .comment("NEVER TOUCH THIS VALUE")
            .defaultValue(0.0)
            .hidden()
            .build());
}
