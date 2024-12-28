package net.minecraft.client.gui.screens.advancements;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public enum AdvancementWidgetType {
   OBTAINED(0),
   UNOBTAINED(1);

   private final int f_97318_;

   private AdvancementWidgetType(int p_97324_) {
      this.f_97318_ = p_97324_;
   }

   public int m_97325_() {
      return this.f_97318_;
   }
}