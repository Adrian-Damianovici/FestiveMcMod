package net.minecraft.client.multiplayer;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.RemoteChatSession;
import net.minecraft.network.chat.SignedMessageValidator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.GameType;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PlayerInfo {
   /** The GameProfile for the player represented by this NetworkPlayerInfo instance */
   private final GameProfile profile;
   private final Map<MinecraftProfileTexture.Type, ResourceLocation> f_105299_ = Maps.newEnumMap(MinecraftProfileTexture.Type.class);
   private GameType gameMode = GameType.DEFAULT_MODE;
   private int latency;
   private boolean f_105302_;
   @Nullable
   private String f_105303_;
   /** When this is non-null, it is displayed instead of the player's real name */
   @Nullable
   private Component tabListDisplayName;
   @Nullable
   private RemoteChatSession chatSession;
   private SignedMessageValidator messageValidator;

   public PlayerInfo(GameProfile pProfile, boolean pEnforeSecureChat) {
      this.profile = pProfile;
      this.messageValidator = fallbackMessageValidator(pEnforeSecureChat);
   }

   /**
    * Returns the GameProfile for the player represented by this NetworkPlayerInfo instance
    */
   public GameProfile getProfile() {
      return this.profile;
   }

   @Nullable
   public RemoteChatSession getChatSession() {
      return this.chatSession;
   }

   public SignedMessageValidator getMessageValidator() {
      return this.messageValidator;
   }

   public boolean hasVerifiableChat() {
      return this.chatSession != null;
   }

   protected void setChatSession(RemoteChatSession pChatSession) {
      this.chatSession = pChatSession;
      this.messageValidator = pChatSession.createMessageValidator();
   }

   protected void clearChatSession(boolean pEnforcesSecureChat) {
      this.chatSession = null;
      this.messageValidator = fallbackMessageValidator(pEnforcesSecureChat);
   }

   private static SignedMessageValidator fallbackMessageValidator(boolean pEnforeSecureChat) {
      return pEnforeSecureChat ? SignedMessageValidator.REJECT_ALL : SignedMessageValidator.ACCEPT_UNSIGNED;
   }

   public GameType getGameMode() {
      return this.gameMode;
   }

   protected void setGameMode(GameType pGameMode) {
      net.minecraftforge.client.ForgeHooksClient.onClientChangeGameType(this, this.gameMode, pGameMode);
      this.gameMode = pGameMode;
   }

   public int getLatency() {
      return this.latency;
   }

   protected void setLatency(int pLatency) {
      this.latency = pLatency;
   }

   public boolean m_171808_() {
      return this.m_105338_() != null;
   }

   public boolean m_105335_() {
      return this.m_105337_() != null;
   }

   public String m_105336_() {
      return this.f_105303_ == null ? DefaultPlayerSkin.m_118629_(this.profile.getId()) : this.f_105303_;
   }

   public ResourceLocation m_105337_() {
      this.m_105341_();
      return MoreObjects.firstNonNull(this.f_105299_.get(Type.SKIN), DefaultPlayerSkin.m_118627_(this.profile.getId()));
   }

   @Nullable
   public ResourceLocation m_105338_() {
      this.m_105341_();
      return this.f_105299_.get(Type.CAPE);
   }

   @Nullable
   public ResourceLocation m_105339_() {
      this.m_105341_();
      return this.f_105299_.get(Type.ELYTRA);
   }

   @Nullable
   public PlayerTeam getTeam() {
      return Minecraft.getInstance().level.getScoreboard().getPlayersTeam(this.getProfile().getName());
   }

   protected void m_105341_() {
      synchronized(this) {
         if (!this.f_105302_) {
            this.f_105302_ = true;
            Minecraft.getInstance().getSkinManager().m_118817_(this.profile, (p_105320_, p_105321_, p_105322_) -> {
               this.f_105299_.put(p_105320_, p_105321_);
               if (p_105320_ == Type.SKIN) {
                  this.f_105303_ = p_105322_.getMetadata("model");
                  if (this.f_105303_ == null) {
                     this.f_105303_ = "default";
                  }
               }

            }, true);
         }

      }
   }

   public void setTabListDisplayName(@Nullable Component pDisplayName) {
      this.tabListDisplayName = pDisplayName;
   }

   @Nullable
   public Component getTabListDisplayName() {
      return this.tabListDisplayName;
   }
}
