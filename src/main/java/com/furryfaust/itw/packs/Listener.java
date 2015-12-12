package com.furryfaust.itw.packs;

import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;

public class Listener implements org.bukkit.event.Listener {

    Packs pl;

    public Listener(Packs pl) {
        this.pl = pl;
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        pl.db.stripeInvites(event.getPlayer().getUniqueId());
    }

}
