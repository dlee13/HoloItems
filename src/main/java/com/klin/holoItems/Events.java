package com.klin.holoItems;

import com.klin.holoItems.abstractClasses.Enchant;
import com.klin.holoItems.abstractClasses.Pack;
import com.klin.holoItems.abstractClasses.Wiring;
import com.klin.holoItems.collections.gen1.melCollection.items.ReadingGlasses;
import com.klin.holoItems.collections.gen2.shionCollection.items.Fireball;
import com.klin.holoItems.collections.gen2.shionCollection.items.SecretBrew;
import com.klin.holoItems.collections.gen3.noelCollection.items.MilkBottle;
import com.klin.holoItems.collections.gen3.pekoraCollection.items.DoubleUp;
import com.klin.holoItems.collections.gen3.pekoraCollection.items.PekoNote;
import com.klin.holoItems.collections.gen5.botanCollection.items.ScopedRifle;
import com.klin.holoItems.collections.gen5.botanCollection.items.Sentry;
import com.klin.holoItems.collections.gen5.lamyCollection.items.Starch;
import com.klin.holoItems.collections.hidden.opCollection.items.GalleryFrame;
import com.klin.holoItems.collections.hidden.opCollection.items.QuartzGranule;
import com.klin.holoItems.collections.misc.ingredientsCollection.IngredientsCollection;
import com.klin.holoItems.interfaces.*;
import com.klin.holoItems.interfaces.customMobs.Retaliable;
import com.klin.holoItems.interfaces.customMobs.Targetable;
import com.klin.holoItems.utility.ReflectionUtils;
import com.klin.holoItems.utility.Utility;
import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.block.data.Levelled;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.*;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import java.util.*;

import static org.bukkit.Material.*;

public class Events implements Listener {
    private final Map<Material, Material> buckets = Map.of(
            BUCKET, CAULDRON,
            WATER_BUCKET, WATER_CAULDRON,
            LAVA_BUCKET, LAVA_CAULDRON,
            POWDER_SNOW_BUCKET, POWDER_SNOW_CAULDRON,
            BOWL, BOWL
    );
    private final Set<String> ingredients = new HashSet<>() {{
        for (Item item : Collections.collections.get(IngredientsCollection.name).collection)
            add(item.name);
        add(SecretBrew.name);
        add(DoubleUp.name);
        add(PekoNote.name);
        add(MilkBottle.name);
        add(Sentry.name);
        add(ScopedRifle.name);
        add(Starch.name);
    }};
    //add permissible interfaces for each prohibitedInv
    private final Set<InventoryType> prohibitedInv = Set.of(
            InventoryType.BEACON,
            InventoryType.BREWING,
            InventoryType.CARTOGRAPHY,
            InventoryType.LECTERN,
            InventoryType.LOOM,
            InventoryType.MERCHANT,
            InventoryType.SMOKER,
            InventoryType.STONECUTTER
    );
    private final Map<Integer, Enchantment[]> multiplier = new HashMap<>() {{
        put(1, new Enchantment[]{
                Enchantment.PROTECTION_ENVIRONMENTAL, Enchantment.DAMAGE_ALL,
                Enchantment.DIG_SPEED, Enchantment.ARROW_DAMAGE,
                Enchantment.LOYALTY, Enchantment.PIERCING
        });
        put(2, new Enchantment[]{
                Enchantment.PROTECTION_FIRE, Enchantment.PROTECTION_FALL,
                Enchantment.PROTECTION_PROJECTILE, Enchantment.DAMAGE_UNDEAD,
                Enchantment.DAMAGE_ARTHROPODS, Enchantment.KNOCKBACK,
                Enchantment.DURABILITY, Enchantment.QUICK_CHARGE
        });
        put(4, new Enchantment[]{
                Enchantment.PROTECTION_EXPLOSIONS, Enchantment.OXYGEN,
                Enchantment.DEPTH_STRIDER, Enchantment.WATER_WORKER,
                Enchantment.FIRE_ASPECT, Enchantment.LOOT_BONUS_MOBS,
                Enchantment.LOOT_BONUS_BLOCKS, Enchantment.ARROW_KNOCKBACK,
                Enchantment.ARROW_FIRE, Enchantment.LUCK, Enchantment.LURE,
                Enchantment.FROST_WALKER, Enchantment.MENDING, Enchantment.IMPALING,
                Enchantment.RIPTIDE, Enchantment.MULTISHOT, Enchantment.SWEEPING_EDGE
        });
        put(8, new Enchantment[]{
                Enchantment.THORNS, Enchantment.SILK_TOUCH, Enchantment.ARROW_INFINITE,
                Enchantment.BINDING_CURSE, Enchantment.VANISHING_CURSE,
                Enchantment.CHANNELING, Enchantment.SOUL_SPEED
        });
    }};
    private final Set<Set<Enchantment>> with = Set.of(
            Set.of(Enchantment.DAMAGE_ALL, Enchantment.DAMAGE_UNDEAD, Enchantment.DAMAGE_ARTHROPODS),
            Set.of(Enchantment.PROTECTION_FIRE, Enchantment.PROTECTION_PROJECTILE, Enchantment.PROTECTION_ENVIRONMENTAL, Enchantment.PROTECTION_FALL, Enchantment.PROTECTION_EXPLOSIONS),
            Set.of(Enchantment.DEPTH_STRIDER, Enchantment.FROST_WALKER),
            Set.of(Enchantment.LOOT_BONUS_BLOCKS, Enchantment.SILK_TOUCH),
            Set.of(Enchantment.ARROW_INFINITE, Enchantment.MENDING),
            Set.of(Enchantment.MULTISHOT, Enchantment.PIERCING)
    );
    private final Map<Enchantment, Set<Enchantment>> exclusive = new HashMap<>(){{
        put(Enchantment.CHANNELING, Set.of(Enchantment.RIPTIDE));
        put(Enchantment.LOYALTY, Set.of(Enchantment.RIPTIDE));
        put(Enchantment.RIPTIDE, Set.of(Enchantment.CHANNELING, Enchantment.LOYALTY));
        for(Set<Enchantment> incompatible : with){
            for(Enchantment enchantment : incompatible)
                put(enchantment, incompatible);
        }
    }};
    public static Set<Activatable> activatables = new HashSet<>();
    public static Set<Player> bedrock = new HashSet<>();

    @EventHandler(ignoreCancelled = true)
    public void clickItem(InventoryClickEvent event){
        InventoryView invView = event.getView();
        Inventory inv = invView.getTopInventory();
        InventoryType invType = inv.getType();
        Player player = (Player) event.getWhoClicked();

        int slot = event.getRawSlot();
        boolean current = true;
        for (ItemStack item : new ItemStack[]{event.getCurrentItem(), event.getCursor()}) {
            if (item == null || item.getItemMeta() == null) {
                current = false;
                continue;
            }
            ItemMeta meta = item.getItemMeta();
            String id = meta.getPersistentDataContainer().get(Utility.key, PersistentDataType.STRING);
            if (id == null) {
                current = false;
                continue;
            }
            if(current && Utility.onCooldown(item)) {
                meta.getPersistentDataContainer().remove(Utility.cooldown);
                item.setItemMeta(meta);
                return;
            }
            if (Collections.disabled.contains(id)) {
                current = false;
                continue;
            }
            Item generic = Collections.items.get(id);
            if (generic instanceof Clickable)
                ((Clickable) generic).ability(event, current);
            current = false;
        }

        if(inv.getHolder()==null){
            if(invView.getTitle().equals("Price")){
                (new GalleryFrame()).ability(event);
                return;
            }

            ItemStack item = player.getInventory().getItemInMainHand();
            if(item.getType()!=Material.AIR && item.getItemMeta()!=null) {
                String id = item.getItemMeta().
                        getPersistentDataContainer().get(Utility.key, PersistentDataType.STRING);
                if (id != null) {
                    Item generic = Collections.items.get(id);
                    if (generic instanceof Pack && (((Pack) generic).display ||
                            item.equals(event.getCurrentItem()))) {
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }
        ItemStack item = event.getCurrentItem();
        if (item==null || item.getItemMeta()==null)
            return;
        Item generic = Utility.findItem(item, Item.class);
        if (generic == null)
            return;
        if(invType==InventoryType.GRINDSTONE){
            if(slot!=2) {
//                inv.setItem(0, item);
//                item.setAmount(0);
                return;
            }
            ItemStack combined = inv.getItem(2);
            ItemMeta meta = combined.getItemMeta();
            List<String> lore = meta.getLore();
            if (lore!=null && !lore.isEmpty()) {
                String enchants = meta.getPersistentDataContainer().get(Utility.enchant, PersistentDataType.STRING);
                if(enchants==null)
                    return;
                event.setCancelled(true);
                for(String ignored : enchants.split(" "))
                    lore.remove(0);
                lore.remove(0);
                meta.setLore(lore);
                meta.getPersistentDataContainer().remove(Utility.enchant);
                combined.setItemMeta(meta);
                inv.setItem(2, combined);
            }
            return;
        }
        //rewrite to account for new smeltable items
        if(slot>=inv.getSize() && prohibitedInv.contains(invType) && !ingredients.contains(generic.name))
            event.setCancelled(true);
    }

    private void combine(InventoryHolder holder, ItemStack result, int cost){
        Player player = (Player) holder;
        int level = player.getLevel();
        if(level<cost)
            player.sendMessage("§a[§5Shion§a]§f: Guess who's covering the "+cost+" level exp cost");
        else {
            player.setLevel(level - cost);
            PlayerInventory inv = player.getInventory();
            inv.setItemInMainHand(result);
            inv.setItemInOffHand(null);
            player.sendMessage("§a[§5Shion§a]§f: You're welcome");
        }
    }

    @EventHandler
    public void combineItems(PrepareAnvilEvent event){
        Inventory inv = event.getView().getTopInventory();
        boolean anvil = inv instanceof AnvilInventory;
        ItemStack reactant;
        ItemStack reagent;
        if(anvil){
            reactant = inv.getItem(0);
            reagent = inv.getItem(1);
        } else{
            InventoryHolder holder = inv.getHolder();
            if(holder instanceof Player)
                inv = ((Player) inv.getHolder()).getInventory();
            else
                return;
            reactant = ((PlayerInventory) inv).getItemInMainHand();
            reagent = ((PlayerInventory) inv).getItemInOffHand();
        } if(reactant==null || reagent==null)
            return;
        Item item = Utility.findItem(reactant, Item.class);
        Item enchantItem = Utility.findItem(reagent, Item.class);
        if(item==null && enchantItem==null)
            return;
        else if(enchantItem instanceof Enchant && !enchantItem.equals(item)) {
            Enchant enchant = (Enchant) enchantItem;
            String enchants = reactant.getItemMeta().getPersistentDataContainer().get(Utility.enchant, PersistentDataType.STRING);
            if (enchants != null && enchants.contains(enchant.name)) {
                event.setResult(null);
                return;
            }
            if ((enchant.acceptedIds == null || item == null || !enchant.acceptedIds.contains(item.name)) &&
                    (enchant.acceptedTypes == null || !(item==null && enchant.acceptedTypes.contains(reactant.getType())))) {
                event.setResult(null);
                return;
            }
            ItemStack result = Utility.addEnchant(reactant.clone(), enchant);
            int cost = Math.min(39, enchant.expCost + ReflectionUtils.getRepairCost(reactant));
            ItemMeta meta = result.getItemMeta();
            if(anvil) {
                String renameText = ((AnvilInventory) inv).getRenameText();
                String originalText = meta.hasDisplayName() ? meta.getDisplayName().substring(2) : "";
                if (renameText != null && !renameText.trim().isEmpty() && !renameText.equals(originalText)) {
                    meta.setDisplayName("§6" + renameText);
                    result.setItemMeta(meta);
                    cost++;
                }
                ((AnvilInventory) inv).setRepairCost(cost);
                if (enchant.exclusive != null) {
                    for (Enchantment enchantment : result.getEnchantments().keySet()) {
                        if (enchant.exclusive.contains(enchantment))
                            result.removeEnchantment(enchantment);
                    }
                }
            }
            if (item == null) {
                if (meta instanceof Damageable) {
                    meta.setUnbreakable(true);
                    meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
                    meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                }
                List<String> addDurability = meta.getLore();
                if (addDurability == null)
                    addDurability = new ArrayList<>();
                if (!addDurability.get(addDurability.size() - 1).startsWith("§fDurability: ")) {
                    int maxDurability = reactant.getType().getMaxDurability();
                    int currDurability = maxDurability - ((Damageable) meta).getDamage();
                    addDurability.add("");
                    addDurability.add("§fDurability: " + currDurability + "/" + maxDurability);
                    meta.setLore(addDurability);
                }
                result.setItemMeta(meta);
            } if (!result.equals(event.getResult())) {
                event.setResult(result);
                if(!anvil)
                    combine(inv.getHolder(), result, cost);
            }
            return;
        } else if(enchantItem==null){
            if(reagent.getType()==Material.ENCHANTED_BOOK && reagent.getItemMeta() instanceof EnchantmentStorageMeta){
                Map<Enchantment, Integer> enchantments = ((EnchantmentStorageMeta) reagent.getItemMeta()).getStoredEnchants();
                ItemStack result = reactant.clone();
                int levelCost = 0;
                if(item.accepted != null) {
                    String enchants = reactant.getItemMeta().getPersistentDataContainer().get(Utility.enchant, PersistentDataType.STRING);
                    Set<Enchantment> excluded = new HashSet<>();
                    if(enchants!=null){
                        for(String id : enchants.split(" ")){
                            Enchant reactantEnchant = Utility.findItem(id, Enchant.class);
                            if(reactantEnchant!=null && reactantEnchant.exclusive!=null)
                                excluded.addAll(reactantEnchant.exclusive);
                        }
                    }
                    for (Enchantment enchantment : enchantments.keySet()) {
                        if (item.accepted.contains(enchantment)) {
                            if(excluded.contains(enchantment))
                                continue;
                            Set<Enchantment> incompatible = exclusive.get(enchantment);
                            if(incompatible!=null) {
                                for (Enchantment resultEnchantment : result.getEnchantments().keySet()) {
                                    if (incompatible.contains(resultEnchantment))
                                        result.removeEnchantment(resultEnchantment);
                                }
                            }
                            int resultLevel = result.getItemMeta().getEnchantLevel(enchantment);
                            int enchantmentLevel = enchantments.get(enchantment);
                            if (resultLevel == enchantmentLevel && enchantmentLevel < enchantment.getMaxLevel())
                                enchantmentLevel++;
                            else if (resultLevel > enchantmentLevel)
                                enchantmentLevel = 0;
                            result.addUnsafeEnchantment(enchantment, Math.max(resultLevel, enchantmentLevel));
                            levelCost += enchantmentLevel * ((findMultiplier(enchantment) + 1) / 2);
                        }
                    }
                } if(levelCost>0) {
                    ItemMeta meta = result.getItemMeta();
                    if(anvil) {
                        String renameText = ((AnvilInventory) inv).getRenameText();
                        String originalText = meta.getDisplayName().substring(2);
                        if (renameText != null && !renameText.trim().isEmpty() && !renameText.equals(originalText)) {
                            meta.setDisplayName("§6" + renameText);
                            levelCost++;
                        }
                    }
                    List<String> lore = meta.getLore();
                    if (lore != null && !lore.isEmpty() && lore.get(0).startsWith("§b")) {
                        lore.add(0, "");
                        meta.setLore(lore);
                    }
                    result.setItemMeta(meta);
                    if (!result.equals(event.getResult())) {
                        event.setResult(result);
                        int cost = levelCost + Math.max(ReflectionUtils.getRepairCost(reactant), ReflectionUtils.getRepairCost(reagent));
                        if(anvil)
                            ((AnvilInventory) inv).setRepairCost(cost);
                        else
                            combine(inv.getHolder(), result, cost);
                    }
                } else event.setResult(null);
            } else event.setResult(null);
            return;
        } if(event.getResult()!=null || !enchantItem.equals(item))
            return;
        if(item.durability==0){
            event.setResult(null);
            return;
        }

        int levelCost = 0;
        ItemStack result = reactant.clone();
        int[] durabilityA = Utility.getDurability(reactant.getItemMeta().getLore());
        if(durabilityA[0]!=durabilityA[1] && reagent.getItemMeta().getLore()!=null) {
            int[] durabilityB = Utility.getDurability(reagent.getItemMeta().getLore());
            Utility.addDurability(result, durabilityB[0] + (int) (0.12 * durabilityB[1]), null);
            levelCost += 2;
        }
        Map<Enchantment, Integer> enchantments = new HashMap<>(reagent.getEnchantments());
        for(Enchantment enchantment : enchantments.keySet()) {
            if (item.accepted.contains(enchantment)){
                Set<Enchantment> incompatible = exclusive.get(enchantment);
                for (Enchantment resultEnchantment : result.getEnchantments().keySet()) {
                    if (incompatible.contains(resultEnchantment))
                        result.removeEnchantment(resultEnchantment);
                }
                int resultLevel = result.getItemMeta().getEnchantLevel(enchantment);
                int enchantmentLevel = enchantments.get(enchantment);
                if(resultLevel==enchantmentLevel && enchantmentLevel<enchantment.getMaxLevel())
                    enchantmentLevel++;
                else if(resultLevel>enchantmentLevel)
                    enchantmentLevel = 0;
                result.addUnsafeEnchantment(enchantment, Math.max(resultLevel, enchantmentLevel));
                levelCost += enchantmentLevel*(findMultiplier(enchantment)+1)/2;
            }
        }
        ItemMeta meta = result.getItemMeta();
        if(anvil) {
            String renameText = ((AnvilInventory) inv).getRenameText();
            String originalText = meta.getDisplayName().substring(2);
            if (renameText != null && !renameText.trim().isEmpty() && !renameText.equals(originalText)) {
                meta.setDisplayName("§6" + renameText);
                levelCost++;
            }
        }
        result.setItemMeta(meta);
        event.setResult(result);
        int cost = levelCost+ Math.max(ReflectionUtils.getRepairCost(reactant), ReflectionUtils.getRepairCost(reagent));
        if(anvil)
            ((AnvilInventory) inv).setRepairCost(cost);
        else
            combine(inv.getHolder(), result, cost);
    }

    @EventHandler(ignoreCancelled = true)
    public void craftItem(CraftItemEvent event){
        CraftingInventory inv = event.getInventory();
        for(ItemStack item : event.getInventory().getMatrix()){
            if(item!=null && item.getItemMeta()!=null){
                String id = item.getItemMeta().getPersistentDataContainer().
                        get(Utility.key, PersistentDataType.STRING);
                if(id!=null) {
                    if(ingredients.contains(id)) {
                        ItemStack result = event.getRecipe().getResult();
                        ItemMeta meta = result.getItemMeta();
                        if(meta==null || meta.getPersistentDataContainer().get(Utility.key, PersistentDataType.STRING)==null){
                            event.setCancelled(true);
                            return;
                        }
                        continue;
                    }
                    event.setCancelled(true);
                    return;
                }
            }
        }
        Player player = (Player) event.getWhoClicked();
        ItemStack curr = event.getCurrentItem();
        if (curr == null || curr.getItemMeta() == null)
            return;
        ItemMeta currMeta = curr.getItemMeta();
        String id = currMeta.getPersistentDataContainer().get(Utility.key, PersistentDataType.STRING);
        if (id == null)
            return;
        Item item = Collections.items.get(id);
        if(item==null)
            return;
        if(Collections.disabled.contains(id)) {
            player.sendMessage("§cThis item has been disabled");
            return;
        } if(player.getGameMode()!=GameMode.CREATIVE) {
            int progress = -2;
            for(Collection collection : Collections.collections.values()){
                if(collection.collection.contains(item)){
                    progress = Utility.add(collection.getStat(player));
                    break;
                }
            }
            if (item.cost > progress) {
                event.setCancelled(true);
                player.sendMessage("Progress to unlocking " + Utility.formatName(item.name) + ": " + progress + "/" + item.cost);
            } else if (!item.stackable && item.item.getMaxStackSize() > 1) {
                World world = player.getWorld();
                Location loc = player.getLocation();
                boolean excess = false;
                boolean reset = false;
                for (ItemStack ingredient : inv.getMatrix()) {
                    if (ingredient!=null) {
                        int amount = ingredient.getAmount();
                        ingredient.setAmount(amount-1);
                        excess = true;
                        if(amount==1)
                            reset = true;
                    }
                }
                currMeta.getPersistentDataContainer().set(Utility.stack, PersistentDataType.DOUBLE, Math.random());
                curr.setItemMeta(currMeta);
                if (excess) {
                    event.setCancelled(true);
                    world.dropItemNaturally(loc, curr);
                    if(reset)
                        curr.setAmount(0);
                    return;
                }
            }
        } if(item instanceof Craftable)
            ((Craftable) item).ability(event);
    }

    @EventHandler(ignoreCancelled = true)
    public void enchantItem(EnchantItemEvent event){
        ItemStack item = event.getItem();
        if(item.getItemMeta()==null)
            return;
        ItemMeta meta = item.getItemMeta();
        String id = meta.getPersistentDataContainer().get(Utility.key, PersistentDataType.STRING);
        if(id==null)
            return;

        Set<Enchantment> accepted = Collections.items.get(id).accepted;
        if(accepted==null) {
            event.setCancelled(true);
            return;
        }
        List<String> lore = meta.getLore();
        if(lore!=null && !lore.isEmpty() && lore.get(0).startsWith("§b")) {
            lore.add(0, "");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        int size = accepted.size();
        Enchantment[] acceptedArray = accepted.toArray(new Enchantment[size]);
        Map<Enchantment, Integer> enchantments = event.getEnchantsToAdd();
        Iterator<Enchantment> keys = new LinkedHashSet<>(enchantments.keySet()).iterator();
        boolean first = true;
        int initialSize = enchantments.size();

        Set<Enchantment> toRemove = new HashSet<>();
        for(int i=0; i<initialSize; i++){
            Enchantment next = keys.next();
            if(first){
                first = false;
                if(accepted.contains(next))
                    continue;
            }
            Enchantment randomEnchant;
            int failsafe = 0;
            do{
                randomEnchant = acceptedArray[(int) (Math.random()*size)];
                Set<Enchantment> incompatible = exclusive.get(randomEnchant);
                for (Enchantment enchantment : item.getEnchantments().keySet()) {
                    if (incompatible.contains(enchantment))
                        item.removeEnchantment(enchantment);
                }
                failsafe++;
            } while (item.containsEnchantment(randomEnchant) && failsafe<=5);
            item.addUnsafeEnchantment(randomEnchant, Math.min(randomEnchant.getMaxLevel(), enchantments.get(next)));
            if(!randomEnchant.equals(next))
                toRemove.add(next);
        }
        new BukkitRunnable() {
            public void run(){
                for(Enchantment enchant : toRemove)
                    item.removeEnchantment(enchant);
            }
        }.runTaskLater(HoloItems.getInstance(), 1);
    }

    @EventHandler
    public void mendItem(PlayerExpChangeEvent event){
        //no isCancelled()
        Player player = event.getPlayer();
        PlayerInventory inv = player.getInventory();
        for(ItemStack item : new ItemStack[]{inv.getItemInMainHand(), inv.getItemInOffHand(), inv.getHelmet(), inv.getChestplate(), inv.getLeggings(), inv.getBoots()}) {
            if (item == null || item.getType() == Material.AIR || item.getItemMeta() == null)
                continue;
            PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
            Item generic = Utility.findItem(container.get(Utility.key, PersistentDataType.STRING), Item.class, player);
            if(generic instanceof Chargeable)
                ((Chargeable) generic).ability(event);
            String enchants = container.get(Utility.enchant, PersistentDataType.STRING);
            if(enchants!=null){
                for(String enchant : enchants.split(" ")){
                    Chargeable chargeable = Utility.findItem(enchant, Chargeable.class, player);
                    if(chargeable!=null)
                        chargeable.ability(event);
                }
            }
            if (generic==null|| !item.getItemMeta().hasEnchant(Enchantment.MENDING))
                continue;
            int amount = Utility.addDurability(item, event.getAmount(), player);
            event.setAmount(amount);
            if(amount==0)
                return;
        }
    }

//    @EventHandler
//    public void smithItem(PrepareSmithingEvent event){
//        ItemStack result = event.getResult();
//        if(result==null)
//            return;
//        ItemMeta meta = result.getItemMeta();
//        if(meta==null)
//            return;
//        PersistentDataContainer container = meta.getPersistentDataContainer();
//        if(container.get(Utility.key, PersistentDataType.STRING)!=null || container.get(Utility.enchant, PersistentDataType.STRING)==null)
//            return;
//        List<String> addDurability = meta.getLore();
//        if (addDurability == null)
//            return;
//        int index = addDurability.size() - 1;
//        if (addDurability.get(index).startsWith("§fDurability: ")) {
//            int maxDurability = result.getType().getMaxDurability();
//            int[] durability = Utility.getDurability(addDurability);
//            int currDurability = maxDurability;
//            if(durability!=null)
//                currDurability -= durability[1] - durability[0];
//            addDurability.set(index, "§fDurability: " + currDurability + "/" + maxDurability);
//            meta.setLore(addDurability);
//            result.setItemMeta(meta);
//            event.setResult(result);
//        }
//    }

    @EventHandler(ignoreCancelled = true)
    public void activateAbility(CreatureSpawnEvent event){
        for(Activatable activatable : activatables) {
            if(activatable instanceof Spawnable)
                ((Spawnable) activatable).ability(event);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void afflictAbility(EntityDamageByEntityEvent event){
        Entity entity = event.getDamager();
        com.klin.holoItems.interfaces.Retaliable retaliable = Utility.findItem(entity, com.klin.holoItems.interfaces.Retaliable.class);
        if(retaliable!=null)
            retaliable.ability(event, entity);
        else {
            String modifiers = entity.getPersistentDataContainer().get(Utility.pack, PersistentDataType.STRING);
            if (modifiers != null) {
                for (String modifier : modifiers.split("-")) {
                    int index = modifier.indexOf(":");
                    Retaliable modified = Utility.findItem(index>-1?modifier.substring(0, index):modifier, Retaliable.class);
                    if (modified != null)
                        modified.ability(event, entity, index>-1?modifier.substring(index+1) : null);
                }
            }
        }

        if(!(entity instanceof LivingEntity))
            return;
        LivingEntity living = (LivingEntity) entity;
        EntityEquipment equipment = living.getEquipment();
        if(equipment==null)
            return;
        ItemStack item = equipment.getItemInMainHand();
        if(item.getType()==Material.AIR || item.getItemMeta()==null)
            return;
        String id = item.getItemMeta().getPersistentDataContainer().get(Utility.key, PersistentDataType.STRING);
        if(id!=null) {
            if(event.getDamage()>0)
                Utility.addDurability(item, -1, living);
            if (Collections.disabled.contains(id))
                return;
            Item generic = Collections.items.get(id);
            if (generic instanceof Afflictable)
                ((Afflictable) generic).ability(event, item);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void brewAbility(BrewEvent event){
        BrewerInventory inv = event.getContents();
        ItemStack ingredient = inv.getIngredient();
        Brewable brewable = Utility.findItem(ingredient, Brewable.class);
        if(brewable!=null)
            brewable.ability(event, ingredient, inv);

        if(event.isCancelled())
            return;
        for(int i=0; i<3; i++) {
            ItemStack item = inv.getItem(i);
            if(item==null)
                continue;
            Mixable mixable = Utility.findItem(item, Mixable.class);
            if(mixable==null){
                int slot = i;
                Material material = item.getType();
                PotionMeta meta = (PotionMeta) item.getItemMeta();
                new BukkitRunnable() {
                    public void run(){
                        ItemStack item = inv.getItem(slot);
                        if(item.getType()==Material.LINGERING_POTION && (material!=LINGERING_POTION)==(ingredient.getType()==DRAGON_BREATH)){
                            for(PotionEffect effect : meta.getCustomEffects()){
                                int duration = effect.getDuration()/4;
                                int amplifier = effect.getAmplifier();
                                PotionEffectType type = effect.getType();
                                meta.addCustomEffect(new PotionEffect(type, duration, amplifier), true);
                            }
                            item.setItemMeta(meta);
                            item.setType(LINGERING_POTION);
                        }
                    }
                }.runTask(HoloItems.getInstance());
            }
            else
                mixable.ability(event, item ,ingredient, inv, i);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void collectAbility(EntityDropItemEvent event){
        Entity entity = event.getEntity();
        Collectable collectable = Utility.findItem(entity, Collectable.class);
        if(collectable!=null)
            collectable.ability(event, entity);
    }

    @EventHandler(ignoreCancelled = true)
    public void consumeAbility(PlayerItemConsumeEvent event){
        ItemStack item = event.getItem();
        if (item.getType()==Material.AIR || item.getItemMeta()==null)
            return;
        String id = item.getItemMeta().getPersistentDataContainer().get(Utility.key, PersistentDataType.STRING);
        if (id == null)
            return;
        if(Collections.disabled.contains(id)) {
            event.getPlayer().sendMessage("§cThis item has been disabled");
            return;
        }
        Item generic = Collections.items.get(id);
        if(generic instanceof Consumable)
            ((Consumable) generic).ability(event, item);
        else
            event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void defendAbility(EntityDamageByEntityEvent event){
        Entity entity = event.getEntity();
        Harmable harmable = Utility.findItem(entity, Harmable.class);
        if(harmable!=null)
            harmable.ability(event);

        if(!(event.getEntity() instanceof LivingEntity))
            return;
        LivingEntity livingEntity = (LivingEntity) entity;
        EntityEquipment equipment = livingEntity.getEquipment();
        if(equipment==null)
            return;
        for(ItemStack item : equipment.getArmorContents()) {
            if (item == null || item.getItemMeta() == null)
                continue;
            PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
            String id = container.get(Utility.key, PersistentDataType.STRING);
            if (id!=null) {
                //reading glasses easter egg
                if(id.equals(ReadingGlasses.name) && item.containsEnchantment(Enchantment.BINDING_CURSE) || Collections.disabled.contains(id))
                    continue;
                boolean broken = Utility.addDurability(item, -1, livingEntity) == -1;
                Item generic = Collections.items.get(id);
                if (generic instanceof Wearable)
                    ((Wearable) generic).ability(event, broken);
            }
        }

        if(!(entity instanceof Player))
            return;
        Player player = (Player) entity;
        if(!player.isBlocking())
            return;
        for(ItemStack item : new ItemStack[]{equipment.getItemInMainHand(), equipment.getItemInOffHand()}) {
            Defensible defensible = Utility.findItem(item, Defensible.class, player);
            if(defensible!=null) {
                defensible.ability(event);
                return;
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void dispenseAbility(BlockDispenseEvent event){
        Block block = event.getBlock();
        BlockState state = block.getState();
        if(!(state instanceof Dispenser))
            return;

        Wiring wiring = Utility.findItem(block, Wiring.class);
        if(wiring!=null)
            wiring.ability(event);

        ItemStack item = event.getItem();
        if(item.getItemMeta()==null)
            return;
        String id = item.getItemMeta().getPersistentDataContainer().get(Utility.key, PersistentDataType.STRING);
        if(id==null){
            Material type = item.getType();
            if(buckets.containsKey(type)){
                Block dispenser = event.getBlock();
                Block relative = dispenser.getRelative(((org.bukkit.block.data.type.Dispenser) dispenser.getBlockData()).getFacing());
                Material cauldron = relative.getType();
                if(buckets.containsValue(cauldron) && type!=Material.BOWL) {
                    if(relative.getBlockData() instanceof Levelled && ((Levelled) relative.getBlockData()).getLevel()!=3 ||
                            (type==Material.BUCKET) == (cauldron==Material.CAULDRON))
                        return;
                    relative.setType(buckets.get(type));
                    Material bucket;
                    if (relative.getBlockData() instanceof Levelled) {
                        bucket = Material.BUCKET;
                        Levelled levelled = (Levelled) relative.getBlockData();
                        levelled.setLevel(3);
                        relative.setBlockData(levelled);
                    } else {
                        String filling = cauldron.toString();
                        bucket = Material.getMaterial(filling.substring(0, filling.indexOf("CAULDRON")) + "BUCKET");
                    }
                    event.setCancelled(true);
                    new BukkitRunnable() {
                        public void run() {
                            Inventory inv = ((Dispenser) state).getInventory();
                            if(!inv.removeItem(item).isEmpty()){
                                Block input = block.getRelative(BlockFace.DOWN);
                                if(input.getState() instanceof Hopper && !((Hopper) input.getState()).getInventory().removeItem(item).isEmpty()){
                                    input = input.getRelative(BlockFace.DOWN);
                                    if(input.getState() instanceof Container)
                                        ((Container) input.getState()).getInventory().removeItem(item);
                                }
                            }
                            inv.addItem(new ItemStack(bucket));
                        }
                    }.runTask(HoloItems.getInstance());
                    return;
                }

                Location loc = relative.getLocation().add(0.5, 0.5, 0.5);
                java.util.Collection<Entity> cows = loc.getWorld().getNearbyEntities(loc, 0.5, 0.5, 0.5, entity -> (entity instanceof Cow));
                if(cows.isEmpty())
                    return;
                int mushroomCows = 0;
                for(Entity cow : cows){
                    if(cow instanceof MushroomCow)
                        mushroomCows++;
                }
                if(type==Material.BUCKET && cows.size()>mushroomCows)
                    event.setItem(new ItemStack(Material.MILK_BUCKET));
                else if(type==Material.BOWL && mushroomCows!=0)
                    event.setItem(new ItemStack(Material.MUSHROOM_STEW));
                else
                    return;
                new BukkitRunnable(){
                    public void run(){
                        Inventory inv = ((Dispenser) state).getInventory();
                        inv.removeItem(item);
                    }
                }.runTask(HoloItems.getInstance());
            }
            return;
        }

        if(Collections.disabled.contains(id))
            return;
        Item generic = Collections.items.get(id);
        if(generic instanceof Dispensable)
            ((Dispensable) generic).ability(event);
    }

//    @EventHandler
//    public void diveAbility(EntityToggleSwimEvent event){
//
//    }

    @EventHandler(ignoreCancelled = true)
    public void dropAbility(PlayerDropItemEvent event){
        Player player = event.getPlayer();
        org.bukkit.entity.Item item = event.getItemDrop();
        Dropable dropable = Utility.findItem(item, Dropable.class, player);
        if(dropable!=null)
            dropable.ability(event);
    }

    @EventHandler(ignoreCancelled = true)
    public void extractAbility(BlockBreakEvent event){
        Block block = event.getBlock();
        Player player = event.getPlayer();
        Breakable breakable = Utility.findItem(block, Breakable.class, player);
        if(breakable!=null)
            breakable.ability(event);

        PlayerInventory inv = player.getInventory();
        ItemStack[] items = new ItemStack[]{inv.getItemInMainHand(), inv.getItemInOffHand()};
        for(int i=0; i<items.length; i++) {
            ItemStack item = items[i];
            if(item.getType()==Material.AIR || item.getItemMeta()==null)
                continue;
            String id = item.getItemMeta().getPersistentDataContainer().get(Utility.key, PersistentDataType.STRING);
            String enchant = item.getItemMeta().getPersistentDataContainer().get(Utility.enchant, PersistentDataType.STRING);
            if(id!=null || enchant!=null) {
                if(i==0 && id!=null)
                    Utility.addDurability(item, -1, event.getPlayer());
                //temp
                if(id==null){
                    ItemMeta meta = item.getItemMeta();
                    if (meta instanceof Damageable) {
                        meta.setUnbreakable(true);
                        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
                        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                    }
                    List<String> addDurability = meta.getLore();
                    if (addDurability == null)
                        addDurability = new ArrayList<>();
                    if (!addDurability.get(addDurability.size() - 1).startsWith("§fDurability: ")) {
                        int maxDurability = item.getType().getMaxDurability();
                        int currDurability = maxDurability - ((Damageable) meta).getDamage();
                        addDurability.add("");
                        addDurability.add("§fDurability: " + currDurability + "/" + maxDurability);
                        meta.setLore(addDurability);
                    }
                    item.setItemMeta(meta);
                }
                //
            } else
                continue;
            Extractable extractable = Utility.findItem(id, Extractable.class, player);
            if(extractable !=null && i!=0 == extractable instanceof Holdable)
                extractable.ability(event);
            if(i==0) {
                if(enchant==null)
                    continue;
                for (String enchantment : enchant.split(" ")) {
                    Extractable enchanted = Utility.findItem(enchantment, Extractable.class);
                    if (enchanted!=null)
                        enchanted.ability(event);
                }
            }
        }
    }

    @EventHandler
    public void entityAbility(PlayerInteractEntityEvent event){
        //delayed isCancelled()
        Entity entity = event.getRightClicked();
        Player player = event.getPlayer();
        String id = entity.getPersistentDataContainer().get(Utility.key, PersistentDataType.STRING);
        Reactable reactable = Utility.findItem(id, Reactable.class, player);
        if(reactable!=null)
            reactable.ability(event);

        ItemStack item;
        if(event.getHand()==EquipmentSlot.HAND)
            item = player.getInventory().getItemInMainHand();
        else
            item = player.getInventory().getItemInOffHand();
        Collection collection = Collections.collections.get(id);
        if(collection!=null)
            collection.inquire(player, item, event);

        if(event.isCancelled())
            return;
        Item responsible = Utility.findItem(item, Item.class, player);
        if(responsible!=null)
            event.setCancelled(true);
        if(responsible instanceof Responsible && ((Responsible) responsible).ability(event, item))
            Utility.addDurability(item, -1, player);
    }

    @EventHandler(ignoreCancelled = true)
    public void fishAbility(PlayerFishEvent event){
        PlayerInventory inv = event.getPlayer().getInventory();
        ItemStack item = inv.getItemInMainHand();
        Fishable fishable = Utility.findItem(item, Fishable.class);
        if(fishable!=null) {
            fishable.ability(event, item);
            return;
        }
        item = inv.getItemInOffHand();
        fishable = Utility.findItem(item, Fishable.class);
        if(fishable!=null)
            fishable.ability(event, item);
    }

    @EventHandler(ignoreCancelled = true)
    public void flauntAbility(AsyncPlayerChatEvent event){
        //temp
        if(Utility.test)
            System.out.println(event.getMessage());
        //
        if(!event.isAsynchronous())
            return;
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getHelmet();
        Flauntable flauntable = Utility.findItem(item, Flauntable.class, player);
        if(flauntable!=null)
            flauntable.ability(event);
        if(!activatables.isEmpty()) {
            for (Activatable activatable : activatables) {
                if (activatable instanceof Flauntable && activatable.survey().contains(player))
                    ((Flauntable) activatable).ability(event);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void hangingAbility(HangingPlaceEvent event){
        Player player = event.getPlayer();
        if(player==null)
            return;
        ItemStack item = player.getInventory().getItemInMainHand();
        Hangable hangable = Utility.findItem(item, Hangable.class, player);
        if(hangable!=null)
            hangable.ability(event);
    }

    @EventHandler
    public void hitAbility(ProjectileHitEvent event){
        //no isCancelled()
        Projectile projectile = event.getEntity();
        if(projectile.doesBounce())
            return;
        Hitable hitable;
        if(projectile instanceof ThrownPotion)
            hitable = Utility.findItem(((ThrownPotion) projectile).getItem(), Hitable.class);
        else if(projectile instanceof ThrowableProjectile)
            hitable = Utility.findItem(((ThrowableProjectile) projectile).getItem(), Hitable.class);
        else
            hitable = Utility.findItem(projectile, Hitable.class);
        if(hitable!=null)
            hitable.ability(event);
    }

    @EventHandler(ignoreCancelled = true)
    public void igniteAbility(BlockIgniteEvent event){
        Block block = event.getBlock();
        Ignitable ignitable = Utility.findItem(block, Ignitable.class);
        if(ignitable !=null)
            ignitable.ability(event);
    }

    @EventHandler
    public void interactAbility(PlayerInteractEvent event){
        //no isCancelled()
        Action action = event.getAction();
        Player player = event.getPlayer();
        if(action==Action.PHYSICAL){
            ItemStack item = player.getInventory().getBoots();
            Passable passable = Utility.findItem(item, Passable.class, player);
            if(passable!=null)
                passable.ability(event, player.getWorld().getBlockAt(player.getLocation()));
            return;
        }
        Block block = event.getClickedBlock();
        if(block!=null && !player.isSneaking()){
            Punchable punchable = Utility.findItem(block, Punchable.class, player);
            if (punchable!=null)
                punchable.ability(event, action);
        }
        ItemStack item = event.getItem();
        if(item==null || item.getItemMeta()==null)
            return;
        String id = item.getItemMeta().getPersistentDataContainer().get(Utility.key, PersistentDataType.STRING);
        String enchant = item.getItemMeta().getPersistentDataContainer().get(Utility.enchant, PersistentDataType.STRING);
        if(id!=null) {
            if (block!=null && block.getType()==JUKEBOX)
                event.setUseItemInHand(Event.Result.DENY);
            else if (Collections.disabled.contains(id))
                player.sendMessage("§cThis item has been disabled");
            else {
                Item generic = Collections.items.get(id);
                if (generic instanceof Interactable)
                    ((Interactable) generic).ability(event, action);
            }
        } if(event.getHand()!=EquipmentSlot.OFF_HAND){
            ItemStack offhand = player.getInventory().getItemInOffHand();
            Interactable interactable = Utility.findItem(offhand, Interactable.class, player);
            if(interactable instanceof Holdable)
                interactable.ability(event, offhand);
        } if(enchant!=null) {
            //temp
            if(id==null){
                ItemMeta meta = item.getItemMeta();
                if (meta instanceof Damageable) {
                    meta.setUnbreakable(true);
                    meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
                    meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                }
                List<String> addDurability = meta.getLore();
                if (addDurability == null)
                    addDurability = new ArrayList<>();
                if (!addDurability.get(addDurability.size() - 1).startsWith("§fDurability: ")) {
                    int maxDurability = item.getType().getMaxDurability();
                    int currDurability = maxDurability - ((Damageable) meta).getDamage();
                    addDurability.add("");
                    addDurability.add("§fDurability: " + currDurability + "/" + maxDurability);
                    meta.setLore(addDurability);
                }
                item.setItemMeta(meta);
            }
            //
            for (String enchantment : enchant.split(" ")) {
                if (Collections.disabled.contains(enchantment))
                    return;
                Item generic = Collections.items.get(enchantment);
                if (generic instanceof Interactable)
                    ((Interactable) generic).ability(event, action);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void launchAbility(ProjectileLaunchEvent event){
        ProjectileSource entity = event.getEntity().getShooter();
        if(!(entity instanceof LivingEntity))
            return;
        LivingEntity shooter = (LivingEntity) entity;
        EntityEquipment equipment = shooter.getEquipment();
        if(equipment==null)
            return;
        boolean offhand = false;
        for(ItemStack item : new ItemStack[]{equipment.getItemInMainHand(), equipment.getItemInOffHand()}) {
            Player player = shooter instanceof Player ? (Player) shooter : null;
            Launchable launchable = Utility.findItem(item, Launchable.class, player);
            if(launchable !=null && offhand == launchable instanceof Holdable){
                launchable.ability(event, item);
                Utility.addDurability(item, -1, player);
            }
            offhand = true;
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void manipulateAbility(PlayerArmorStandManipulateEvent event){
        ArmorStand stand = event.getRightClicked();
        String id = stand.getPersistentDataContainer().get(Utility.key, PersistentDataType.STRING);
        if(id==null || Collections.disabled.contains(id))
            return;
        event.setCancelled(true);

        Item generic = Collections.items.get(id);
        if(generic instanceof Manipulatable)
            ((Manipulatable) generic).ability(event);
    }

    @EventHandler
    public void powerAbility(BlockRedstoneEvent event){
        //no isCancelled()
        Block block = event.getBlock();
        Powerable powerable = Utility.findItem(block, Powerable.class);
        if(powerable!=null)
            powerable.ability(event);
    }

    @EventHandler
    public void packAbility(InventoryCloseEvent event){
        //no isCancelled()
        InventoryView view = event.getView();
        Inventory inv = view.getTopInventory();
        InventoryHolder holder = inv.getHolder();
        if(holder!=null) {
            Player player = (Player) event.getPlayer();
            if(holder.equals(player) && bedrock.contains(player)){
                PrepareAnvilEvent anvilEvent = new PrepareAnvilEvent(view, null);
                Bukkit.getServer().getPluginManager().callEvent(anvilEvent);
                if(anvilEvent.getResult()==null)
                    player.sendMessage("§a[§5Shion§a]§f: That.. doesn't work");
                bedrock.remove(player);
                return;
            }
            Closeable closeable = Utility.findItem(player.getInventory().getItemInOffHand(), Closeable.class, player);
            if(closeable instanceof Holdable)
                closeable.ability(event);
            return;
        }

        if(view.getTitle().equals("Price")){
            Item item = Collections.items.get(GalleryFrame.name);
            if(item instanceof GalleryFrame)
                ((GalleryFrame) item).ability(event);
            return;
        }
        Player player = (Player) event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        Pack pack = Utility.findItem(item, Pack.class, player);
        if(pack!=null && view.getTitle().equals(pack.title))
            pack.ability(inv, item, player);
    }

    @EventHandler
    public void perishAbility(EntityDeathEvent event){
        //no isCancelled()
        LivingEntity entity = event.getEntity();
        if(entity instanceof ArmorStand)
            return;
        String id = entity.getPersistentDataContainer().get(Utility.key, PersistentDataType.STRING);
        String modifiers = entity.getPersistentDataContainer().get(Utility.pack, PersistentDataType.STRING);
        if(id!=null || modifiers!=null) {
            event.getDrops().clear();
            event.setDroppedExp(0);
            if(entity.getType()==EntityType.SLIME){
                Slime slime = (Slime) entity;
                int size = slime.getSize();
                new BukkitRunnable() {
                    public void run() {
                        for (Entity nearby : entity.getNearbyEntities(0.5, 0.5, 0.5)) {
                            if (nearby instanceof Slime && ((Slime) nearby).getSize() == size/2) {
                                if (id != null)
                                    nearby.getPersistentDataContainer().set(Utility.key, PersistentDataType.STRING, id);
                                if (modifiers != null)
                                    nearby.getPersistentDataContainer().set(Utility.pack, PersistentDataType.STRING, modifiers);
                            }
                        }
                    }
                }.runTaskLater(HoloItems.getInstance(), 30);
            }
        } else if(entity instanceof EnderDragon) {
            Location loc = entity.getLocation();
            World world = loc.getWorld();
            Block block = world.getHighestBlockAt(loc);
            int i = 0;
            while(block.getType()==BEDROCK){
                if(i>7)
                    return;
                block = block.getRelative(BlockFace.NORTH);
                block = world.getHighestBlockAt(block.getLocation());
                i++;
            }
            block = block.getRelative(BlockFace.UP);
            QuartzGranule.setUpSkull(block);
            //temp
            world.dropItemNaturally(block.getLocation(), Collections.items.get(Fireball.name).item);
            //
        }

//        Player player = entity.getKiller();
//        if(player==null)
//            return;
//        PlayerInventory inv = player.getInventory();
//        int slot = 0;
//        for(ItemStack item : new ItemStack[]{inv.getItemInMainHand(), inv.getItemInOffHand()}) {
//            Perishable perishable = Utility.findItem(item, Perishable.class, player);
//            if(perishable!=null)
//                perishable.ability(event, item, slot, player);
//            slot++;
//        }
//        for(ItemStack item : new ItemStack[]{inv.getHelmet(), inv.getChestplate(), inv.getLeggings(), inv.getBoots()}) {
//            slot++;
//            if(item==null)
//                continue;
//            ItemMeta meta = item.getItemMeta();
//            if(meta==null)
//                continue;
//            String enchants = meta.getPersistentDataContainer().get(Utility.enchant, PersistentDataType.STRING);
//            if(enchants==null)
//                continue;
//            for(String enchant : enchants.split(" ")) {
//                Perishable perishable = Utility.findItem(enchant, Perishable.class, player);
//                if (perishable != null)
//                    perishable.ability(event, item, slot-1, player);
//            }
//        }
    }

    @EventHandler(ignoreCancelled = true)
    public void placeAbility(BlockPlaceEvent event){
        Player player = event.getPlayer();
        ItemStack item = event.getItemInHand();
        Item generic = Utility.findItem(item, Item.class, player);
        if(generic!=null) {
            event.setCancelled(true);
            if(generic instanceof Placeable && !(generic instanceof Holdable))
                ((Placeable) generic).ability(event);
        } if(item.getType()!=AIR) {
            Placeable placeable = Utility.findItem(player.getInventory().getItemInOffHand(), Placeable.class, player);
            if (placeable instanceof Holdable)
                placeable.ability(event);
        }
    }

    @EventHandler
    public void retainAbility(PlayerDeathEvent event){
        //no isCancelled()
        Player player = event.getEntity();
        for(ItemStack item : player.getInventory().getContents()) {
            Retainable retainable = Utility.findItem(item, Retainable.class, player);
            if(retainable!=null && retainable.ability(event, item)) {
                item.setAmount(item.getAmount() - 1);
                if(event.getKeepInventory()) {
                    return;
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void swapAbility(PlayerSwapHandItemsEvent event){
        Player player = event.getPlayer();
        ItemStack item = event.getMainHandItem();
        Swappable swappable = Utility.findItem(item, Swappable.class, player);
        if(swappable!=null)
            swappable.ability(event, player, item, true);
        item = event.getOffHandItem();
        swappable = Utility.findItem(item, Swappable.class, player);
        if(swappable!=null)
            swappable.ability(event, player, item, false);

        manageDurability(player);
    }

    @EventHandler(ignoreCancelled = true)
    public void targetAbility(EntityTargetLivingEntityEvent event){
        String modifiers = event.getEntity().getPersistentDataContainer().get(Utility.pack, PersistentDataType.STRING);
        if(modifiers==null)
            return;
        for(String modifier : modifiers.split("-")){
            Targetable targetable = Utility.findItem(modifier, Targetable.class);
            if(targetable!=null)
                targetable.ability(event);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void teleportAbility(EntityTeleportEvent event){
        Entity entity = event.getEntity();
        if(!(entity instanceof Player) && entity.getPersistentDataContainer().get(Utility.pack, PersistentDataType.STRING)!=null)
            event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void toggleAbility(PlayerToggleSneakEvent event){
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getBoots();
        if(item==null || item.getItemMeta()==null)
            return;
        String id = item.getItemMeta().getPersistentDataContainer().get(Utility.key, PersistentDataType.STRING);
        String enchant = item.getItemMeta().getPersistentDataContainer().get(Utility.enchant, PersistentDataType.STRING);
        if(id!=null) {
            if (Collections.disabled.contains(id))
                player.sendMessage("§cThis item has been disabled");
            else {
                Item generic = Collections.items.get(id);
                if (generic instanceof Togglable)
                    ((Togglable) generic).ability(event, item);
            }
        }
        if(enchant!=null) {
            for (String enchantment : enchant.split(" ")) {
                if (Collections.disabled.contains(id))
                    player.sendMessage("§cThis item has been disabled");
                else {
                    Item generic = Collections.items.get(enchantment);
                    if (generic instanceof Togglable)
                        ((Togglable) generic).ability(event, item);
                }
            }
        }
    }

    @EventHandler
    public void writeAbility(PlayerEditBookEvent event){
        Player player = event.getPlayer();
        BookMeta meta = event.getNewBookMeta();
        String id = meta.getPersistentDataContainer().get(Utility.key, PersistentDataType.STRING);
        Writable writable = Utility.findItem(id, Writable.class, player);
        if(writable!=null)
            writable.ability(event, meta);
    }

    private int findMultiplier(Enchantment enchant){
        for(Integer i : multiplier.keySet()){
            for(Enchantment enchantment : multiplier.get(i)){
                if(enchantment.equals(enchant))
                    return i;
            }
        }
        return -1;
    }

    @EventHandler
    public void manageDurability(PlayerItemHeldEvent event){
        manageDurability(event.getPlayer());
    }

    public static Set<Player> managers = new HashSet<>();

    public static boolean manageDurability(Player player){
        if(!managers.contains(player))
            return true;
        PlayerInventory inv = player.getInventory();
        Scoreboard board = player.getScoreboard();
        Objective obj = board.getObjective("durability");
        //temp
        if(obj==null) {
            board = Bukkit.getScoreboardManager().getNewScoreboard();
            player.setScoreboard(board);
        } else
            obj.unregister();
        Scoreboard scoreboard = board;
        //
        new BukkitRunnable(){
            public void run(){
                Objective obj = scoreboard.getObjective("durability");
                if(obj!=null)
                    return;
                ItemStack[] items = new ItemStack[2];
                items[0] = inv.getItemInMainHand();
                items[1] = inv.getItemInOffHand();
                int size = 0;
                int[][] durability = new int[2][2];
                durability[0] = Utility.getDurability(items[0]);
                durability[1] = Utility.getDurability(items[1]);
                if(durability[0]==null)
                    items[0] = null;
                else
                    size++;
                if(durability[1]==null)
                    items[1] = null;
                else
                    size++;
                if(size==0)
                    return;
                size *= 2;
                Objective objective = scoreboard.registerNewObjective("durability", "manage", "Durability");
                objective.setDisplaySlot(DisplaySlot.SIDEBAR);
                for(int i=0; i<2; i++){
                    ItemStack item = items[i];
                    if(item==null)
                        continue;
                    String name = item.getItemMeta().getDisplayName();
                    if(name.isEmpty())
                        name = Utility.formatType(item.getType());
                    else
                        name = ChatColor.stripColor(name);
                    Score score = objective.getScore((i==0?"§6":"§b") + name);
                    score.setScore(size);
                    String bar = "████";
                    int index = -1;
                    char code;
                    switch((int) ((((double) durability[i][0])/durability[i][1])*10)){
                        case 10:
                            if(durability[i][0]==durability[i][1])
                                index = 4;
                        case 9:
                        case 8:
                        case 7:
                        case 6:
                            code = 'a';
                            if(index==-1)
                                index = 3;
                            break;
                        case 5:
                        case 4:
                        case 3:
                            code = 'e';
                            index = 2;
                            break;
                        case 2:
                        case 1:
                        case 0:
                            code = 'c';
                            if(durability[i][0]==1)
                                index = 4;
                            else
                                index = 1;
                            break;
                        default:
                            code = '7';
                            index = 4;
                    }
                    score = objective.getScore("§" + code + bar.substring(0, index) + "§7" + bar.substring(index) + " §f" + durability[i][0] + (i==1?" ":""));
                    size--;
                    score.setScore(size);
                    size--;
                }
            }
        }.runTask(HoloItems.getInstance());
        return false;
    }

    @EventHandler(ignoreCancelled = true)
    public void preventWither(EntityChangeBlockEvent event){
        if(event.getBlock().getType()==Material.PLAYER_HEAD)
            event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void preventExplosion(EntityExplodeEvent event){
        for(Block block : event.blockList()){
            if(block.getType()==Material.PLAYER_HEAD){
                event.setCancelled(true);
                return;
            }
        }
    }

    /**
     * This event handler is triggered when clicking on the output slot of the Cartography Inventory
     * @param event Bukkit InventoryClickEvent
     * @implNote
     * It's not feasible to stop an item to be added to the Cartography Table's input slots:
     * 1) When dropping what's on the cursor, the event must not be cancelled or else Minecraft client freezes.
     * 2) When right-clicking on a recipe item (stack splitting), the event must not be cancelled or else Minecraft client freezes.
     * 3) If an event with InventoryAction.MOVE_TO_OTHER_INVENTORY is cancelled, it's possible to create ghost items in
     * the inventory in survival or dupe in creative mode.
     */
    @EventHandler(ignoreCancelled = true)
    public void onCartographyResultEvent(InventoryClickEvent event){
        //Filter from InventoryClickEvent
        if (event.getSlotType() != InventoryType.SlotType.RESULT) return;
        final Inventory clickedInventory = event.getClickedInventory();
        if (clickedInventory == null) return;
        if (clickedInventory.getType() != InventoryType.CARTOGRAPHY) return;

        //https://wiki.vg/Inventory#Cartography_Table
        ItemStack mapSlot = clickedInventory.getItem(0);
        ItemStack paperSlot = clickedInventory.getItem(1);

        //If an item added to the Cartography inventory has a Utility.key, it mustn't be crafted away
        //This is the case of Verification Seal
        if (Utility.hasPersistentUtilityKey(mapSlot) || Utility.hasPersistentUtilityKey(paperSlot)) {
            event.setCancelled(true);
        }
    }
}
