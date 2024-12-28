package net.minecraft.client.gui.screens;

import com.mojang.authlib.minecraft.BanDetails;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.time.Duration;
import java.time.Instant;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.chat.report.BanReason;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.Style;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.StringUtils;

@OnlyIn(Dist.CLIENT)
public class BanNoticeScreen {
   private static final Component f_238586_ = Component.translatable("gui.banned.title.temporary").withStyle(ChatFormatting.BOLD);
   private static final Component f_238702_ = Component.translatable("gui.banned.title.permanent").withStyle(ChatFormatting.BOLD);

   public static ConfirmLinkScreen m_239967_(BooleanConsumer p_239968_, BanDetails p_239969_) {
      return new ConfirmLinkScreen(p_239968_, m_239952_(p_239969_), m_239137_(p_239969_), "https://aka.ms/mcjavamoderation", CommonComponents.GUI_ACKNOWLEDGE, true);
   }

   private static Component m_239952_(BanDetails p_239953_) {
      return m_239500_(p_239953_) ? f_238586_ : f_238702_;
   }

   private static Component m_239137_(BanDetails p_239138_) {
      return Component.translatable("gui.banned.description", m_239533_(p_239138_), m_239318_(p_239138_), Component.literal("https://aka.ms/mcjavamoderation"));
   }

   private static Component m_239533_(BanDetails p_239534_) {
      String s = p_239534_.reason();
      String s1 = p_239534_.reasonMessage();
      if (StringUtils.isNumeric(s)) {
         int i = Integer.parseInt(s);
         BanReason banreason = BanReason.byId(i);
         Component component;
         if (banreason != null) {
            component = ComponentUtils.mergeStyles(banreason.title().copy(), Style.EMPTY.withBold(true));
         } else if (s1 != null) {
            component = Component.translatable("gui.banned.description.reason_id_message", i, s1).withStyle(ChatFormatting.BOLD);
         } else {
            component = Component.translatable("gui.banned.description.reason_id", i).withStyle(ChatFormatting.BOLD);
         }

         return Component.translatable("gui.banned.description.reason", component);
      } else {
         return Component.translatable("gui.banned.description.unknownreason");
      }
   }

   private static Component m_239318_(BanDetails p_239319_) {
      if (m_239500_(p_239319_)) {
         Component component = m_239879_(p_239319_);
         return Component.translatable("gui.banned.description.temporary", Component.translatable("gui.banned.description.temporary.duration", component).withStyle(ChatFormatting.BOLD));
      } else {
         return Component.translatable("gui.banned.description.permanent").withStyle(ChatFormatting.BOLD);
      }
   }

   private static Component m_239879_(BanDetails p_239880_) {
      Duration duration = Duration.between(Instant.now(), p_239880_.expires());
      long i = duration.toHours();
      if (i > 72L) {
         return CommonComponents.days(duration.toDays());
      } else {
         return i < 1L ? CommonComponents.minutes(duration.toMinutes()) : CommonComponents.hours(duration.toHours());
      }
   }

   private static boolean m_239500_(BanDetails p_239501_) {
      return p_239501_.expires() != null;
   }
}