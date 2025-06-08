package org.mvplugins.multiverse.portals.config;

import com.dumptruckman.minecraft.util.Logging;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.core.config.handle.CommentedConfigurationHandle;
import org.mvplugins.multiverse.core.config.handle.StringPropertyHandle;
import org.mvplugins.multiverse.core.config.migration.ConfigMigrator;
import org.mvplugins.multiverse.core.config.migration.VersionMigrator;
import org.mvplugins.multiverse.core.config.migration.action.MoveMigratorAction;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.vavr.control.Try;
import org.mvplugins.multiverse.portals.MultiversePortals;

import java.nio.file.Path;
import java.util.List;

/**
 *
 * @since 5.1
 */
@ApiStatus.AvailableSince("5.1")
@Service
public final class PortalsConfig {
    public static final String CONFIG_FILENAME = "config.yml";

    private final Path configPath;
    private final PortalsConfigNodes configNodes;
    private final CommentedConfigurationHandle configHandle;
    private final StringPropertyHandle stringPropertyHandle;

    @Inject
    PortalsConfig(@NotNull MultiversePortals plugin, @NotNull PortalsConfigNodes configNodes) {
        this.configPath = Path.of(plugin.getDataFolder().getPath(), CONFIG_FILENAME);
        this.configNodes = configNodes;
        this.configHandle = CommentedConfigurationHandle.builder(configPath, configNodes.getNodes())
                .logger(Logging.getLogger())
                .migrator(ConfigMigrator.builder(configNodes.version)
                        .addVersionMigrator(VersionMigrator.builder(5.0)
                                .addAction(MoveMigratorAction.of("wand", "portal-creation.wand-material"))
                                .addAction(MoveMigratorAction.of("useronmove", "portal-usage.use-on-move"))
                                .addAction(MoveMigratorAction.of("bucketfilling", "portal-creation.bucket-filling"))
                                .addAction(MoveMigratorAction.of("portalsdefaulttonether", "portal-usage.portals-default-to-nether"))
                                .addAction(MoveMigratorAction.of("enforceportalaccess", "portal-usage.enforce-portal-access"))
                                .addAction(MoveMigratorAction.of("portalcooldown", "portal-usage.portal-cooldown"))
                                .addAction(MoveMigratorAction.of("clearonremove", "portal-creation.clear-on-remove"))
                                .addAction(MoveMigratorAction.of("teleportvehicles", "portal-usage.teleport-vehicles"))
                                .addAction(MoveMigratorAction.of("netheranimation", "portal-usage.nether-animation"))
                                .addAction(MoveMigratorAction.of("framematerials", "portal-creation.frame-materials"))
                                .build())
                        .build())
                .build();
        this.stringPropertyHandle = new StringPropertyHandle(configHandle);
    }

    public Try<Void> load() {
        return configHandle.load()
                .onFailure(e -> {
                    Logging.severe("Failed to load Multiverse-Core config.yml!");
                    e.printStackTrace();
                });
    }

    public boolean isLoaded() {
        return configHandle.isLoaded();
    }

    public Try<Void> save() {
        return configHandle.save()
                .onFailure(e -> {
                    Logging.severe("Failed to save Multiverse-Core config.yml!");
                    e.printStackTrace();
                });
    }

    /**
     *
     * @return
     *
     * @since 5.1
     */
    @ApiStatus.AvailableSince("5.1")
    public StringPropertyHandle getStringPropertyHandle() {
        return stringPropertyHandle;
    }

    /**
     *
     * @return
     *
     * @since 5.1
     */
    @ApiStatus.AvailableSince("5.1")
    public Material getWandMaterial() {
        return configHandle.get(configNodes.wandMaterial);
    }

    /**
     *
     * @param wand
     * @return
     *
     * @since 5.1
     */
    @ApiStatus.AvailableSince("5.1")
    public Try<Void> setWandMaterial(Material wand) {
        return configHandle.set(configNodes.wandMaterial, wand);
    }

    /**
     *
     * @return
     *
     * @since 5.1
     */
    @ApiStatus.AvailableSince("5.1")
    public boolean getBucketFilling() {
        return configHandle.get(configNodes.bucketFilling);
    }


    /**
     *
     * @param bucketFilling
     * @return
     *
     * @since 5.1
     */
    @ApiStatus.AvailableSince("5.1")
    public Try<Void> setBucketFilling(boolean bucketFilling) {
        return configHandle.set(configNodes.bucketFilling, bucketFilling);
    }

    /**
     *
     * @return
     *
     * @since 5.1
     */
    @ApiStatus.AvailableSince("5.1")
    public List<Material> getFrameMaterials() {
        return configHandle.get(configNodes.frameMaterials);
    }

    /**
     *
     * @param frameMaterials
     * @return
     *
     * @since 5.1
     */
    @ApiStatus.AvailableSince("5.1")
    public Try<Void> setFrameMaterials(List<Material> frameMaterials) {
        return configHandle.set(configNodes.frameMaterials, frameMaterials);
    }

    /**
     *
     * @return
     *
     * @since 5.1
     */
    @ApiStatus.AvailableSince("5.1")
    public boolean getClearOnRemove() {
        return configHandle.get(configNodes.clearOnRemove);
    }

    /**
     *
     * @param clearOnRemove
     * @return
     *
     * @since 5.1
     */
    @ApiStatus.AvailableSince("5.1")
    public Try<Void> setClearOnRemove(boolean clearOnRemove) {
        return configHandle.set(configNodes.clearOnRemove, clearOnRemove);
    }

    /**
     *
     * @return
     *
     * @since 5.1
     */
    @ApiStatus.AvailableSince("5.1")
    public boolean getEnforcePortalAccess() {
        return configHandle.get(configNodes.enforcePortalAccess);
    }

    /**
     *
     * @param enforcePortalAccess
     * @return
     *
     * @since 5.1
     */
    @ApiStatus.AvailableSince("5.1")
    public Try<Void> setEnforcePortalAccess(boolean enforcePortalAccess) {
        return configHandle.set(configNodes.enforcePortalAccess, enforcePortalAccess);
    }

    /**
     *
     * @return
     *
     * @since 5.1
     */
    @ApiStatus.AvailableSince("5.1")
    public int getPortalCooldown() {
        return configHandle.get(configNodes.portalCooldown);
    }

    /**
     *
     * @param portalCooldown
     * @return
     *
     * @since 5.1
     */
    @ApiStatus.AvailableSince("5.1")
    public Try<Void> setPortalCooldown(int portalCooldown) {
        return configHandle.set(configNodes.portalCooldown, portalCooldown);
    }

    /**
     *
     * @return
     *
     * @since 5.1
     */
    @ApiStatus.AvailableSince("5.1")
    public boolean getPortalsDefaultToNether() {
        return configHandle.get(configNodes.portalsDefaultToNether);
    }

    /**
     *
     * @param portalsDefaultToNether
     * @return
     *
     * @since 5.1
     */
    @ApiStatus.AvailableSince("5.1")
    public Try<Void> setPortalsDefaultToNether(boolean portalsDefaultToNether) {
        return configHandle.set(configNodes.portalsDefaultToNether, portalsDefaultToNether);
    }

    /**
     *
     * @return
     *
     * @since 5.1
     */
    @ApiStatus.AvailableSince("5.1")
    public boolean getNetherAnimation() {
        return configHandle.get(configNodes.netherAnimation);
    }

    /**
     *
     * @param netherAnimation
     * @return
     *
     * @since 5.1
     */
    @ApiStatus.AvailableSince("5.1")
    public Try<Void> setNetherAnimation(boolean netherAnimation) {
        return configHandle.set(configNodes.netherAnimation, netherAnimation);
    }

    /**
     *
     * @return
     *
     * @since 5.1
     */
    @ApiStatus.AvailableSince("5.1")
    public boolean getTeleportVehicles() {
        return configHandle.get(configNodes.teleportVehicles);
    }

    /**
     *
     * @param teleportVehicles
     * @return
     *
     * @since 5.1
     */
    @ApiStatus.AvailableSince("5.1")
    public Try<Void> setTeleportVehicles(boolean teleportVehicles) {
        return configHandle.set(configNodes.teleportVehicles, teleportVehicles);
    }

    /**
     *
     * @return
     *
     * @since 5.1
     */
    @ApiStatus.AvailableSince("5.1")
    public boolean getUseOnMove() {
        return configHandle.get(configNodes.useOnMove);
    }

    /**
     *
     * @param useOnMove
     * @return
     *
     * @since 5.1
     */
    @ApiStatus.AvailableSince("5.1")
    public Try<Void> setUseOnMove(boolean useOnMove) {
        return configHandle.set(configNodes.useOnMove, useOnMove);
    }

    /**
     *
     * @return
     *
     * @since 5.1
     *
     * @deprecated Only here to allow for backwards compatibility with old methods in {@link MultiversePortals} class.
     */
    @ApiStatus.AvailableSince("5.1")
    @Deprecated(since = "5.1", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "6.0")
    public FileConfiguration getConfig() {
        return configHandle.getConfig();
    }
}
