package io.github.potaseval;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
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
        if (args.length == 1 && args[0].equalsIgnoreCase("guide")) {
            giveGuideBook(player);
            return true;
        }
        if (!player.hasPermission("greatweeb.admin")) {
            sender.sendMessage("§cУ вас недостаточно прав.");
            return true;
        }

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        if (subCommand.equals("all")) {
            giveAllItems(player);
            return true;
        }

        if (subCommand.equals("smell") || subCommand.equals("nosmoke")) {
            executeSmellOrNosmoke(player, args);
            return true;
        }

        if (args.length == 1) {
            ItemStack item = createSingleItem(subCommand);
            if (item != null) {
                player.getInventory().addItem(item);
                player.sendMessage("§aВы получили " + item.getItemMeta().displayName() + "!");
                return true;
            }
            sendHelp(player);
            return true;
        }

        if (args.length == 2) {
            executeTwoArgsCommand(player, subCommand, args[1].toLowerCase());
            return true;
        }

        sendHelp(player);
        return true;
    }

    private void executeSmellOrNosmoke(Player player, String[] args) {
        Player target = args.length >= 2 ? Bukkit.getPlayer(args[1]) : player;
        if (target == null) {
            player.sendMessage("§cИгрок не найден.");
            return;
        }

        if (args[0].equalsIgnoreCase("smell")) {
            plugin.getSmokeEffectManager().stop(target);
            player.sendMessage("§aЗапах убран" + (target != player ? " у " + target.getName() : "") + ".");
        } else {
            plugin.getOverdoseListener().clearPlayerData(target);
            player.sendMessage("§aИстория курения очищена" + (target != player ? " для " + target.getName() : "") + ".");
        }
    }

    private ItemStack createSingleItem(String name) {
        return switch (name) {
            case "bong" -> plugin.getBongItems().createBong();
            case "fertilizer" -> plugin.getFertilizerItems().createFertilizer();
            case "spice" -> plugin.getGashItems().createSpice();
            case "shredded" -> plugin.getGashItems().createShreddedWeed();
            case "brownie" -> plugin.getGashItems().createCannaBrownie();
            case "cookie" -> plugin.getGashItems().createTastyCookie();
            case "gash" -> plugin.getGashItems().createGash();
            case "gash_oil" -> plugin.getGashItems().createGashOil();
            case "gash_briquette" -> plugin.getGashItems().createGashBriquette();
            case "gash_pack" -> plugin.getGashItems().createGashPack();
            case "spice_briquette" -> plugin.getGashItems().createSpiceBriquette();
            case "spice_pack" -> plugin.getGashItems().createSpicePack();
            case "medical_boshka" -> plugin.getMedicalItems().createMedicalBoshka();
            case "medical_joint" -> plugin.getMedicalItems().createMedicalJoint();
            case "medical_gum" -> plugin.getMedicalItems().createMedicalGum();
            case "tobacco_seed" -> plugin.getTobaccoItems().createSeed();
            case "tobacco" -> plugin.getTobaccoItems().createTobacco();
            case "box_fertilizer" -> plugin.getTobaccoItems().createBoxFertilizer();
            case "cigarette_pack" -> plugin.getTobaccoItems().createCigarettePack();
            case "cigarette_block" -> plugin.getTobaccoItems().createCigaretteBlock();
            case "filter" -> plugin.getTobaccoItems().createFilter();
            case "cigarette" -> plugin.getTobaccoItems().createCigarette();
            case "watering_can" -> plugin.getSekatorItems().createEmptyWateringCan();
            case "dried_tobacco" -> plugin.getTobaccoItems().createDriedTobacco();
            case "shredded_tobacco" -> plugin.getTobaccoItems().createShreddedTobacco();
            default -> null;
        };
    }

    private void executeTwoArgsCommand(Player player, String strain, String itemType) {
        ItemStack item = switch (strain) {
            case "sativa", "indica" -> createStrainItem(strain, itemType);
            case "gash" -> createGashItem(itemType);
            case "spice" -> createSpiceItem(itemType);
            case "medical" -> createMedicalItem(itemType);
            case "tobacco" -> createTobaccoItem(itemType);
            default -> null;
        };

        if (item != null) {
            player.getInventory().addItem(item);
            player.sendMessage("§aВы получили " + item.getItemMeta().displayName() + "!");
            return;
        }

        player.sendMessage(strain.equals("sativa") || strain.equals("indica")
                ? "§cНеизвестный предмет. Доступно: seed, bud, boshka, joint, briquette, pack"
                : strain.equals("gash")
                  ? "§cНеизвестный предмет. Доступно: gash, oil, cake, briquette, pack"
                  : strain.equals("spice")
                    ? "§cНеизвестный предмет. Доступно: spice, briquette, pack"
                    : strain.equals("medical")
                      ? "§cНеизвестный предмет. Доступно: boshka, joint, gum"
                      : strain.equals("tobacco")
                        ? "§cНеизвестный предмет. Доступно: seed, tobacco, filter, cigarette"
                        : "§cНеизвестная категория. Доступно: sativa, indica, gash, spice, medical, tobacco");
    }

    private ItemStack createStrainItem(String strain, String type) {
        RecipeManager.StrainItems strainItems = strain.equals("sativa")
                ? plugin.getSativaItems()
                : plugin.getIndicaItems();
        return switch (type) {
            case "seed" -> strainItems.createSeed();
            case "bud" -> strainItems.createBud();
            case "boshka" -> strainItems.createBoshka();
            case "joint" -> strainItems.createJoint();
            case "briquette" -> strainItems.createBriquette();
            case "pack" -> strainItems.createPack();
            default -> null;
        };
    }

    private ItemStack createGashItem(String type) {
        return switch (type) {
            case "gash" -> plugin.getGashItems().createGash();
            case "oil" -> plugin.getGashItems().createGashOil();
            case "cake" -> plugin.getGashItems().createGashCake();
            case "briquette" -> plugin.getGashItems().createGashBriquette();
            case "pack" -> plugin.getGashItems().createGashPack();
            default -> null;
        };
    }

    private ItemStack createSpiceItem(String type) {
        return switch (type) {
            case "spice" -> plugin.getGashItems().createSpice();
            case "briquette" -> plugin.getGashItems().createSpiceBriquette();
            case "pack" -> plugin.getGashItems().createSpicePack();
            default -> null;
        };
    }

    private ItemStack createMedicalItem(String type) {
        return switch (type) {
            case "boshka" -> plugin.getMedicalItems().createMedicalBoshka();
            case "joint" -> plugin.getMedicalItems().createMedicalJoint();
            case "gum" -> plugin.getMedicalItems().createMedicalGum();
            default -> null;
        };
    }

    private ItemStack createTobaccoItem(String type) {
        return switch (type) {
            case "seed" -> plugin.getTobaccoItems().createSeed();
            case "tobacco" -> plugin.getTobaccoItems().createTobacco();
            case "filter" -> plugin.getTobaccoItems().createFilter();
            case "cigarette" -> plugin.getTobaccoItems().createCigarette();
            default -> null;
        };
    }
    private void giveGuideBook(Player player) {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();
        meta.setAuthor("GreatWeeb");
        meta.setTitle("Гроверский гайд");

        // Страница 1 – вступление
        Component page1 = Component.text()
                .append(Component.text("════════════\n").color(TextColor.color(0xFFAA00)))
                .append(Component.text("  GreatWeeb\n").color(TextColor.color(0x008800)).decorate(TextDecoration.BOLD))
                .append(Component.text("════════════\n\n").color(TextColor.color(0xFFAA00)))
                .append(Component.text("Добро пожаловать,\nгровер!\n\n").color(TextColor.color(0x000000)))
                .append(Component.text("Этот гайд научит\nвыращивать и перерабатывать растения.\n").color(TextColor.color(0x000000)))
                .build();

        // Страница 2 – содержание (часть 1)
        Component page2 = Component.text()
                .append(Component.text("Содержание (1)\n").color(TextColor.color(0xFFAA00)).decorate(TextDecoration.BOLD))
                .append(Component.text("\n4. Семена\n5-6. Уход\n7. Бошка и брикет\n8. Пак\n9-10. Гашиш и спайс\n").color(TextColor.color(0x000000)))
                .build();

        // Страница 3 – содержание (часть 2)
        Component page3 = Component.text()
                .append(Component.text("Содержание (2)\n").color(TextColor.color(0xFFAA00)).decorate(TextDecoration.BOLD))
                .append(Component.text("\n11. Прочие советы\n12. Инструменты\n13. Удобрения\n14. Сушилка\n15-16. Сигареты\n17-18. Бонг\n19-20. Канна-кухня\n21-22. Дозировка\n").color(TextColor.color(0x000000)))
                .build();

        // Страница 4 – получение семян (бывшая 3)
        Component page4 = Component.text()
                .append(Component.text("Получение семян\n").color(TextColor.color(0xFFAA00)).decorate(TextDecoration.BOLD))
                .append(Component.text("\nСативу, индику и табак\nможно получить:\n\n").color(TextColor.color(0x000000)))
                .append(Component.text("• Рыбалка в открытой\nводе\n").color(TextColor.color(0x0000FF)))
                .append(Component.text("• Покупка через\nзакладки (аддон\nBadCourier, /bc shop)\n").color(TextColor.color(0x0000FF)))
                .append(Component.text("\nУдачного урожая!").color(TextColor.color(0x008800)))
                .build();

        // Страница 5 – уход, часть 1 (бывшая 4)
        Component page5 = Component.text()
                .append(Component.text("Уход: полив и удобрение\n\n").color(TextColor.color(0xFFAA00)).decorate(TextDecoration.BOLD))
                .append(Component.text("1. Полив\n").color(TextColor.color(0x000000)))
                .append(Component.text("Если куст засох,\nполей ведром воды\nили лейкой.\n\n").color(TextColor.color(0x000000)))
                .append(Component.text("2. Удобрение\n").color(TextColor.color(0x000000)))
                .append(Component.text("Удобряй только что\nпосаженный куст.\nБоксовое удобрение\nзащищает от засыхания.\n").color(TextColor.color(0x000000)))
                .build();

        // Страница 6 – уход, часть 2 (бывшая 5)
        Component page6 = Component.text()
                .append(Component.text("3. Ультрафиолет\n").color(TextColor.color(0x000000)))
                .append(Component.text("Перламутровая квампа\n(+20% к росту)\nили фиолетовое стекло\nпод ней (+30%).\n\n").color(TextColor.color(0x000000)))
                .append(Component.text("4. Ультрахлорофилит\n").color(TextColor.color(0x008800)))
                .append(Component.text("Зелёное стекло +\nмалахитовая квампа.\nМедленнее рост (-15%),\nно +2-3 соцветия\nпри сборе!\n\n").color(TextColor.color(0x000000)))
                .append(Component.text("5. Сбор\n").color(TextColor.color(0x000000)))
                .append(Component.text("На выросшем кусте\nиспользуй ножницы\nили секатор.\nСекатор даёт\nбольше опыта!\n\n").color(TextColor.color(0x000000)))
                .append(Component.text("Хорошего урожая!").color(TextColor.color(0x008800)))
                .build();

        // Страница 7 – бошка и брикет (бывшая 6)
        Component page7 = Component.text()
                .append(Component.text("Бошка и брикет\n\n").color(TextColor.color(0xFFAA00)).decorate(TextDecoration.BOLD))
                .append(Component.text("Бошка\n").color(TextColor.color(0x000000)))
                .append(Component.text("Высуши соцветие\nв печи (или сушилке).\n\n").color(TextColor.color(0x000000)))
                .append(Component.text("Брикет\n").color(TextColor.color(0x000000)))
                .append(Component.text("8 бошек + бумага\n(крафт без формы).\nПКМ по брикету\nвернёт 8 бошек.\n").color(TextColor.color(0x000000)))
                .build();

        // Страница 8 – пак (бывшая 7)
        Component page8 = Component.text()
                .append(Component.text("Пак\n").color(TextColor.color(0xFFAA00)).decorate(TextDecoration.BOLD))
                .append(Component.text("\n9 брикетов в сетке 3х3.\n\nПоставь как блок\nи сломай — получишь\nобратно 9 брикетов.\n\nТакже можно перекрафтить пак в 9 брикетов\nв верстаке.\n").color(TextColor.color(0x000000)))
                .build();

        // Страница 9 – гашиш (бывшая 8)
        Component page9 = Component.text()
                .append(Component.text("Гашиш и спайс\n\n").color(TextColor.color(0xFFAA00)).decorate(TextDecoration.BOLD))
                .append(Component.text("Гашиш:\n").color(TextColor.color(0x000000)))
                .append(Component.text("Обожги бошку сативы\nили индики в печи.\n\nБрикеты и паки\nдля гашиша делаются\nаналогично.\n").color(TextColor.color(0x000000)))
                .build();

        // Страница 10 – спайс (бывшая 9)
        Component page10 = Component.text()
                .append(Component.text("Спайс:\n").color(TextColor.color(0xFFAA00)).decorate(TextDecoration.BOLD))
                .append(Component.text("\nСветопыль + 2 измельчённой марихуаны +\n1 гашишное масло.\n(крафт без формы)\n\nБрикеты и паки\nдля спайса также\nработают!\n").color(TextColor.color(0x000000)))
                .build();

        // Страница 11 – прочие советы (бывшая 10)
        Component page11 = Component.text()
                .append(Component.text("Полезные советы\n\n").color(TextColor.color(0xFFAA00)).decorate(TextDecoration.BOLD))
                .append(Component.text("• Измельчай соцветия\n   ножницами.\n").color(TextColor.color(0x000000)))
                .append(Component.text("• Смешивание сортов\n   при курении опасно.\n").color(TextColor.color(0x000000)))
                .append(Component.text("• Используй сушилку\n   для автоматизации.\n").color(TextColor.color(0x000000)))
                .build();

        // Страница 12 – инструменты (бывшая 11)
        Component page12 = Component.text()
                .append(Component.text("Инструменты\n\n").color(TextColor.color(0xFFAA00)).decorate(TextDecoration.BOLD))
                .append(Component.text("Секатор\n").color(TextColor.color(0x000000)))
                .append(Component.text("Ножницы + 2 железных\nслитка (форма: N сверху,\nI I снизу).\n\n").color(TextColor.color(0x000000)))
                .append(Component.text("Лейка\n").color(TextColor.color(0x000000)))
                .append(Component.text("Ведро + 2 железных\nслитка + нить\n(крафт без формы).\nНаполни из воды.\n").color(TextColor.color(0x000000)))
                .build();

        // Страница 13 – удобрения (бывшая 12)
        Component page13 = Component.text()
                .append(Component.text("Удобрения\n").color(TextColor.color(0xFFAA00)).decorate(TextDecoration.BOLD))
                .append(Component.text("Обычное удобрение\n").color(TextColor.color(0x000000)))
                .append(Component.text("Костная мука + гнилая\nплоть + измельчённая\nмарихуана.\n").color(TextColor.color(0x000000)))
                .append(Component.text("\nБоксовое удобрение\n").color(TextColor.color(0x000000)))
                .append(Component.text("4 костной муки +\nизмельчённая марихуана\n+ 3 табака + любой лёд.\n(крафт без формы)\n").color(TextColor.color(0x000000)))
                .build();

        // Страница 14 – сушилка (бывшая 13)
        Component page14 = Component.text()
                .append(Component.text("Сушилка\n\n").color(TextColor.color(0xFFAA00)).decorate(TextDecoration.BOLD))
                .append(Component.text("В верстаке:\n\nВерхний ряд — 3 доски\nСредний ряд — нить,\nпусто, нить\nНижний ряд — 3 доски\n\nСушит соцветия и табак\nбез топлива.\n").color(TextColor.color(0x000000)))
                .build();

        // Страница 15 – Сигареты (1): фильтр и сигарета
        Component page15 = Component.text()
                .append(Component.text("Сигареты (1)\n\n").color(TextColor.color(0xFFAA00)).decorate(TextDecoration.BOLD))
                .append(Component.text("Фильтр:\nшерсть + 2 бумаги.\n\n").color(TextColor.color(0x000000)))
                .append(Component.text("Сигарета:\nфильтр + 2 бумаги +\nизмельчённый табак\n(крафт без формы).\n").color(TextColor.color(0x000000)))
                .build();

        // Страница 16 – Сигареты (2): пачка и блок
        Component page16 = Component.text()
                .append(Component.text("Сигареты (2)\n\n").color(TextColor.color(0xFFAA00)).decorate(TextDecoration.BOLD))
                .append(Component.text("Пачка:\n20 сигарет + бумага\n(ПКМ в руке, также\nможно распаковать).\n\n").color(TextColor.color(0x000000)))
                .append(Component.text("Блок сигарет:\n9 пачек в верстаке\n(без формы). Поставь\nи сломай для 9 пачек.\n").color(TextColor.color(0x000000)))
                .build();

        // Страница 17 – Бонг (1): крафт и загрузка
        Component page17 = Component.text()
                .append(Component.text("Бонг\n\n").color(TextColor.color(0xFFAA00)).decorate(TextDecoration.BOLD))
                .append(Component.text("Крафт:\nбутылка + палка +\nогниво ИЛИ\nбутылка + стержешь ифрита.\n\n").color(TextColor.color(0x000000)))
                .append(Component.text("Загрузка:\nПКМ с бошкой/гашишем\n/спайсом в левой руке.\n").color(TextColor.color(0x000000)))
                .build();

        // Страница 18 – Бонг (2): курение
        Component page18 = Component.text()
                .append(Component.text("Бонг\n\n").color(TextColor.color(0xFFAA00)).decorate(TextDecoration.BOLD))
                .append(Component.text("Курение:\nзажмите зажигалку\nв левой руке и ПКМ\nпо бонгу.\n\n").color(TextColor.color(0x000000)))
                .append(Component.text("Эффект сильнее,\nчем от косяка.\n").color(TextColor.color(0x000000)))
                .build();

        // Страница 19 – Канна-кухня (1): брауни и печенье
        Component page19 = Component.text()
                .append(Component.text("Канна-кухня (1)\n\n").color(TextColor.color(0xFFAA00)).decorate(TextDecoration.BOLD))
                .append(Component.text("Брауни:\n2 пшеницы + какао +\nсахар + гашишное масло.\n\n").color(TextColor.color(0x000000)))
                .append(Component.text("Вкусное печенье:\n2 пшеницы + какао +\nмедицинская бошка\n(даёт 2 шт.).\n").color(TextColor.color(0x000000)))
                .build();

        // Страница 20 – Канна-кухня (2): гашишный пирог
        Component page20 = Component.text()
                .append(Component.text("Канна-кухня (2)\n\n").color(TextColor.color(0xFFAA00)).decorate(TextDecoration.BOLD))
                .append(Component.text("Гашишный пирог:\n2 гашишного масла +\nгашиш + 3 пшеницы.\n\n").color(TextColor.color(0x000000)))
                .append(Component.text("Наслаждайся\nс осторожностью!").color(TextColor.color(0x008800)))
                .build();

        // Страница 21 – Дозировка
        Component page21 = Component.text()
                .append(Component.text("Дозировка\n\n").color(TextColor.color(0xFFAA00)).decorate(TextDecoration.BOLD))
                .append(Component.text("Не смешивай сорта!\nЧастое курение ведёт\nк тошноте, слепоте\nи слабости.\n\n").color(TextColor.color(0x000000)))
                .append(Component.text("Передышка: 10 сек.\n").color(TextColor.color(0x000000)))
                .build();

        // Страница 22 – Передозировка и помощь
        Component page22 = Component.text()
                .append(Component.text("Передозировка\n\n").color(TextColor.color(0xFFAA00)).decorate(TextDecoration.BOLD))
                .append(Component.text("Симптомы: тошнота,\nслепота, слабость,\nотравление.\n\nСпайс смертельно\nопасен!\n\n").color(TextColor.color(0x000000)))
                .append(Component.text("Мед. жвачка убирает\nзапах и помогает.").color(TextColor.color(0x000000)))
                .build();

        meta.addPages(page1, page2, page3, page4, page5, page6, page7, page8, page9,
                page10, page11, page12, page13, page14, page15, page16, page17, page18,
                page19, page20, page21, page22);
        book.setItemMeta(meta);
        player.openBook(book);
    }
    private void sendHelp(Player player) {
        player.sendMessage("§eИспользование:");
        player.sendMessage("§7/gw <sativa|indica|gash|spice|medical|tobacco> <предмет>");
        player.sendMessage("§7/gw <bong|fertilizer|spice|shredded|brownie|cookie|gash|gash_oil|gash_briquette|gash_pack|spice_briquette|spice_pack|medical_boshka|medical_joint|medical_gum|tobacco_seed|tobacco|box_fertilizer|cigarette_pack|cigarette_block|filter|cigarette|watering_can>");
        player.sendMessage("§7/gw smell [ник] — убрать зелёный дым");
        player.sendMessage("§7/gw nosmoke [ник] — очистить историю курения");
        player.sendMessage("§7/gw all — выдать все предметы плагина");
    }

    private void giveAllItems(Player player) {
        var sativa = plugin.getSativaItems();
        var indica = plugin.getIndicaItems();
        var gash = plugin.getGashItems();
        var bong = plugin.getBongItems();
        var fertilizer = plugin.getFertilizerItems();
        var medical = plugin.getMedicalItems();
        var tobacco = plugin.getTobaccoItems();
        var filter = plugin.getTobaccoItems();
        var cigarette = plugin.getTobaccoItems();
        var sekator = plugin.getSekatorItems();

        addStrainItems(player, sativa);
        addStrainItems(player, indica);

        player.getInventory().addItem(gash.createGash());
        player.getInventory().addItem(gash.createGashOil());
        player.getInventory().addItem(gash.createGashCake());
        player.getInventory().addItem(gash.createSpice());
        player.getInventory().addItem(gash.createShreddedWeed());
        player.getInventory().addItem(gash.createCannaBrownie());
        player.getInventory().addItem(gash.createTastyCookie());
        player.getInventory().addItem(gash.createGashBriquette());
        player.getInventory().addItem(gash.createGashPack());
        player.getInventory().addItem(gash.createSpiceBriquette());
        player.getInventory().addItem(gash.createSpicePack());

        player.getInventory().addItem(bong.createBong());
        player.getInventory().addItem(fertilizer.createFertilizer());

        player.getInventory().addItem(medical.createMedicalBoshka());
        player.getInventory().addItem(medical.createMedicalJoint());
        player.getInventory().addItem(medical.createMedicalGum());

        player.getInventory().addItem(tobacco.createSeed());
        player.getInventory().addItem(tobacco.createTobacco());
        player.getInventory().addItem(tobacco.createBoxFertilizer());
        player.getInventory().addItem(tobacco.createCigarettePack());
        player.getInventory().addItem(tobacco.createCigaretteBlock());
        player.getInventory().addItem(filter.createFilter());
        player.getInventory().addItem(cigarette.createCigarette());

        player.getInventory().addItem(sekator.createEmptyWateringCan());

        player.sendMessage("§aВам выданы все предметы GreatWeeb!");
    }

    private void addStrainItems(Player player, RecipeManager.StrainItems strain) {
        player.getInventory().addItem(strain.createSeed());
        player.getInventory().addItem(strain.createBud());
        player.getInventory().addItem(strain.createBoshka());
        player.getInventory().addItem(strain.createJoint());
        player.getInventory().addItem(strain.createBriquette());
        player.getInventory().addItem(strain.createPack());
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            if (sender.hasPermission("greatweeb.admin")) {
                completions.addAll(filterStarting(args[0],
                        "sativa", "indica", "gash", "spice", "medical", "tobacco",
                        "bong", "fertilizer", "spice", "shredded",
                        "brownie", "cookie", "gash", "gash_oil", "gash_briquette", "gash_pack",
                        "spice_briquette", "spice_pack",
                        "medical_boshka", "medical_joint", "medical_gum",
                        "tobacco_seed", "tobacco", "box_fertilizer", "cigarette_pack", "cigarette_block",
                        "filter", "cigarette", "watering_can",
                        "smell", "nosmoke", "all"));
            }
            if ("guide".startsWith(args[0].toLowerCase())) {
                completions.add("guide");
            }
            return completions;
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
                return switch (strain) {
                    case "sativa", "indica" -> filterStarting(args[1], "seed", "bud", "boshka", "joint", "briquette", "pack");
                    case "gash" -> filterStarting(args[1], "gash", "oil", "cake", "briquette", "pack");
                    case "spice" -> filterStarting(args[1], "spice", "briquette", "pack");
                    case "medical" -> filterStarting(args[1], "boshka", "joint", "gum");
                    case "tobacco" -> filterStarting(args[1], "seed", "tobacco", "filter", "cigarette");
                    default -> List.of();
                };
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