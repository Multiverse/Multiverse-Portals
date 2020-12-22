/*
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.
 * Multiverse 2 is licensed under the BSD License.
 * For more information please check the README.md file included
 * with this project
 */

package com.onarandombox.MultiversePortals.commands;

//public class DebugCommand extends PortalCommand {
//
//    public DebugCommand(MultiversePortals plugin) {
//        super(plugin);
//        this.setName("Portal Debug Mode");
//        this.setCommandUsage("/mvp debug" + ChatColor.GOLD + " [on|off]");
//        this.setArgRange(0, 1);
//        this.addKey("mvp debug");
//        this.addKey("mvpd");
//        this.addKey("mvpdebug");
//        this.setPermission("multiverse.portal.debug", "Instead of teleporting you to a place when you walk into a portal you will see the details about it. This command toggles.", PermissionDefault.OP);
//    }
//
//    @Override
//    public void runCommand(CommandSender sender, List<String> args) {
//        Player p = null;
//        if (!(sender instanceof Player)) {
//            sender.sendMessage("This command must be run by a player");
//            return;
//        }
//        p = (Player) sender;
//
//        if (!this.plugin.getCore().getMVWorldManager().isMVWorld(p.getWorld().getName())) {
//            this.plugin.getCore().showNotMVWorldMessage(sender, p.getWorld().getName());
//            return;
//        }
//
//        PortalPlayerSession ps = this.plugin.getPortalSession(p);
//        if (args.size() == 1) {
//            ps.setDebugMode(args.get(0).equalsIgnoreCase("on"));
//            return;
//        }
//        ps.setDebugMode(!ps.isDebugModeOn());
//    }
//}
