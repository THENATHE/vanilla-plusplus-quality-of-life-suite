package qa;

import com.thenathe.multiscale.AtlasOptions;
import com.thenathe.multiscale.AtlasCartographyCopy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import me.pajic.mapstitch.component.ModDataComponents;
import me.pajic.mapstitch.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.CartographyTableMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

/** Atlas copy commands and actual vanilla menu click paths, with over-capacity stored contents. */
public final class AtlasCartographyQa {
    private int checks;
    private void check(boolean ok,String message) { checks++;if(!ok)throw new AssertionError(message); }
    public static int run(MinecraftServer server,ServerPlayer player,ArrayList<net.minecraft.world.entity.item.ItemEntity> drops) throws Exception {
        var qa=new AtlasCartographyQa();qa.verify(server,player,drops);return qa.checks;
    }
    private void verify(MinecraftServer server,ServerPlayer player,ArrayList<net.minecraft.world.entity.item.ItemEntity> drops) throws Exception {
        var level=server.overworld();player.setPos(-80,100,-80);level.getChunkAt(player.blockPosition());
        int originalLimit=me.pajic.mapstitch.MapStitch.CONFIG.maxAtlasItems.get();
        var originalGameMode=player.gameMode.getGameModeForPlayer();
        me.pajic.mapstitch.MapStitch.CONFIG.maxAtlasItems.accept(4);
        try {
            var source=new ItemStack(ModItems.ATLAS);AtlasOptions.ensureIdentity(source);
            AtlasOptions.setGenerationMask(source,21);source.set(ModDataComponents.ATLAS_SCALE,3);
            source.set(DataComponents.CUSTOM_NAME,net.minecraft.network.chat.Component.literal("Complete map collection"));
            net.minecraft.world.item.component.CustomData.update(DataComponents.CUSTOM_DATA,source,tag->tag.putString("qa-copy-other","preserve"));
            var originals=new HashMap<MapId,MapItemSavedData>();var pixels=new HashMap<MapId,byte[]>();var base=new ArrayList<ItemStack>();
            for(var dimension:List.of(Level.OVERWORLD,Level.NETHER,Level.END))for(byte scale=0;scale<5;scale++) {
                var map=MapItem.create(server.getLevel(dimension),-80,-80,scale,true,false);var id=map.get(DataComponents.MAP_ID);
                var data=MapItem.getSavedData(map,level).locked();java.util.Arrays.fill(data.colors,(byte)(64+scale));
                level.setMapData(id,data);originals.put(id,data);pixels.put(id,data.colors.clone());base.add(map);
            }
            var entries=new ArrayList<ItemStackTemplate>();entries.add(ItemStackTemplate.fromNonEmptyStack(new ItemStack(Items.MAP,7)));
            entries.add(ItemStackTemplate.fromNonEmptyStack(new ItemStack(Items.PAPER,11)));
            for(int i=0;i<900;i++) {
                var map=base.get(i%base.size()).copy();map.set(DataComponents.CUSTOM_NAME,net.minecraft.network.chat.Component.literal("Stored map "+i));
                entries.add(ItemStackTemplate.fromNonEmptyStack(map));
            }
            var treasure=new ItemStack(Items.BURIED_TREASURE_MAP);treasure.set(DataComponents.MAP_ID,base.getFirst().get(DataComponents.MAP_ID));
            entries.add(ItemStackTemplate.fromNonEmptyStack(treasure));
            var missing=new ItemStack(Items.FILLED_MAP);missing.set(DataComponents.MAP_ID,new MapId(Integer.MAX_VALUE));missing.setCount(4);
            entries.add(ItemStackTemplate.fromNonEmptyStack(missing));entries.add(ItemStackTemplate.fromNonEmptyStack(new ItemStack(Items.FILLED_MAP)));
            var mutable=new BundleContents(entries).asMutable();mutable.toggleSelectedItem(203);source.set(DataComponents.BUNDLE_CONTENTS,mutable.toImmutable());
            source.set(ModDataComponents.ATLAS_ACTIVE_MAP_ID,base.get(3).get(DataComponents.MAP_ID).id());
            source.set(ModDataComponents.ATLAS_FULLNESS,4);
            var before=source.copy();
            assertCopy(before,AtlasCartographyCopy.copy(before));
            var partial=source.copy();var partialMap=base.getFirst().copyWithCount(4);
            var partialContents=new BundleContents(List.of(ItemStackTemplate.fromNonEmptyStack(new ItemStack(Items.MAP,7)),
                    ItemStackTemplate.fromNonEmptyStack(partialMap))).asMutable();partialContents.toggleSelectedItem(0);
            partial.set(DataComponents.BUNDLE_CONTENTS,partialContents.toImmutable());var partialBefore=partial.copy();
            me.pajic.mapstitch.MapStitch.CONFIG.maxAtlasItems.accept(256);
            var partialCopy=AtlasCartographyCopy.copy(partial);
            me.pajic.mapstitch.MapStitch.CONFIG.maxAtlasItems.accept(4);
            check(partialCopy.get(ModDataComponents.ATLAS_FULLNESS)==1,"copy recalculates partially filled UI state rather than preserving source fullness4");
            check(partialCopy.get(DataComponents.BUNDLE_CONTENTS).getSelectedItemIndex()==BundleContents.NO_SELECTED_ITEM_INDEX
                    &&partialCopy.get(DataComponents.BUNDLE_CONTENTS).items().equals(List.of(ItemStackTemplate.fromNonEmptyStack(partialMap))),"copy clears removed blank selection without losing retained stack count");
            check(ItemStack.isSameItemSameComponents(partial,partialBefore),"partial copy retains source components exactly");
            for(int mode=0;mode<5;mode++) {
                player.getInventory().clearContent();var input=source.copy();var untouched=source.copy();
                player.gameMode.changeGameModeForPlayer(mode==4?net.minecraft.world.level.GameType.CREATIVE:originalGameMode);
                if(mode==2) {
                    var pouch=new ItemStack(me.pajic.toolpouch.item.ModItems.TOOL_POUCH);
                    pouch.set(DataComponents.CONTAINER,ItemContainerContents.fromItems(List.of(input)));player.getInventory().setItem(0,pouch);
                } else player.getInventory().setItem(mode==1?40:0,input);
                if(mode>=3)for(int slot=2;slot<36;slot++)player.getInventory().setItem(slot,new ItemStack(Items.STONE,64));
                player.getInventory().setItem(1,new ItemStack(Items.BOOK,3));player.getInventory().setItem(3,untouched);
                int priorDrops=drops.size();
                check(server.getCommands().getDispatcher().execute("atlas makecopy",player.createCommandSourceStack())==1,"makecopy succeeds for target "+mode);
                check(player.getInventory().getItem(1).is(Items.BOOK)&&player.getInventory().getItem(1).getCount()==2,"makecopy consumes exactly one ordinary book "+mode);
                var selected=mode==2?com.thenathe.toolpouchcompat.AtlasBridge.atlases(player).getFirst():player.getInventory().getItem(mode==1?40:0);
                check(ItemStack.isSameItemSameComponents(selected,before)&&ItemStack.isSameItemSameComponents(untouched,before),"makecopy retains source and other atlas exactly "+mode);
                var outputs=new ArrayList<ItemStack>();
                for(int slot=0;slot<player.getInventory().getContainerSize();slot++) {
                    var stack=player.getInventory().getItem(slot);
                    if(stack.is(ModItems.ATLAS)&&!AtlasOptions.identity(stack).equals(AtlasOptions.identity(before)))outputs.add(stack);
                }
                for(var drop:drops.subList(priorDrops,drops.size()))outputs.add(drop.getItem());
                check(outputs.size()==1,"makecopy creates exactly one output even creative overflow "+mode);
                check(mode>=3?drops.size()==priorDrops+1:drops.size()==priorDrops,"copy goes inventory first and drops only overflow "+mode);
                assertCopy(before,outputs.getFirst());
            }
            player.gameMode.changeGameModeForPlayer(originalGameMode);
            player.getInventory().clearContent();player.getInventory().setItem(0,source.copy());player.getInventory().setItem(1,new ItemStack(Items.ENCHANTED_BOOK));
            int priorDrops=drops.size();
            check(server.getCommands().getDispatcher().execute("atlas makecopy",player.createCommandSourceStack())==0,"enchanted book cannot substitute ordinary book");
            check(ItemStack.isSameItemSameComponents(player.getInventory().getItem(0),before)&&player.getInventory().getItem(1).is(Items.ENCHANTED_BOOK)&&drops.size()==priorDrops,"missing ordinary book leaves everything unchanged");
            player.getInventory().clearContent();player.getInventory().setItem(1,new ItemStack(Items.BOOK));
            check(server.getCommands().getDispatcher().execute("atlas makecopy",player.createCommandSourceStack())==0&&player.getInventory().getItem(1).getCount()==1,"no source atlas consumes no book");
            var empty=source.copy();var emptyContents=new BundleContents(List.of(ItemStackTemplate.fromNonEmptyStack(new ItemStack(Items.MAP,7)),
                    ItemStackTemplate.fromNonEmptyStack(new ItemStack(Items.PAPER,11)))).asMutable();emptyContents.toggleSelectedItem(1);
            empty.set(DataComponents.BUNDLE_CONTENTS,emptyContents.toImmutable());var emptyBefore=empty.copy();
            player.getInventory().clearContent();player.getInventory().setItem(0,empty);player.getInventory().setItem(1,new ItemStack(Items.BOOK));
            check(server.getCommands().getDispatcher().execute("atlas makecopy",player.createCommandSourceStack())==1,"zero-filled atlas can be copied for one book");
            var emptyOutput=player.getInventory().getNonEquipmentItems().stream().filter(stack->stack.is(ModItems.ATLAS)
                    &&!AtlasOptions.identity(stack).equals(AtlasOptions.identity(emptyBefore))).findFirst().orElseThrow();
            check(emptyOutput.get(DataComponents.BUNDLE_CONTENTS).isEmpty()&&emptyOutput.get(DataComponents.BUNDLE_CONTENTS).getSelectedItemIndex()==BundleContents.NO_SELECTED_ITEM_INDEX
                    &&emptyOutput.get(ModDataComponents.ATLAS_FULLNESS)==0,"zero-filled copy excludes all blanks/paper, clears selection and recomputes empty fullness");
            check(ItemStack.isSameItemSameComponents(empty,emptyBefore)&&player.getInventory().getNonEquipmentItems().stream().noneMatch(stack->stack.is(Items.BOOK)),"zero-filled copy retains source and consumes one book");

            var pos=new BlockPos(-80,60,-80);level.setBlock(pos,net.minecraft.world.level.block.Blocks.CARTOGRAPHY_TABLE.defaultBlockState(),3);
            player.getInventory().clearContent();player.getInventory().setItem(0,source.copy());player.getInventory().setItem(1,new ItemStack(Items.BOOK));
            var shifted=new CartographyTableMenu(29,player.getInventory(),ContainerLevelAccess.create(level,pos));player.containerMenu=shifted;
            shifted.clicked(30,0,ContainerInput.QUICK_MOVE,player);shifted.clicked(31,0,ContainerInput.QUICK_MOVE,player);
            check(shifted.container.getItem(0).is(ModItems.ATLAS)&&shifted.container.getItem(1).is(Items.BOOK)&&shifted.getSlot(2).hasItem(),"shift inputs route atlas/book into correct cartography slots");
            check(player.getInventory().getItem(0).isEmpty()&&player.getInventory().getItem(1).isEmpty(),"shift inputs transfer source once without duplication");
            shifted.removed(player);player.containerMenu=player.inventoryMenu;
            for(int mode=0;mode<5;mode++) {
                player.getInventory().clearContent();player.containerMenu=player.inventoryMenu;
                var menu=new CartographyTableMenu(30+mode,player.getInventory(),ContainerLevelAccess.create(level,pos));player.containerMenu=menu;
                check(menu.getSlot(0).mayPlace(source)&&menu.getSlot(1).mayPlace(new ItemStack(Items.BOOK)),"cartography slots accept atlas and book "+mode);
                menu.container.setItem(0,source.copy());menu.container.setItem(1,new ItemStack(Items.BOOK,mode==1?3:1));
                check(menu.getSlot(2).hasItem(),"table previews complete copy "+mode);assertCopy(before,menu.getSlot(2).getItem());
                check(ItemStack.isSameItemSameComponents(menu.container.getItem(0),before),"preview never mutates source "+mode);
                if(mode==0) {
                    // The upstream atlas clears its bundle selection on a normal primary pickup.
                    menu.clicked(2,0,ContainerInput.PICKUP,player);assertCopy(before,menu.getCarried(),BundleContents.NO_SELECTED_ITEM_INDEX);
                    check(menu.container.getItem(1).isEmpty()&&!menu.getSlot(2).hasItem(),"pickup consumes last book and clears preview");
                    var carried=menu.getCarried().copy();menu.clicked(2,0,ContainerInput.PICKUP,player);
                    check(ItemStack.isSameItemSameComponents(carried,menu.getCarried()),"second pickup without book creates no copy");
                    menu.setCarried(ItemStack.EMPTY);
                } else if(mode==1) {
                    for(int n=0;n<3;n++)menu.clicked(2,0,ContainerInput.QUICK_MOVE,player);
                    var identities=new HashSet<String>();int count=0;
                    for(int slot=0;slot<36;slot++)if(player.getInventory().getItem(slot).is(ModItems.ATLAS)) {
                        var result=player.getInventory().getItem(slot);assertCopy(before,result);count+=result.getCount();
                        check(identities.add(AtlasOptions.identity(result)),"shift copies have distinct book identities");
                    }
                    check(count==3&&menu.container.getItem(1).isEmpty()&&!menu.getSlot(2).hasItem(),"shift click uses exactly all three books, no extra copy");
                } else if(mode==2) {
                    menu.clicked(2,4,ContainerInput.SWAP,player);assertCopy(before,player.getInventory().getItem(4));
                    check(menu.container.getItem(1).isEmpty()&&!menu.getSlot(2).hasItem(),"number-key output consumes one book");
                } else if(mode==3) {
                    int start=drops.size();menu.clicked(2,1,ContainerInput.THROW,player);
                    check(drops.size()==start+1,"dropping result creates one actual atlas entity");assertCopy(before,drops.getLast().getItem());
                    check(menu.container.getItem(1).isEmpty()&&!menu.getSlot(2).hasItem(),"result drop consumes one book");
                }
                check(ItemStack.isSameItemSameComponents(menu.container.getItem(0),before),"all result-taking paths retain original in input slot "+mode);
                menu.removed(player);player.containerMenu=player.inventoryMenu;
                if(mode==4) {
                    check(player.getInventory().getNonEquipmentItems().stream().filter(stack->stack.is(Items.BOOK)).mapToInt(ItemStack::getCount).sum()==1,"cancel returns unconsumed book");
                    var atlases=player.getInventory().getNonEquipmentItems().stream().filter(stack->stack.is(ModItems.ATLAS)).toList();
                    check(atlases.size()==1&&ItemStack.isSameItemSameComponents(atlases.getFirst(),before),"cancel returns only source, never preview output");
                }
            }
            // Ordinary filled-map duplication remains vanilla: consume both inputs, output two maps.
            player.getInventory().clearContent();var ordinary=new CartographyTableMenu(50,player.getInventory(),ContainerLevelAccess.create(level,pos));player.containerMenu=ordinary;
            ordinary.container.setItem(0,base.getFirst().copy());ordinary.container.setItem(1,new ItemStack(Items.MAP));
            check(ordinary.getSlot(2).getItem().getCount()==2,"ordinary vanilla map duplicate preview remains two maps");
            ordinary.clicked(2,0,ContainerInput.PICKUP,player);
            check(ordinary.getCarried().getCount()==2&&ordinary.container.getItem(0).isEmpty()&&ordinary.container.getItem(1).isEmpty(),"ordinary map take still consumes both inputs");
            ordinary.setCarried(ItemStack.EMPTY);ordinary.removed(player);player.containerMenu=player.inventoryMenu;
            for(var entry:originals.entrySet())check(MapItem.getSavedData(entry.getKey(),level)==entry.getValue()
                    &&java.util.Arrays.equals(entry.getValue().colors,pixels.get(entry.getKey())),"copy never allocates or changes saved map record "+entry.getKey());
        } finally {
            me.pajic.mapstitch.MapStitch.CONFIG.maxAtlasItems.accept(originalLimit);
            player.gameMode.changeGameModeForPlayer(originalGameMode);
            player.containerMenu=player.inventoryMenu;player.getInventory().clearContent();
        }
    }
    private void assertCopy(ItemStack source,ItemStack result) {
        assertCopy(source,result,201);
    }
    private void assertCopy(ItemStack source,ItemStack result,int selected) {
        check(result.is(ModItems.ATLAS)&&result.getCount()==1,"copy output is one atlas");
        check(!AtlasOptions.identity(result).isEmpty()&&!AtlasOptions.identity(source).equals(AtlasOptions.identity(result)),"copy gets new book identity");
        var expected=source.get(DataComponents.BUNDLE_CONTENTS).items().stream().filter(entry->entry.get(DataComponents.MAP_ID)!=null).toList();
        check(result.get(DataComponents.BUNDLE_CONTENTS).items().equals(expected),"copy preserves every905filled map counts/types/components/order without capacity loss");
        check(result.get(DataComponents.BUNDLE_CONTENTS).items().stream().mapToInt(ItemStackTemplate::count).sum()==905,"copy preserves all stored filled map quantity");
        check(result.get(DataComponents.BUNDLE_CONTENTS).getSelectedItemIndex()==selected,"copy preserves rebased selection or upstream primary-pickup reset; source="+source.get(DataComponents.BUNDLE_CONTENTS).getSelectedItemIndex()+" expected="+selected+" output="+result.get(DataComponents.BUNDLE_CONTENTS).getSelectedItemIndex());
        check(result.get(ModDataComponents.ATLAS_FULLNESS)==4,"copy recomputes fullness with current limit");
        var normalized=result.copy();normalized.set(DataComponents.BUNDLE_CONTENTS,source.get(DataComponents.BUNDLE_CONTENTS));
        normalized.set(ModDataComponents.ATLAS_FULLNESS,source.get(ModDataComponents.ATLAS_FULLNESS));
        net.minecraft.world.item.component.CustomData.update(DataComponents.CUSTOM_DATA,normalized,
                tag->tag.putString(AtlasOptions.IDENTITY,AtlasOptions.identity(source)));
        check(ItemStack.isSameItemSameComponents(source,normalized),"copy preserves all other names/options/custom data/components");
    }
}
