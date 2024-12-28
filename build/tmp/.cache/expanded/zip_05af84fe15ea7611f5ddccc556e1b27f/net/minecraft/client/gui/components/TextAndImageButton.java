package net.minecraft.client.gui.components;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TextAndImageButton extends Button {
   protected final ResourceLocation f_273941_;
   protected final int f_273904_;
   protected final int f_273857_;
   protected final int f_273911_;
   protected final int f_273880_;
   protected final int f_273871_;
   private final int f_267392_;
   private final int f_267457_;
   private final int f_267436_;
   private final int f_267434_;

   TextAndImageButton(Component p_268357_, int p_268106_, int p_268141_, int p_268331_, int p_268045_, int p_268300_, int p_268151_, int p_267955_, int p_268114_, int p_268103_, ResourceLocation p_268067_, Button.OnPress p_268052_) {
      super(0, 0, 150, 20, p_268357_, p_268052_, DEFAULT_NARRATION);
      this.f_273880_ = p_268114_;
      this.f_273871_ = p_268103_;
      this.f_273904_ = p_268106_;
      this.f_273857_ = p_268141_;
      this.f_273911_ = p_268300_;
      this.f_273941_ = p_268067_;
      this.f_267392_ = p_268331_;
      this.f_267457_ = p_268045_;
      this.f_267436_ = p_268151_;
      this.f_267434_ = p_267955_;
   }

   public void renderWidget(GuiGraphics p_282062_, int p_283189_, int p_283584_, float p_283402_) {
      super.renderWidget(p_282062_, p_283189_, p_283584_, p_283402_);
      this.m_280322_(p_282062_, this.f_273941_, this.m_267702_(), this.m_267831_(), this.f_273904_, this.f_273857_, this.f_273911_, this.f_267436_, this.f_267434_, this.f_273880_, this.f_273871_);
   }

   public void m_280139_(GuiGraphics p_281792_, Font p_283239_, int p_283135_) {
      int i = this.getX() + 2;
      int j = this.getX() + this.getWidth() - this.f_267436_ - 6;
      renderScrollingString(p_281792_, p_283239_, this.getMessage(), i, this.getY(), j, this.getY() + this.getHeight(), p_283135_);
   }

   private int m_267702_() {
      return this.getX() + (this.width / 2 - this.f_267436_ / 2) + this.f_267392_;
   }

   private int m_267831_() {
      return this.getY() + this.f_267457_;
   }

   public static TextAndImageButton.Builder m_267772_(Component p_268304_, ResourceLocation p_268277_, Button.OnPress p_268297_) {
      return new TextAndImageButton.Builder(p_268304_, p_268277_, p_268297_);
   }

   @OnlyIn(Dist.CLIENT)
   public static class Builder {
      private final Component f_267375_;
      private final ResourceLocation f_267378_;
      private final Button.OnPress f_267427_;
      private int f_267472_;
      private int f_267366_;
      private int f_267451_;
      private int f_267364_;
      private int f_267408_;
      private int f_267430_;
      private int f_267387_;
      private int f_267417_;
      private int f_267433_;

      public Builder(Component p_267988_, ResourceLocation p_268260_, Button.OnPress p_268075_) {
         this.f_267375_ = p_267988_;
         this.f_267378_ = p_268260_;
         this.f_267427_ = p_268075_;
      }

      public TextAndImageButton.Builder m_267752_(int p_267995_, int p_268187_) {
         this.f_267472_ = p_267995_;
         this.f_267366_ = p_268187_;
         return this;
      }

      public TextAndImageButton.Builder m_267570_(int p_268306_, int p_268207_) {
         this.f_267417_ = p_268306_;
         this.f_267433_ = p_268207_;
         return this;
      }

      public TextAndImageButton.Builder m_267809_(int p_268008_) {
         this.f_267451_ = p_268008_;
         return this;
      }

      public TextAndImageButton.Builder m_267765_(int p_268087_, int p_268011_) {
         this.f_267364_ = p_268087_;
         this.f_267408_ = p_268011_;
         return this;
      }

      public TextAndImageButton.Builder m_267643_(int p_268166_, int p_268310_) {
         this.f_267430_ = p_268166_;
         this.f_267387_ = p_268310_;
         return this;
      }

      public TextAndImageButton m_267775_() {
         return new TextAndImageButton(this.f_267375_, this.f_267472_, this.f_267366_, this.f_267417_, this.f_267433_, this.f_267451_, this.f_267364_, this.f_267408_, this.f_267430_, this.f_267387_, this.f_267378_, this.f_267427_);
      }
   }
}