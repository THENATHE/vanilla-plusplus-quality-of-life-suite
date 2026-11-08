package com.thenathe.toolpouchcompat;
import com.thenathe.toolpouchcompat.mixin.PouchMendingAccess;
import me.pajic.mapstitch.item.ModItems;
import me.pajic.toolpouch.menu.ToolPouchMenu;
import me.pajic.toolpouch.util.ToolPouchUtil;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import java.util.List;
/** Mirrors the upstream MapStitch pouch patch using the active pouch selection. */
public final class AtlasBridge {
 public static boolean hasItem(Player player, Item item) { return ToolPouchUtil.toolPouchHasItem(player, stack -> stack.is(item)); }
 public static List<ItemStack> atlases(Player player) {
  // The open menu owns a mutable copy. Never mutate its stale stored snapshot.
  if (player.containerMenu instanceof ToolPouchMenu) return List.of();
  if (player.level().isClientSide()) {
   var source = PouchMendingAccess.toolpouchCompat$activePouch(player);
   return ((AtlasCacheOwner) player).toolpouchCompat$atlasCache().copies(player.level(), source);
  }
  return ToolPouchUtil.getItemsFromToolPouch(player, stack -> stack.is(ModItems.ATLAS)).stream().map(ItemStackTemplate::create).toList();
 }
 public static void save(Player player, ItemStack atlas, int index) {
  if (player.containerMenu instanceof ToolPouchMenu) return;
  // Indices follow atlases(): enabled, non-empty atlas slots in pouch order.
  var pouch = ToolPouchUtil.getToolPouch(player);
  var slots = ToolPouchUtil.findSlotsInToolPouch(pouch, stack -> stack.is(ModItems.ATLAS));
  if (index >= 0 && index < slots.size()) ToolPouchUtil.setItemInToolPouch(pouch, slots.getInt(index), atlas);
 }
 public static void tick(Player player) {
  if (player.level().isClientSide()) return;
  var atlases = atlases(player);
  for (int i=0; i<atlases.size(); i++) {
   ItemStack atlas=atlases.get(i), before=atlas.copy();
   atlas.inventoryTick(player.level(), player, null);
   if (!ItemStack.isSameItemSameComponents(before, atlas)) save(player, atlas, i);
  }
 }
}
