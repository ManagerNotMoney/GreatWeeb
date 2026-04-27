package io.github.potaseval.listeners;

import io.github.potaseval.GreatWeeb;
import io.github.potaseval.items.IndicaItems;
import io.github.potaseval.items.SativaItems;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class OverdoseListener implements Listener { // интерфейс можно оставить, но слушатель не используется для курения

    private final SativaItems sativaItems;
    private final IndicaItems indicaItems;
    private static final int DEFAULT_OVERDOSE_THRESHOLD = 6;
    private static final int SPICE_OVERDOSE_THRESHOLD = 3;

    private final Map<UUID, List<Long>> smokeHistory = new HashMap<>();
    private final Map<UUID, String> lastStrain = new HashMap<>();
    private final Map<UUID, Boolean> wasOverdosed = new HashMap<>();

    private final Map<String, Integer> thresholds = new HashMap<>() {{
        put("sativa", DEFAULT_OVERDOSE_THRESHOLD);
        put("indica", DEFAULT_OVERDOSE_THRESHOLD);
        put("gash", DEFAULT_OVERDOSE_THRESHOLD);
        put("cake", DEFAULT_OVERDOSE_THRESHOLD);
        put("spice", SPICE_OVERDOSE_THRESHOLD);
        put("brownie", 4);
    }};

    public OverdoseListener(GreatWeeb plugin) {
        this.sativaItems = plugin.getSativaItems();
        this.indicaItems = plugin.getIndicaItems();
    }

    public void clearPlayerData(Player player) {
        UUID uuid = player.getUniqueId();
        smokeHistory.remove(uuid);
        lastStrain.remove(uuid);
        wasOverdosed.remove(uuid);
    }

    public void registerSmoke(Player player, String currentStrain) {
        UUID playerId = player.getUniqueId();
        long now = System.currentTimeMillis();
        List<Long> timestamps = smokeHistory.computeIfAbsent(playerId, k -> new ArrayList<>());
        timestamps.removeIf(time -> now - time > 90_000);
        timestamps.add(now);

        String previous = lastStrain.get(playerId);
        boolean mixed = previous != null && !previous.equals(currentStrain);
        if (mixed) {
            player.sendMessage("§7Это была плохая идея смешивать их...");
        }
        lastStrain.put(playerId, currentStrain);

        int count = timestamps.size();
        int threshold = thresholds.getOrDefault(currentStrain, DEFAULT_OVERDOSE_THRESHOLD);
        boolean overdoseCondition = count >= threshold || mixed;
        boolean alreadyOverdosed = wasOverdosed.getOrDefault(playerId, false);

        if (overdoseCondition) {
            applyOverdose(player, currentStrain);
            if (!alreadyOverdosed) {
                smokeHistory.remove(playerId);
                lastStrain.remove(playerId);
                wasOverdosed.put(playerId, true);
            } else {
                player.sendMessage("§cМне становится ещё хуже...");
            }
        } else {
            wasOverdosed.remove(playerId);
            if (count == threshold - 1) {
                player.sendMessage("§7Ох, кажется мне достаточно..");
            }
        }
    }

    private void applyOverdose(Player player, String strain) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 30 * 20, 2));
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 15 * 20, 0));
        player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 60 * 20, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 10 * 20, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 20 * 20, 2));
        if ("spice".equals(strain)) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 120 * 20, 1)); // Иссушение II (уровень 1)
        }

        player.sendMessage("§cЧто-то мне совсем не хорошо...");
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1.0f, 0.5f);
    }
}