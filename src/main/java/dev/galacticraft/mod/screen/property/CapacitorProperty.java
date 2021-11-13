/*
 * Copyright (c) 2019-2021 Team Galacticraft
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

package dev.galacticraft.mod.screen.property;

import dev.galacticraft.mod.lookup.storage.MachineEnergyStorage;
import net.minecraft.screen.PropertyDelegate;

/**
 * @author <a href="https://github.com/TeamGalacticraft">TeamGalacticraft</a>
 */
public class CapacitorProperty implements PropertyDelegate {
    private final MachineEnergyStorage capacitor;

    public CapacitorProperty(MachineEnergyStorage capacitor) {
        this.capacitor = capacitor;
    }

    @Override
    public int get(int index) {
        return (int) (index == 0 ? this.capacitor.getAmount() & 0b1111111111111111111111111111111L : (this.capacitor.getAmount() >> 32 & 0b1111111111111111111111111111111L));
    }

    @Override
    public void set(int index, int value) {
        this.capacitor.setEnergy(((long) value) << (index == 0 ? 0 : 32) | (capacitor.getAmount() & (0b1111111111111111111111111111111L << (index == 1 ? 0 : 32))));
    }

    @Override
    public int size() {
        return 2;
    }
}
