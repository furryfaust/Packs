package com.furryfaust.itw.packs;

import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_8_R3.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.v1_8_R3.PacketPlayOutTitle;
import org.bukkit.Chunk;
import org.bukkit.craftbukkit.v1_8_R3.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.FixedMetadataValue;

public class Listener implements org.bukkit.event.Listener {

    Packs pl;

    public Listener(Packs pl) {
        this.pl = pl;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        player.setMetadata("chunk", new FixedMetadataValue(pl, ""));
        player.setMetadata("chunk_x", new FixedMetadataValue(pl, player.getLocation().getChunk().getX()));
        player.setMetadata("chunk_z", new FixedMetadataValue(pl, player.getLocation().getChunk().getZ()));
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        pl.db.stripeInvites(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        int x = player.getMetadata("chunk_x").get(0).asInt(),
                z = player.getMetadata("chunk_z").get(0).asInt();
        int playerX = player.getLocation().getChunk().getX(),
                playerZ = player.getLocation().getChunk().getZ();

        if (!(playerX != x || playerZ != z)) {
            return;
        }

        player.removeMetadata("chunk_x", pl);
        player.removeMetadata("chunk_z", pl);
        player.setMetadata("chunk_x", new FixedMetadataValue(pl, playerX));
        player.setMetadata("chunk_z", new FixedMetadataValue(pl, playerZ));

        String previous = player.getMetadata("chunk").get(0).asString();
        Pack pack = new Pack(pl.db.getPackAtChunk(event.getPlayer().getLocation().getChunk()));

        String claimer = pack.exists() ? pack.getName() : "";
        String message = claimer.equals("") ?
                "&cWilderness" :
                "&b" + claimer + "'s Territory";

        if (!claimer.equals(previous)) {
            sendEnterTitle(player, ChatColor.translateAlternateColorCodes('&', message));
            player.removeMetadata("chunk", pl);
            player.setMetadata("chunk", new FixedMetadataValue(pl, claimer));
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        Action action = event.getAction();
        if (!(action == Action.LEFT_CLICK_BLOCK || action == Action.RIGHT_CLICK_BLOCK)) {
            return;
        }

        Chunk chunk = event.getClickedBlock().getChunk();
        int x = player.getMetadata("chunk_x").get(0).asInt(),
                z = player.getMetadata("chunk_z").get(0).asInt();

        Pack pack = new Pack(pl.db.getPack(event.getPlayer().getUniqueId()));

        if (x == chunk.getX() && z == chunk.getZ()) {
            String curr = player.getMetadata("chunk").get(0).asString();
            if (curr == "") {
                return;
            }

            if (!curr.equals(pack.getName())) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYou cannot interact with blocks in " + curr + "'s territory"));
                event.setCancelled(true);
            }
        } else {
            Pack claimer = new Pack(pl.db.getPackAtChunk(event.getClickedBlock().getChunk()));
            if (!claimer.exists()) {
                return;
            }

            if (!pack.exists() || pack.exists() && !pack.getName().equals(claimer.getName())) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYou cannot interact with blocks in " + claimer.getName() + "'s territory"));
                event.setCancelled(true);
            }
        }
    }

    private void sendEnterTitle(Player player, String territory) {
        EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();

        IChatBaseComponent title = IChatBaseComponent.ChatSerializer.a("{\"text\":\"Entering\"}");
        PacketPlayOutTitle titlePacket = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TITLE, title, 1, 2, 1);
        entityPlayer.playerConnection.sendPacket(titlePacket);

        IChatBaseComponent subtitle = IChatBaseComponent.ChatSerializer.a("{\"text\":\"" + territory + "\"}");
        PacketPlayOutTitle subtitlePacket = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.SUBTITLE, subtitle, 1, 2, 1);
        entityPlayer.playerConnection.sendPacket(subtitlePacket);
    }
}
