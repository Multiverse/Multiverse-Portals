package com.onarandombox.MultiversePortals.utils;

import org.bukkit.block.Block;

public class NetherPortalRotator implements BlockModifier {

    @Override
    public void modify(Block block) {
        org.bukkit.block.data.Orientable blockData = (org.bukkit.block.data.Orientable) block.getBlockData();
        blockData.setAxis(org.bukkit.Axis.X);
        block.setBlockData(blockData, false);
    }
}
