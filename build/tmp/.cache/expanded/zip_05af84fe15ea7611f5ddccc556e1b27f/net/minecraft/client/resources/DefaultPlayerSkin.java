package net.minecraft.client.resources;

import java.util.UUID;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DefaultPlayerSkin {
   private static final DefaultPlayerSkin.SkinType[] DEFAULT_SKINS = new DefaultPlayerSkin.SkinType[]{new DefaultPlayerSkin.SkinType("textures/entity/player/slim/alex.png", DefaultPlayerSkin.ModelType.SLIM), new DefaultPlayerSkin.SkinType("textures/entity/player/slim/ari.png", DefaultPlayerSkin.ModelType.SLIM), new DefaultPlayerSkin.SkinType("textures/entity/player/slim/efe.png", DefaultPlayerSkin.ModelType.SLIM), new DefaultPlayerSkin.SkinType("textures/entity/player/slim/kai.png", DefaultPlayerSkin.ModelType.SLIM), new DefaultPlayerSkin.SkinType("textures/entity/player/slim/makena.png", DefaultPlayerSkin.ModelType.SLIM), new DefaultPlayerSkin.SkinType("textures/entity/player/slim/noor.png", DefaultPlayerSkin.ModelType.SLIM), new DefaultPlayerSkin.SkinType("textures/entity/player/slim/steve.png", DefaultPlayerSkin.ModelType.SLIM), new DefaultPlayerSkin.SkinType("textures/entity/player/slim/sunny.png", DefaultPlayerSkin.ModelType.SLIM), new DefaultPlayerSkin.SkinType("textures/entity/player/slim/zuri.png", DefaultPlayerSkin.ModelType.SLIM), new DefaultPlayerSkin.SkinType("textures/entity/player/wide/alex.png", DefaultPlayerSkin.ModelType.WIDE), new DefaultPlayerSkin.SkinType("textures/entity/player/wide/ari.png", DefaultPlayerSkin.ModelType.WIDE), new DefaultPlayerSkin.SkinType("textures/entity/player/wide/efe.png", DefaultPlayerSkin.ModelType.WIDE), new DefaultPlayerSkin.SkinType("textures/entity/player/wide/kai.png", DefaultPlayerSkin.ModelType.WIDE), new DefaultPlayerSkin.SkinType("textures/entity/player/wide/makena.png", DefaultPlayerSkin.ModelType.WIDE), new DefaultPlayerSkin.SkinType("textures/entity/player/wide/noor.png", DefaultPlayerSkin.ModelType.WIDE), new DefaultPlayerSkin.SkinType("textures/entity/player/wide/steve.png", DefaultPlayerSkin.ModelType.WIDE), new DefaultPlayerSkin.SkinType("textures/entity/player/wide/sunny.png", DefaultPlayerSkin.ModelType.WIDE), new DefaultPlayerSkin.SkinType("textures/entity/player/wide/zuri.png", DefaultPlayerSkin.ModelType.WIDE)};

   public static ResourceLocation m_118626_() {
      return DEFAULT_SKINS[6].f_256814_();
   }

   public static ResourceLocation m_118627_(UUID p_118628_) {
      return m_257644_(p_118628_).f_256814_;
   }

   public static String m_118629_(UUID p_118630_) {
      return m_257644_(p_118630_).f_256901_.f_256945_;
   }

   private static DefaultPlayerSkin.SkinType m_257644_(UUID p_260299_) {
      return DEFAULT_SKINS[Math.floorMod(p_260299_.hashCode(), DEFAULT_SKINS.length)];
   }

   @OnlyIn(Dist.CLIENT)
   static enum ModelType {
      SLIM("slim"),
      WIDE("default");

      final String f_256945_;

      private ModelType(String p_260160_) {
         this.f_256945_ = p_260160_;
      }
   }

   @OnlyIn(Dist.CLIENT)
   static record SkinType(ResourceLocation f_256814_, DefaultPlayerSkin.ModelType f_256901_) {
      public SkinType(String p_259984_, DefaultPlayerSkin.ModelType p_259456_) {
         this(new ResourceLocation(p_259984_), p_259456_);
      }
   }
}