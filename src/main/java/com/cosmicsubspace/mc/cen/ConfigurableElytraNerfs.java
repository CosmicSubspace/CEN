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
import java.util.List;
import java.util.ArrayList;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.util.Vector;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionEffect;

public class ConfigurableElytraNerfs extends JavaPlugin 
{   
    
    class IcarusTickListener implements Listener{
        @EventHandler 
        public void onPlayerMove(PlayerMoveEvent evt){
            //getLogger().info("PlayerMove!");
        }
    }
    
    Map<String,Map<String,Long>> lastNotifiedTime = new HashMap<>();
    boolean rateLimitMsg(String type, String username, int millisec){
        if (lastNotifiedTime.get(type) == null){
            lastNotifiedTime.put(type,new HashMap<>());
        }
        Map<String,Long> username2time = lastNotifiedTime.get(type);
        if (username2time.get(username)==null){
            username2time.put(username,-1000000L);
        }
        long lastNotified=username2time.get(username);
        long t=System.currentTimeMillis();
        if (Math.abs(t-lastNotified)>millisec){
            username2time.put(username,t);
            return true;
        }
        return false;
    }
    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        
        //getLogger().info("Enabling Icarus...");
        //getServer().getPluginManager().registerEvents(new IcarusTickListener(), this);
        BukkitScheduler scheduler = getServer().getScheduler();
        
        FileConfiguration config = getConfig();
        
        boolean conf_all_disable = config.getBoolean("cen_all_disable");
        boolean conf_use_ticktime = config.getBoolean("cen-use-tick-time");
        
        
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
                                
                                if (rateLimitMsg("Icarus",pname,10000)){
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
        if ((!conf_all_disable) && conf_icarus_enabled)    
            scheduler.scheduleSyncRepeatingTask(this,icarusRunnable, 0L, 10L);
        
        
        // Glider
        String glider_warn=
            "["+
            ChatColor.BLUE+"CEN"+
            ChatColor.RESET+"/"+
            ChatColor.AQUA+"Glider"+
            ChatColor.RESET+"] "+
            ChatColor.RED+ChatColor.BOLD+"Elytra boosting is not allowed on this server!"+
            ChatColor.RESET;
        
        boolean conf_glider_enabled = config.getBoolean("glider-enabled");
        Listener gliderListener = new Listener(){
            @EventHandler 
            public void onPlayerInteract(PlayerInteractEvent evt){
                //getLogger().info("PlayerInteract!");
                Player p=evt.getPlayer();
                Material mat=evt.getMaterial();
                boolean is_fw=(mat==Material.FIREWORK_ROCKET);
                boolean gliding = p.isGliding();
                //getLogger().info("FW "+is_fw+" GL "+gliding);
                if (is_fw && gliding){
                    evt.setCancelled(true);
                    if (rateLimitMsg("glider",p.getName(),1000)){
                        p.sendMessage(glider_warn);
                    }
                }
            }
        };
        if ((!conf_all_disable) && conf_glider_enabled)    
            getServer().getPluginManager().registerEvents(gliderListener, this);
        
        
        
        // Terminal Velocity
        boolean conf_tv_enabled = config.getBoolean("terminal-velocity-enabled");
        // Convert from m/s to m/tick
        double conf_tv_maxvel_mps = config.getDouble("terminal-velocity-speed");
        double conf_tv_maxvel_mpt = conf_tv_maxvel_mps/20.0;
         
        String termvel_warn=
            "["+
            ChatColor.BLUE+"CEN"+
            ChatColor.RESET+"/"+
            ChatColor.AQUA+"Terminal Velocity"+
            ChatColor.RESET+"] "+
            ChatColor.RED+"Max elytra speed is "+
            ChatColor.BOLD+conf_tv_maxvel_mps+"m/s"+
            ChatColor.RESET;
        
        Listener termvelListener = new Listener(){
            @EventHandler 
            public void onPlayerMove(PlayerMoveEvent evt){
                //getLogger().info("PlayerInteract!");
                Player p=evt.getPlayer();
                Vector v=p.getVelocity();
                double speed = v.length(); 
                boolean gliding = p.isGliding();
                boolean overspeed=speed>conf_tv_maxvel_mpt;
                
                //getLogger().info("SPD "+(speed/20)+"m/s GL "+gliding+" OS "+overspeed);
                if (overspeed && gliding){
                    p.setVelocity(v.normalize().multiply(conf_tv_maxvel_mpt));
                    if (rateLimitMsg("TermVel",p.getName(),10000)){
                        p.sendMessage(termvel_warn);
                    }
                }
            }
        };
        if ((!conf_all_disable) && conf_tv_enabled)    
            getServer().getPluginManager().registerEvents(termvelListener, this);
            
        
        // Limit Boost
        boolean conf_limitboost_enabled = config.getBoolean("limit-boost-enabled");
        int conf_limitboost_time_ms = (int)Math.round(config.getDouble("limit-boost-time-period")*1000);
        int conf_limitboost_count = (int)config.getDouble("limit-boost-count");
        String limitboost_warn_tmp=
            "["+
            ChatColor.BLUE+"CEN"+
            ChatColor.RESET+"/"+
            ChatColor.AQUA+"LimitBoost"+
            ChatColor.RESET+"] "+
            ChatColor.RED+"You may only boost ";
            
        if (conf_limitboost_count!=1)
            limitboost_warn_tmp = limitboost_warn_tmp+
            ChatColor.BOLD+conf_limitboost_count+
            ChatColor.RESET+ChatColor.RED+" times every ";
        else
            limitboost_warn_tmp = limitboost_warn_tmp+
            ChatColor.BOLD+"ONCE"+
            ChatColor.RESET+ChatColor.RED+" every ";
        limitboost_warn_tmp = limitboost_warn_tmp+
            ChatColor.BOLD+(conf_limitboost_time_ms/1000)+
            ChatColor.RESET+ChatColor.RED+" seconds."+
            ChatColor.RESET;
        
        final String limitboost_warn=limitboost_warn_tmp;
        Map<String,List<Long>> boost_log= new HashMap();
        
        Listener limitboostListener = new Listener(){
            @EventHandler 
            public void onPlayerInteract(PlayerInteractEvent evt){
                
                Player p=evt.getPlayer();
                String pn = p.getName();
                Material mat=evt.getMaterial();
                boolean is_fw=(mat==Material.FIREWORK_ROCKET);
                boolean gliding = p.isGliding();
                
                long t;
                if (conf_use_ticktime) t = p.getWorld().getFullTime()*50;
                else t=System.currentTimeMillis();
                //getLogger().info("LB FW "+is_fw+" GL "+gliding+" T "+t);
                if (is_fw && gliding){
                    if (boost_log.get(pn) == null) boost_log.put(pn,new ArrayList());
                    
                    List<Long> personalList = boost_log.get(pn);
                    while (
                        personalList.size()>0
                        && (Math.abs(personalList.get(0)-t)>conf_limitboost_time_ms))
                        personalList.remove(0);
                    
                    // Remove duplicate event
                    if (personalList.size()>0 &&
                        (Math.abs(personalList.get(personalList.size()-1)-t)<10)){}
                    else{
                        //getLogger().info("BL "+personalList.size());
                        if (personalList.size()>=conf_limitboost_count){
                            evt.setCancelled(true);
                            if (rateLimitMsg("limitboost",p.getName(),1000)){
                                p.sendMessage(limitboost_warn);
                            }
                        }else{
                            personalList.add(t);
                        }
                    }
                }
            }
        };
        if ((!conf_all_disable) && conf_limitboost_enabled)    
            getServer().getPluginManager().registerEvents(limitboostListener, this);
            
            
        // Acrophobia
        boolean conf_acrophobia_enabled = config.getBoolean("acrophobia-enabled");
        double conf_acrophobia_height = config.getDouble("acrophobia-height");
        double conf_acrophobia_duration_sec = config.getDouble("acrophobia-duration");
        int conf_acrophobia_duration_ticks = (int)Math.round(conf_acrophobia_duration_sec*20);
        int conf_acrophobia_power = (int)config.getDouble("acrophobia-power")-1;
        double conf_acrophobia_delay = config.getDouble("acrophobia-delay");
        
        String acrophobia_prefix=
            "["+
            ChatColor.BLUE+"CEN"+
            ChatColor.RESET+"/"+
            ChatColor.AQUA+"Acrophobia"+
            ChatColor.RESET+"] ";
        String acrophobia_warn=
            acrophobia_prefix+
            ChatColor.RED+"You are afraid of heights..."+
            ChatColor.RESET;        
        String acrophobia_notice=
            acrophobia_prefix+
            ChatColor.RED+ChatColor.BOLD+"You are blinded by fear!"+
            ChatColor.RESET;        
        
        Map<String,Long> lastOnGround = new HashMap<>();
        
        Runnable acrophobiaRunnable = new Runnable() {
            @Override
            public void run() {
                for (Player p: getServer().getOnlinePlayers()){
                    long t;
                    if (conf_use_ticktime) t = p.getWorld().getFullTime()*50;
                    else t=System.currentTimeMillis();
                
                    String pname=p.getName();
                    boolean gliding = p.isGliding();
                    Location loc=p.getLocation();                    
                    
                    World w=p.getWorld();
                    Chunk c=w.getChunkAt(loc);
                    ChunkSnapshot cs=c.getChunkSnapshot();
                    Block highestBlock= w.getHighestBlockAt(loc); //excludes passable blocks - is this what we want? idk
                    Location hightestBlockLoc=highestBlock.getLocation();
                    
                    boolean tooHigh = (loc.getY() - hightestBlockLoc.getY())>conf_acrophobia_height;
                    boolean scared=tooHigh && gliding;
                    
                    if (lastOnGround.get(pname)==null) lastOnGround.put(pname,0L);
                    
                    if (!scared) lastOnGround.put(pname,t);
                    else{
                        if (rateLimitMsg("acrophobia-warn",p.getName(),10000)){
                                p.sendMessage(acrophobia_warn);
                            }
                        if (t-lastOnGround.get(pname)>(conf_acrophobia_delay*1000-0.1)){
                            p.addPotionEffect(
                                new PotionEffect(
                                    PotionEffectType.BLINDNESS,
                                    conf_acrophobia_duration_ticks,
                                    conf_acrophobia_power, true // ambient
                                    ));
                            if (rateLimitMsg("acrophobia-notice",p.getName(),10000)){
                                p.sendMessage(acrophobia_notice);
                            }
                        }
                    }
                    
                    //getLogger().info("Player: "+pname+" Y: "+loc.getY()+" HBy: "+hightestBlockLoc.getY()+" G: "+gliding);
                    
                    
                }
            }
        };
        if ((!conf_all_disable) && conf_acrophobia_enabled)    
            scheduler.scheduleSyncRepeatingTask(this,acrophobiaRunnable, 5L, 10L);
    }
    @Override
    public void onDisable() {
        //getLogger().info("Disabling Icarus...");
    }
}


