package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SuspiciousStewItem;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;

/**
 * LootItemFunction that adds an effect to any suspicious stew items. A random effect is chosen from the given map every
 * time.
 */
public class SetStewEffectFunction extends LootItemConditionalFunction {
   final Map<MobEffect, NumberProvider> f_81214_;

   SetStewEffectFunction(LootItemCondition[] p_81216_, Map<MobEffect, NumberProvider> p_81217_) {
      super(p_81216_);
      this.f_81214_ = ImmutableMap.copyOf(p_81217_);
   }

   public LootItemFunctionType getType() {
      return LootItemFunctions.SET_STEW_EFFECT;
   }

   /**
    * Get the parameters used by this object.
    */
   public Set<LootContextParam<?>> getReferencedContextParams() {
      return this.f_81214_.values().stream().flatMap((p_279082_) -> {
         return p_279082_.getReferencedContextParams().stream();
      }).collect(ImmutableSet.toImmutableSet());
   }

   /**
    * Called to perform the actual action of this function, after conditions have been checked.
    */
   public ItemStack run(ItemStack pStack, LootContext pContext) {
      if (pStack.is(Items.SUSPICIOUS_STEW) && !this.f_81214_.isEmpty()) {
         RandomSource randomsource = pContext.getRandom();
         int i = randomsource.nextInt(this.f_81214_.size());
         Map.Entry<MobEffect, NumberProvider> entry = Iterables.get(this.f_81214_.entrySet(), i);
         MobEffect mobeffect = entry.getKey();
         int j = entry.getValue().getInt(pContext);
         if (!mobeffect.isInstantenous()) {
            j *= 20;
         }

         SuspiciousStewItem.m_43258_(pStack, mobeffect, j);
         return pStack;
      } else {
         return pStack;
      }
   }

   public static SetStewEffectFunction.Builder stewEffect() {
      return new SetStewEffectFunction.Builder();
   }

   public static class Builder extends LootItemConditionalFunction.Builder<SetStewEffectFunction.Builder> {
      private final Map<MobEffect, NumberProvider> f_81229_ = Maps.newLinkedHashMap();

      protected SetStewEffectFunction.Builder getThis() {
         return this;
      }

      public SetStewEffectFunction.Builder withEffect(MobEffect pEffect, NumberProvider pDurationValue) {
         this.f_81229_.put(pEffect, pDurationValue);
         return this;
      }

      public LootItemFunction build() {
         return new SetStewEffectFunction(this.getConditions(), this.f_81229_);
      }
   }

   public static class Serializer extends LootItemConditionalFunction.Serializer<SetStewEffectFunction> {
      public void m_6170_(JsonObject p_81247_, SetStewEffectFunction p_81248_, JsonSerializationContext p_81249_) {
         super.m_6170_(p_81247_, p_81248_, p_81249_);
         if (!p_81248_.f_81214_.isEmpty()) {
            JsonArray jsonarray = new JsonArray();

            for(MobEffect mobeffect : p_81248_.f_81214_.keySet()) {
               JsonObject jsonobject = new JsonObject();
               ResourceLocation resourcelocation = BuiltInRegistries.MOB_EFFECT.getKey(mobeffect);
               if (resourcelocation == null) {
                  throw new IllegalArgumentException("Don't know how to serialize mob effect " + mobeffect);
               }

               jsonobject.add("type", new JsonPrimitive(resourcelocation.toString()));
               jsonobject.add("duration", p_81249_.serialize(p_81248_.f_81214_.get(mobeffect)));
               jsonarray.add(jsonobject);
            }

            p_81247_.add("effects", jsonarray);
         }

      }

      public SetStewEffectFunction m_6821_(JsonObject p_81239_, JsonDeserializationContext p_81240_, LootItemCondition[] p_81241_) {
         Map<MobEffect, NumberProvider> map = Maps.newLinkedHashMap();
         if (p_81239_.has("effects")) {
            for(JsonElement jsonelement : GsonHelper.getAsJsonArray(p_81239_, "effects")) {
               String s = GsonHelper.getAsString(jsonelement.getAsJsonObject(), "type");
               MobEffect mobeffect = BuiltInRegistries.MOB_EFFECT.getOptional(new ResourceLocation(s)).orElseThrow(() -> {
                  return new JsonSyntaxException("Unknown mob effect '" + s + "'");
               });
               NumberProvider numberprovider = GsonHelper.getAsObject(jsonelement.getAsJsonObject(), "duration", p_81240_, NumberProvider.class);
               map.put(mobeffect, numberprovider);
            }
         }

         return new SetStewEffectFunction(p_81241_, map);
      }
   }
}