/* Copyright (c) 2016 AlexIIL and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.api.bpt;

import com.google.common.collect.ImmutableSet;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import net.minecraftforge.fluids.FluidStack;

import buildcraft.lib.permission.PlayerOwner;

// TODO: What does this encompass? Is this just a context, or is it everything?
// Should implementations delegate to something else for item/fluid getting?
// How do "robot builders" work? Don't they have lots of different positions depending
// on which one is executing it?
public interface IBuilderAccessor {
    World getWorld();

    /** @return The position from where building animations should start. Most of the time this will be inside the
     *         builder block, however this may not be the case if a player or robot is building. */
    Vec3d getBuilderPosition();

    ImmutableSet<BptPermissions> getPermissions();

    PlayerOwner getOwner();

    /** @return The number of ticks the animation will take */
    int startBlockAnimation(Vec3d target, IBlockState state, int delay);

    /** @return The number of ticks the animation will take */
    int startItemStackAnimation(Vec3d target, ItemStack display, int delay);

    /** @return The number of ticks the animation will take. It is an array {start, end} of the fluid flowing
     *         timings. */
    // FIXME Ambiguous timings doc!
    int[] startFluidAnimation(Vec3d target, FluidStack fluid, int delay);

    /** @return The number of ticks the animation will take. It is an array {start, end} of the power flowing
     *         timings. */
    // FIXME Ambiguous timings doc!
    int[] startPowerAnimation(Vec3d target, int milliJoules, int delay);

    /** Requests a single item stack. Can be null, in which case a  */
    IRequestedItem requestStack(ItemStack stack);

    /** Requests a (single) {@link ItemStack} that would be required to place the given {@link IBlockState}. */
    IRequestedItem requestStackForBlock(IBlockState state);

    IRequestedFluid requestFluid(FluidStack fluid);
    
    void addAction(IBptAction action, int delay);

    /** Designates *something* that can be requested. Use a child interface rather than this directly.
     *
     * @param <T> */
    public interface IRequested {

        /** Attempts to fully reserve the stack, but without actually using it.
         * 
         * @return True if this stack was available and has been properly reserved, or if this has been previously
         *         locked.
         * @throws IllegalStateException if this has already been locked, and {@link #release()} has been called
         *             successfully. */
        boolean lock() throws IllegalStateException;

        /** @return True if this stack is currently locked, or it has already been used. */
        boolean isLocked();

        /** Uses up the stack, unlocking it and making this request useless. Future calls to {@link #lock()} will throw
         * an {@link IllegalStateException}.
         * 
         * @throws IllegalStateException if this was not locked previously. */
        void use() throws IllegalStateException;

        /** Unlocks this request WITHOUT using it up. Note that this never throws, so it is safe to call this at any
         * time. */
        void release();
    }

    /** An item stack that has preciously been requested. This starts off unlocked (it may or may not actually
     * exist). */
    public interface IRequestedItem extends IRequested {
        /** @return The {@link ItemStack} that was requested. */
        ItemStack getRequested();
    }

    /** A fluid stack that has preciously been requested. This starts off unlocked (it may or may not actually
     * exist). */
    public interface IRequestedFluid extends IRequested {
        /** @return The {@link FluidStack} that was requested */
        FluidStack getRequested();
    }
}