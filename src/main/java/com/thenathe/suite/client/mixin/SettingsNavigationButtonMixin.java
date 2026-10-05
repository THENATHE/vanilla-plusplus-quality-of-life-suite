package com.thenathe.suite.client.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.thenathe.suite.client.SuiteSettings;
import me.fzzyhmstrs.fzzy_config.entry.EntryCreator;
import me.fzzyhmstrs.fzzy_config.screen.entry.EntryCreators;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/** Shorten only the suite's root-row action; category names and narration remain native. */
@Mixin(value = EntryCreators.class, remap = false)
public abstract class SettingsNavigationButtonMixin {
    @ModifyArg(method = "createConfigEntry$lambda$0$1", at = @At(value = "INVOKE",
            target = "Lme/fzzyhmstrs/fzzy_config/screen/widget/custom/CustomButtonWidget$Companion;builder(Lnet/minecraft/network/chat/Component;Ljava/util/function/Consumer;)Lme/fzzyhmstrs/fzzy_config/screen/widget/custom/CustomButtonWidget$Builder;"), index = 0)
    private static Component suite$configureLabel(Component original, @Local(argsOnly = true) EntryCreator.CreatorContext context) {
        return SuiteSettings.isGroupedConfigScope(context.getScope())
                ? Component.translatable("thenathe_mod_suite.configure") : original;
    }
}
