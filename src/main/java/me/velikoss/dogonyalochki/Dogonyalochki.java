package me.velikoss.dogonyalochki;

import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public final class Dogonyalochki extends JavaPlugin implements CommandExecutor, Listener {

    String current;
    String last;
    Component currents = Component.text("Ждём игроков...");
    int time = 200;
    PotionEffect effect = new PotionEffect(PotionEffectType.SLOW, 100, 10);
    HashMap<String, Integer> peeCount = new HashMap<>();

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void playerHit(EntityDamageByEntityEvent e) {
        if(e.getEntityType() != EntityType.PLAYER || e.getDamager().getType() != EntityType.PLAYER || !e.getDamager().getName().equals(current) || e.getEntity().getName().equals(last)) return;
        last = current; current = e.getEntity().getName();
        currents = Component.text("Текущий догоняла: " + current);
        effect.apply((LivingEntity) e.getEntity());
        time = 200;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player p = (Player) sender;
        if(label == "pee") {
            peeCount.put(p.getName(), 100);
        }else{
            Location loc = new Location(p.getWorld(), p.getEyeLocation().getX(), p.getEyeLocation().getY() - 0.5, p.getEyeLocation().getZ());
            Snowball snowball = p.getWorld().spawn(loc,
                    Snowball.class);
            snowball.setVelocity(p.getLocation().getDirection()
                    .multiply(1.5));
            snowball.setShooter(p);
        }
        return false;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if(Bukkit.getOnlinePlayers().size() < 2) getServer().sendActionBar(Component.text("Ждём пидорков..."));
        else if(current == null || current.isEmpty()) {
            current = ((Player)Bukkit.getOnlinePlayers().toArray()[new Random().nextInt(Bukkit.getOnlinePlayers().size())]).getName();
            currents = Component.text("Текущий догоняла: " + current);
        }
    }

    @EventHandler
    public void rageQuit(PlayerQuitEvent e){
        if(!e.getPlayer().getName().equals(current)) return;
        while(Objects.requireNonNull(Bukkit.getPlayer(current)).getGameMode() == GameMode.SPECTATOR) current = ((Player)Bukkit.getOnlinePlayers().toArray()[new Random().nextInt(Bukkit.getOnlinePlayers().size())]).getName();
        currents = Component.text("Текущий догоняла: " + current);
        if(peeCount.containsKey(e.getPlayer().getName())) peeCount.remove(e.getPlayer().getName());
    }

    @EventHandler
    public void tick(ServerTickEndEvent e) {
        getServer().sendActionBar(currents);
        if(time-- < 0) {last = ""; time = -1; currents = Component.text("Текущий догоняла: " + current);}
        else currents = Component.text("Текущий догоняла: ").append(Component.text(current).color(TextColor.color(100,100,100)));
        Bukkit.getOnlinePlayers().forEach(p -> {
            if(peeCount.containsKey(p.getName())) {
                int pe = peeCount.getOrDefault(p.getName(), 100);
                pe--;
                peeCount.put(p.getName(), pe);
                Location loc = new Location(p.getWorld(), p.getEyeLocation().getX(), p.getEyeLocation().getY() - 0.5, p.getEyeLocation().getZ());
                Snowball snowball = p.getWorld().spawn(loc,
                        Snowball.class);
                snowball.setItem(new ItemStack(Material.HONEY_BLOCK));
                snowball.setVelocity(p.getLocation().getDirection()
                        .multiply(1.5));
                snowball.setShooter(p);
                if(pe < 0) peeCount.remove(p.getName());
            }
        });
    }
}
