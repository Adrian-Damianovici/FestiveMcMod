package net.minecraft.client.resources;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.hash.Hashing;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.InsecurePublicKeyException;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import com.mojang.authlib.properties.Property;
import com.mojang.blaze3d.systems.RenderSystem;
import java.io.File;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.HttpTexture;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SkinManager {
   public static final String PROPERTY_TEXTURES = "textures";
   private final TextureManager f_118807_;
   private final File f_118808_;
   private final MinecraftSessionService f_118809_;
   private final LoadingCache<String, Map<MinecraftProfileTexture.Type, MinecraftProfileTexture>> f_118810_;

   public SkinManager(TextureManager pTextureManager, File p_118813_, final MinecraftSessionService pSessionService) {
      this.f_118807_ = pTextureManager;
      this.f_118808_ = p_118813_;
      this.f_118809_ = pSessionService;
      this.f_118810_ = CacheBuilder.newBuilder().expireAfterAccess(15L, TimeUnit.SECONDS).build(new CacheLoader<String, Map<MinecraftProfileTexture.Type, MinecraftProfileTexture>>() {
         public Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> load(String p_118853_) {
            GameProfile gameprofile = new GameProfile((UUID)null, "dummy_mcdummyface");
            gameprofile.getProperties().put("textures", new Property("textures", p_118853_, ""));

            try {
               return pSessionService.getTextures(gameprofile, false);
            } catch (Throwable throwable) {
               return ImmutableMap.of();
            }
         }
      });
   }

   public ResourceLocation m_118825_(MinecraftProfileTexture p_118826_, MinecraftProfileTexture.Type p_118827_) {
      return this.m_118828_(p_118826_, p_118827_, (SkinManager.SkinTextureCallback)null);
   }

   private ResourceLocation m_118828_(MinecraftProfileTexture p_118829_, MinecraftProfileTexture.Type p_118830_, @Nullable SkinManager.SkinTextureCallback p_118831_) {
      String s = Hashing.sha1().hashUnencodedChars(p_118829_.getHash()).toString();
      ResourceLocation resourcelocation = m_242632_(p_118830_, s);
      AbstractTexture abstracttexture = this.f_118807_.getTexture(resourcelocation, MissingTextureAtlasSprite.getTexture());
      if (abstracttexture == MissingTextureAtlasSprite.getTexture()) {
         File file1 = new File(this.f_118808_, s.length() > 2 ? s.substring(0, 2) : "xx");
         File file2 = new File(file1, s);
         HttpTexture httptexture = new HttpTexture(file2, p_118829_.getUrl(), DefaultPlayerSkin.m_118626_(), p_118830_ == Type.SKIN, () -> {
            if (p_118831_ != null) {
               p_118831_.m_118856_(p_118830_, resourcelocation, p_118829_);
            }

         });
         this.f_118807_.register(resourcelocation, httptexture);
      } else if (p_118831_ != null) {
         p_118831_.m_118856_(p_118830_, resourcelocation, p_118829_);
      }

      return resourcelocation;
   }

   private static ResourceLocation m_242632_(MinecraftProfileTexture.Type p_242930_, String p_242947_) {
      String s1;
      switch (p_242930_) {
         case SKIN:
            s1 = "skins";
            break;
         case CAPE:
            s1 = "capes";
            break;
         case ELYTRA:
            s1 = "elytra";
            break;
         default:
            throw new IncompatibleClassChangeError();
      }

      String s = s1;
      return new ResourceLocation(s + "/" + p_242947_);
   }

   public void m_118817_(GameProfile p_118818_, SkinManager.SkinTextureCallback p_118819_, boolean p_118820_) {
      Runnable runnable = () -> {
         Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> map = Maps.newHashMap();

         try {
            map.putAll(this.f_118809_.getTextures(p_118818_, p_118820_));
         } catch (InsecurePublicKeyException insecurepublickeyexception1) {
         }

         if (map.isEmpty()) {
            p_118818_.getProperties().clear();
            if (p_118818_.getId().equals(Minecraft.getInstance().getUser().m_92548_().getId())) {
               p_118818_.getProperties().putAll(Minecraft.getInstance().m_91095_());
               map.putAll(this.f_118809_.getTextures(p_118818_, false));
            } else {
               this.f_118809_.fillProfileProperties(p_118818_, p_118820_);

               try {
                  map.putAll(this.f_118809_.getTextures(p_118818_, p_118820_));
               } catch (InsecurePublicKeyException insecurepublickeyexception) {
               }
            }
         }

         Minecraft.getInstance().execute(() -> {
            RenderSystem.recordRenderCall(() -> {
               ImmutableList.of(Type.SKIN, Type.CAPE).forEach((p_174848_) -> {
                  if (map.containsKey(p_174848_)) {
                     this.m_118828_(map.get(p_174848_), p_174848_, p_118819_);
                  }

               });
            });
         });
      };
      Util.backgroundExecutor().execute(runnable);
   }

   public Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> m_118815_(GameProfile p_118816_) {
      Property property = Iterables.getFirst(p_118816_.getProperties().get("textures"), (Property)null);
      return (Map<MinecraftProfileTexture.Type, MinecraftProfileTexture>)(property == null ? ImmutableMap.of() : this.f_118810_.getUnchecked(property.getValue()));
   }

   public ResourceLocation m_240306_(GameProfile p_240307_) {
      MinecraftProfileTexture minecraftprofiletexture = this.m_118815_(p_240307_).get(Type.SKIN);
      return minecraftprofiletexture != null ? this.m_118825_(minecraftprofiletexture, Type.SKIN) : DefaultPlayerSkin.m_118627_(UUIDUtil.m_235875_(p_240307_));
   }

   @OnlyIn(Dist.CLIENT)
   public interface SkinTextureCallback {
      void m_118856_(MinecraftProfileTexture.Type p_118857_, ResourceLocation p_118858_, MinecraftProfileTexture p_118859_);
   }
}