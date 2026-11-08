package me.pajic.simple_death_improvements.compat;

//? fabric && 1.21.1 {

/*import dev.emi.trinkets.api.TrinketEnums;
import dev.emi.trinkets.api.event.TrinketDropCallback;
import me.pajic.simple_death_improvements.SDI;
import me.pajic.simple_death_improvements.util.ModUtil;

public class TrinketsLegacyCompat implements AccessoryUtil {

    @Override
    public void registerDropEvent() {
        TrinketDropCallback.EVENT.register((rule, stack, ref, entity) -> {
            if (
                    SDI.CONFIG.keepAccessoriesOnDeath.get() &&
                    !SDI.CONFIG.accessoryDropList.contains(SDI.getItemId(stack))
            ) return TrinketEnums.DropRule.KEEP;
            if (
                    rule == TrinketEnums.DropRule.DEFAULT &&
                    ref.inventory().getSlotType().getDropRule() == TrinketEnums.DropRule.DEFAULT &&
                    ModUtil.tryKeepDamageableItem(entity, stack)
            ) return TrinketEnums.DropRule.KEEP;
            return rule;
        });
    }
}
*///?}
