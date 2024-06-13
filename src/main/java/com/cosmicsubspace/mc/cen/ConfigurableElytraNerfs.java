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
        //getLogger().info("Enabling Icarus...");
        //getServer().getPluginManager().registerEvents(new IcarusTickListener(), this);
        BukkitScheduler scheduler = getServer().getScheduler();
        scheduler.scheduleSyncRepeatingTask(this, new Runnable() {
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
                    boolean sunUp = w.isClearWeather() && isDay && w.hasSkyLight();
                    
                    boolean sunlightOnPlayer = (skylight==15) && sunUp;
                    
                    if (w.isUltraWarm()) sunlightOnPlayer=true;
                    
                    
                    
                    PlayerInventory pinv= p.getInventory();
                    ItemStack chestplate=pinv.getChestplate();
                    int damage=-1;
                    boolean wearingElytra=false;
                    if (chestplate != null){
                        wearingElytra = (chestplate.getType() == Material.ELYTRA);
                        ItemMeta imeta=chestplate.getItemMeta();
                        if (wearingElytra && sunlightOnPlayer && gliding){
                            if (imeta instanceof Damageable){
                                Damageable dmg = (Damageable)imeta;
                                damage=dmg.getDamage();
                                damage+=10; // 10/432 = ~2.5%/0.5s -> ~5%/sec
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
                                    String prefix="["+ChatColor.BLUE+"CEN"+ChatColor.RESET+"/"+ChatColor.AQUA+"ICARUS"+ChatColor.RESET+"] ";
                                    p.sendMessage(prefix+ChatColor.RED+ChatColor.BOLD+"!!! YOUR WINGS ARE MELTING !!!"+ChatColor.RESET);
                                    p.sendMessage(prefix+ChatColor.GRAY+ChatColor.ITALIC+"When flying under direct sunlight (or in nether),"+ChatColor.RESET);
                                    p.sendMessage(prefix+ChatColor.GRAY+ChatColor.ITALIC+"Your elytra will take 5% damage every second."+ChatColor.RESET);
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
        }, 0L, 10L);
    }
    @Override
    public void onDisable() {
        //getLogger().info("Disabling Icarus...");
    }
}


