package net.minecraft.client;

import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public record GuiMessageTag(int indicatorColor, @Nullable GuiMessageTag.Icon icon, @Nullable Component text, @Nullable String logTag) {
   private static final Component SYSTEM_TEXT = Component.translatable("chat.tag.system");
   private static final Component SYSTEM_TEXT_SINGLE_PLAYER = Component.translatable("chat.tag.system_single_player");
   private static final Component CHAT_NOT_SECURE_TEXT = Component.translatable("chat.tag.not_secure");
   private static final Component CHAT_MODIFIED_TEXT = Component.translatable("chat.tag.modified");
   private static final int CHAT_NOT_SECURE_INDICATOR_COLOR = 13684944;
   private static final int CHAT_MODIFIED_INDICATOR_COLOR = 6316128;
   private static final GuiMessageTag SYSTEM = new GuiMessageTag(13684944, (GuiMessageTag.Icon)null, SYSTEM_TEXT, "System");
   private static final GuiMessageTag SYSTEM_SINGLE_PLAYER = new GuiMessageTag(13684944, (GuiMessageTag.Icon)null, SYSTEM_TEXT_SINGLE_PLAYER, "System");
   private static final GuiMessageTag CHAT_NOT_SECURE = new GuiMessageTag(13684944, (GuiMessageTag.Icon)null, CHAT_NOT_SECURE_TEXT, "Not Secure");
   static final ResourceLocation f_240343_ = new ResourceLocation("textures/gui/chat_tags.png");

   public static GuiMessageTag system() {
      return SYSTEM;
   }

   public static GuiMessageTag systemSinglePlayer() {
      return SYSTEM_SINGLE_PLAYER;
   }

   public static GuiMessageTag chatNotSecure() {
      return CHAT_NOT_SECURE;
   }

   public static GuiMessageTag chatModified(String pOriginalText) {
      Component component = Component.literal(pOriginalText).withStyle(ChatFormatting.GRAY);
      Component component1 = Component.empty().append(CHAT_MODIFIED_TEXT).append(CommonComponents.NEW_LINE).append(component);
      return new GuiMessageTag(6316128, GuiMessageTag.Icon.CHAT_MODIFIED, component1, "Modified");
   }

   @OnlyIn(Dist.CLIENT)
   public static enum Icon {
      CHAT_MODIFIED(0, 0, 9, 9);

      public final int f_240366_;
      public final int f_240349_;
      public final int width;
      public final int height;

      private Icon(int pWidth, int pHeight, int p_240607_, int p_240531_) {
         this.f_240366_ = pWidth;
         this.f_240349_ = pHeight;
         this.width = p_240607_;
         this.height = p_240531_;
      }

      public void draw(GuiGraphics pGuiGraphics, int pX, int pY) {
         pGuiGraphics.blit(GuiMessageTag.f_240343_, pX, pY, (float)this.f_240366_, (float)this.f_240349_, this.width, this.height, 32, 32);
      }
   }
}