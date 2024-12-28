package net.minecraft.client.gui.components;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.CommonInputs;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractButton extends AbstractWidget {
   protected static final int f_273923_ = 46;
   protected static final int f_273820_ = 200;
   protected static final int f_273885_ = 20;
   protected static final int f_275753_ = 20;
   protected static final int f_275757_ = 4;
   protected static final int TEXT_MARGIN = 2;

   public AbstractButton(int pX, int pY, int pWidth, int pHeight, Component pMessage) {
      super(pX, pY, pWidth, pHeight, pMessage);
   }

   public abstract void onPress();

   protected void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
      Minecraft minecraft = Minecraft.getInstance();
      pGuiGraphics.setColor(1.0F, 1.0F, 1.0F, this.alpha);
      RenderSystem.enableBlend();
      RenderSystem.enableDepthTest();
      pGuiGraphics.m_280027_(f_93617_, this.getX(), this.getY(), this.getWidth(), this.getHeight(), 20, 4, 200, 20, 0, this.m_274533_());
      pGuiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
      int i = getFGColor();
      this.m_280139_(pGuiGraphics, minecraft.font, i | Mth.ceil(this.alpha * 255.0F) << 24);
   }

   public void m_280139_(GuiGraphics pGuiGraphics, Font pFont, int pColor) {
      this.renderScrollingString(pGuiGraphics, pFont, 2, pColor);
   }

   private int m_274533_() {
      int i = 1;
      if (!this.active) {
         i = 0;
      } else if (this.isHoveredOrFocused()) {
         i = 2;
      }

      return 46 + i * 20;
   }

   public void onClick(double pMouseX, double pMouseY) {
      this.onPress();
   }

   /**
    * Called when a keyboard key is pressed within the GUI element.
    * <p>
    * @return {@code true} if the event is consumed, {@code false} otherwise.
    * @param pKeyCode the key code of the pressed key.
    * @param pScanCode the scan code of the pressed key.
    * @param pModifiers the keyboard modifiers.
    */
   public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
      if (this.active && this.visible) {
         if (CommonInputs.selected(pKeyCode)) {
            this.playDownSound(Minecraft.getInstance().getSoundManager());
            this.onPress();
            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }
}
