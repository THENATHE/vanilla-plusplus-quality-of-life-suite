package com.thenathe.toolpouchcompat;

import dev.terminalmc.clientsort.client.config.Operation;
import dev.terminalmc.clientsort.client.interaction.InteractionManager;
import dev.terminalmc.clientsort.client.inventory.operator.client.ClientSurvivalOperator;
import dev.terminalmc.clientsort.client.order.SortOrder;
import dev.terminalmc.clientsort.client.util.SoundManager;
import dev.terminalmc.clientsort.mixin.client.accessor.AbstractContainerScreenAccessor;
import dev.terminalmc.clientsort.util.inject.ISlot;
import me.pajic.toolpouch.menu.ToolPouchMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import java.util.concurrent.atomic.AtomicBoolean;

import static dev.terminalmc.clientsort.client.config.Config.options;

/** Uses ClientSort's selected slots and queue, with live placement checks for restricted storage. */
public final class ClientSortOperator extends ClientSurvivalOperator {
    public ClientSortOperator(AbstractContainerScreen<?> screen, Slot origin, Operation operation) {
        super(screen, origin, operation);
    }

    public static boolean supports(AbstractContainerScreen<?> screen) {
        return screen.getMenu() instanceof ToolPouchMenu
                || screen.getMenu().getClass().getName().equals("me.pajic.tiered_backpacks.ui.BackpackMenu");
    }

    @Override
    protected void sort(SortOrder order) {
        if (screen.getMenu().getCarried().isEmpty()) super.sort(order);
    }

    @Override
    protected void fillStacks() {
        move(originScopeSlots, true, true);
    }

    @Override
    protected void transfer(Slot[] sources) {
        move(sources, false, options().transferReverseOrder);
    }

    private void move(Slot[] sources, boolean fillOnly, boolean reverse) {
        if (sources.length == 0 || otherScopeSlots.length == 0 || !screen.getMenu().getCarried().isEmpty()) return;
        boolean sound = SoundManager.shouldPlayOtherSounds();
        if (sound) SoundManager.resetForCount(fillOnly
                ? SoundManager.estimateStackFillSounds(originScopeStacks, otherScopeStacks)
                : SoundManager.estimateTransferSounds(originScopeStacks, otherScopeStacks));
        InteractionManager.push(new Move(sources, fillOnly, reverse, sound));
    }

    /** One event owns the operation until finished, keeping ClientSort's end-of-operation barrier last. */
    private final class Move implements InteractionManager.InteractionEvent {
        private final Slot[] sources;
        private final boolean fillOnly, reverse, sound;
        private int sourceIndex, destinationIndex, phase;
        private Slot source;
        private ItemStack picked = ItemStack.EMPTY;
        private boolean returning, finished;
        private final AtomicBoolean pending = new AtomicBoolean();
        private volatile boolean complete;

        private Move(Slot[] sources, boolean fillOnly, boolean reverse, boolean sound) {
            this.sources = sources;
            this.fillOnly = fillOnly;
            this.reverse = reverse;
            this.sound = sound;
            sourceIndex = reverse ? sources.length - 1 : 0;
        }

        @Override
        public InteractionManager.Waiter send() {
            complete = step();
            return type -> {
                if (complete) return true;
                // ClientSort ticks its waiter on a scheduler thread; inventories belong to Minecraft's thread.
                if (type == InteractionManager.TriggerType.TICK && pending.compareAndSet(false, true)) {
                    Minecraft.getInstance().execute(() -> {
                        try { complete = step(); }
                        finally { pending.set(false); }
                    });
                }
                return false;
            };
        }

        /** Each tick sends at most one vanilla click. Every source and destination is visited finitely. */
        private boolean step() {
            Minecraft client = Minecraft.getInstance();
            if (finished || client.player == null || client.gui.screen() != screen
                    || client.player.containerMenu != screen.getMenu()) return true;
            ItemStack carried = screen.getMenu().getCarried();
            if (source != null) {
                if (carried.isEmpty()) {
                    source = null;
                    picked = ItemStack.EMPTY;
                    returning = false;
                } else if (returning || !ItemStack.isSameItemSameComponents(carried, picked)) {
                    // A rejected return or external cursor change must not spill into another source.
                    finished = true;
                    return true;
                }
            }
            if (source == null) {
                if (!carried.isEmpty()) return true;
                while (sourceIndex >= 0 && sourceIndex < sources.length) {
                    Slot next = sources[sourceIndex];
                    sourceIndex += reverse ? -1 : 1;
                    if (!next.hasItem() || !next.mayPickup(client.player) || !hasDestination(next.getItem())) continue;
                    source = next;
                    picked = next.getItem().copy();
                    destinationIndex = 0;
                    phase = 0;
                    click(next, false);
                    return false;
                }
                return true;
            }
            // Existing matching stacks first; empty slots only for transfer, never stack-fill.
            while (phase < (fillOnly ? 1 : 2)) {
                while (destinationIndex < otherScopeSlots.length) {
                    Slot target = otherScopeSlots[destinationIndex++];
                    if (target == source || target.hasItem() != (phase == 0) || !accepts(target, carried)) continue;
                    click(target, sound);
                    return false;
                }
                phase++;
                destinationIndex = 0;
            }
            // Live cursor count, not an optimistic plan, determines whether a return is needed.
            returning = true;
            if (accepts(source, carried)) click(source, false);
            else finished = true; // Never drop or swap unrelated items to force completion.
            return false;
        }

        private boolean hasDestination(ItemStack stack) {
            for (Slot target : otherScopeSlots) {
                if ((!fillOnly || target.hasItem()) && accepts(target, stack)) return true;
            }
            return false;
        }

        private boolean accepts(Slot target, ItemStack stack) {
            if (stack.isEmpty() || !target.isActive() || !target.mayPlace(stack)) return false;
            ItemStack current = target.getItem();
            return (current.isEmpty() || ItemStack.isSameItemSameComponents(current, stack))
                    && current.getCount() < Math.min(target.getMaxStackSize(stack), stack.getMaxStackSize());
        }

        private void click(Slot slot, boolean playSound) {
            ((AbstractContainerScreenAccessor) screen).clientsort$slotClicked(slot,
                    ((ISlot) slot).clientsort$getIndexInMenu(), 0, ContainerInput.PICKUP);
            if (playSound) SoundManager.play();
        }
    }
}
