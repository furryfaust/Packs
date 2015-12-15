package com.furryfaust.itw.packs;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Command implements CommandExecutor {

    Packs pl;

    public Command(Packs pl) {
        this.pl = pl;
    }

    public boolean onCommand(CommandSender commandSender, org.bukkit.command.Command command, String cmd, String[] args) {
        Player player = null;
        if (commandSender instanceof Player)
            player = (Player) commandSender;

        if (args.length == 0)
            return true;

        switch (args[0]) {
            case "create":
                return create(player, commandSender, args);
            case "info":
                return info(player, commandSender, args);
            case "disband":
                return disband(player, commandSender, args);
            case "invite":
                return invite(player, commandSender, args);
            case "join":
                return join(player, commandSender, args);
            case "leave":
                return leave(player, commandSender, args);
        }
        return false;
    }

    public boolean create(Player player, CommandSender commandSender, String[] args) {
        if (player == null) {
            message(commandSender, "&cYou must be a player to create a pack.");
            return false;
        }

        if (pl.db.getPack(player.getUniqueId()) != null) {
            message(player, "&cYou are already in a pack.");
            return false;
        }

        if (args.length != 2) {
            message(player, "&cIncorrect arguments length.");
            return false;
        }

        if (pl.db.getPack(args[1]) != null) {
            message(player, "&cThis pack name is taken.");
            return false;
        }


        pl.db.createPack(args[1], player.getUniqueId().toString());
        message(player, "&aYou have successfully created the pack " + args[1]);
        return true;
    }

    public boolean info(Player player, CommandSender commandSender, String[] args) {
        if (args.length == 1 && player != null) {
            Pack pack = new Pack(pl.db.getPack(player.getUniqueId()));
            if (!pack.exists()) {
                message(player, "&cYou are not in a pack.");
                return false;
            }

            System.out.println(pack.getAlpha());
            System.out.println(pack.getOnlineMembers());
            System.out.println(pack.getOfflineMembers());

            message(player, "=============");
            return true;
        }
        return false;
    }

    public boolean disband(Player player, CommandSender commandSender, String[] args) {
        if (player == null) {
            message(commandSender, "&cYou must be a player to disband a pack.");
            return false;
        }

        Pack pack = new Pack(pl.db.getPack(player.getUniqueId()));
        if (!pack.exists()) {
            message(player, "&cYou are not in a pack.");
            return false;
        }

        if (!pl.db.disbandPack(player.getUniqueId())) {
            message(player, "&cYou are not the alpha of the pack.");
            return false;
        }

        pack.sendAll("&a" + player.getName() + "disbanded the pack.");
        return true;
    }

    public boolean invite(Player player, CommandSender commandSender, String[] args) {
        if (player == null) {
            message(commandSender, "&cYou must be a player to invite a player to a pack.");
            return false;
        }

        if (args.length != 2) {
            message(player, "&cIncorrect arguments length.");
            return false;
        }

        Pack pack = new Pack(pl.db.getPack(player.getUniqueId()));
        if (!pack.exists()) {
            message(player, "&cYou are not in a pack.");
            return false;
        }

        if (player.getName().equals(args[1])) {
            message(player, "&cYou cannot invite yourself to a pack.");
            return false;
        }

        Player invited = pl.getServer().getPlayerExact(args[1]);
        if (invited == null) {
            message(player, "&cYou cannot invite an offline player.");
            return false;
        }

        if (pl.db.getPack(invited.getUniqueId()) != null) {
            message(player, "&cYou cannot invite someone already in a pack.");
            return false;
        }

        if (!pl.db.inviteToPack(invited.getUniqueId(), pack.getName())) {
            message(player, "&cYou have already invited this player.");
            return false;
        }

        pack.sendAll("&a" + player.getName() + " invited " + args[1] + " to the pack.");
        message(invited, "&aYou have been invited to " + pack.getName());
        return true;
    }

    public boolean join(Player player, CommandSender commandSender, String[] args) {
        if (player == null) {
            message(commandSender, "&cYou must be a player to join a pack.");
            return false;
        }

        if (args.length != 2) {
            message(player, "&cIncorrect arguments length.");
            return false;
        }

        if (pl.db.getPack(player.getUniqueId()) != null) {
            message(player, "&cYou are already in a pack.");
            return false;
        }

        Pack pack = new Pack(pl.db.getPack(args[1]));
        if (!pack.exists()) {
            message(player, "&cThis pack does not exist.");
            return false;
        }

        if (!pl.db.joinPack(player.getUniqueId(), args[1])) {
            message(player, "&cYou have not been invited to this pack.");
            return false;
        }

        pack.sendAll("&c" + player.getName() + " joined the pack.");
        return true;
    }

    public boolean leave(Player player, CommandSender commandSender, String[] args) {
        if (player == null) {
            message(commandSender, "&cYou must be a player to leave a pack.");
            return false;
        }

        Pack pack = new Pack(pl.db.getPack(player.getUniqueId()));
        if (pack == null) {
            message(player, "&cYou are not in a pack.");
            return false;
        }

        if (pack.getAlpha() == player.getName()) {
            message(player, "&cYou cannot leave a pack you are alpha of.");
            return false;
        }

        pl.db.leavePack(player.getUniqueId(), pl.db.getPack(player.getUniqueId()).getString("Name"));
        message(player, "&aYou have left your pack.");
        return true;
    }

    private void message(CommandSender sender, String message) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7[&9&lPacks&7] " + message));
    }

}
