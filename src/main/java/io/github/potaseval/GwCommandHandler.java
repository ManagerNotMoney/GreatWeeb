package io.github.potaseval;

import io.github.potaseval.RecipeManager;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class GwCommandHandler implements CommandExecutor, TabCompleter {

    private final GreatWeeb plugin;

    public GwCommandHandler(GreatWeeb plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cЭту команду может выполнить только игрок.");
            return true;
        }

        if (!player.hasPermission("greatweeb.admin")) {
            player.sendMessage("§cУ вас недостаточно прав.");
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("all")) {
            giveAllItems(player);
            return true;
        }

        if (args.length >= 1 && (args[0].equalsIgnoreCase("smell") || args[0].equalsIgnoreCase("nosmoke"))) {
            String sub = args[0].toLowerCase();
            Player target = player;

            if (args.length >= 2) {
                target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    player.sendMessage("§cИгрок не найден.");
                    return true;
                }
            }

            if (sub.equals("smell")) {
                plugin.getSmokeEffectManager().stop(target);
                player.sendMessage("§aЗапах убран" + (target != player ? " у " + target.getName() : "") + ".");
            } else {
                plugin.getOverdoseListener().clearPlayerData(target);
                player.sendMessage("§aИстория курения очищена" + (target != player ? " для " + target.getName() : "") + ".");
            }
            return true;
        }

        if (args.length == 1) {
            String itemName = args[0].toLowerCase();
            ItemStack item = switch (itemName) {
                case "bong" -> plugin.getBongItems().createBong();
                case "fertilizer" -> plugin.getFertilizerItems().createFertilizer();
                case "spice" -> plugin.getGashItems().createSpice();
                case "shredded" -> plugin.getGashItems().createShreddedWeed();
                case "brownie" -> plugin.getGashItems().createCannaBrownie();
                case "cookie" -> plugin.getGashItems().createTastyCookie();
                case "medical_boshka" -> plugin.getMedicalItems().createMedicalBoshka();
                case "medical_joint" -> plugin.getMedicalItems().createMedicalJoint();
                default -> null;
            };
            if (item != null) {
                player.getInventory().addItem(item);
                player.sendMessage("§aВы получили " + item.getItemMeta().displayName() + "!");
                return true;
            }
            player.sendMessage("§eИспользование:");
            player.sendMessage("§7/gw <sativa|indica|gash|medical> <предмет>");
            player.sendMessage("§7/gw <bong|fertilizer|spice|shredded|brownie|cookie|medical_boshka|medical_joint>");
            player.sendMessage("§7/gw smell [ник] — убрать зелёный дым");
            player.sendMessage("§7/gw nosmoke [ник] — очистить историю курения");
            player.sendMessage("§7/gw all — выдать все предметы плагина");
            return true;
        }

        if (args.length < 2) {
            player.sendMessage("§eИспользование:");
            player.sendMessage("§7/gw <sativa|indica|gash|medical> <предмет>");
            return true;
        }

        String strainName = args[0].toLowerCase();
        String itemType = args[1].toLowerCase();

        if (strainName.equals("sativa") || strainName.equals("indica")) {
            RecipeManager.StrainItems strain = strainName.equals("sativa")
                    ? plugin.getSativaItems()
                    : plugin.getIndicaItems();
            ItemStack item = switch (itemType) {
                case "seed" -> strain.createSeed();
                case "bud" -> strain.createBud();
                case "boshka" -> strain.createBoshka();
                case "joint" -> strain.createJoint();
                case "briquette" -> strain.createBriquette();
                case "pack" -> strain.createPack();
                default -> null;
            };
            if (item == null) {
                player.sendMessage("§cНеизвестный предмет. Доступно: seed, bud, boshka, joint, briquette, pack");
                return true;
            }
            player.getInventory().addItem(item);
            player.sendMessage("§aВы получили " + item.getItemMeta().displayName() + "!");
            return true;

        } else if (strainName.equals("gash")) {
            ItemStack item = switch (itemType) {
                case "gash" -> plugin.getGashItems().createGash();
                case "oil" -> plugin.getGashItems().createGashOil();
                case "cake" -> plugin.getGashItems().createGashCake();
                default -> null;
            };
            if (item == null) {
                player.sendMessage("§cНеизвестный предмет. Доступно: gash, oil, cake");
                return true;
            }
            player.getInventory().addItem(item);
            player.sendMessage("§aВы получили " + item.getItemMeta().displayName() + "!");
            return true;

        } else if (strainName.equals("medical")) {
            ItemStack item = switch (itemType) {
                case "boshka" -> plugin.getMedicalItems().createMedicalBoshka();
                case "joint" -> plugin.getMedicalItems().createMedicalJoint();
                default -> null;
            };
            if (item == null) {
                player.sendMessage("§cНеизвестный предмет. Доступно: boshka, joint");
                return true;
            }
            player.getInventory().addItem(item);
            player.sendMessage("§aВы получили " + item.getItemMeta().displayName() + "!");
            return true;

        } else {
            player.sendMessage("§cНеизвестная категория. Доступно: sativa, indica, gash, medical");
            return true;
        }
    }

    private void giveAllItems(Player player) {
        var sativa = plugin.getSativaItems();
        var indica = plugin.getIndicaItems();
        var gash = plugin.getGashItems();
        var bong = plugin.getBongItems();
        var fertilizer = plugin.getFertilizerItems();
        var medical = plugin.getMedicalItems();

        player.getInventory().addItem(sativa.createSeed());
        player.getInventory().addItem(sativa.createBud());
        player.getInventory().addItem(sativa.createBoshka());
        player.getInventory().addItem(sativa.createJoint());
        player.getInventory().addItem(sativa.createBriquette());
        player.getInventory().addItem(sativa.createPack());

        player.getInventory().addItem(indica.createSeed());
        player.getInventory().addItem(indica.createBud());
        player.getInventory().addItem(indica.createBoshka());
        player.getInventory().addItem(indica.createJoint());
        player.getInventory().addItem(indica.createBriquette());
        player.getInventory().addItem(indica.createPack());

        player.getInventory().addItem(gash.createGash());
        player.getInventory().addItem(gash.createGashOil());
        player.getInventory().addItem(gash.createGashCake());
        player.getInventory().addItem(gash.createSpice());
        player.getInventory().addItem(gash.createShreddedWeed());
        player.getInventory().addItem(gash.createCannaBrownie());
        player.getInventory().addItem(gash.createTastyCookie());

        player.getInventory().addItem(bong.createBong());
        player.getInventory().addItem(fertilizer.createFertilizer());

        player.getInventory().addItem(medical.createMedicalBoshka());
        player.getInventory().addItem(medical.createMedicalJoint());

        player.sendMessage("§aВам выданы все предметы GreatWeeb!");
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String alias, @NotNull String[] args) {
        if (!sender.hasPermission("greatweeb.admin")) {
            return List.of();
        }

        if (args.length == 1) {
            return filterStarting(args[0],
                    "sativa", "indica", "gash", "medical",
                    "bong", "fertilizer", "spice", "shredded",
                    "brownie", "cookie", "medical_boshka", "medical_joint",
                    "smell", "nosmoke", "all");
        }

        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("smell") || args[0].equalsIgnoreCase("nosmoke")) {
                String prefix = args[1].toLowerCase();
                List<String> names = new ArrayList<>();
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (p.getName().toLowerCase().startsWith(prefix))
                        names.add(p.getName());
                }
                return names;
            }
            if (!args[0].equalsIgnoreCase("all")) {
                String strain = args[0].toLowerCase();
                if (strain.equals("sativa") || strain.equals("indica")) {
                    return filterStarting(args[1], "seed", "bud", "boshka", "joint", "briquette", "pack");
                } else if (strain.equals("gash")) {
                    return filterStarting(args[1], "gash", "oil", "cake");
                } else if (strain.equals("medical")) {
                    return filterStarting(args[1], "boshka", "joint");
                }
            }
        }

        return List.of();
    }

    private List<String> filterStarting(String prefix, String... options) {
        final String lower = prefix.toLowerCase();
        List<String> result = new ArrayList<>();
        for (String opt : options) {
            if (opt.toLowerCase().startsWith(lower)) {
                result.add(opt);
            }
        }
        return result;
    }
}