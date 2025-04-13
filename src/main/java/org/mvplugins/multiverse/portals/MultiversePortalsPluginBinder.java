package org.mvplugins.multiverse.portals;

import org.mvplugins.multiverse.core.module.MultiverseModuleBinder;
import org.mvplugins.multiverse.external.glassfish.hk2.utilities.binding.ScopedBindingBuilder;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;

public class MultiversePortalsPluginBinder  extends MultiverseModuleBinder<MultiversePortals> {

    protected MultiversePortalsPluginBinder(@NotNull MultiversePortals plugin) {
        super(plugin);
    }

    @Override
    protected ScopedBindingBuilder<MultiversePortals> bindPluginClass(
            ScopedBindingBuilder<MultiversePortals> bindingBuilder) {
        return super.bindPluginClass(bindingBuilder).to(MultiversePortals.class);
    }
}
