package com.klin.holoItems.collections.gamers.koroneCollection.items;

import com.klin.holoItems.HoloItems;
import com.klin.holoItems.abstractClasses.Crate;
import com.klin.holoItems.collections.gamers.koroneCollection.KoroneCollection;
import com.klin.holoItems.interfaces.Placeable;
import com.klin.holoItems.utility.Utility;
import org.bukkit.*;
import org.bukkit.block.Barrel;
import org.bukkit.block.Block;
import org.bukkit.block.TileState;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashSet;

public class DeliveryDrone extends Crate implements Placeable {
    public static final String name = "deliveryDrone";
    public static final HashSet<Enchantment> accepted = null;

    private static final Material material = Material.BARREL;
    private static final int quantity = 1;
    private static final String lore =
            "§6Ability" +"/n"+
                "Contains contents to be delivered";
    private static final int durability = 0;
    public static final boolean stackable = false;
    private static final boolean shiny = false;

    public static final int cost = 0;
    public static final char key = '0';
    public static final String id = ""+ KoroneCollection.key+key;

    public DeliveryDrone(){
        super(name, material, quantity, lore, durability, stackable, shiny, cost, id, key);
    }

    public void registerRecipes(){
        ShapedRecipe recipe =
                new ShapedRecipe(new NamespacedKey(HoloItems.getInstance(), name), item);
        recipe.shape("aaa"," b "," c ");
        recipe.setIngredient('a', Material.SKELETON_SKULL);
        recipe.setIngredient('b', Material.BARREL);
        recipe.setIngredient('c', Material.IRON_HORSE_ARMOR);
        recipe.setGroup(name);
        Bukkit.getServer().addRecipe(recipe);
    }

    public void ability(BlockPlaceEvent event){
        event.setCancelled(false);
        TileState state = (TileState) event.getBlockPlaced().getState();
        state.getPersistentDataContainer().set(Utility.key, PersistentDataType.STRING, id);
        state.update();
    }

    public void ability(BlockBreakEvent event) {
        event.setDropItems(false);
        super.ability(event);

        Block block = event.getBlock();
        Location loc = block.getLocation();
        World world = loc.getWorld();
        for(ItemStack content : ((Barrel) block.getState()).getInventory().getContents()) {
            if(content!=null && content.getType()!=Material.AIR)
                world.dropItemNaturally(loc, content);
        }
    }
}