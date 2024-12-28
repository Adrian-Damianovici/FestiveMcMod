package net.minecraft.core.dispenser;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.ChestBoat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;

public class BoatDispenseItemBehavior extends DefaultDispenseItemBehavior {
   private final DefaultDispenseItemBehavior defaultDispenseItemBehavior = new DefaultDispenseItemBehavior();
   private final Boat.Type type;
   private final boolean isChestBoat;

   public BoatDispenseItemBehavior(Boat.Type pType) {
      this(pType, false);
   }

   public BoatDispenseItemBehavior(Boat.Type pType, boolean pIsChestBoat) {
      this.type = pType;
      this.isChestBoat = pIsChestBoat;
   }

   public ItemStack execute(BlockSource pBlockSource, ItemStack pItem) {
      Direction direction = pBlockSource.m_6414_().getValue(DispenserBlock.FACING);
      Level level = pBlockSource.m_7727_();
      double d0 = 0.5625D + (double)EntityType.BOAT.getWidth() / 2.0D;
      double d1 = pBlockSource.x() + (double)direction.getStepX() * d0;
      double d2 = pBlockSource.y() + (double)((float)direction.getStepY() * 1.125F);
      double d3 = pBlockSource.z() + (double)direction.getStepZ() * d0;
      BlockPos blockpos = pBlockSource.m_7961_().relative(direction);
      Boat boat = (Boat)(this.isChestBoat ? new ChestBoat(level, d0, d1, d2) : new Boat(level, d0, d1, d2));
      boat.setVariant(this.type);
      boat.setYRot(direction.toYRot());
      double d4;
      if (boat.canBoatInFluid(level.getFluidState(blockpos))) {
         d4 = 1.0D;
      } else {
         if (!level.getBlockState(blockpos).isAir() || !boat.canBoatInFluid(level.getFluidState(blockpos.below()))) {
            return this.defaultDispenseItemBehavior.dispense(pBlockSource, pItem);
         }

         d4 = 0.0D;
      }

      boat.setPos(d1, d2 + d4, d3);
      level.addFreshEntity(boat);
      pItem.shrink(1);
      return pItem;
   }

   protected void playSound(BlockSource pBlockSource) {
      pBlockSource.m_7727_().levelEvent(1000, pBlockSource.m_7961_(), 0);
   }
}
