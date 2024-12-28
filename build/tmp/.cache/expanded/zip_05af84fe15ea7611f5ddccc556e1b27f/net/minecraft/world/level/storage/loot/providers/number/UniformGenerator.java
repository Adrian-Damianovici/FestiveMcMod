package net.minecraft.world.level.storage.loot.providers.number;

import com.google.common.collect.Sets;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;

/**
 * Generates a random number which is uniformly distributed between a minimum and a maximum.
 * Minimum and maximum are themselves NumberProviders.
 */
public class UniformGenerator implements NumberProvider {
   final NumberProvider min;
   final NumberProvider max;

   UniformGenerator(NumberProvider p_165777_, NumberProvider p_165778_) {
      this.min = p_165777_;
      this.max = p_165778_;
   }

   public LootNumberProviderType getType() {
      return NumberProviders.UNIFORM;
   }

   public static UniformGenerator between(float pMin, float pMax) {
      return new UniformGenerator(ConstantValue.exactly(pMin), ConstantValue.exactly(pMax));
   }

   public int getInt(LootContext pLootContext) {
      return Mth.nextInt(pLootContext.getRandom(), this.min.getInt(pLootContext), this.max.getInt(pLootContext));
   }

   public float getFloat(LootContext pLootContext) {
      return Mth.nextFloat(pLootContext.getRandom(), this.min.getFloat(pLootContext), this.max.getFloat(pLootContext));
   }

   /**
    * Get the parameters used by this object.
    */
   public Set<LootContextParam<?>> getReferencedContextParams() {
      return Sets.union(this.min.getReferencedContextParams(), this.max.getReferencedContextParams());
   }

   public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<UniformGenerator> {
      public UniformGenerator m_7561_(JsonObject p_165801_, JsonDeserializationContext p_165802_) {
         NumberProvider numberprovider = GsonHelper.getAsObject(p_165801_, "min", p_165802_, NumberProvider.class);
         NumberProvider numberprovider1 = GsonHelper.getAsObject(p_165801_, "max", p_165802_, NumberProvider.class);
         return new UniformGenerator(numberprovider, numberprovider1);
      }

      public void m_6170_(JsonObject p_165793_, UniformGenerator p_165794_, JsonSerializationContext p_165795_) {
         p_165793_.add("min", p_165795_.serialize(p_165794_.min));
         p_165793_.add("max", p_165795_.serialize(p_165794_.max));
      }
   }
}