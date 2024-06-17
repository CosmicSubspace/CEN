# Configurable Elytra Nerfs (CEN)
**A Spigot plugin for nerfing Elytras**  
Tested working on Spigot `1.17` `1.18` `1.19` `1.20` `1.21`  
[SpigotMC Resource Link](https://www.spigotmc.org/resources/configurable-elytra-nerfs.117404/)

*Elytras are overpowered.*  
Once a player obtains an elytra, all methods of transportation, be it boats, minecarts, ice boats, horses, etc are instantly made obsolete, and in my opinion, is no fun.

So this server-side Spigot plugin gives you a few methods to nerf Elytras.  
There are many modules in this plugin, and you can enable and disable them independently of one another.  
Just enabling one module will be enough to nerf Elytras significantly to encourage other transportation methods,
but there's nothing stopping you from enabling multiple modules at once.  
Each module works a bit differently, and should have a different impact on the play style of the players.  
So experiment freely!


## Modules & Config options
### Common options
Below are options that affect all modules.

```
# Disables all modules
cen-all-disable: false

# Use game tick time or system clock time as time measurement?
# For example, when the server's running at 10TPS,
# if below is true, all time options will effectively be doubled.
cen-use-tick-time: true
```

### Icarus
![Demo of Icarus module](https://i.imgur.com/6QQ89HY.gif)  
When flying under direct sunlight or in the nether,  
the elytra in use will take 5%(configurable) durability damage each second, ignoring Unbreaking enchants. Like in vanilla, the elytra will never fully break though.  
Does not penalize Elytra usage in the End, at night, or while raining.  
Encourages building elytra tunnels, for safe flight during daytime. Also allows to use the elytra as a 20-second one-time-use transportation method.

```
icarus-enabled: true
# How many durability points to subtract every half-second
# Note that an elytra has 432 total durabilty.
icarus-durability-hit: 10 
icarus-allow-nether: false # Allow elytra in the Nether.
icarus-allow-raining: true # Allow elytra when it is raining (overworld)
icarus-minimum-height: 60 #Minimum Y-height for the effect to kick in
```

### Acrophobia
![Demo of Acrophobia module](https://i.imgur.com/9kqJzST.gif)  
Applies Blindness when flying too high from the closest terrain.  
Encourages building elytra 'highways', a flat road for low-altitude flying.
```
acrophobia-enabled: true
acrophobia-height: 20 #meters over terrain
acrophobia-duration: 5 #seconds
acrophobia-delay: 2 #seconds
acrophobia-power: 1 #power of Blindness effect
```

### Terminal Velocity
![Demo of Terminal Velocity module](https://i.imgur.com/jfQVWAP.gif)  
Enforces an upper limit on Elytra glide speed.  
Straightforward and easy to understand.
```
terminal-velocity-enabled: true
terminal-velocity-speed: 10
```

### Glider
![Demo of Glider module](https://i.imgur.com/X1ns1Wk.gif)  
Disables firework boosting, essentially making the Elytra into a glider.  
Encourages building towers and launch infrastructure.
```
glider-enabled: true
```

### Limit Boost
![Demo of Limit Boost module](https://i.imgur.com/H3ef9mv.gif)  
Limits how many times you can boost in a certain time period.  
Default limit is two fireworks every 60 seconds.  
Encourages longer-duration fireworks and short 'hops'.
```
limit-boost-enabled: true
limit-boost-time-period: 60 #seconds
limit-boost-count: 2 #fireworks
```

## Commands
This plugin only exposes one command.   
All configuration is done through `config.yaml`, not though commands.

`/cen`  
Print information about the plugin.

## Building
`mvn package`
