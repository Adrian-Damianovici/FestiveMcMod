package net.minecraft.core.dispenser;

import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;

public abstract class AbstractProjectileDispenseBehavior extends DefaultDispenseItemBehavior {
   public ItemStack execute(BlockSource pBlockSource, ItemStack pItem) {
      Level level = pBlockSource.m_7727_();
      Position position = DispenserBlock.getDispensePosition(pBlockSource);
      Direction direction = pBlockSource.m_6414_().getValue(DispenserBlock.FACING);
      Projectile projectile = this.getProjectile(level, position, pItem);
      projectile.shoot((double)direction.getStepX(), (double)((float)direction.getStepY() + 0.1F), (double)direction.getStepZ(), this.getPower(), this.getUncertainty());
      level.addFreshEntity(projectile);
      pItem.shrink(1);
      return pItem;
   }

   protected void playSound(BlockSource pBlockSource) {
      pBlockSource.m_7727_().levelEvent(1002, pBlockSource.m_7961_(), 0);
   }

   /**
    * Return the projectile entity spawned by this dispense behavior.
    */
   protected abstract Projectile getProjectile(Level pLevel, Position pPosition, ItemStack pStack);

   protected float getUncertainty() {
      return 6.0F;
   }

   protected float getPower() {
      return 1.1F;
   }
}