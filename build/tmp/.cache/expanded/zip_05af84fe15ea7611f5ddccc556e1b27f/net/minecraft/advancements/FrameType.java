package net.minecraft.advancements;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public enum FrameType {
   TASK("task", 0, ChatFormatting.GREEN),
   CHALLENGE("challenge", 26, ChatFormatting.DARK_PURPLE),
   GOAL("goal", 52, ChatFormatting.GREEN);

   private final String name;
   private final int f_15537_;
   private final ChatFormatting chatColor;
   private final Component displayName;

   private FrameType(String pName, int p_15546_, ChatFormatting pChatColor) {
      this.name = pName;
      this.f_15537_ = p_15546_;
      this.chatColor = pChatColor;
      this.displayName = Component.translatable("advancements.toast." + pName);
   }

   public String getName() {
      return this.name;
   }

   public int m_15551_() {
      return this.f_15537_;
   }

   public static FrameType byName(String pName) {
      for(FrameType frametype : values()) {
         if (frametype.name.equals(pName)) {
            return frametype;
         }
      }

      throw new IllegalArgumentException("Unknown frame type '" + pName + "'");
   }

   public ChatFormatting getChatColor() {
      return this.chatColor;
   }

   public Component getDisplayName() {
      return this.displayName;
   }
}