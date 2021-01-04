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

        player.sendMessage((portalSession.selectPortal(portal))
                ? String.format("You have successfully selected portal %s%s%s!",
                ChatColor.AQUA, portal.getName(), ChatColor.WHITE)
                : String.format("%s There was an error selecting portal %s%s%s! Please check console for more details.",
                ChatColor.RED, ChatColor.AQUA, portal.getName(), ChatColor.RED));
    }

    private void showCurrentSelection(@NotNull Player player,
                                      @NotNull PortalPlayerSession ps) {

        MVPortal selectedPortal = ps.getSelectedPortal();
        if (selectedPortal == null) {
            player.sendMessage(String.format("%sYou do not have any selection yet.", ChatColor.RED));
            player.sendMessage(String.format("Do %s/mvp select <portal> %sto select a portal!", ChatColor.AQUA, ChatColor.WHITE));
            return;
        }

        player.sendMessage(String.format("You have currently selected portal %s%s%s.",
                ChatColor.AQUA, selectedPortal.getName(), ChatColor.WHITE));
    }
}
