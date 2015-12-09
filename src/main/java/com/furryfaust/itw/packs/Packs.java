package com.furryfaust.itw.packs;

import org.bukkit.plugin.java.JavaPlugin;

public class Packs extends JavaPlugin {

    public Database db;

    @Override
    public void onEnable() {
        db = new Database(getConfig());
        getServer().getPluginManager().registerEvents(new Listener(this), this);
        getCommand("pack").setExecutor(new Command(this));
    }

}
