package com.mojang.realmsclient.util.task;

import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.gui.ErrorCallback;
import com.mojang.realmsclient.gui.screens.RealmsLongRunningMcoTaskScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public abstract class LongRunningTask implements ErrorCallback, Runnable {
   protected static final int NUMBER_OF_RETRIES = 25;
   private static final Logger LOGGER = LogUtils.getLogger();
   protected RealmsLongRunningMcoTaskScreen f_90395_;

   protected static void pause(long pSeconds) {
      try {
         Thread.sleep(pSeconds * 1000L);
      } catch (InterruptedException interruptedexception) {
         Thread.currentThread().interrupt();
         LOGGER.error("", (Throwable)interruptedexception);
      }

   }

   public static void setScreen(Screen pScreen) {
      Minecraft minecraft = Minecraft.getInstance();
      minecraft.execute(() -> {
         minecraft.setScreen(pScreen);
      });
   }

   public void m_90400_(RealmsLongRunningMcoTaskScreen p_90401_) {
      this.f_90395_ = p_90401_;
   }

   public void m_5673_(Component pMessage) {
      this.f_90395_.m_5673_(pMessage);
   }

   public void m_90409_(Component p_90410_) {
      this.f_90395_.setTitle(p_90410_);
   }

   public boolean aborted() {
      return this.f_90395_.m_88779_();
   }

   public void tick() {
   }

   public void init() {
   }

   public void abortTask() {
   }
}