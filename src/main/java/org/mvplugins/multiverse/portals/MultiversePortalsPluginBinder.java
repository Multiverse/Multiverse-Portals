package org.mvplugins.multiverse.portals;

import org.mvplugins.multiverse.core.MultiversePlugin;
import org.mvplugins.multiverse.core.inject.binder.JavaPluginBinder;
import org.mvplugins.multiverse.external.glassfish.hk2.utilities.binding.ScopedBindingBuilder;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;

public class MultiversePortalsPluginBinder  extends JavaPluginBinder<MultiversePortals> {

    protected MultiversePortalsPluginBinder(@NotNull MultiversePortals plugin) {
        super(plugin);
    }

    @Override
    protected ScopedBindingBuilder<MultiversePortals> bindPluginClass(
            ScopedBindingBuilder<MultiversePortals> bindingBuilder) {
        return super.bindPluginClass(bindingBuilder).to(MultiversePlugin.class).to(MultiversePortals.class);
    }
}
