package io.github.potaseval;

import io.github.potaseval.items.*;
import io.github.potaseval.listeners.*;
import io.github.potaseval.managers.DelayedEffectManager;
import io.github.potaseval.managers.DryingRackManager;
import io.github.potaseval.managers.SmokeEffectManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class GreatWeeb extends JavaPlugin
        implements Listener {

    private SativaItems sativaItems;
    private IndicaItems indicaItems;
    private GashItems gashItems;
    private OverdoseListener overdoseListener;
    private BongItems bongItems;
    private FertilizerItems fertilizerItems;
    private SmokeEffectManager smokeEffectManager;
    private DelayedEffectManager delayedEffectManager;
    private MedicalItems medicalItems;

    @Override
    public void onEnable() {
        getLogger().info("GreatWeeb Загружен!");
        this.sativaItems = new SativaItems(this);
        this.indicaItems = new IndicaItems(this);
        this.gashItems = new GashItems(this);
        this.bongItems = new BongItems(this);
        this.fertilizerItems = new FertilizerItems(this);
        this.smokeEffectManager = new SmokeEffectManager(this);
        this.delayedEffectManager = new DelayedEffectManager();
        this.medicalItems = new MedicalItems(this);

        this.overdoseListener = new OverdoseListener(this);
        getServer().getPluginManager().registerEvents(overdoseListener, this);

        DryingRack dryingRack = new DryingRack(this);
        DryingRackManager dryingRackManager = new DryingRackManager(this, sativaItems, indicaItems, gashItems, medicalItems);
        getServer().getPluginManager().registerEvents(new DryingRackListener(this, dryingRack, dryingRackManager), this);

        getServer().getPluginManager().registerEvents(
                new JointSmokeListener(this, overdoseListener, smokeEffectManager, delayedEffectManager, medicalItems), this);
        getServer().getPluginManager().registerEvents(
                new BongSmokeListener(this, overdoseListener, smokeEffectManager, delayedEffectManager, medicalItems), this);
        getServer().getPluginManager().registerEvents(new PlantListener(this), this);
        getServer().getPluginManager().registerEvents(new BriquetteUseListener(this), this);
        getServer().getPluginManager().registerEvents(new PlantCareListener(this), this);
        getServer().getPluginManager().registerEvents(new BongLoadListener(this, medicalItems), this);
        getServer().getPluginManager().registerEvents(new WeedShredderListener(this), this);
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new DryingRackInventoryListener(), this);

        RecipeManager recipeManager = new RecipeManager(this, sativaItems, indicaItems, gashItems, bongItems, fertilizerItems, dryingRack,medicalItems);
        recipeManager.registerAll();
        if (getCommand("gw") != null) {
            GwCommandHandler commandHandler = new GwCommandHandler(this);
            getCommand("gw").setExecutor(commandHandler);
            getCommand("gw").setTabCompleter(commandHandler);
        } else {
            getLogger().warning("Команда 'gw' не найдена в plugin.yml!");
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        smokeEffectManager.stop(player);
        overdoseListener.clearPlayerData(player);
        delayedEffectManager.cancel(player);  // <-- отмена отложенных эффектов
    }

    @Override
    public void onDisable() {
        smokeEffectManager.stopAll();
        delayedEffectManager.cancelAll();     // <-- очистка всех задач
        getLogger().info("GreatWeeb Отключен!");
    }

    public SativaItems getSativaItems() { return sativaItems; }
    public IndicaItems getIndicaItems() { return indicaItems; }
    public GashItems getGashItems() { return gashItems; }
    public BongItems getBongItems() { return bongItems; }
    public OverdoseListener getOverdoseListener() { return overdoseListener; }
    public FertilizerItems getFertilizerItems() { return fertilizerItems; }
    public SmokeEffectManager getSmokeEffectManager() { return smokeEffectManager; }
    public MedicalItems getMedicalItems() { return medicalItems; }
    }