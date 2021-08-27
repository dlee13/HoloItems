package com.klin.holoItems.dungeons.inaDungeon.classes;

import com.klin.holoItems.HoloItems;
import com.klin.holoItems.collections.dungeons.inaDungeonCollection.items.Torrent;
import com.klin.holoItems.utility.Task;
import com.klin.holoItems.utility.Utility;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.Set;

public class Gura extends Class{
    private final Set<Material> projectiles;

    public Gura(Player player){
        super(player);
        projectiles = Set.of(Material.BOW, Material.CROSSBOW, Material.TRIDENT, Material.FISHING_ROD, Material.EXPERIENCE_BOTTLE,
                Material.LINGERING_POTION, Material.SPLASH_POTION, Material.EGG, Material.ENDER_PEARL, Material.ENDER_EYE, Material.SNOWBALL);
    }

    public void ability(double angle, PlayerInteractEvent event) {
        if(Math.abs(angle)<Math.PI*1.5)
            return;
        Action action = event.getAction();
        if(cooldown || action!=Action.RIGHT_CLICK_AIR && action!=Action.RIGHT_CLICK_BLOCK)
            return;
        ItemStack item = event.getItem();
        if(item==null)
            return;
        Material material = item.getType();
        if(!projectiles.contains(material))
            return;
        ItemMeta meta = item.getItemMeta();
        if(meta==null)
            return;
//        if(material==Material.CROSSBOW && !((CrossbowMeta) meta).hasChargedProjectiles())
//            return;
        cooldown = true;
        meta.getPersistentDataContainer().set(Utility.key, PersistentDataType.STRING, Torrent.id);
        item.setItemMeta(meta);
        player.setVelocity(new Vector(0, 0.45+0.15*Utility.checkPotionEffect(player, PotionEffectType.JUMP), 0));
        PotionEffect effect = player.getPotionEffect(PotionEffectType.SLOW);
        int duration;
        if(effect==null)
            duration = 0;
        else
            duration = effect.getDuration();
        if(duration<120)
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 120, 1));
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("Command: Torrent"));
        new Task(HoloItems.getInstance(), 1, 1){
            int increment = 0;
            public void run(){
                if(increment>=120 || !player.isHandRaised() || ((Entity) player).isOnGround()){
                    cooldown = false;
                    meta.getPersistentDataContainer().remove(Utility.key);
                    item.setItemMeta(meta);
                    if(duration<120)
                        player.removePotionEffect(PotionEffectType.SLOW_FALLING);
                    cancel();
                    return;
                }
                increment++;
                if(increment>=20)
                    return;
                Vector velocity = player.getVelocity();
                double y = velocity.getY();
                if(y<0)
                    player.setVelocity(velocity.setY(y*-1));
            }
        };
    }
}
