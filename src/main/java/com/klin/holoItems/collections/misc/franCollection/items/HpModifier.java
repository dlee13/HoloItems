package com.klin.holoItems.collections.misc.franCollection.items;

import com.klin.holoItems.Item;
import com.klin.holoItems.collections.misc.franCollection.FranCollection;
import com.klin.holoItems.interfaces.combinable.Combinable;
import com.klin.holoItems.interfaces.combinable.Spawnable;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

import java.util.Set;

public class HpModifier extends Item implements Combinable, Spawnable {
    public static final String name = "hpModifier";
    public static final Set<Enchantment> accepted = null;

    private static final Material material = Material.RED_DYE;
    private static final int quantity = 1;
    private static final String lore =
            "§6Ability" +"/n"+
                "Rename to set HP";
    private static final int durability = 0;
    public static final boolean stackable = false;
    private static final boolean shiny = true;

    public static final int cost = -1;
    public static final char key = '2';

    public HpModifier(){
        super(name, accepted, material, quantity, lore, durability, stackable, shiny, cost,
                ""+FranCollection.key+key, key);
    }

    public void registerRecipes(){}

    public String processInfo(ItemStack item) {
        return ":"+item.getItemMeta().getDisplayName();
    }

    public void ability(LivingEntity entity, String info) {
        try{
            entity.setMaxHealth(Math.max(0.1, Integer.parseInt(info)-0.1));
        }
        catch(NumberFormatException ignored){}
    }
}
