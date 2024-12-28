package net.minecraft.client.gui.spectator;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundTeleportToEntityPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PlayerMenuItem implements SpectatorMenuItem {
   private final GameProfile profile;
   private final ResourceLocation f_101753_;
   private final Component name;

   public PlayerMenuItem(GameProfile pProfile) {
      this.profile = pProfile;
      Minecraft minecraft = Minecraft.getInstance();
      this.f_101753_ = minecraft.getSkinManager().m_240306_(pProfile);
      this.name = Component.literal(pProfile.getName());
   }

   public void selectItem(SpectatorMenu pMenu) {
      Minecraft.getInstance().getConnection().m_104955_(new ServerboundTeleportToEntityPacket(this.profile.getId()));
   }

   public Component getName() {
      return this.name;
   }

   public void renderIcon(GuiGraphics pGuiGraphics, float pShadeColor, int pAlpha) {
      pGuiGraphics.setColor(1.0F, 1.0F, 1.0F, (float)pAlpha / 255.0F);
      PlayerFaceRenderer.draw(pGuiGraphics, this.f_101753_, 2, 2, 12);
      pGuiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
   }

   public boolean isEnabled() {
      return true;
   }
}