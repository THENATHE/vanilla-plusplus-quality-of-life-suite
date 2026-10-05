package me.pajic.toolpouch.config;

import me.fzzyhmstrs.fzzy_config.annotations.Version;
import me.fzzyhmstrs.fzzy_config.config.Config;
import me.fzzyhmstrs.fzzy_config.config.ConfigSection;
import me.fzzyhmstrs.fzzy_config.validation.ValidatedField;
import me.fzzyhmstrs.fzzy_config.validation.collection.ValidatedSet;
import me.fzzyhmstrs.fzzy_config.validation.misc.ValidatedAny;
import me.fzzyhmstrs.fzzy_config.validation.misc.ValidatedBoolean;
import me.fzzyhmstrs.fzzy_config.validation.number.ValidatedInt;
import me.pajic.toolpouch.ToolPouch;
import me.pajic.toolpouch.platform.MultiLoaderUtil;
import me.pajic.toolpouch.util.AllowedItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

@Version(version = 1)
public class ModConfig extends Config {

	private final ValidatedAny<AllowedItem> allowedItem = new ValidatedAny<>(new AllowedItem());

    public ModConfig() {
        super(ToolPouch.id("config"));
		ValidatedField.Companion.attachProvider(allowedItem, Provider.Companion.getWIDGET_TITLE(), (ai, _) -> {
			String s = ai.id.get();
			if (s.startsWith("#")) {
				Identifier tagId = Identifier.tryParse(s.substring(1));
				if (tagId != null) {
					TagKey<Item> tag = TagKey.create(Registries.ITEM, tagId);
					return Component.translatable("text.toolpouch.tag", Component.translatableWithFallback(
							MultiLoaderUtil.INSTANCE.getTagTranslationKey(tag), tag.location().toString())
					);
				}
			}
			Item item = BuiltInRegistries.ITEM.getValue(Identifier.tryParse(s));
			if (item == Items.AIR) return Component.literal(s);
			return item.getName(new ItemStack(item));
		});
    }

	public ValidatedInt toolPouchRows = new ValidatedInt(4, 16, 1);
	public ValidatedInt toolPouchColumns = new ValidatedInt(4, 16, 1);
	public ValidatedInt netheriteToolPouchRows = new ValidatedInt(5, 16, 1);
	public ValidatedInt netheriteToolPouchColumns = new ValidatedInt(5, 16, 1);
	public ValidatedBoolean canOpenFromInventory = new ValidatedBoolean(true);
	public ValidatedBoolean canOpenWithRightClick = new ValidatedBoolean(true);
	public ValidatedBoolean canAttachToLeggings = new ValidatedBoolean(true);
	public ValidatedBoolean allowUseFromInventory = new ValidatedBoolean(true);
	public ValidatedBoolean preventUnequipWhenNotEmpty = new ValidatedBoolean(false);
	public ValidatedBoolean equippableItemTooltip = new ValidatedBoolean(true);
	public ValidatedSet<AllowedItem> allowedItems = allowedItem.toSet(
			new AllowedItem("#minecraft:arrows", 0, 0),
			new AllowedItem("#minecraft:lanterns", 1, 1),
			new AllowedItem("#minecraft:shulker_boxes", 1, 0),
			new AllowedItem("minecraft:clock", 1, 1),
			new AllowedItem("minecraft:compass", 1, 0),
			new AllowedItem("minecraft:elytra", 1, 1),
			new AllowedItem("minecraft:ender_chest", 1, 1),
			new AllowedItem("minecraft:filled_map", 1, 0),
			new AllowedItem("minecraft:firework_rocket", 0, 0),
			new AllowedItem("minecraft:recovery_compass", 1, 1),
			new AllowedItem("minecraft:spyglass", 1, 1),
			new AllowedItem("minecraft:totem_of_undying", 1, 1),
			new AllowedItem("improved-maps:atlas", 1, 0),
			new AllowedItem("sereneseasons:calendar", 1, 1),
			new AllowedItem("spelunkery:magnetic_compass", 1, 1),
			new AllowedItem("firmaciv:nav_clock", 1, 1),
			new AllowedItem("firmaciv:firmaciv_compass", 1, 1),
			new AllowedItem("spelunkery:depth_gauge", 1, 1),
			new AllowedItem("caverns_and_chasms:depth_gauge", 1, 1),
			new AllowedItem("additionaladditions:depth_meter", 1, 1),
			new AllowedItem("supplementaries:altimeter", 1, 1),
			new AllowedItem("depthmeter:depthmeter", 1, 1),
			new AllowedItem("firmaciv:sextant", 1, 1),
			new AllowedItem("caverns_and_chasms:barometer", 1, 1),
			new AllowedItem("firmaciv:barometer", 1, 1),
			new AllowedItem("oreganized:thermometer", 1, 1),
			new AllowedItem("toughasnails:thermometer", 1, 1),
			new AllowedItem("legendarysurvivaloverhaul:thermometer", 1, 1),
			new AllowedItem("cold_sweat:thermometer", 1, 1),
			new AllowedItem("seasonsextras:season_calendar", 1, 1),
			new AllowedItem("eclipticseasons:calendar", 1, 1),
			new AllowedItem("oreganized:speedometer", 1, 1),
			new AllowedItem("speedometer:speedometer", 1, 1),
			new AllowedItem("create:goggles", 1, 1),
			new AllowedItem("map_atlases:atlas", 1, 1),
			new AllowedItem("map_atlases:end_atlas", 1, 1),
			new AllowedItem("map_atlases:nether_atlas", 1, 1),
			new AllowedItem("naturescompass:naturescompass", 1, 1),
			new AllowedItem("antiqueatlas:antique_atlas", 1, 1)
	);
	public InfoOverlaySettings infoOverlaySettings = new InfoOverlaySettings();
	public ValidatedBoolean hideDebugInfoInSurvival = new ValidatedBoolean(false);

	public static class InfoOverlaySettings extends ConfigSection {
		public OverlayFields overlayFields = new OverlayFields();
		public ValidatedBoolean showYCoordinate = new ValidatedBoolean(true);
		public ValidatedBoolean obfuscateCompassIfNotOverworld = new ValidatedBoolean(false);
		public ValidatedBoolean obfuscateClockIfNotOverworld = new ValidatedBoolean(true);
		public ValidatedBoolean useObfuscationEffect = new ValidatedBoolean(true);
		public ValidatedBoolean hideCalendarTooltip = new ValidatedBoolean(false);
	}

	public static class OverlayFields extends ConfigSection {
		public ValidatedBoolean coordinates = new ValidatedBoolean(true);
		public ValidatedBoolean direction = new ValidatedBoolean(true);
		public ValidatedBoolean biome = new ValidatedBoolean(true);
		public ValidatedBoolean dayAndTime = new ValidatedBoolean(true);
		public ValidatedBoolean weather = new ValidatedBoolean(true);
		public ValidatedBoolean season = new ValidatedBoolean(true);
		public ValidatedBoolean moonPhase = new ValidatedBoolean(true);
		public ValidatedBoolean lastDeathLocation = new ValidatedBoolean(true);
	}
}
