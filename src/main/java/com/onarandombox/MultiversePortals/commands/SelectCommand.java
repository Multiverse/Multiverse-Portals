package com.onarandombox.MultiversePortals.commands;

import com.onarandombox.MultiversePortals.MVPortal;
import com.onarandombox.MultiversePortals.MultiversePortals;
import com.onarandombox.MultiversePortals.PortalPlayerSession;
import com.onarandombox.acf.annotation.CommandAlias;
import com.onarandombox.acf.annotation.CommandCompletion;
import com.onarandombox.acf.annotation.Description;
import com.onarandombox.acf.annotation.Optional;
import com.onarandombox.acf.annotation.Subcommand;
import com.onarandombox.acf.annotation.Syntax;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@CommandAlias("mvp")
public class SelectCommand extends PortalCommand {

    private static final Set<String> requiredPermissions = Collections.unmodifiableSet(new HashSet<String>() {{
        add("AND");
        add("multiverse.portal.select");
        add("multiverse.portal.create");
    }});

    public SelectCommand(MultiversePortals plugin) {
        super(plugin);
    }

    @Override
    public Set<String> getRequiredPermissions() {
        return requiredPermissions;
    }

    @Subcommand("select")
    @Syntax("[portal]")
    @CommandCompletion("@MVPortals")
    @Description("Selects a portal so you can perform multiple modifications on it.")
    public void onSelectCommand(@NotNull Player player,
                                @NotNull PortalPlayerSession portalSession,

                                @Syntax("<portal>")
                                @Description("Portal name that you want to select.")
                                @Nullable @Optional MVPortal portal) {

        if (portal == null) {
            showCurrentSelection(player, portalSession);
            return;
        }

        portalSession.selectPortal(portal);
        player.sendMessage("You have successfully selected portal " + ChatColor.AQUA + portal.getName() + ChatColor.WHITE + "!");
    }

    private void showCurrentSelection(@NotNull Player player,
                                      @NotNull PortalPlayerSession ps) {

        MVPortal selectedPortal = ps.getSelectedPortal();
        if (selectedPortal == null) {
            player.sendMessage(ChatColor.RED + "You do not have any selection yet.");
            player.sendMessage("Do " + ChatColor.GREEN + "/mvp select <portal>" + ChatColor.WHITE + " to select a portal!");
            return;
        }

        player.sendMessage("You have currently selected portal " + ChatColor.AQUA + selectedPortal.getName() + ChatColor.WHITE + ".");
    }

    //TODO: Deselect portal.
}
