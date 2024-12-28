package net.minecraft.client.gui.components;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LockIconButton extends Button {
   private boolean locked;

   public LockIconButton(int pX, int pY, Button.OnPress pOnPress) {
      super(pX, pY, 20, 20, Component.translatable("narrator.button.difficulty_lock"), pOnPress, DEFAULT_NARRATION);
   }

   protected MutableComponent createNarrationMessage() {
      return CommonComponents.joinForNarration(super.createNarrationMessage(), this.isLocked() ? Component.translatable("narrator.button.difficulty_lock.locked") : Component.translatable("narrator.button.difficulty_lock.unlocked"));
   }

   public boolean isLocked() {
      return this.locked;
   }

   public void setLocked(boolean pLocked) {
      this.locked = pLocked;
   }

   public void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
      LockIconButton.Icon lockiconbutton$icon;
      if (!this.active) {
         lockiconbutton$icon = this.locked ? LockIconButton.Icon.LOCKED_DISABLED : LockIconButton.Icon.UNLOCKED_DISABLED;
      } else if (this.isHoveredOrFocused()) {
         lockiconbutton$icon = this.locked ? LockIconButton.Icon.LOCKED_HOVER : LockIconButton.Icon.UNLOCKED_HOVER;
      } else {
         lockiconbutton$icon = this.locked ? LockIconButton.Icon.LOCKED : LockIconButton.Icon.UNLOCKED;
      }

      pGuiGraphics.blit(Button.f_93617_, this.getX(), this.getY(), lockiconbutton$icon.m_94326_(), lockiconbutton$icon.m_94327_(), this.width, this.height);
   }

   @OnlyIn(Dist.CLIENT)
   static enum Icon {
      LOCKED(0, 146),
      LOCKED_HOVER(0, 166),
      LOCKED_DISABLED(0, 186),
      UNLOCKED(20, 146),
      UNLOCKED_HOVER(20, 166),
      UNLOCKED_DISABLED(20, 186);

      private final int f_94317_;
      private final int f_94318_;

      private Icon(int p_94324_, int p_94325_) {
         this.f_94317_ = p_94324_;
         this.f_94318_ = p_94325_;
      }

      public int m_94326_() {
         return this.f_94317_;
      }

      public int m_94327_() {
         return this.f_94318_;
      }
   }
}