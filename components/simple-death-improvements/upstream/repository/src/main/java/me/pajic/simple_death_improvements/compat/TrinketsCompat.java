package me.pajic.simple_death_improvements.compat;

//? >=26.1 {

import eu.pb4.trinkets.api.TrinketDropRule;
import eu.pb4.trinkets.api.event.TrinketDropCallback;
import me.pajic.simple_death_improvements.SDI;
import me.pajic.simple_death_improvements.util.ModUtil;

public class TrinketsCompat implements AccessoryUtil {

    @Override
    public void registerDropEvent() {
        TrinketDropCallback.EVENT.register((rule, stack, ref, entity) -> {
            if (
                    SDI.CONFIG.keepAccessoriesOnDeath.get() &&
                    !SDI.CONFIG.accessoryDropList.contains(SDI.getItemId(stack))
            ) return TrinketDropRule.KEEP;
            if (
                    rule == TrinketDropRule.DEFAULT &&
                    ref.slotType().dropRule() == TrinketDropRule.DEFAULT &&
                    ModUtil.tryKeepDamageableItem(entity, stack)
            ) return TrinketDropRule.KEEP;
            return rule;
        });
    }
}
//?}
