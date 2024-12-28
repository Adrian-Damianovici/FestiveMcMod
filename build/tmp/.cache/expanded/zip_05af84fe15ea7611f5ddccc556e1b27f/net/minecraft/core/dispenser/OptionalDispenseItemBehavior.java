package net.minecraft.core.dispenser;

import net.minecraft.core.BlockSource;

public abstract class OptionalDispenseItemBehavior extends DefaultDispenseItemBehavior {
   private boolean success = true;

   public boolean isSuccess() {
      return this.success;
   }

   public void setSuccess(boolean pSuccess) {
      this.success = pSuccess;
   }

   protected void playSound(BlockSource pBlockSource) {
      pBlockSource.m_7727_().levelEvent(this.isSuccess() ? 1000 : 1001, pBlockSource.m_7961_(), 0);
   }
}