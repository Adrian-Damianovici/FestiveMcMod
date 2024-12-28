package net.minecraft.advancements.critereon;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.slf4j.Logger;

public class LocationPredicate {
   private static final Logger f_52593_ = LogUtils.getLogger();
   public static final LocationPredicate f_52592_ = new LocationPredicate(MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, (ResourceKey<Biome>)null, (ResourceKey<Structure>)null, (ResourceKey<Level>)null, (Boolean)null, LightPredicate.f_51335_, BlockPredicate.f_17902_, FluidPredicate.f_41094_);
   private final MinMaxBounds.Doubles f_52594_;
   private final MinMaxBounds.Doubles f_52595_;
   private final MinMaxBounds.Doubles f_52596_;
   @Nullable
   private final ResourceKey<Biome> biome;
   @Nullable
   private final ResourceKey<Structure> structure;
   @Nullable
   private final ResourceKey<Level> dimension;
   @Nullable
   private final Boolean smokey;
   private final LightPredicate light;
   private final BlockPredicate block;
   private final FluidPredicate fluid;

   public LocationPredicate(MinMaxBounds.Doubles p_207916_, MinMaxBounds.Doubles p_207917_, MinMaxBounds.Doubles p_207918_, @Nullable ResourceKey<Biome> p_207919_, @Nullable ResourceKey<Structure> p_207920_, @Nullable ResourceKey<Level> p_207921_, @Nullable Boolean p_207922_, LightPredicate p_207923_, BlockPredicate p_207924_, FluidPredicate p_207925_) {
      this.f_52594_ = p_207916_;
      this.f_52595_ = p_207917_;
      this.f_52596_ = p_207918_;
      this.biome = p_207919_;
      this.structure = p_207920_;
      this.dimension = p_207921_;
      this.smokey = p_207922_;
      this.light = p_207923_;
      this.block = p_207924_;
      this.fluid = p_207925_;
   }

   public static LocationPredicate m_52634_(ResourceKey<Biome> p_52635_) {
      return new LocationPredicate(MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, p_52635_, (ResourceKey<Structure>)null, (ResourceKey<Level>)null, (Boolean)null, LightPredicate.f_51335_, BlockPredicate.f_17902_, FluidPredicate.f_41094_);
   }

   public static LocationPredicate m_52638_(ResourceKey<Level> p_52639_) {
      return new LocationPredicate(MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, (ResourceKey<Biome>)null, (ResourceKey<Structure>)null, p_52639_, (Boolean)null, LightPredicate.f_51335_, BlockPredicate.f_17902_, FluidPredicate.f_41094_);
   }

   public static LocationPredicate m_220589_(ResourceKey<Structure> p_220590_) {
      return new LocationPredicate(MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, (ResourceKey<Biome>)null, p_220590_, (ResourceKey<Level>)null, (Boolean)null, LightPredicate.f_51335_, BlockPredicate.f_17902_, FluidPredicate.f_41094_);
   }

   public static LocationPredicate m_187442_(MinMaxBounds.Doubles p_187443_) {
      return new LocationPredicate(MinMaxBounds.Doubles.ANY, p_187443_, MinMaxBounds.Doubles.ANY, (ResourceKey<Biome>)null, (ResourceKey<Structure>)null, (ResourceKey<Level>)null, (Boolean)null, LightPredicate.f_51335_, BlockPredicate.f_17902_, FluidPredicate.f_41094_);
   }

   public boolean matches(ServerLevel pLevel, double pX, double pY, double pZ) {
      if (!this.f_52594_.matches(pX)) {
         return false;
      } else if (!this.f_52595_.matches(pY)) {
         return false;
      } else if (!this.f_52596_.matches(pZ)) {
         return false;
      } else if (this.dimension != null && this.dimension != pLevel.dimension()) {
         return false;
      } else {
         BlockPos blockpos = BlockPos.containing(pX, pY, pZ);
         boolean flag = pLevel.isLoaded(blockpos);
         if (this.biome == null || flag && pLevel.getBiome(blockpos).is(this.biome)) {
            if (this.structure == null || flag && pLevel.structureManager().getStructureWithPieceAt(blockpos, this.structure).isValid()) {
               if (this.smokey == null || flag && this.smokey == CampfireBlock.isSmokeyPos(pLevel, blockpos)) {
                  if (!this.light.matches(pLevel, blockpos)) {
                     return false;
                  } else if (!this.block.matches(pLevel, blockpos)) {
                     return false;
                  } else {
                     return this.fluid.matches(pLevel, blockpos);
                  }
               } else {
                  return false;
               }
            } else {
               return false;
            }
         } else {
            return false;
         }
      }
   }

   public JsonElement serializeToJson() {
      if (this == f_52592_) {
         return JsonNull.INSTANCE;
      } else {
         JsonObject jsonobject = new JsonObject();
         if (!this.f_52594_.isAny() || !this.f_52595_.isAny() || !this.f_52596_.isAny()) {
            JsonObject jsonobject1 = new JsonObject();
            jsonobject1.add("x", this.f_52594_.m_55328_());
            jsonobject1.add("y", this.f_52595_.m_55328_());
            jsonobject1.add("z", this.f_52596_.m_55328_());
            jsonobject.add("position", jsonobject1);
         }

         if (this.dimension != null) {
            Level.RESOURCE_KEY_CODEC.encodeStart(JsonOps.INSTANCE, this.dimension).resultOrPartial(f_52593_::error).ifPresent((p_52633_) -> {
               jsonobject.add("dimension", p_52633_);
            });
         }

         if (this.structure != null) {
            jsonobject.addProperty("structure", this.structure.location().toString());
         }

         if (this.biome != null) {
            jsonobject.addProperty("biome", this.biome.location().toString());
         }

         if (this.smokey != null) {
            jsonobject.addProperty("smokey", this.smokey);
         }

         jsonobject.add("light", this.light.m_51340_());
         jsonobject.add("block", this.block.m_17913_());
         jsonobject.add("fluid", this.fluid.m_41103_());
         return jsonobject;
      }
   }

   public static LocationPredicate fromJson(@Nullable JsonElement pJson) {
      if (pJson != null && !pJson.isJsonNull()) {
         JsonObject jsonobject = GsonHelper.convertToJsonObject(pJson, "location");
         JsonObject jsonobject1 = GsonHelper.getAsJsonObject(jsonobject, "position", new JsonObject());
         MinMaxBounds.Doubles minmaxbounds$doubles = MinMaxBounds.Doubles.fromJson(jsonobject1.get("x"));
         MinMaxBounds.Doubles minmaxbounds$doubles1 = MinMaxBounds.Doubles.fromJson(jsonobject1.get("y"));
         MinMaxBounds.Doubles minmaxbounds$doubles2 = MinMaxBounds.Doubles.fromJson(jsonobject1.get("z"));
         ResourceKey<Level> resourcekey = jsonobject.has("dimension") ? ResourceLocation.CODEC.parse(JsonOps.INSTANCE, jsonobject.get("dimension")).resultOrPartial(f_52593_::error).map((p_52637_) -> {
            return ResourceKey.create(Registries.DIMENSION, p_52637_);
         }).orElse((ResourceKey<Level>)null) : null;
         ResourceKey<Structure> resourcekey1 = jsonobject.has("structure") ? ResourceLocation.CODEC.parse(JsonOps.INSTANCE, jsonobject.get("structure")).resultOrPartial(f_52593_::error).map((p_207927_) -> {
            return ResourceKey.create(Registries.STRUCTURE, p_207927_);
         }).orElse((ResourceKey<Structure>)null) : null;
         ResourceKey<Biome> resourcekey2 = null;
         if (jsonobject.has("biome")) {
            ResourceLocation resourcelocation = new ResourceLocation(GsonHelper.getAsString(jsonobject, "biome"));
            resourcekey2 = ResourceKey.create(Registries.BIOME, resourcelocation);
         }

         Boolean obool = jsonobject.has("smokey") ? jsonobject.get("smokey").getAsBoolean() : null;
         LightPredicate lightpredicate = LightPredicate.m_51344_(jsonobject.get("light"));
         BlockPredicate blockpredicate = BlockPredicate.m_17917_(jsonobject.get("block"));
         FluidPredicate fluidpredicate = FluidPredicate.m_41107_(jsonobject.get("fluid"));
         return new LocationPredicate(minmaxbounds$doubles, minmaxbounds$doubles1, minmaxbounds$doubles2, resourcekey2, resourcekey1, resourcekey, obool, lightpredicate, blockpredicate, fluidpredicate);
      } else {
         return f_52592_;
      }
   }

   public static class Builder {
      private MinMaxBounds.Doubles x = MinMaxBounds.Doubles.ANY;
      private MinMaxBounds.Doubles y = MinMaxBounds.Doubles.ANY;
      private MinMaxBounds.Doubles z = MinMaxBounds.Doubles.ANY;
      @Nullable
      private ResourceKey<Biome> biome;
      @Nullable
      private ResourceKey<Structure> structure;
      @Nullable
      private ResourceKey<Level> dimension;
      @Nullable
      private Boolean smokey;
      private LightPredicate light = LightPredicate.f_51335_;
      private BlockPredicate block = BlockPredicate.f_17902_;
      private FluidPredicate fluid = FluidPredicate.f_41094_;

      public static LocationPredicate.Builder location() {
         return new LocationPredicate.Builder();
      }

      public LocationPredicate.Builder setX(MinMaxBounds.Doubles pX) {
         this.x = pX;
         return this;
      }

      public LocationPredicate.Builder setY(MinMaxBounds.Doubles pY) {
         this.y = pY;
         return this;
      }

      public LocationPredicate.Builder setZ(MinMaxBounds.Doubles pZ) {
         this.z = pZ;
         return this;
      }

      public LocationPredicate.Builder setBiome(@Nullable ResourceKey<Biome> pBiome) {
         this.biome = pBiome;
         return this;
      }

      public LocationPredicate.Builder setStructure(@Nullable ResourceKey<Structure> pStructure) {
         this.structure = pStructure;
         return this;
      }

      public LocationPredicate.Builder setDimension(@Nullable ResourceKey<Level> pDimension) {
         this.dimension = pDimension;
         return this;
      }

      public LocationPredicate.Builder setLight(LightPredicate p_153969_) {
         this.light = p_153969_;
         return this;
      }

      public LocationPredicate.Builder setBlock(BlockPredicate p_52653_) {
         this.block = p_52653_;
         return this;
      }

      public LocationPredicate.Builder setFluid(FluidPredicate p_153967_) {
         this.fluid = p_153967_;
         return this;
      }

      public LocationPredicate.Builder setSmokey(Boolean p_52655_) {
         this.smokey = p_52655_;
         return this;
      }

      public LocationPredicate build() {
         return new LocationPredicate(this.x, this.y, this.z, this.biome, this.structure, this.dimension, this.smokey, this.light, this.block, this.fluid);
      }
   }
}