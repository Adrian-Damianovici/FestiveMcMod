package com.mojang.realmsclient.util;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.util.UUIDTypeAdapter;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RealmsUtil {
   static final MinecraftSessionService f_90216_ = Minecraft.getInstance().getMinecraftSessionService();
   private static final Component RIGHT_NOW = Component.translatable("mco.util.time.now");
   private static final LoadingCache<String, GameProfile> f_268555_ = CacheBuilder.newBuilder().expireAfterWrite(60L, TimeUnit.MINUTES).build(new CacheLoader<String, GameProfile>() {
      public GameProfile load(String p_90229_) {
         return RealmsUtil.f_90216_.fillProfileProperties(new GameProfile(UUIDTypeAdapter.fromString(p_90229_), (String)null), false);
      }
   });
   private static final int MINUTES = 60;
   private static final int HOURS = 3600;
   private static final int DAYS = 86400;

   public static String m_90221_(String p_90222_) {
      return f_268555_.getUnchecked(p_90222_).getName();
   }

   public static GameProfile m_269239_(String p_270932_) {
      return f_268555_.getUnchecked(p_270932_);
   }

   public static Component convertToAgePresentation(long pMillis) {
      if (pMillis < 0L) {
         return RIGHT_NOW;
      } else {
         long i = pMillis / 1000L;
         if (i < 60L) {
            return Component.translatable("mco.time.secondsAgo", i);
         } else if (i < 3600L) {
            long l = i / 60L;
            return Component.translatable("mco.time.minutesAgo", l);
         } else if (i < 86400L) {
            long k = i / 3600L;
            return Component.translatable("mco.time.hoursAgo", k);
         } else {
            long j = i / 86400L;
            return Component.translatable("mco.time.daysAgo", j);
         }
      }
   }

   public static Component convertToAgePresentationFromInstant(Date pDate) {
      return convertToAgePresentation(System.currentTimeMillis() - pDate.getTime());
   }

   public static void renderPlayerFace(GuiGraphics pGuiGraphics, int pX, int pY, int pSize, String p_282512_) {
      GameProfile gameprofile = m_269239_(p_282512_);
      ResourceLocation resourcelocation = Minecraft.getInstance().getSkinManager().m_240306_(gameprofile);
      PlayerFaceRenderer.draw(pGuiGraphics, resourcelocation, pX, pY, pSize);
   }
}