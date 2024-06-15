# Configurable Elytra Nerfs (CEN)
**A Spigot plugin for nerfing Elytras**

Elytras are overpowered.
Once players obtain an elytra and puts Unbreaking and Mending on it,
all methods of transportation, be it boats, minecarts, ice boats, horses, etc
are made obsolete instantly, and in my opinion, is no fun.

So this server-side plugin gives you a few methods to nerf Elytras.
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

# Use tick time as time measurement?
# When the server's running at 10TPS,
# if below is true, all time options will effectively be doubled.
cen-use-tick-time: true
```


### Terminal Velocity
Enforces an upper limit on Elytra glide speed.
Straightforward and easy to understand.
```
terminal-velocity-enabled: true
terminal-velocity-speed: 16
```


### Glider
Disables firework boosting, essentially making the Elytra into a glider.
Encourages building towers and launch infrastructure.

`glider-enabled: true`


### Limit Boost
Limits how many times you can boost in a certain time period.
Default limit is one firework every 60 seconds.
Encourages longer-duration fireworks and short 'hops'.
```
limit-boost-enabled: true
limit-boost-time-period: 60 #seconds
limit-boost-count: 1 #fireworks
```

### Acrophobia
Applies Blindness when flying too high, measured as a distance from the closest terrain.
Encourages building elytra 'highways', a flat road for low-altitude flying.
```
acrophobia-enabled: true
acrophobia-height: 10 #meters over terrain
acrophobia-duration: 10 #seconds
acrophobia-delay: 2 #seconds
acrophobia-power: 1 #power of Blindness effect
```


### Icarus
When flying under direct sunlight or in the nether,
the elytra in use will take 5%(configurable) durability damage each second, ignoring Unbreaking enchants. Like in vanilla, the elytra will never fully break though.
Does not penalize Elytra usage in the End, at night, or while raining.
Encourages building elytra tunnels, for safe flight during daytime.
Also allows to use the elytra as a 20-second one-time-use transportation method

```
icarus-enabled: true
# How many durability points to subtract every half-second
# Note that an elytra has 432 total durabilty.
icarus-durability-hit: 10 
icarus-allow-nether: false # Allow elytra in the Nether.
icarus-allow-raining: true # Allow elytra when it is raining (overworld)
icarus-minimum-height: 60 #Minimum Y-height for the effect to kick in
```

## Commands
Only exposes one command. All configuration is done through `config.yaml`

`/cen`
Print information about the plugin.

## Building
`mvn package`