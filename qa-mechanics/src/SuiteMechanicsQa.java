package qa;
import chalk.qa.ChalkRecipeChecks;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import java.nio.file.*;
import java.util.*;

/** Regression against actual recipes, menus, entities, and mapped suite registries. */
public final class SuiteMechanicsQa implements ModInitializer {
 public void onInitialize() { ServerLifecycleEvents.SERVER_STARTED.register(server -> {
  try {
   ChalkRecipeChecks.check(server);
   for (DyeColor color : DyeColor.values()) for(boolean glow:new boolean[]{false,true}) {
    var stacks = new ArrayList<ItemStack>(); stacks.add(new ItemStack(Items.CALCITE)); stacks.add(new ItemStack(Items.CALCITE));
    if(color!=DyeColor.WHITE) stacks.add(new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.withDefaultNamespace(color.getSerializedName()+"_dye"))));
    if(glow) stacks.add(new ItemStack(Items.GLOW_INK_SAC));
    while(stacks.size()<4)stacks.add(ItemStack.EMPTY);
    var input=CraftingInput.of(2,2,stacks);
    var recipe=server.getRecipeManager().getRecipeFor(RecipeType.CRAFTING,input,server.overworld()).orElseThrow();
    var variant=de.dafuqs.chalk.common.ChalkRegistry.chalkVariants.get(color);
    var result=recipe.value().assemble(input);
    if(!result.is(glow?variant.glowChalkItem:variant.chalkItem))throw new AssertionError("Calcite recipe "+color+" glow="+glow);
   }
   CleanserChecks.runChecks(server);
   var drops=new DropFixQa();drops.run(server);
   if(drops.failed!=0)throw new AssertionError("Drop fix failed "+drops.failed);
   Files.writeString(Path.of("suite-mechanics-result.txt"),"PASS all Chalk conversion transitions, 32 calcite recipes, real curse-removal menus, "+drops.passed+" Defaulted drop regressions\n");
   System.out.println("SUITE_MECHANICS_PASS");
  } catch(Throwable error) {
   error.printStackTrace();
   try {Files.writeString(Path.of("suite-mechanics-result.txt"),"FAIL "+error);}catch(Exception ignored){}
  } finally {server.halt(false);}
 }); }
}
