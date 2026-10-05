package qa;

import com.mojang.authlib.GameProfile;
import com.thenathe.amethystcursecleanser.CurseCleansing;
import java.nio.file.*;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.minecraft.server.network.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public final class CleanserChecks {
 private static void check(boolean ok,String label) {
  if(!ok) throw new AssertionError(label);
  System.out.println("CLEANSER QA PASS: "+label);
 }
 public static void runChecks(MinecraftServer server) {
  var player=new ServerPlayer(server,server.overworld(),new GameProfile(UUID.randomUUID(),"CleanserQA"),ClientInformation.createDefault());
  player.connection=new ServerGamePacketListenerImpl(server,new Connection(PacketFlow.SERVERBOUND),player,CommonListenerCookie.createInitial(player.getGameProfile(),false));
  var ench=server.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
  ItemStack gear=new ItemStack(Items.DIAMOND_CHESTPLATE);
  gear.setDamageValue(17);
  gear.set(DataComponents.CUSTOM_NAME,Component.literal("Keep me"));
  gear.enchant(ench.getOrThrow(Enchantments.BINDING_CURSE),1);
  gear.enchant(ench.getOrThrow(Enchantments.VANISHING_CURSE),1);
  gear.enchant(ench.getOrThrow(Enchantments.UNBREAKING),3);
  check(!CurseCleansing.isEligibleCursedGear(new ItemStack(Items.DIAMOND_CHESTPLATE)),"plain gear rejected");
  for(int order=0;order<3;order++) {
   AbstractContainerMenu menu=order<2 ? new GrindstoneMenu(order+1,player.getInventory()) : new SmithingMenu(3,player.getInventory());
   int gs=order<2?order:SmithingMenu.BASE_SLOT;
   int ss=order<2?1-order:SmithingMenu.ADDITIONAL_SLOT;
   int rs=order<2?GrindstoneMenu.RESULT_SLOT:SmithingMenu.RESULT_SLOT;
   menu.getSlot(gs).set(gear.copy());
   menu.getSlot(ss).set(new ItemStack(Items.AMETHYST_SHARD,2));
   if(menu instanceof SmithingMenu smithing) smithing.createResult();
   ItemStack result=menu.getSlot(rs).getItem();
   check(!result.isEmpty(),"menu "+order+" creates result");
   check(result.getDamageValue()==17 && result.getHoverName().getString().equals("Keep me"),"data preserved "+order);
   check(EnchantmentHelper.getItemEnchantmentLevel(ench.getOrThrow(Enchantments.UNBREAKING),result)==3,"unbreaking preserved "+order);
   check(!CurseCleansing.isEligibleCursedGear(result),"both curses removed "+order);
   menu.getSlot(rs).onTake(player,result.copy());
   check(menu.getSlot(gs).getItem().isEmpty() && menu.getSlot(ss).getItem().getCount()==1,"inputs consumed once "+order);
  }
  check(player.getInventory().countItem(Items.ECHO_SHARD)==3,"one echo reward per craft");
  check(CurseCleansing.isEligibleCursedGear(gear),"source item not mutated");
 }
}
