package net.minecraft.client.gui.components;

import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CommonButtons {
   public static TextAndImageButton m_271983_(Button.OnPress p_273337_) {
      return TextAndImageButton.m_267772_(Component.translatable("options.language"), Button.f_93617_, p_273337_).m_267752_(3, 109).m_267570_(65, 3).m_267809_(20).m_267765_(14, 14).m_267643_(256, 256).m_267775_();
   }

   public static TextAndImageButton m_272052_(Button.OnPress p_273354_) {
      return TextAndImageButton.m_267772_(Component.translatable("options.accessibility.title"), Button.f_267372_, p_273354_).m_267752_(3, 2).m_267570_(65, 2).m_267809_(20).m_267765_(14, 16).m_267643_(32, 64).m_267775_();
   }
}