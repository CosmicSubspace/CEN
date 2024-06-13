package com.cosmicsubspace.mc.cen;

import org.bukkit.plugin.java.JavaPlugin;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.entity.Player;
import org.bukkit.World;
import org.bukkit.Location;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.block.Block;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.Material;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.ItemStack;
import java.lang.Math;
import org.bukkit.ChatColor;
import java.util.Map;
import java.util.HashMap;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigurableElytraNerfs extends JavaPlugin 
{   
    Map<String,Long> lastNotified = new HashMap<>();
    class IcarusTickListener implements Listener{
        @EventHandler 
        public void onPlayerMove(PlayerMoveEvent evt){
            //getLogger().info("PlayerMove!");
        }
    }
    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        
        //getLogger().info("Enabling Icarus...");
        //getServer().getPluginManager().registerEvents(new IcarusTickListener(), this);
        BukkitScheduler scheduler = getServer().getScheduler();
        
        FileConfiguration config = getConfig();
        
        
        // ICARUS
        boolean conf_icarus_enabled = config.getBoolean("icarus-enabled");
        int conf_icarus_hit = config.getInt("icarus-durability-hit");
        boolean conf_icarus_allow_nether = config.getBoolean("icarus-allow-nether");
        boolean conf_icarus_allow_raining = config.getBoolean("icarus-allow-raining");
        int conf_icarus_minY = config.getInt("icarus-minimum-height");
        
        int icarus_hit_per_sec = (int)Math.round(conf_icarus_hit*2/432.0*100);
        
        String icarus_warn_prefix=
            "["+
            ChatColor.BLUE+"CEN"+
            ChatColor.RESET+"/"+
            ChatColor.AQUA+"ICARUS"+
            ChatColor.RESET+"] ";
        String icarus_warn_line1=
            icarus_warn_prefix+
            ChatColor.RED+ChatColor.BOLD+"!!! YOUR WINGS ARE MELTING !!!"+
            ChatColor.RESET;
        String icarus_warn_line2;
        if (conf_icarus_allow_nether){
            icarus_warn_line2=
                icarus_warn_prefix+
                ChatColor.GRAY+ChatColor.ITALIC+
                "When flying under direct sunlight,"+
                ChatColor.RESET;
        }else{
            icarus_warn_line2=
                icarus_warn_prefix+
                ChatColor.GRAY+ChatColor.ITALIC+
                "When flying under direct sunlight (or in nether),"+
                ChatColor.RESET;
        }
        
        String icarus_warn_line3=
            icarus_warn_prefix+
            ChatColor.GRAY+ChatColor.ITALIC+
            "Your elytra will take "+icarus_hit_per_sec+"% damage every second."+
            ChatColor.RESET;
        
        
        Runnable icarusRunnable = new Runnable() {
            @Override
            public void run() {
                long systemTime=System.currentTimeMillis();
                for (Player p: getServer().getOnlinePlayers()){
                    String pname=p.getName();
                    boolean gliding = p.isGliding();
                    Location loc=p.getLocation();                    
                    
                    World w=p.getWorld();
                    Chunk c=w.getChunkAt(loc);
                    ChunkSnapshot cs=c.getChunkSnapshot();
                    Block highestBlock= w.getHighestBlockAt(loc); //excludes passable blocks - is this what we want? idk
                    Location hightestBlockLoc=highestBlock.getLocation();
                    
                    boolean skylightEnabled = w.hasSkyLight();
                    
                    int chunkX=((loc.getBlockX()%16)+16)%16;
                    int chunkY=loc.getBlockY();
                    int chunkZ=((loc.getBlockZ()%16)+16)%16;
                    int skylight=cs.getBlockSkyLight(chunkX,chunkY,chunkZ);
                    
                    Block playerBlock = c.getBlock(chunkX,chunkY,chunkZ);
                    int lightTotal = playerBlock.getLightLevel();
                    int lightSky = playerBlock.getLightFromSky();
                    int lightBlock = playerBlock.getLightFromBlocks();
                    
                    long time=w.getTime();
                    // Below is the time range where daylight is at 15 (strongest)
                    // For a more generous definition of day, we can use 23000~13000
                    // Which includes dusk/sunrise
                    boolean isDay = (time>=0) && (time<=12000);
                    boolean sunUp;
                    if (conf_icarus_allow_raining){
                        sunUp = w.isClearWeather() && isDay && w.hasSkyLight();
                    }else{
                        sunUp = isDay && w.hasSkyLight();
                    }
                    
                    boolean sunlightOnPlayer = (skylight==15) && sunUp;
                    
                    if (!conf_icarus_allow_nether){
                        if (w.isUltraWarm()) sunlightOnPlayer=true;
                    }
                    
                    boolean height_high = loc.getBlockY() > conf_icarus_minY;
                    
                    PlayerInventory pinv= p.getInventory();
                    ItemStack chestplate=pinv.getChestplate();
                    int damage=-1;
                    boolean wearingElytra=false;
                    if (chestplate != null){
                        wearingElytra = (chestplate.getType() == Material.ELYTRA);
                        ItemMeta imeta=chestplate.getItemMeta();
                        if (wearingElytra && sunlightOnPlayer && gliding && height_high){
                            if (imeta instanceof Damageable){
                                Damageable dmg = (Damageable)imeta;
                                damage=dmg.getDamage();
                                damage+=conf_icarus_hit; // 10/432 = ~2.5%/0.5s -> ~5%/sec
                                if (damage>=Material.ELYTRA.getMaxDurability()){
                                    damage=Material.ELYTRA.getMaxDurability()-1;
                                }
                                dmg.setDamage(damage);
                                float durabilityRatio=1.0f-damage/(float)Material.ELYTRA.getMaxDurability();
                                chestplate.setItemMeta(dmg);
                                pinv.setChestplate(chestplate);
                                
                                if (lastNotified.get(pname)==null){
                                    lastNotified.put(pname,-1000000L);
                                }
                                if (Math.abs(lastNotified.get(pname)-systemTime)>15000){
                                    lastNotified.put(pname,systemTime);
                                    /*
                                    p.sendTitle(
                                        "", //title
                                        ""+ChatColor.RED+"Your wings are melting!"+ChatColor.RESET, //subtitle
                                        10, //fadein, ticks
                                        60, //sustain, ticks
                                        20); //FadeOut, ticks
                                    */
                                    p.sendMessage(icarus_warn_line1);
                                    p.sendMessage(icarus_warn_line2);
                                    p.sendMessage(icarus_warn_line3);
                                }
                                
                                p.sendTitle(
                                        "", //title
                                        ChatColor.RED+"Elytra "+Math.round(durabilityRatio*100)+"%", //subtitle
                                        0, //fadein, ticks
                                        20, //sustain, ticks
                                        20); //FadeOut, ticks
                                
                            }
                        }
                    }
                    
                    
                    //getLogger().info("Player: "+pname+" SL: "+skylight+" SOP: "+sunlightOnPlayer+" G: "+gliding+" WE: "+wearingElytra+" CD: "+damage );
                    
                    
                }
            }
        };
        if (conf_icarus_enabled)    
            scheduler.scheduleSyncRepeatingTask(this,icarusRunnable, 0L, 10L);
    }
    @Override
    public void onDisable() {
        //getLogger().info("Disabling Icarus...");
    }
}


