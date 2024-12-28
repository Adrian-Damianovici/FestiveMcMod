package net.minecraft.client.player;

import com.google.common.hash.Hashing;
import com.mojang.authlib.GameProfile;
import java.io.File;
import java.util.Locale;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.HttpTexture;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractClientPlayer extends Player {
   private static final String f_172517_ = "http://skins.minecraft.net/MinecraftSkins/%s.png";
   @Nullable
   private PlayerInfo playerInfo;
   protected Vec3 deltaMovementOnPreviousTick = Vec3.ZERO;
   public float elytraRotX;
   public float elytraRotY;
   public float elytraRotZ;
   public final ClientLevel clientLevel;

   public AbstractClientPlayer(ClientLevel pClientLevel, GameProfile pGameProfile) {
      super(pClientLevel, pClientLevel.getSharedSpawnPos(), pClientLevel.getSharedSpawnAngle(), pGameProfile);
      this.clientLevel = pClientLevel;
   }

   /**
    * Returns {@code true} if the player is in spectator mode.
    */
   public boolean isSpectator() {
      PlayerInfo playerinfo = this.getPlayerInfo();
      return playerinfo != null && playerinfo.getGameMode() == GameType.SPECTATOR;
   }

   public boolean isCreative() {
      PlayerInfo playerinfo = this.getPlayerInfo();
      return playerinfo != null && playerinfo.getGameMode() == GameType.CREATIVE;
   }

   public boolean m_108555_() {
      return this.getPlayerInfo() != null;
   }

   @Nullable
   protected PlayerInfo getPlayerInfo() {
      if (this.playerInfo == null) {
         this.playerInfo = Minecraft.getInstance().getConnection().getPlayerInfo(this.getUUID());
      }

      return this.playerInfo;
   }

   /**
    * Called to update the entity's position/logic.
    */
   public void tick() {
      this.deltaMovementOnPreviousTick = this.getDeltaMovement();
      super.tick();
   }

   public Vec3 getDeltaMovementLerped(float pPatialTick) {
      return this.deltaMovementOnPreviousTick.lerp(this.getDeltaMovement(), (double)pPatialTick);
   }

   public boolean m_108559_() {
      PlayerInfo playerinfo = this.getPlayerInfo();
      return playerinfo != null && playerinfo.m_105335_();
   }

   public ResourceLocation m_108560_() {
      PlayerInfo playerinfo = this.getPlayerInfo();
      return playerinfo == null ? DefaultPlayerSkin.m_118627_(this.getUUID()) : playerinfo.m_105337_();
   }

   @Nullable
   public ResourceLocation m_108561_() {
      PlayerInfo playerinfo = this.getPlayerInfo();
      return playerinfo == null ? null : playerinfo.m_105338_();
   }

   public boolean m_108562_() {
      return this.getPlayerInfo() != null;
   }

   @Nullable
   public ResourceLocation m_108563_() {
      PlayerInfo playerinfo = this.getPlayerInfo();
      return playerinfo == null ? null : playerinfo.m_105339_();
   }

   public static void m_172521_(ResourceLocation p_172522_, String p_172523_) {
      TextureManager texturemanager = Minecraft.getInstance().getTextureManager();
      AbstractTexture abstracttexture = texturemanager.getTexture(p_172522_, MissingTextureAtlasSprite.getTexture());
      if (abstracttexture == MissingTextureAtlasSprite.getTexture()) {
         AbstractTexture httptexture = new HttpTexture((File)null, String.format(Locale.ROOT, "http://skins.minecraft.net/MinecraftSkins/%s.png", StringUtil.stripColor(p_172523_)), DefaultPlayerSkin.m_118627_(UUIDUtil.createOfflinePlayerUUID(p_172523_)), true, (Runnable)null);
         texturemanager.register(p_172522_, httptexture);
      }

   }

   public static ResourceLocation m_108556_(String p_108557_) {
      return new ResourceLocation("skins/" + Hashing.sha1().hashUnencodedChars(StringUtil.stripColor(p_108557_)));
   }

   public String m_108564_() {
      PlayerInfo playerinfo = this.getPlayerInfo();
      return playerinfo == null ? DefaultPlayerSkin.m_118629_(this.getUUID()) : playerinfo.m_105336_();
   }

   public float getFieldOfViewModifier() {
      float f = 1.0F;
      if (this.getAbilities().flying) {
         f *= 1.1F;
      }

      f *= ((float)this.getAttributeValue(Attributes.MOVEMENT_SPEED) / this.getAbilities().getWalkingSpeed() + 1.0F) / 2.0F;
      if (this.getAbilities().getWalkingSpeed() == 0.0F || Float.isNaN(f) || Float.isInfinite(f)) {
         f = 1.0F;
      }

      ItemStack itemstack = this.getUseItem();
      if (this.isUsingItem()) {
         if (itemstack.is(Items.BOW)) {
            int i = this.getTicksUsingItem();
            float f1 = (float)i / 20.0F;
            if (f1 > 1.0F) {
               f1 = 1.0F;
            } else {
               f1 *= f1;
            }

            f *= 1.0F - f1 * 0.15F;
         } else if (Minecraft.getInstance().options.getCameraType().isFirstPerson() && this.isScoping()) {
            return 0.1F;
         }
      }

      return net.minecraftforge.client.ForgeHooksClient.getFieldOfViewModifier(this, f);
   }
}
