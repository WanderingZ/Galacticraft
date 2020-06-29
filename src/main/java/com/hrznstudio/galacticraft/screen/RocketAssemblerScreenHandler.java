/*
 * Copyright (c) 2019 HRZN LTD
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.hrznstudio.galacticraft.screen;

import com.hrznstudio.galacticraft.Galacticraft;
import com.hrznstudio.galacticraft.accessor.ServerPlayerEntityAccessor;
import com.hrznstudio.galacticraft.api.rocket.RocketData;
import com.hrznstudio.galacticraft.energy.GalacticraftEnergy;
import com.hrznstudio.galacticraft.items.GalacticraftItems;
import com.hrznstudio.galacticraft.block.entity.RocketAssemblerBlockEntity;
import net.fabricmc.fabric.api.container.ContainerFactory;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

/**
 * @author <a href="https://github.com/StellarHorizons">StellarHorizons</a>
 */
public class RocketAssemblerScreenHandler extends ScreenHandler {

    public static final ContainerFactory<ScreenHandler> FACTORY = (syncId, id, player, buffer) -> {
        BlockPos pos = buffer.readBlockPos();
        BlockEntity be = player.world.getBlockEntity(pos);
        if (be instanceof RocketAssemblerBlockEntity) {
            return new RocketAssemblerScreenHandler(syncId, player, (RocketAssemblerBlockEntity) be);
        } else {
            return null;
        }
    };

    protected Inventory inventory;
    protected RocketAssemblerBlockEntity blockEntity;

    public RocketAssemblerScreenHandler(int syncId, PlayerEntity playerEntity, RocketAssemblerBlockEntity blockEntity) {
        super(null, syncId);
        this.blockEntity = blockEntity;
        this.inventory = blockEntity.getInventory().asInventory();

        final int playerInvYOffset = 94;
        final int playerInvXOffset = 148;

        // Output slot
        this.addSlot(new Slot(this.inventory, RocketAssemblerBlockEntity.SCHEMATIC_INPUT_SLOT, 235, 19) {
            @Override
            public boolean canInsert(ItemStack stack) {
                RocketData data = RocketData.fromItem(stack);
                return stack.getItem() != GalacticraftItems.ROCKET_SCHEMATIC || !this.getStack().isEmpty() || (!(playerEntity instanceof ServerPlayerEntity)) || (
                        ((ServerPlayerEntityAccessor) playerEntity).getResearchTracker().isUnlocked(Galacticraft.ROCKET_PARTS.getId(data.getCone())) &&
                        ((ServerPlayerEntityAccessor) playerEntity).getResearchTracker().isUnlocked(Galacticraft.ROCKET_PARTS.getId(data.getBody())) &&
                        ((ServerPlayerEntityAccessor) playerEntity).getResearchTracker().isUnlocked(Galacticraft.ROCKET_PARTS.getId(data.getBooster())) &&
                        ((ServerPlayerEntityAccessor) playerEntity).getResearchTracker().isUnlocked(Galacticraft.ROCKET_PARTS.getId(data.getBottom())) &&
                        ((ServerPlayerEntityAccessor) playerEntity).getResearchTracker().isUnlocked(Galacticraft.ROCKET_PARTS.getId(data.getFin())) &&
                        ((ServerPlayerEntityAccessor) playerEntity).getResearchTracker().isUnlocked(Galacticraft.ROCKET_PARTS.getId(data.getUpgrade()))
                );
            }

            @Override
            public boolean canTakeItems(PlayerEntity playerEntity) {
                return true;
            }
        });

        this.addSlot(new Slot(this.inventory, RocketAssemblerBlockEntity.ROCKET_OUTPUT_SLOT, 299, 19) {
            @Override
            public boolean canInsert(ItemStack itemStack_1) {
                return itemStack_1.getItem() == GalacticraftItems.ROCKET && this.getStack().isEmpty();
            }

            @Override
            public boolean canTakeItems(PlayerEntity playerEntity_1) {
                return true;
            }
        });

        this.addSlot(new Slot(this.inventory, RocketAssemblerBlockEntity.ENERGY_INPUT_SLOT, 156, 72) {
            @Override
            public boolean canInsert(ItemStack itemStack_1) {
                return GalacticraftEnergy.isEnergyItem(itemStack_1) && this.getStack().isEmpty();
            }

            @Override
            public boolean canTakeItems(PlayerEntity playerEntity_1) {
                return true;
            }
        });

        // Hotbar slots
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerEntity.inventory, i, 8 + i * 18 + playerInvXOffset, playerInvYOffset + 58));
        }

        // Player inventory slots
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerEntity.inventory, j + i * 9 + 9, 8 + (j * 18) + playerInvXOffset, playerInvYOffset + i * 18));
            }
        }
    }

    @Override
    public ItemStack onSlotClick(int i, int j, SlotActionType actionType, PlayerEntity playerEntity) {
        if (actionType == SlotActionType.QUICK_MOVE) {
            if (slots.get(i).getStack().getItem() != GalacticraftItems.ROCKET_SCHEMATIC) {
                return ItemStack.EMPTY;
            } else {
                if(inventory.getStack(0).isEmpty()) {
                    inventory.setStack(0, slots.get(i).getStack().copy());
                    slots.get(i).setStack(ItemStack.EMPTY);
                    return inventory.getStack(0);
                } else {
                    return ItemStack.EMPTY;
                }
            }
        }
        return super.onSlotClick(i, j, actionType, playerEntity);
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }
}