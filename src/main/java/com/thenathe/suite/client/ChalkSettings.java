package com.thenathe.suite.client;

import de.dafuqs.chalk.config.ChalkConfig;
import de.dafuqs.chalk.common.Chalk;
import me.fzzyhmstrs.fzzy_config.config.Config;
import me.fzzyhmstrs.fzzy_config.validation.misc.ValidatedBoolean;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.resources.Identifier;

/** Client UI bridge; Chalk's original AutoConfig file remains the source of truth. */
public final class ChalkSettings extends Config {
    public ValidatedBoolean emitParticles = new ValidatedBoolean(true);
    public ChalkSettings() { super(Identifier.fromNamespaceAndPath(SuiteSettings.SCOPE, "chalk")); }
    public void refreshFromChalk() {
        var canonical = AutoConfig.getConfigHolder(ChalkConfig.class).getConfig();
        Chalk.CONFIG = canonical;
        emitParticles.validateAndSet(canonical.EmitParticles);
    }
    @Override public void onUpdateClient() {
        var holder = AutoConfig.getConfigHolder(ChalkConfig.class);
        holder.getConfig().EmitParticles = emitParticles.get();
        Chalk.CONFIG = holder.getConfig();
        holder.save();
    }
}
