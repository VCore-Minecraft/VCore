/*
 * Copyright (c) 2021. Lukas Jonsson
 */

package de.verdox.vcore.impl.gameserver.paper.utils.util.wrapper;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnegative;
import java.util.ArrayList;
import java.util.List;


public class StackPile {
    private final ItemStack stack;
    private final int amount;

    public StackPile(@NotNull ItemStack stack) {
        this.stack = stack;
        this.amount = stack.getAmount();
    }

    public StackPile(@NotNull ItemStack stack, @Nonnegative int amount) {
        this.stack = stack;
        this.amount = amount;
    }

    public StackPile withAmount(@Nonnegative int amount) {
        return new StackPile(stack, amount);
    }

    public StackPile withStack(@NotNull ItemStack stack) {
        return new StackPile(stack, amount);
    }

    public ItemStack getStack() {
        return stack;
    }

    public int getAmount() {
        return amount;
    }

    public ItemStack[] splitIntoVanillaStacks() {
        List<ItemStack> stackList = new ArrayList<>();
        int stacks = amount / stack.getMaxStackSize();
        int lastStack = amount % stack.getMaxStackSize();

        for (int i = 0; i < stacks; i++)
            stackList.add(stack.asQuantity(stack.getMaxStackSize()));
        stackList.add(stack.asQuantity(lastStack));
        return stackList.toArray(new ItemStack[0]);
    }
}
