/*
 * Copyright (c) 2019-2022 Team Galacticraft
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

package dev.galacticraft.mod.api.block.entity;

import dev.galacticraft.mod.Constant;
import dev.galacticraft.mod.api.wire.Wire;
import dev.galacticraft.mod.api.wire.WireNetwork;
import dev.galacticraft.mod.attribute.energy.WireEnergyStorage;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author <a href="https://github.com/TeamGalacticraft">TeamGalacticraft</a>
 */
public class WireBlockEntity extends BlockEntity implements Wire {
    private @Nullable WireNetwork network = null;
    private @NotNull WireEnergyStorage @Nullable[] insertables = null;
    private final int maxTransferRate;
    private final boolean[] connections = new boolean[6];

    public WireBlockEntity(BlockEntityType<? extends WireBlockEntity> type, BlockPos pos, BlockState state, int maxTransferRate) {
        super(type, pos, state);
        this.maxTransferRate = maxTransferRate;
    }

    public static WireBlockEntity createT1(BlockEntityType<? extends WireBlockEntity> type, BlockPos pos, BlockState state) {
        return new WireBlockEntity(type, pos, state, 240);
    }

    public static WireBlockEntity createT2(BlockEntityType<? extends WireBlockEntity> type, BlockPos pos, BlockState state) {
        return new WireBlockEntity(type, pos, state, 480);
    }

    @Override
    public void setNetwork(@Nullable WireNetwork network) {
        this.network = network;
        for (WireEnergyStorage insertable : this.getInsertables()) {
            insertable.setNetwork(network);
        }
    }

    @Override
    public @NotNull WireNetwork getOrCreateNetwork() {
        if (this.network == null) {
            if (!this.world.isClient()) {
                for (Direction direction : Constant.Misc.DIRECTIONS) {
                    if (this.canConnect(direction)) {
                        BlockEntity entity = world.getBlockEntity(pos.offset(direction));
                        if (entity instanceof Wire wire && wire.getNetwork() != null) {
                            if (wire.canConnect(direction.getOpposite())) {
                                if (wire.getOrCreateNetwork().isCompatibleWith(this)) {
                                    wire.getNetwork().addWire(pos, this);
                                }
                            }
                        }
                    }
                }
                if (this.network == null) {
                    this.setNetwork(WireNetwork.create((ServerWorld) world, this.getMaxTransferRate()));
                    this.network.addWire(pos, this);
                }
            }
        }
        return this.network;
    }

    @Override
    public @Nullable WireNetwork getNetwork() {
        return this.network;
    }

    public @NotNull WireEnergyStorage @NotNull[] getInsertables() {
        if (this.insertables == null) {
            this.insertables = new WireEnergyStorage[6];
            for (Direction direction : Constant.Misc.DIRECTIONS) {
                this.insertables[direction.ordinal()] = new WireEnergyStorage(direction, this.getMaxTransferRate(), this.pos);
            }
        }
        return this.insertables;
    }

    @Override
    public int getMaxTransferRate() {
        return maxTransferRate;
    }

    @Override
    public void markRemoved() {
        super.markRemoved();
        if (this.getNetwork() != null) {
            this.getOrCreateNetwork().removeWire(this, this.pos);
        }
    }

    @Override
    public boolean[] getConnections() {
        return this.connections;
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        this.writeConnectionNbt(nbt);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        this.readConnectionNbt(nbt);
        if (world.isClient) {
            MinecraftClient.getInstance().worldRenderer.scheduleBlockRender(pos.getX(), pos.getY(), pos.getZ());
        }
    }
}
