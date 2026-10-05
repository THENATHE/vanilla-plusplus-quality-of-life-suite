package me.pajic.misctweaks.mixson;

//? <26.1 {

/*import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.pajic.misctweaks.MiscTweaks;
import net.ramixin.mixson.util.Index;

public class DataPatches {

    private static final JsonElement LODESTONE_POOL = JsonParser.parseString("""
        {
          "bonus_rolls": 0.0,
          "entries": [
            {
              "type": "minecraft:empty"
            },
            {
              "type": "minecraft:item",
              "functions": [
                {
                  "add": false,
                  "count": {
                    "type": "minecraft:uniform",
                    "max": 2.0,
                    "min": 1.0
                  },
                  "function": "minecraft:set_count"
                }
              ],
              "name": "minecraft:lodestone",
              "weight": 2
            }
          ],
          "rolls": 1.0
        }
    """);

    public static void init() {
        if (MiscTweaks.CONFIG.lodestoneChangesBackport.get()) {
            MixsonHelper.registerSingleJson(
                    "Modify lodestone recipe",
                    new Index("minecraft:recipe/lodestone"),
                    context -> {
                        JsonElement value = context.getFile().getAsJsonObject()
                                .getAsJsonObject("key")
                                .get("#");
                        if (value != null) {
                            if (value.isJsonPrimitive()) context.getFile().getAsJsonObject()
                                    .getAsJsonObject("key")
                                    .addProperty("#", "minecraft:iron_ingot");
                            else context.getFile().getAsJsonObject()
                                    .getAsJsonObject("key")
                                    .getAsJsonObject("#")
                                    //? if fabric
                                    .addProperty("item", "minecraft:iron_ingot");
                            //? if neoforge
                            //.addProperty("tag", "c:ingots/iron");
                        } else {
                            MiscTweaks.LOGGER.error("MiscTweaks tried to modify the lodestone recipe, but it has been replaced by another mod or a datapack. Skipping patch.");
                        }
                    }
            );
            MixsonHelper.registerSingleJson(
                    "Add lodestones to ruined portal chests",
                    new Index("minecraft:loot_table/chests/ruined_portal"),
                    context -> {
                        JsonArray pools = context.getFile().getAsJsonObject().getAsJsonArray("pools");
                        if (!pools.contains(LODESTONE_POOL)) pools.add(LODESTONE_POOL.deepCopy());
                    }
            );
            MixsonHelper.registerSingleJson(
                    "Modify lodestone recipe unlock condition",
                    new Index("minecraft:advancement/recipes/decorations/lodestone"),
                    context -> {
                        JsonArray items = context.getFile().getAsJsonObject()
                                .getAsJsonObject("criteria")
                                .getAsJsonObject("has_netherite_ingot")
                                .getAsJsonObject("conditions")
                                .getAsJsonArray("items");
                        for (int i = 0; i < items.size(); ++i) {
                            JsonObject item = items.get(i).getAsJsonObject();
                            if (item.get("items").getAsString().equals("minecraft:netherite_ingot")) {
                                item.addProperty("items", "minecraft:iron_ingot");
                                break;
                            }
                        }
                    }
            );
            MixsonHelper.registerSingleJson(
                    "Move lodestone usage advancement",
                    new Index("minecraft:advancement/nether/use_lodestone"),
                    context -> context.getFile().getAsJsonObject()
                            .addProperty("parent", "minecraft:adventure/root")
            );
        }
    }
}
*///?}
