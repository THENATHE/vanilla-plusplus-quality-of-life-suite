package com.thenathe.suite.features;

import me.fzzyhmstrs.fzzy_config.annotations.Action;
import me.fzzyhmstrs.fzzy_config.annotations.RequiresAction;
import me.fzzyhmstrs.fzzy_config.api.ConfigApiJava;
import me.fzzyhmstrs.fzzy_config.config.Config;
import me.fzzyhmstrs.fzzy_config.validation.misc.ValidatedBoolean;
import me.fzzyhmstrs.fzzy_config.validation.misc.ValidatedString;
import me.fzzyhmstrs.fzzy_config.validation.number.ValidatedDouble;
import me.fzzyhmstrs.fzzy_config.validation.number.ValidatedFloat;
import me.fzzyhmstrs.fzzy_config.validation.number.ValidatedInt;
import net.minecraft.resources.Identifier;

/** Server-authoritative settings shared by the suite's native feature modules. */
public final class FeatureConfig extends Config {
    public static final FeatureConfig CONFIG = ConfigApiJava.registerAndLoadConfig(FeatureConfig::new);

    public ValidatedBoolean dropsIntoShulker = new ValidatedBoolean(true);
    public ValidatedBoolean shulkerBeforeBackpack = new ValidatedBoolean(true);

    public ValidatedBoolean beaconPlacement = new ValidatedBoolean(true);
    public ValidatedInt beaconMaximumLayers = new ValidatedInt(4, 4, 1);
    public ValidatedBoolean breakBeaconBase = new ValidatedBoolean(true);
    public ValidatedBoolean dropReplacedBeaconBlocks = new ValidatedBoolean(true);

    public ValidatedBoolean conduitPlacement = new ValidatedBoolean(true);
    public ValidatedFloat conduitPlacementDistance = new ValidatedFloat(4.0F, 64.0F, 0.0F);
    public ValidatedBoolean breakConduitFrame = new ValidatedBoolean(true);
    public ValidatedBoolean dropReplacedConduitBlocks = new ValidatedBoolean(true);
    public ValidatedBoolean clearCompletedConduit = new ValidatedBoolean(true);

    public ValidatedBoolean grindEnchantments = new ValidatedBoolean(true);
    public ValidatedBoolean grindExtraction = new ValidatedBoolean(true);
    public ValidatedBoolean grindTransfer = new ValidatedBoolean(true);
    public ValidatedBoolean extractCurses = new ValidatedBoolean(false);
    public ValidatedBoolean grindConsumeSource = new ValidatedBoolean(false);
    public ValidatedBoolean grindOnlyFirst = new ValidatedBoolean(false);
    public ValidatedBoolean grindCostInPoints = new ValidatedBoolean(false);
    public ValidatedDouble grindExtractionFactor = new ValidatedDouble(0.3, 100.0, 0.0);
    public ValidatedDouble grindExtractionOffset = new ValidatedDouble(8.0, 1000.0, 0.0);
    public ValidatedDouble grindTransferNormalFactor = new ValidatedDouble(3.0, 100.0, 0.0);
    public ValidatedDouble grindTransferTreasureFactor = new ValidatedDouble(8.0, 100.0, 0.0);
    public ValidatedDouble grindTransferFactor = new ValidatedDouble(0.5, 100.0, 0.0);
    public ValidatedDouble grindTransferOffset = new ValidatedDouble(0.5, 1000.0, 0.0);

    public ValidatedBoolean netheriteInstamine = new ValidatedBoolean(true);
    public ValidatedDouble instaminePickaxeSpeed = new ValidatedDouble(30.0, 1024.0, 0.0);
    public ValidatedDouble instamineAxeSpeed = new ValidatedDouble(10.0, 1024.0, 0.0);
    public ValidatedBoolean noEndermanGrief = new ValidatedBoolean(true);
    public ValidatedBoolean allWeatherChanneling = new ValidatedBoolean(true);

    @RequiresAction(action = Action.RESTART)
    public ValidatedBoolean swiftFlight = new ValidatedBoolean(true);
    @RequiresAction(action = Action.RESTART)
    public ValidatedBoolean swiftFlightBookLoot = new ValidatedBoolean(true);
    public ValidatedBoolean transferPetOwner = new ValidatedBoolean(true);
    public ValidatedBoolean callParrot = new ValidatedBoolean(true);
    public ValidatedBoolean callNautilus = new ValidatedBoolean(true);
    public ValidatedBoolean callHorse = new ValidatedBoolean(true);
    public ValidatedBoolean callHappyGhast = new ValidatedBoolean(true);
    public ValidatedBoolean callDog = new ValidatedBoolean(true);
    public ValidatedBoolean callCat = new ValidatedBoolean(true);
    public ValidatedBoolean callCrossDimensions = new ValidatedBoolean(true);
    public ValidatedBoolean nautilusRequiresWater = new ValidatedBoolean(true);
    public ValidatedBoolean callMountedPets = new ValidatedBoolean(false);
    public ValidatedBoolean callOccupiedHappyGhasts = new ValidatedBoolean(true);
    public ValidatedBoolean callUnownedHornsShared = new ValidatedBoolean(true);
    public ValidatedBoolean mountCalledPets = new ValidatedBoolean(false);
    public ValidatedBoolean preservePetLeashes = new ValidatedBoolean(true);
    public ValidatedString callHorseTypes = new ValidatedString("horse,donkey,mule,llama,trader_llama,skeleton_horse,zombie_horse,camel,camel_husk");
    public ValidatedString callNautilusTypes = new ValidatedString("nautilus,zombie_nautilus");

    public FeatureConfig() {
        super(Identifier.fromNamespaceAndPath("vpp_mod_suite", "features"));
    }

    /** Load the synchronized settings before either client menus or world callbacks use them. */
    public static void initialize() {}
}
