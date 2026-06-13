package io.github.potaseval;

import io.github.potaseval.items.*;
import io.github.potaseval.listeners.block.CigaretteBlockListener;
import io.github.potaseval.listeners.block.PackBlockListener;
import io.github.potaseval.listeners.dryingrack.DryingRackInventoryListener;
import io.github.potaseval.listeners.dryingrack.DryingRackListener;
import io.github.potaseval.listeners.item.BriquetteUseListener;
import io.github.potaseval.listeners.plant.PlantCareListener;
import io.github.potaseval.listeners.plant.PlantListener;
import io.github.potaseval.listeners.plant.WateringCanListener;
import io.github.potaseval.listeners.item.WeedShredderListener;
import io.github.potaseval.listeners.smoking.*;
import io.github.potaseval.managers.DelayedEffectManager;
import io.github.potaseval.managers.DryingRackManager;
import io.github.potaseval.managers.SmokeEffectManager;
import io.github.potaseval.util.PlantDataUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class GreatWeeb extends JavaPlugin implements Listener {

    // -------------------------------
    //  Предметные контроллеры (items)
    // -------------------------------
    private SativaItems sativaItems;
    private IndicaItems indicaItems;
    private GashItems gashItems;
    private BongItems bongItems;
    private FertilizerItems fertilizerItems;
    private MedicalItems medicalItems;
    private SekatorItems sekatorItems;
    private TobaccoItems tobaccoItems;

    // -------------------------------
    //  Менеджеры (managers)
    // -------------------------------
    private SmokeEffectManager smokeEffectManager;
    private DelayedEffectManager delayedEffectManager;
    private DryingRackManager dryingRackManager;

    // -------------------------------
    //  Слушатели / побочные объекты
    // -------------------------------
    private OverdoseListener overdoseListener;

    // -------------------------------
    //  Прочие данные
    // -------------------------------
    private final Map<UUID, Long> smokeCooldowns = new HashMap<>();

    @Override
    public void onEnable() {
        getLogger().info("GreatWeeb Загружен!");

        // 1. Инициализация всех предметных классов
        PlantDataUtils.init(this);
        initItemControllers();

        // 2. Инициализация менеджеров
        initManagers();

        // 3. Регистрация всех слушателей
        registerListeners();

        // 4. Регистрация рецептов
        registerRecipes();

        // 5. Регистрация команд
        registerCommands();
    }

    // ----------------------------------
    //  Методы инициализации компонентов
    // ----------------------------------

    private void initItemControllers() {
        sativaItems = new SativaItems(this);
        indicaItems = new IndicaItems(this);
        gashItems = new GashItems(this);
        bongItems = new BongItems(this);
        fertilizerItems = new FertilizerItems(this);
        medicalItems = new MedicalItems(this);
        sekatorItems = new SekatorItems(this);
        tobaccoItems = new TobaccoItems(this);
    }

    private void initManagers() {
        smokeEffectManager = new SmokeEffectManager(this);
        delayedEffectManager = new DelayedEffectManager();

        DryingRack dryingRack = new DryingRack(this);
        dryingRackManager = new DryingRackManager(this, sativaItems, indicaItems, gashItems, medicalItems, tobaccoItems);
        dryingRackManager.loadRacks();
    }

    private void registerListeners() {
        // Слушатель передозировки
        overdoseListener = new OverdoseListener(this);
        getServer().getPluginManager().registerEvents(overdoseListener, this);

        // Слушатели сушилок
        getServer().getPluginManager().registerEvents(
                new DryingRackListener(this, new DryingRack(this), dryingRackManager), this);
        getServer().getPluginManager().registerEvents(new DryingRackInventoryListener(), this);

        // Слушатели курения и загрузки бонга
        getServer().getPluginManager().registerEvents(
                new JointSmokeListener(this, overdoseListener, smokeEffectManager, delayedEffectManager, medicalItems), this);
        getServer().getPluginManager().registerEvents(
                new BongSmokeListener(this, overdoseListener, smokeEffectManager, delayedEffectManager, medicalItems), this);
        getServer().getPluginManager().registerEvents(
                new BongLoadListener(this, medicalItems), this);
        getServer().getPluginManager().registerEvents(
                new CigaretteSmokeListener(this, delayedEffectManager, smokeEffectManager), this);

        // Слушатели растений
        PlantListener plantListener = new PlantListener(this);
        PlantCareListener plantCareListener = new PlantCareListener(this);
        getServer().getPluginManager().registerEvents(plantListener, this);
        getServer().getPluginManager().registerEvents(plantCareListener, this);
        getServer().getPluginManager().registerEvents(
                new WateringCanListener(this), this);

        // Слушатели предметов (брикеты, измельчение, блоки)
        getServer().getPluginManager().registerEvents(new BriquetteUseListener(this), this);
        getServer().getPluginManager().registerEvents(new WeedShredderListener(this), this);
        getServer().getPluginManager().registerEvents(new CigaretteBlockListener(this), this);
        getServer().getPluginManager().registerEvents(new PackBlockListener(this), this);

        // Слушатель самого плагина (PlayerQuit)
        getServer().getPluginManager().registerEvents(this, this);
    }

    private void registerRecipes() {
        RecipeManager recipeManager = new RecipeManager(this,
                sativaItems, indicaItems, gashItems, bongItems, fertilizerItems,
                new DryingRack(this), medicalItems, sekatorItems, tobaccoItems);
        recipeManager.registerAll();
    }

    private void registerCommands() {
        if (getCommand("gw") != null) {
            GwCommandHandler commandHandler = new GwCommandHandler(this);
            getCommand("gw").setExecutor(commandHandler);
            getCommand("gw").setTabCompleter(commandHandler);
        } else {
            getLogger().warning("Команда 'gw' не найдена в plugin.yml!");
        }
    }

    // ----------------------------------
    //  Обработчик выхода игрока
    // ----------------------------------
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        smokeEffectManager.stop(player);
        overdoseListener.clearPlayerData(player);
        delayedEffectManager.cancel(player);
        smokeCooldowns.remove(player.getUniqueId());
    }

    // ----------------------------------
    //  Отключение плагина
    // ----------------------------------
    @Override
    public void onDisable() {
        smokeEffectManager.stopAll();
        delayedEffectManager.cancelAll();
        dryingRackManager.saveRacks();
        getLogger().info("GreatWeeb Отключен!");
    }

    // ----------------------------------
    //  Утилиты для кулдауна курения
    // ----------------------------------
    public boolean canSmoke(Player player) {
        Long last = smokeCooldowns.get(player.getUniqueId());
        return last == null || System.currentTimeMillis() - last >= 10_000;
    }

    public void setSmokeCooldown(Player player) {
        smokeCooldowns.put(player.getUniqueId(), System.currentTimeMillis());
    }

    public long getSmokeCooldownRemaining(Player player) {
        Long last = smokeCooldowns.get(player.getUniqueId());
        if (last == null) return 0;
        long elapsed = System.currentTimeMillis() - last;
        return elapsed >= 10_000 ? 0 : (10_000 - elapsed) / 1000;
    }

    // ----------------------------------
    //  Геттеры
    // ----------------------------------
    public SativaItems getSativaItems() { return sativaItems; }
    public IndicaItems getIndicaItems() { return indicaItems; }
    public GashItems getGashItems() { return gashItems; }
    public BongItems getBongItems() { return bongItems; }
    public OverdoseListener getOverdoseListener() { return overdoseListener; }
    public FertilizerItems getFertilizerItems() { return fertilizerItems; }
    public SmokeEffectManager getSmokeEffectManager() { return smokeEffectManager; }
    public MedicalItems getMedicalItems() { return medicalItems; }
    public SekatorItems getSekatorItems() { return sekatorItems; }
    public TobaccoItems getTobaccoItems() { return tobaccoItems; }
}