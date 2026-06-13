package io.github.potaseval.listeners.plant;

import io.github.potaseval.GreatWeeb;
import io.github.potaseval.items.*;
import io.github.potaseval.util.ItemUtils;
import io.github.potaseval.util.PlantDataUtils;
import io.github.potaseval.util.PlantDataUtils.StrainType;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class PlantCareListener implements Listener {

    private final GreatWeeb plugin;
    private final SativaItems sativaItems;
    private final IndicaItems indicaItems;
    private final Random random = new Random();
    private final FertilizerItems fertilizerItems;
    private final SekatorItems sekatorItems;
    private final TobaccoItems tobaccoItems;

    public PlantCareListener(GreatWeeb plugin) {
        this.plugin = plugin;
        this.sativaItems = plugin.getSativaItems();
        this.indicaItems = plugin.getIndicaItems();
        this.fertilizerItems = plugin.getFertilizerItems();
        this.sekatorItems = plugin.getSekatorItems();
        this.tobaccoItems = plugin.getTobaccoItems();
    }

    @EventHandler
    public void onRightClickWheat(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;

        Block block = event.getClickedBlock();
        if (block == null || block.getType() != Material.WHEAT) return;

        Player player = event.getPlayer();
        StrainType strainType = PlantDataUtils.getStrainType(block);
        if (strainType == null) return;

        if (strainType == StrainType.TOBACCO) {
            Ageable ageable = (Ageable) block.getBlockData();
            int age = ageable.getAge();
            int maxAge = ageable.getMaximumAge();
            ItemStack mainHand = player.getInventory().getItemInMainHand();

            // 1. Ускорение роста (костная мука) — УБРАНО для табака
            // больше не обрабатываем BONE_MEAL

            boolean dry = PlantDataUtils.isDry(block) && age != maxAge;

            // 2. Полив при засыхании
            if (dry) {
                if (mainHand != null && mainHand.getType() == Material.WATER_BUCKET) {
                    PlantDataUtils.setDry(block, false);
                    mainHand.setAmount(mainHand.getAmount() - 1);
                    ItemUtils.giveOrDrop(player, new ItemStack(Material.BUCKET));
                    player.playSound(player.getLocation(), Sound.ITEM_BUCKET_EMPTY, 1.0f, 1.0f);
                    player.sendMessage("§aВы полили растение.");
                    event.setCancelled(true);
                    return;
                }
            }

            // 3. Удобрения (только для только что посаженного куста)
            boolean hasFertilizer = mainHand != null && fertilizerItems.isFertilizer(mainHand) && !dry;
            boolean hasBoxFertilizer = mainHand != null && tobaccoItems.isBoxFertilizer(mainHand) && !dry;

            if (hasFertilizer) {
                if (age != 0) {
                    player.sendMessage("§cУдобрять можно только только что посаженные кусты.");
                    event.setCancelled(true);
                    return;
                }
                if (PlantDataUtils.isFertilized(block)) {
                    player.sendMessage("§cЭтот куст уже удобрен.");
                    event.setCancelled(true);
                    return;
                }
                PlantDataUtils.setFertilized(block, true);
                mainHand.setAmount(mainHand.getAmount() - 1);
                event.setCancelled(true);
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_WORK_FARMER, 1.0f, 1.0f);
                player.sendMessage("§aВы удобрили куст. Теперь он даст больше урожая!");
                return;
            }

            if (hasBoxFertilizer) {
                if (age != 0) {
                    player.sendMessage("§cУдобрять можно только только что посаженные кусты.");
                    event.setCancelled(true);
                    return;
                }
                if (PlantDataUtils.isFertilized(block)) {
                    player.sendMessage("§cЭтот куст уже удобрен.");
                    event.setCancelled(true);
                    return;
                }
                PlantDataUtils.setFertilized(block, true);
                PlantDataUtils.setBoxFertilized(block, true);
                mainHand.setAmount(mainHand.getAmount() - 1);
                event.setCancelled(true);
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_WORK_FARMER, 1.0f, 1.0f);
                player.sendMessage("§aВы удобрили куст Боксовым удобрением. Теперь он защищён от засыхания и даст больше урожая!");
                return;
            }

            boolean hasShears = mainHand != null && mainHand.getType() == Material.SHEARS;
            boolean hasSekator = sekatorItems.isSekator(mainHand);
            if ((hasShears || hasSekator) && age == maxAge) {
                int damage = hasSekator ? 6 : 3;
                ItemUtils.damageItem(mainHand, damage, player);

                int base = hasSekator ? 3 : 2;
                int amount = random.nextInt(hasSekator ? 3 : 2) + base;
                boolean fertilized = PlantDataUtils.isFertilized(block);
                if (fertilized) {
                    amount += 2; // +2 табака за любое удобрение
                    PlantDataUtils.setFertilized(block, false);
                    PlantDataUtils.setBoxFertilized(block, false);
                }
                // XP drop: more XP from sekator
                int xpAmount = hasSekator ? random.nextInt(3) + 3 : random.nextInt(2) + 1; // sekator: 3-5, shears: 1-2
                if (xpAmount > 0) {
                    block.getWorld().spawn(block.getLocation().add(0.5, 0.5, 0.5), org.bukkit.entity.ExperienceOrb.class, orb -> {
                        orb.setExperience(xpAmount);
                    });
                }

                ItemStack tobaccoDrop = tobaccoItems.createTobacco();
                tobaccoDrop.setAmount(amount);
                block.getWorld().dropItemNaturally(block.getLocation().add(0.5, 0.5, 0.5), tobaccoDrop);

                // XP drop: shears = 1-2, sekator = 3-5
                int xpAmountTobacco = hasSekator ? (random.nextInt(3) + 3) : (random.nextInt(2) + 1);
                block.getWorld().spawn(block.getLocation().add(0.5, 0.5, 0.5), org.bukkit.entity.ExperienceOrb.class, orb -> {
                    orb.setExperience(xpAmountTobacco);
                });

                ageable.setAge(0);
                block.setBlockData(ageable);

                event.setCancelled(true);
                player.playSound(player.getLocation(), Sound.ENTITY_SHEEP_SHEAR, 1.0f, 1.0f);
                player.sendMessage("§aВы собрали " + amount + " табака.");
                return;
            }

            boolean fertilized = PlantDataUtils.isFertilized(block);
            boolean boxFertilized = PlantDataUtils.isBoxFertilized(block);
            player.sendMessage("§e=== Информация о кусте ===");
            if (age == maxAge) {
                player.sendMessage("§7Статус: §aВырос");
            } else if (dry) {
                player.sendMessage("§7Статус: §cЗасох");
            } else {
                player.sendMessage("§7Статус: §eРастёт (" + (age * 100 / maxAge) + "%)");
            }
            if (fertilized) {
                if (boxFertilized) {
                    player.sendMessage("§7Удобрение: §6Боксовое (защита от засыхания)");
                } else {
                    player.sendMessage("§7Удобрение: §aОбычное");
                }
            } else {
                player.sendMessage("§7Удобрение: §cНе удобрено");
            }
            event.setCancelled(true);
            return;
        }

        Ageable ageable = (Ageable) block.getBlockData();
        int age = ageable.getAge();
        int maxAge = ageable.getMaximumAge();

        ItemStack mainHand = player.getInventory().getItemInMainHand();
        boolean hasShears = mainHand != null && mainHand.getType() == Material.SHEARS;
        boolean hasBoneMeal = mainHand != null && mainHand.getType() == Material.BONE_MEAL;
        boolean hasFertilizer = mainHand != null && fertilizerItems.isFertilizer(mainHand);
        boolean dry = PlantDataUtils.isDry(block) && age != maxAge;
        boolean isIce = mainHand != null &&
                (mainHand.getType() == Material.ICE || mainHand.getType() == Material.PACKED_ICE || mainHand.getType() == Material.BLUE_ICE);

        if (isIce && !dry) {
            if (PlantDataUtils.isMoist(block)) {
                player.sendMessage("§cЭтот куст уже увлажнён.");
                event.setCancelled(true);
            } else {
                PlantDataUtils.setMoist(block, true);
                mainHand.setAmount(mainHand.getAmount() - 1);
                player.playSound(player.getLocation(), Sound.BLOCK_GLASS_BREAK, 1.0f, 1.0f);
                player.sendMessage("§bВы увлажнили куст. Теперь он защищён от засыхания.");
                event.setCancelled(true);
            }
            return;
        }

        if (dry) {
            if (mainHand != null && mainHand.getType() == Material.WATER_BUCKET) {
                PlantDataUtils.setDry(block, false);
                mainHand.setAmount(mainHand.getAmount() - 1);
                ItemUtils.giveOrDrop(player, new ItemStack(Material.BUCKET));
                player.playSound(player.getLocation(), Sound.ITEM_BUCKET_EMPTY, 1.0f, 1.0f);
                player.sendMessage("§aВы полили растение.");
                event.setCancelled(true);
                return;
            }
            hasFertilizer = false;
            hasBoneMeal = false;
            hasShears = false;
        }

        if (hasFertilizer) {
            if (age != 0) {
                player.sendMessage("§cУдобрять можно только только что посаженные кусты.");
                event.setCancelled(true);
                return;
            }
            if (PlantDataUtils.isFertilized(block)) {
                player.sendMessage("§cЭтот куст уже удобрен.");
                event.setCancelled(true);
                return;
            }
            PlantDataUtils.setFertilized(block, true);
            mainHand.setAmount(mainHand.getAmount() - 1);
            event.setCancelled(true);
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_WORK_FARMER, 1.0f, 1.0f);
            player.sendMessage("§aВы удобрили куст. Теперь он растёт лучше!");
            return;
        }

        boolean hasBoxFertilizer = mainHand != null && tobaccoItems.isBoxFertilizer(mainHand);
        if (hasBoxFertilizer) {
            if (age != 0) {
                player.sendMessage("§cУдобрять можно только только что посаженные кусты.");
                event.setCancelled(true);
                return;
            }
            if (PlantDataUtils.isFertilized(block)) {
                player.sendMessage("§cЭтот куст уже удобрен.");
                event.setCancelled(true);
                return;
            }
            PlantDataUtils.setFertilized(block, true);
            PlantDataUtils.setBoxFertilized(block, true);
            mainHand.setAmount(mainHand.getAmount() - 1);
            event.setCancelled(true);
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_WORK_FARMER, 1.0f, 1.0f);
            player.sendMessage("§aВы удобрили куст Боксовым удобрением. Теперь он защищён от засыхания и даст больше урожая!");
            return;
        }

        if (hasBoneMeal && age < maxAge) {
            int newAge = Math.min(maxAge, age + random.nextInt(3) + 1);
            ageable.setAge(newAge);
            block.setBlockData(ageable);
            mainHand.setAmount(mainHand.getAmount() - 1);
            event.setCancelled(true);
            return;
        }

        boolean isSekator = sekatorItems.isSekator(mainHand);
        if ((hasShears || isSekator) && age == maxAge) {
            int damage = isSekator ? 6 : 3;
            ItemUtils.damageItem(mainHand, damage, player);
            int amount = random.nextInt(2) + (isSekator ? 4 : 2);
            boolean fertilized = PlantDataUtils.isFertilized(block);
            if (fertilized) {
                amount += random.nextInt(2) + 1;
                PlantDataUtils.setFertilized(block, false);
                PlantDataUtils.setBoxFertilized(block, false);
            }
            // Ultrachlorophyll bonus: +2-3 extra buds when green stained glass + verdant froglight above
            Block above = block.getRelative(0, 1, 0);
            if (above.getType() == Material.GREEN_STAINED_GLASS
                    && above.getRelative(0, 1, 0).getType() == Material.VERDANT_FROGLIGHT) {
                int ultrachlorophyllBonus = random.nextInt(2) + 2; // 2-3 extra
                amount += ultrachlorophyllBonus;
                player.sendMessage("§2☘ Ультрахлорофилитовый свет усилил урожай! +" + ultrachlorophyllBonus + " соцветий.");
            }
            // XP drop: more XP from sekator
            int xpAmount = isSekator ? random.nextInt(3) + 3 : random.nextInt(2) + 1; // sekator: 3-5, shears: 1-2
            if (xpAmount > 0) {
                block.getWorld().spawn(block.getLocation().add(0.5, 0.5, 0.5), org.bukkit.entity.ExperienceOrb.class, orb -> {
                    orb.setExperience(xpAmount);
                });
            }
            ItemStack buds = (strainType == StrainType.SATIVA) ? sativaItems.createBud() : indicaItems.createBud();
            buds.setAmount(amount);
            block.getWorld().dropItemNaturally(block.getLocation().add(0.5, 0.5, 0.5), buds);
            ageable.setAge(0);
            block.setBlockData(ageable);
            PlantDataUtils.setBlockPDC(block,
                    strainType == StrainType.SATIVA ? PlantDataUtils.SATIVA_WHEAT_KEY : PlantDataUtils.INDICA_WHEAT_KEY,
                    true);
            event.setCancelled(true);
            player.playSound(player.getLocation(), Sound.ENTITY_SHEEP_SHEAR, 1.0f, 1.0f);
            player.sendMessage("§aВы собрали " + amount + " соцветий, и растение начало расти заново.");
            return;
        }

        event.setCancelled(true);
        String strainName = strainType == StrainType.SATIVA ? "Сативы" : "Индики";
        int growthPercent = (age * 100) / maxAge;
        boolean fertilized = PlantDataUtils.isFertilized(block);
        player.sendMessage("§e=== Информация о кусте ===");
        if (age == maxAge) {
            player.sendMessage("§7Статус: §aВыросло");
        } else if (dry) {
            player.sendMessage("§7Статус: §cЗасохло");
        } else if (PlantDataUtils.isMoist(block)) {
            player.sendMessage("§7Статус: §bУвлажнен");
        } else {
            player.sendMessage("§7Статус: §aВ норме");
        }
        player.sendMessage("§7Сорт: " + strainName);
        if (growthPercent == 100) {
            player.sendMessage("§7Стадия роста: §a" + growthPercent + "%");
        } else {
            player.sendMessage("§7Стадия роста: " + growthPercent + "%");
        }
        boolean boxFertilized = PlantDataUtils.isBoxFertilized(block);
        if (fertilized) {
            if (boxFertilized) {
                player.sendMessage("§7Удобрение: §6Боксовое (защита от засыхания)");
            } else {
                player.sendMessage("§7Удобрение: §aОбычное");
            }
        } else {
            player.sendMessage("§7Удобрение: §cНе удобрено");
        }

        Block above = block.getRelative(0, 1, 0);
        String uvStatus;
        if (above.getType() == Material.PEARLESCENT_FROGLIGHT) {
            uvStatus = "§aДа (+20% к скорости роста)";
        } else if (above.getType() == Material.PURPLE_STAINED_GLASS
                && above.getRelative(0, 1, 0).getType() == Material.PEARLESCENT_FROGLIGHT) {
            uvStatus = "§aДа (+30% к скорости роста)";
        } else if (above.getType() == Material.GREEN_STAINED_GLASS
                && above.getRelative(0, 1, 0).getType() == Material.VERDANT_FROGLIGHT) {
            uvStatus = "§2☘ Ультрахлорофилит (-15% скорость, +2-3 соцветия при сборе)";
        } else {
            uvStatus = "§cНет";
        }
        player.sendMessage("§7Ультрафиолет: " + uvStatus);
        if (!hasShears && !isSekator && !hasBoneMeal && !hasFertilizer && !dry) {
            player.sendMessage("§8Возьмите ножницы или секатор, чтобы собрать соцветия.");
        } else if (dry) {
            player.sendMessage("§8Полейте растение ведром воды.");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void preventAutoHarvest(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block block = event.getClickedBlock();
        if (block == null || block.getType() != Material.WHEAT) return;
        if (PlantDataUtils.getStrainType(block) == null) return;

        event.setCancelled(true);
    }
}