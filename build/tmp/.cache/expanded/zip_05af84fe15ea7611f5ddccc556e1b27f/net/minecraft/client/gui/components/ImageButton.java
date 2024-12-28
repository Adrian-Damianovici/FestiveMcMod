package net.minecraft.client.gui.components;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ImageButton extends Button {
   protected final ResourceLocation f_94223_;
   protected final int f_94224_;
   protected final int f_94225_;
   protected final int f_94226_;
   protected final int f_94227_;
   protected final int f_94228_;

   public ImageButton(int pWidth, int pHeight, int p_169013_, int p_169014_, int p_169015_, int p_169016_, ResourceLocation p_169017_, Button.OnPress pOnPress) {
      this(pWidth, pHeight, p_169013_, p_169014_, p_169015_, p_169016_, p_169014_, p_169017_, 256, 256, pOnPress);
   }

   public ImageButton(int p_94269_, int p_94270_, int p_94271_, int p_94272_, int p_94273_, int p_94274_, int p_94275_, ResourceLocation p_94276_, Button.OnPress p_94277_) {
      this(p_94269_, p_94270_, p_94271_, p_94272_, p_94273_, p_94274_, p_94275_, p_94276_, 256, 256, p_94277_);
   }

   public ImageButton(int pX, int pY, int pWidth, int pHeight, int p_94234_, int p_94235_, int p_94236_, ResourceLocation p_94237_, int p_94238_, int p_94239_, Button.OnPress pOnPress) {
      this(pX, pY, pWidth, pHeight, p_94234_, p_94235_, p_94236_, p_94237_, p_94238_, p_94239_, pOnPress, CommonComponents.EMPTY);
   }

   public ImageButton(int pX, int pY, int pWidth, int pHeight, int p_94260_, int p_94261_, int p_94262_, ResourceLocation p_94263_, int p_94264_, int p_94265_, Button.OnPress pOnPress, Component pMessage) {
      super(pX, pY, pWidth, pHeight, pMessage, pOnPress, DEFAULT_NARRATION);
      this.f_94227_ = p_94264_;
      this.f_94228_ = p_94265_;
      this.f_94224_ = p_94260_;
      this.f_94225_ = p_94261_;
      this.f_94226_ = p_94262_;
      this.f_94223_ = p_94263_;
   }

   public void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
      this.m_280322_(pGuiGraphics, this.f_94223_, this.getX(), this.getY(), this.f_94224_, this.f_94225_, this.f_94226_, this.width, this.height, this.f_94227_, this.f_94228_);
   }
}