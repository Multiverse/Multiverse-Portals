package org.mvplugins.multiverse.portals;

import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;
import org.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.core.inject.PluginServiceLocator;
import org.mvplugins.multiverse.portals.utils.PortalFiller;
import org.mvplugins.multiverse.portals.utils.PortalManager;

import java.util.Objects;

public class MultiversePortalsApi {

    private static MultiversePortalsApi instance;

    static void init(@NotNull MultiversePortals multiversePortals) {
        if (instance != null) {
            throw new IllegalStateException("MultiversePortalsApi has already been initialized!");
        }
        instance = new MultiversePortalsApi(multiversePortals.getServiceLocator());
        Bukkit.getServicesManager().register(MultiversePortalsApi.class, instance, multiversePortals, ServicePriority.Normal);
    }

    static void shutdown() {
        Bukkit.getServicesManager().unregister(instance);
        instance = null;
    }

    /**
     * Gets the MultiversePortalsApi. This will throw an exception if the Multiverse-Portals has not been initialized.
     *
     * @return The MultiversePortalsApi
     */
    public static @NotNull MultiversePortalsApi get() {
        if (instance == null) {
            throw new IllegalStateException("MultiversePortalsApi has not been initialized!");
        }
        return instance;
    }

    private final PluginServiceLocator serviceLocator;

    private MultiversePortalsApi(@NotNull PluginServiceLocator serviceProvider) {
        this.serviceLocator = serviceProvider;
    }

    /**
     * Gets the instance of the PortalFiller.
     *
     * @return The PortalFiller instance
     */
    public @NotNull PortalFiller getPortalFiller() {
        return Objects.requireNonNull(serviceLocator.getService(PortalFiller.class));
    }

    /**
     * Gets the instance of the PortalManager.
     *
     * @return The PortalManager instance
     */
    public @NotNull PortalManager getPortalManager() {
        return Objects.requireNonNull(serviceLocator.getService(PortalManager.class));
    }

    /**
     * Gets the instance of Multiverse-Portals's PluginServiceLocator.
     * <br/>
     * You can use this to hook into the hk2 dependency injection system used by Multiverse-Portals.
     *
     * @return The Multiverse-Portals's PluginServiceLocator
     */
    public @NotNull PluginServiceLocator getServiceLocator() {
        return serviceLocator;
    }
}
