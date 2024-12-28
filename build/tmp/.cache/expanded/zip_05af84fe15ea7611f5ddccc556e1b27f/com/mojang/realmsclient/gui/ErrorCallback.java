package com.mojang.realmsclient.gui;

import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface ErrorCallback {
   void m_5673_(Component p_87793_);

   default void m_87791_(String p_87792_) {
      this.m_5673_(Component.literal(p_87792_));
   }
}