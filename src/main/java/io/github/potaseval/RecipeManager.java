package io.github.potaseval;

import io.github.potaseval.items.*;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.*;
import org.bukkit.plugin.java.JavaPlugin;

public class RecipeManager {

    private final JavaPlugin plugin;
    private final SativaItems sativa;
    private final IndicaItems indica;
    private final GashItems gash;
    private final BongItems bong;
    private final FertilizerItems fertilizer;
    private final DryingRack dryingRack;
    private final MedicalItems medicalItems;
    private final SekatorItems sekatorItems;
    private final TobaccoItems tobaccoItems;

    public RecipeManager(JavaPlugin plugin, SativaItems sativa, IndicaItems indica, GashItems gash,
                         BongItems bong, FertilizerItems fertilizer, DryingRack dryingRack,
                         MedicalItems medicalItems, SekatorItems sekatorItems,
                         TobaccoItems tobaccoItems) {
        this.plugin = plugin;
        this.sativa = sativa;
        this.indica = indica;
        this.gash = gash;
        this.bong = bong;
        this.fertilizer = fertilizer;
        this.dryingRack = dryingRack;
        this.medicalItems = medicalItems;
        this.sekatorItems = sekatorItems;
        this.tobaccoItems = tobaccoItems;
    }

    public void registerAll() {
        registerStrainRecipes(sativa);
        registerStrainRecipes(indica);
        registerGashRecipes();
        registerBongRecipe();
        registerSpiceRecipe();
        registerFertilizerRecipe();
        registerDryingRackRecipe();
        registerMedicalJointRecipe();
        registerCannaBrownieRecipe();
        registerTastyCookieRecipe();
        registerMedicalGumRecipe();
        registerSpiceBriquetteRecipe();
        registerSpicePackRecipe();
        registerGashBriquetteRecipe();
        registerGashPackRecipe();
        registerSekatorRecipe();
        registerFilterRecipe();
        registerCigaretteRecipe();
        registerBoxFertilizerRecipe();
        registerCigarettePackUnpacking();
        registerWateringCanRecipe();
        registerCigaretteBlockRecipe();
    }
    private void registerSekatorRecipe() {
        NamespacedKey key = new NamespacedKey(plugin, "sekator_crafting");
        ShapedRecipe recipe = new ShapedRecipe(key, sekatorItems.createSekator());
        recipe.shape(" N ", "I I", "   ");
        recipe.setIngredient('N', Material.SHEARS);
        recipe.setIngredient('I', Material.IRON_INGOT);
        plugin.getServer().addRecipe(recipe);
    }
    private void registerFilterRecipe() {
        NamespacedKey key = new NamespacedKey(plugin, "filter_crafting");
        ShapelessRecipe recipe = new ShapelessRecipe(key, tobaccoItems.createFilter());
        recipe.addIngredient(1, Material.WHITE_WOOL);
        recipe.addIngredient(2, Material.PAPER);
        plugin.getServer().addRecipe(recipe);
    }
    private void registerCigaretteRecipe() {
        NamespacedKey key = new NamespacedKey(plugin, "cigarette_crafting");
        ShapelessRecipe recipe = new ShapelessRecipe(key, tobaccoItems.createCigarette());
        recipe.addIngredient(new RecipeChoice.ExactChoice(tobaccoItems.createFilter()));
        recipe.addIngredient(2, Material.PAPER);
        recipe.addIngredient(new RecipeChoice.ExactChoice(tobaccoItems.createShreddedTobacco()));
        plugin.getServer().addRecipe(recipe);
    }
    private void registerStrainRecipes(StrainItems strain) {
        String name = strain.getName();

        NamespacedKey smokingKey = new NamespacedKey(plugin, name + "_boshka_smoking");
        SmokingRecipe smokingRecipe = new SmokingRecipe(smokingKey,
                strain.createBoshka(),
                new RecipeChoice.ExactChoice(strain.createBud()),
                0.5f, 250);
        plugin.getServer().addRecipe(smokingRecipe);

        NamespacedKey jointKey = new NamespacedKey(plugin, name + "_joint_crafting");
        ShapedRecipe jointRecipe = new ShapedRecipe(jointKey, strain.createJoint());
        jointRecipe.shape(" P ", " B ", " P ");
        jointRecipe.setIngredient('P', Material.PAPER);
        jointRecipe.setIngredient('B', new RecipeChoice.ExactChoice(strain.createBoshka()));
        plugin.getServer().addRecipe(jointRecipe);
        NamespacedKey seedKey = new NamespacedKey(plugin, name + "_seed_crafting");
        ShapelessRecipe seedRecipe = new ShapelessRecipe(seedKey, strain.createSeed());
        RecipeChoice budChoice = new RecipeChoice.ExactChoice(strain.createBud());
        for (int i = 0; i < 6; i++) {
            seedRecipe.addIngredient(budChoice);
        }
        seedRecipe.addIngredient(3, Material.BONE_MEAL);
        plugin.getServer().addRecipe(seedRecipe);

        NamespacedKey briquetteKey = new NamespacedKey(plugin, name + "_briquette_crafting");
        ShapelessRecipe briquetteRecipe = new ShapelessRecipe(briquetteKey, strain.createBriquette());
        RecipeChoice boshkaChoice = new RecipeChoice.ExactChoice(strain.createBoshka());
        for (int i = 0; i < 8; i++) {
            briquetteRecipe.addIngredient(boshkaChoice);
        }
        briquetteRecipe.addIngredient(Material.PAPER);
        plugin.getServer().addRecipe(briquetteRecipe);

        NamespacedKey packKey = new NamespacedKey(plugin, name + "_pack_crafting");
        ShapedRecipe packRecipe = new ShapedRecipe(packKey, strain.createPack());
        packRecipe.shape("BBB", "BBB", "BBB");
        packRecipe.setIngredient('B', new RecipeChoice.ExactChoice(strain.createBriquette()));
        plugin.getServer().addRecipe(packRecipe);

        NamespacedKey unpackKey = new NamespacedKey(plugin, name + "_pack_unpacking");
        ItemStack nineBriquettes = strain.createBriquette();
        nineBriquettes.setAmount(9);
        ShapelessRecipe unpackRecipe = new ShapelessRecipe(unpackKey, nineBriquettes);
        unpackRecipe.addIngredient(new RecipeChoice.ExactChoice(strain.createPack()));
        plugin.getServer().addRecipe(unpackRecipe);
    }
    private void registerBongRecipe() {
        NamespacedKey keyMain = new NamespacedKey(plugin, "bong_crafting");
        ShapelessRecipe recipeMain = new ShapelessRecipe(keyMain, bong.createBong());
        recipeMain.addIngredient(Material.STICK);
        recipeMain.addIngredient(Material.GLASS_BOTTLE);
        recipeMain.addIngredient(Material.FLINT_AND_STEEL);
        plugin.getServer().addRecipe(recipeMain);

        NamespacedKey keyBlaze = new NamespacedKey(plugin, "bong_crafting_blaze");
        ShapelessRecipe recipeBlaze = new ShapelessRecipe(keyBlaze, bong.createBong());
        recipeBlaze.addIngredient(Material.GLASS_BOTTLE);
        recipeBlaze.addIngredient(Material.BLAZE_ROD);
        plugin.getServer().addRecipe(recipeBlaze);

    }
    private void registerGashBriquetteRecipe() {
        NamespacedKey key = new NamespacedKey(plugin, "gash_briquette_crafting");
        ShapelessRecipe recipe = new ShapelessRecipe(key, gash.createGashBriquette());
        RecipeChoice gashChoice = new RecipeChoice.ExactChoice(gash.createGash());
        for (int i = 0; i < 8; i++) {
            recipe.addIngredient(gashChoice);
        }
        recipe.addIngredient(Material.PAPER);
        plugin.getServer().addRecipe(recipe);
    }

    private void registerBoxFertilizerRecipe() {
        NamespacedKey key = new NamespacedKey(plugin, "box_fertilizer_crafting");
        ShapelessRecipe recipe = new ShapelessRecipe(key, tobaccoItems.createBoxFertilizer());
        recipe.addIngredient(4, Material.BONE_MEAL);
        recipe.addIngredient(new RecipeChoice.ExactChoice(gash.createShreddedWeed()));
        recipe.addIngredient(new RecipeChoice.ExactChoice(tobaccoItems.createTobacco()));
        recipe.addIngredient(new RecipeChoice.ExactChoice(tobaccoItems.createTobacco()));
        recipe.addIngredient(new RecipeChoice.ExactChoice(tobaccoItems.createTobacco()));
        recipe.addIngredient(new RecipeChoice.MaterialChoice(Material.ICE, Material.PACKED_ICE, Material.BLUE_ICE));
        plugin.getServer().addRecipe(recipe);
    }
    private void registerGashPackRecipe() {
        NamespacedKey packKey = new NamespacedKey(plugin, "gash_pack_crafting");
        ShapedRecipe packRecipe = new ShapedRecipe(packKey, gash.createGashPack());
        packRecipe.shape("BBB", "BBB", "BBB");
        packRecipe.setIngredient('B', new RecipeChoice.ExactChoice(gash.createGashBriquette()));
        plugin.getServer().addRecipe(packRecipe);

        NamespacedKey unpackKey = new NamespacedKey(plugin, "gash_pack_unpacking");
        ItemStack nineBriquettes = gash.createGashBriquette();
        nineBriquettes.setAmount(9);
        ShapelessRecipe unpackRecipe = new ShapelessRecipe(unpackKey, nineBriquettes);
        unpackRecipe.addIngredient(new RecipeChoice.ExactChoice(gash.createGashPack()));
        plugin.getServer().addRecipe(unpackRecipe);
    }
    private void registerSpiceRecipe() {
        NamespacedKey spiceKey = new NamespacedKey(plugin, "spice_crafting");
        ShapelessRecipe spiceRecipe = new ShapelessRecipe(spiceKey, gash.createSpice());
        spiceRecipe.addIngredient(Material.GLOWSTONE_DUST);
        RecipeChoice.ExactChoice shredded = new RecipeChoice.ExactChoice(gash.createShreddedWeed());
        spiceRecipe.addIngredient(shredded);
        spiceRecipe.addIngredient(shredded);
        spiceRecipe.addIngredient(new RecipeChoice.ExactChoice(gash.createGashOil()));
        plugin.getServer().addRecipe(spiceRecipe);
    }
    private void registerMedicalJointRecipe() {
        NamespacedKey key = new NamespacedKey(plugin, "medical_joint_crafting");
        ShapedRecipe recipe = new ShapedRecipe(key, medicalItems.createMedicalJoint());
        recipe.shape(" P ", " B ", " P ");
        recipe.setIngredient('P', Material.PAPER);
        recipe.setIngredient('B', new RecipeChoice.ExactChoice(medicalItems.createMedicalBoshka()));
        plugin.getServer().addRecipe(recipe);
    }
    private void registerGashRecipes() {
        NamespacedKey sativaBoshkaToGash = new NamespacedKey(plugin, "sativa_boshka_to_gash");
        FurnaceRecipe sativaFurnace = new FurnaceRecipe(
                sativaBoshkaToGash,
                gash.createGash(),
                new RecipeChoice.ExactChoice(sativa.createBoshka()),
                0.3f,
                250
        );
        plugin.getServer().addRecipe(sativaFurnace);

        NamespacedKey indicaBoshkaToGash = new NamespacedKey(plugin, "indica_boshka_to_gash");
        FurnaceRecipe indicaFurnace = new FurnaceRecipe(
                indicaBoshkaToGash,
                gash.createGash(),
                new RecipeChoice.ExactChoice(indica.createBoshka()),
                0.3f,
                200
        );
        plugin.getServer().addRecipe(indicaFurnace);

        NamespacedKey gashToOil = new NamespacedKey(plugin, "gash_to_oil");
        FurnaceRecipe gashFurnace = new FurnaceRecipe(
                gashToOil,
                gash.createGashOil(),
                new RecipeChoice.ExactChoice(gash.createGash()),
                0.5f,
                300
        );
        plugin.getServer().addRecipe(gashFurnace);
        // Рецептик гашишной вкуснятины
        NamespacedKey cakeKey = new NamespacedKey(plugin, "gash_cake_crafting");
        ShapelessRecipe cakeRecipe = new ShapelessRecipe(cakeKey, gash.createGashCake());
        cakeRecipe.addIngredient(new RecipeChoice.ExactChoice(gash.createGashOil()));
        cakeRecipe.addIngredient(new RecipeChoice.ExactChoice(gash.createGashOil()));
        cakeRecipe.addIngredient(new RecipeChoice.ExactChoice(gash.createGash()));
        cakeRecipe.addIngredient(3, Material.WHEAT);
        plugin.getServer().addRecipe(cakeRecipe);
    }
    // Рецептик жосткой жвачки
    private void registerMedicalGumRecipe() {
        NamespacedKey key = new NamespacedKey(plugin, "medical_gum_crafting");
        ShapelessRecipe recipe = new ShapelessRecipe(key, medicalItems.createMedicalGum());
        recipe.addIngredient(new RecipeChoice.ExactChoice(medicalItems.createMedicalBoshka()));
        recipe.addIngredient(Material.SLIME_BALL);
        recipe.addIngredient(Material.SUGAR);
        plugin.getServer().addRecipe(recipe);
    }
    private void registerDryingRackRecipe() {
        NamespacedKey key = new NamespacedKey(plugin, "drying_rack_crafting");
        ShapedRecipe recipe = new ShapedRecipe(key, dryingRack.createDryingRack());
        recipe.shape("WWW", "S S", "WWW");
        recipe.setIngredient('W', Material.OAK_PLANKS);
        recipe.setIngredient('S', Material.STRING);
        plugin.getServer().addRecipe(recipe);
    }
    private void registerSpiceBriquetteRecipe() {
        NamespacedKey key = new NamespacedKey(plugin, "spice_briquette_crafting");
        ShapelessRecipe recipe = new ShapelessRecipe(key, gash.createSpiceBriquette());
        RecipeChoice spiceChoice = new RecipeChoice.ExactChoice(gash.createSpice());
        for (int i = 0; i < 8; i++) {
            recipe.addIngredient(spiceChoice);
        }
        recipe.addIngredient(Material.PAPER);
        plugin.getServer().addRecipe(recipe);
    }

    private void registerSpicePackRecipe() {
        NamespacedKey packKey = new NamespacedKey(plugin, "spice_pack_crafting");
        ShapedRecipe packRecipe = new ShapedRecipe(packKey, gash.createSpicePack());
        packRecipe.shape("BBB", "BBB", "BBB");
        packRecipe.setIngredient('B', new RecipeChoice.ExactChoice(gash.createSpiceBriquette()));
        plugin.getServer().addRecipe(packRecipe);

        NamespacedKey unpackKey = new NamespacedKey(plugin, "spice_pack_unpacking");
        ItemStack nineBriquettes = gash.createSpiceBriquette();
        nineBriquettes.setAmount(9);
        ShapelessRecipe unpackRecipe = new ShapelessRecipe(unpackKey, nineBriquettes);
        unpackRecipe.addIngredient(new RecipeChoice.ExactChoice(gash.createSpicePack()));
        plugin.getServer().addRecipe(unpackRecipe);
    }
    private void registerFertilizerRecipe() {
        NamespacedKey key = new NamespacedKey(plugin, "fertilizer_crafting");
        ShapelessRecipe recipe = new ShapelessRecipe(key, fertilizer.createFertilizer());
        recipe.addIngredient(Material.BONE_MEAL);
        recipe.addIngredient(Material.ROTTEN_FLESH);
        recipe.addIngredient(new RecipeChoice.ExactChoice(gash.createShreddedWeed()));
        plugin.getServer().addRecipe(recipe);
    }
    private void registerCannaBrownieRecipe() {
        NamespacedKey key = new NamespacedKey(plugin, "canna_brownie_crafting");
        ShapelessRecipe recipe = new ShapelessRecipe(key, gash.createCannaBrownie());
        recipe.addIngredient(2, Material.WHEAT);
        recipe.addIngredient(1, Material.COCOA_BEANS);
        recipe.addIngredient(1, Material.SUGAR);
        recipe.addIngredient(new RecipeChoice.ExactChoice(gash.createGashOil()));
        plugin.getServer().addRecipe(recipe);
    }
    private void registerTastyCookieRecipe() {
        NamespacedKey key = new NamespacedKey(plugin, "tasty_cookie_crafting");
        ItemStack result = gash.createTastyCookie();
        result.setAmount(2);
        ShapelessRecipe recipe = new ShapelessRecipe(key, result);
        recipe.addIngredient(2, Material.WHEAT);
        recipe.addIngredient(1, Material.COCOA_BEANS);
        recipe.addIngredient(new RecipeChoice.ExactChoice(medicalItems.createMedicalBoshka()));
        plugin.getServer().addRecipe(recipe);
    }
    private void registerCigarettePackUnpacking() {
        NamespacedKey key = new NamespacedKey(plugin, "cigarette_pack_unpacking");
        ItemStack twentyCigarettes = tobaccoItems.createCigarette();
        twentyCigarettes.setAmount(20);
        ShapelessRecipe recipe = new ShapelessRecipe(key, twentyCigarettes);
        recipe.addIngredient(new RecipeChoice.ExactChoice(tobaccoItems.createCigarettePack()));
        plugin.getServer().addRecipe(recipe);
    }
    private void registerCigaretteBlockRecipe() {
        // Крафт блока из 9 пачек
        NamespacedKey key = new NamespacedKey(plugin, "cigarette_block_craft");
        ShapelessRecipe recipe = new ShapelessRecipe(key, tobaccoItems.createCigaretteBlock());
        RecipeChoice.ExactChoice packChoice = new RecipeChoice.ExactChoice(tobaccoItems.createCigarettePack());
        for (int i = 0; i < 9; i++) {
            recipe.addIngredient(packChoice);
        }
        plugin.getServer().addRecipe(recipe);
        NamespacedKey unpackKey = new NamespacedKey(plugin, "cigarette_block_unpack");
        ItemStack ninePacks = tobaccoItems.createCigarettePack();
        ninePacks.setAmount(9);
        ShapelessRecipe unpackRecipe = new ShapelessRecipe(unpackKey, ninePacks);
        unpackRecipe.addIngredient(new RecipeChoice.ExactChoice(tobaccoItems.createCigaretteBlock()));
        plugin.getServer().addRecipe(unpackRecipe);
    }
    private void registerWateringCanRecipe() {
        NamespacedKey key = new NamespacedKey(plugin, "watering_can_crafting");
        ShapelessRecipe recipe = new ShapelessRecipe(key, sekatorItems.createEmptyWateringCan());
        recipe.addIngredient(1, Material.BUCKET);
        recipe.addIngredient(2, Material.IRON_INGOT);
        recipe.addIngredient(1, Material.STRING);
        plugin.getServer().addRecipe(recipe);
    }
    public interface StrainItems {
        String getName();
        ItemStack createSeed();
        ItemStack createBud();
        ItemStack createBoshka();
        ItemStack createJoint();
        ItemStack createBriquette();
        ItemStack createPack();
    }
}