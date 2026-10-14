package qa;
import java.util.*;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.minecraft.server.network.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Blocks;

/** Real upstream recipes and menus; synthetic server players, no packet/UI claim. */
public final class StorageChecks {
 static int n; static void ok(boolean b,String s){n++;if(!b)throw new AssertionError(s);}
 static ItemStack contents(Item i){var s=new ItemStack(i);s.set(DataComponents.CONTAINER,ItemContainerContents.fromItems(List.of(new ItemStack(Items.DIAMOND,7),new ItemStack(Items.COMPASS))));s.set(DataComponents.DYED_COLOR,new DyedItemColor(0x123456));return s;}
 static boolean sameContents(ItemStack a,ItemStack b){return Objects.equals(a.get(DataComponents.CONTAINER),b.get(DataComponents.CONTAINER));}
 public static int run(MinecraftServer server){
  n=0;var profile=new GameProfile(UUID.randomUUID(),"StorageQA");var p=new ServerPlayer(server,server.overworld(),profile,ClientInformation.createDefault());p.connection=new ServerGamePacketListenerImpl(server,new Connection(PacketFlow.SERVERBOUND),p,CommonListenerCookie.createInitial(profile,false)){public void send(Packet<?> packet){}};
  var mending=server.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.MENDING);
  Item[] packs={me.pajic.tiered_backpacks.item.ModItems.LEATHER_BACKPACK,me.pajic.tiered_backpacks.item.ModItems.COPPER_BACKPACK,me.pajic.tiered_backpacks.item.ModItems.IRON_BACKPACK,me.pajic.tiered_backpacks.item.ModItems.GOLDEN_BACKPACK,me.pajic.tiered_backpacks.item.ModItems.DIAMOND_BACKPACK,me.pajic.tiered_backpacks.item.ModItems.NETHERITE_BACKPACK};
  for(int i=0;i<packs.length;i++){
   var pack=contents(packs[i]);var chest=new ItemStack(i==5?Items.NETHERITE_CHESTPLATE:Items.IRON_CHESTPLATE);chest.setDamageValue(31);chest.enchant(mending,1);
   var attach=new me.pajic.tiered_backpacks.recipe.AttachBackpackRecipe();var grid=CraftingInput.of(2,1,List.of(pack,chest));ok(attach.matches(grid,server.overworld()),"attach backpack tier "+i);var attached=attach.assemble(grid);
   ok(sameContents(pack,attached),"attach contents "+i);ok(attached.getDamageValue()==31&&attached.getEnchantments().getLevel(mending)==1,"armor properties "+i);
   var detach=new me.pajic.tiered_backpacks.recipe.DetachBackpackRecipe();grid=CraftingInput.of(1,1,List.of(attached));ok(detach.matches(grid,server.overworld()),"detach backpack "+i);var detached=detach.assemble(grid);var armor=detach.getRemainingItems(grid).getFirst();
   ok(detached.is(packs[i])&&sameContents(pack,detached),"detach item contents "+i);ok(new DyedItemColor(0x123456).equals(detached.get(DataComponents.DYED_COLOR)),"detach dye "+i);ok(!armor.has(DataComponents.CONTAINER)&&armor.getDamageValue()==31&&armor.getEnchantments().getLevel(mending)==1,"armor remainder "+i);
   for(var holder:List.of(pack,attached)){
    p.getInventory().clearContent();var menu=new me.pajic.tiered_backpacks.ui.BackpackMenu(1,p.getInventory(),holder);int size=menu.slots.size()-36;
    ok(size>0&&menu.getSlot(0).getItem().getCount()==7,"menu storage "+i);ok(!menu.getSlot(0).mayPlace(new ItemStack(packs[0])),"nested backpack denied "+i);
    p.getInventory().setItem(9,new ItemStack(Items.EMERALD,11));ok(!menu.quickMoveStack(p,size).isEmpty(),"into pack transfer "+i);ok(p.getInventory().getItem(9).isEmpty(),"into source consumed "+i);ok(!menu.quickMoveStack(p,0).isEmpty(),"out transfer "+i);
    menu.removed(p);ok(holder.get(DataComponents.CONTAINER).itemCopies().filter(s->s.is(Items.EMERALD)).mapToInt(ItemStack::getCount).sum()==11,"menu saveback "+i);ok(p.getInventory().getNonEquipmentItems().stream().filter(s->s.is(Items.DIAMOND)).mapToInt(ItemStack::getCount).sum()==7,"move conservation "+i);
   }
  }
  var invalid=new me.pajic.tiered_backpacks.recipe.AttachBackpackRecipe();ok(!invalid.matches(CraftingInput.of(2,1,List.of(new ItemStack(packs[5]),new ItemStack(Items.IRON_CHESTPLATE))),server.overworld()),"netherite backpack rejects nonfireproof armor");
  for(var item:List.of(me.pajic.toolpouch.item.ModItems.TOOL_POUCH,me.pajic.toolpouch.item.ModItems.NETHERITE_TOOL_POUCH)){
   var pouch=contents(item);var legs=new ItemStack(item==me.pajic.toolpouch.item.ModItems.NETHERITE_TOOL_POUCH?Items.NETHERITE_LEGGINGS:Items.IRON_LEGGINGS);legs.setDamageValue(23);legs.enchant(mending,1);
   var attach=new me.pajic.toolpouch.recipe.AttachToolPouchRecipe();var grid=CraftingInput.of(2,1,List.of(pouch,legs));ok(attach.matches(grid,server.overworld()),"attach pouch");var attached=attach.assemble(grid);ok(sameContents(pouch,attached),"pouch attached contents");
   var detach=new me.pajic.toolpouch.recipe.DetachToolPouchRecipe();grid=CraftingInput.of(1,1,List.of(attached));ok(detach.matches(grid,server.overworld()),"detach pouch");var detached=detach.assemble(grid);var remainder=detach.getRemainingItems(grid).getFirst();ok(detached.is(item)&&sameContents(pouch,detached),"pouch detached contents/tier");ok(new DyedItemColor(0x123456).equals(detached.get(DataComponents.DYED_COLOR)),"pouch detached dye");ok(!remainder.has(DataComponents.CONTAINER)&&remainder.getDamageValue()==23&&remainder.getEnchantments().getLevel(mending)==1,"pouch armor remainder");
   for(var holder:List.of(pouch,attached)){
    p.getInventory().clearContent();holder.set(DataComponents.CONTAINER,ItemContainerContents.fromItems(List.of(new ItemStack(Items.COMPASS))));var menu=new me.pajic.toolpouch.menu.ToolPouchMenu(2,p.getInventory(),holder);int size=menu.slots.size()-36;
    ok(size>0,"pouch capacity");ok(menu.getSlot(1).mayPlace(new ItemStack(Items.CLOCK)),"clock allowed");ok(!menu.getSlot(1).mayPlace(new ItemStack(Items.DIRT)),"dirt denied");ok(!menu.getSlot(1).mayPlace(new ItemStack(item)),"nested pouch denied");
    p.getInventory().setItem(9,new ItemStack(Items.CLOCK));ok(!menu.quickMoveStack(p,size).isEmpty(),"clock transfer");ok(p.getInventory().getItem(9).isEmpty(),"clock source consumed");ok(!menu.quickMoveStack(p,0).isEmpty(),"compass transfer out");menu.removed(p);ok(holder.get(DataComponents.CONTAINER).itemCopies().filter(s->s.is(Items.CLOCK)).mapToInt(ItemStack::getCount).sum()==1,"pouch saveback");ok(p.getInventory().getNonEquipmentItems().stream().filter(s->s.is(Items.COMPASS)).mapToInt(ItemStack::getCount).sum()==1,"compass conservation");
   }
  }
  ok(!new me.pajic.toolpouch.recipe.AttachToolPouchRecipe().matches(CraftingInput.of(2,1,List.of(new ItemStack(me.pajic.toolpouch.item.ModItems.NETHERITE_TOOL_POUCH),new ItemStack(Items.IRON_LEGGINGS))),server.overworld()),"netherite pouch rejects nonfireproof leggings");
  p.getInventory().clearContent();var pos=new BlockPos(12,200,12);server.overworld().setBlock(pos,Blocks.SMITHING_TABLE.defaultBlockState(),2);var menu=new SmithingMenu(3,p.getInventory(),ContainerLevelAccess.create(server.overworld(),pos));var diamond=contents(packs[4]);var diamondBefore=diamond.copy();menu.getSlot(0).set(new ItemStack(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE));menu.getSlot(1).set(diamond);menu.getSlot(2).set(new ItemStack(Items.NETHERITE_INGOT));menu.createResult();ok(menu.getSlot(3).getItem().is(packs[5]),"netherite backpack smithing preview");ok(sameContents(diamond,menu.getSlot(3).getItem()),"netherite upgrade content preservation");menu.clicked(3,0,ContainerInput.PICKUP,p);ok(menu.getCarried().is(packs[5])&&sameContents(diamondBefore,menu.getCarried()),"netherite upgrade click result");ok(menu.getSlot(0).getItem().isEmpty()&&menu.getSlot(1).getItem().isEmpty()&&menu.getSlot(2).getItem().isEmpty(),"netherite upgrade ingredients consumed");return n;
 }
}
