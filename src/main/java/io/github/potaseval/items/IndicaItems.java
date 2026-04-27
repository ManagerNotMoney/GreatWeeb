package io.github.potaseval.items;

import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

public class IndicaItems extends StrainItemsImpl {
    public IndicaItems(JavaPlugin plugin) {
        super(plugin, "indica", "Индики", TextColor.color(0x8B00FF),
                3001, Material.MELON_SEEDS, Material.BUSH);
    }
}