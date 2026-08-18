package org.mvplugins.multiverse.portals;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import com.dumptruckman.minecraft.util.Logging;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.core.inject.PluginServiceLocator;
import org.mvplugins.multiverse.external.vavr.control.Try;
import org.mvplugins.multiverse.portals.action.ActionHandlerProvider;
import org.mvplugins.multiverse.portals.config.PortalsConfig;
import org.mvplugins.multiverse.portals.utils.PortalFiller;
import org.mvplugins.multiverse.portals.utils.PortalManager;

public final class MultiversePortalsApi {

    private static MultiversePortalsApi instance;
    private static final List<Consumer<MultiversePortalsApi>> WHEN_LOADED_CALLBACKS = new ArrayList<>();

    static void init(@NotNull MultiversePortals multiversePortals) {
        if (instance != null) {
            throw new IllegalStateException("MultiversePortalsApi has already been initialized!");
        }
        instance = new MultiversePortalsApi(multiversePortals.getServiceLocator());
        Bukkit.getServicesManager().register(MultiversePortalsApi.class, instance, multiversePortals, ServicePriority.Normal);

        List<Consumer<MultiversePortalsApi>> callbacks = List.copyOf(WHEN_LOADED_CALLBACKS);
        WHEN_LOADED_CALLBACKS.clear();
        MultiversePortalsApi loadedApi = instance;
        callbacks.forEach(callback -> runLoadCallback(callback, loadedApi));
    }

    private static void runLoadCallback(
            @NotNull Consumer<MultiversePortalsApi> callback,
            @NotNull MultiversePortalsApi loadedApi) {
        Try.run(() -> callback.accept(loadedApi))
                .onFailure(exception -> Logging.warning(
                        "A Multiverse-Portals API load callback failed: %s", exception.getMessage()));
    }

    static void shutdown() {
        if (instance == null) {
            return;
        }
        Bukkit.getServicesManager().unregister(instance);
        instance = null;
    }

    /**
     * Executes a callback once the Multiverse-Portals API has been initialized.
     * The callback is executed immediately if the API is already initialized.
     *
     * @param consumer The callback to execute
     *
     * @since 5.3
     */
    @ApiStatus.AvailableSince("5.3")
    public static void whenLoaded(@NotNull Consumer<MultiversePortalsApi> consumer) {
        if (instance != null) {
            consumer.accept(instance);
        } else {
            WHEN_LOADED_CALLBACKS.add(consumer);
        }
    }

    /**
     * Checks whether the Multiverse-Portals API has been initialized.
     *
     * @return {@code true} if the API has been initialized, otherwise {@code false}
     *
     * @since 5.3
     */
    @ApiStatus.AvailableSince("5.3")
    public static boolean isLoaded() {
        return instance != null;
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
     * Gets the instance of the PortalsConfig.
     *
     * @return The PortalsConfig instance
     *
     * @since 5.1
     */
    @ApiStatus.AvailableSince( "5.1")
    public @NotNull PortalsConfig getPortalsConfig() {
        return Objects.requireNonNull(serviceLocator.getService(PortalsConfig.class));
    }

    /**
     * Gets the instance of ActionHandlerProvider.
     *
     * @return The ActionHandlerProvider instance.
     *
     * @since 5.3
     */
    @ApiStatus.AvailableSince("5.3")
    public @NotNull ActionHandlerProvider getActionHandlerProvider() {
        return Objects.requireNonNull(serviceLocator.getService(ActionHandlerProvider.class));
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
