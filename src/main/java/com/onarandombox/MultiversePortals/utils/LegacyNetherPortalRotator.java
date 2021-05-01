package com.onarandombox.MultiversePortals.utils;

import org.bukkit.block.Block;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class LegacyNetherPortalRotator implements BlockModifier {

    static Method legacyBlockSetData;

    static {
        try {
            legacyBlockSetData = Block.class.getDeclaredMethod("setData", byte.class, boolean.class);
            legacyBlockSetData.setAccessible(true);
        } catch (NoSuchMethodException e) {
            // Ignore
        }
    }

    @Override
    public void modify(Block block) {
        try {
            legacyBlockSetData.invoke(block, (byte) 2, false);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
