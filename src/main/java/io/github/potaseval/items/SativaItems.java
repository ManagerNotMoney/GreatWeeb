package io.github.potaseval.items;

import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

public class SativaItems extends StrainItemsImpl {
    public SativaItems(JavaPlugin plugin) {
        super(plugin, "sativa", "Сативы", TextColor.color(0x00FF00),
                2001, Material.MELON_SEEDS, Material.FERN);
    }
}