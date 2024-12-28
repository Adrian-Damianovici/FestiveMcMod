package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.storage.loot.LootContext;

public class TameAnimalTrigger extends SimpleCriterionTrigger<TameAnimalTrigger.TriggerInstance> {
   static final ResourceLocation f_68825_ = new ResourceLocation("tame_animal");

   public ResourceLocation m_7295_() {
      return f_68825_;
   }

   public TameAnimalTrigger.TriggerInstance createInstance(JsonObject pJson, ContextAwarePredicate p_286765_, DeserializationContext pDeserializationContext) {
      ContextAwarePredicate contextawarepredicate = EntityPredicate.fromJson(pJson, "entity", pDeserializationContext);
      return new TameAnimalTrigger.TriggerInstance(p_286765_, contextawarepredicate);
   }

   public void trigger(ServerPlayer pPlayer, Animal pEntity) {
      LootContext lootcontext = EntityPredicate.createContext(pPlayer, pEntity);
      this.trigger(pPlayer, (p_68838_) -> {
         return p_68838_.matches(lootcontext);
      });
   }

   public static class TriggerInstance extends AbstractCriterionTriggerInstance {
      private final ContextAwarePredicate entity;

      public TriggerInstance(ContextAwarePredicate p_286593_, ContextAwarePredicate p_286484_) {
         super(TameAnimalTrigger.f_68825_, p_286593_);
         this.entity = p_286484_;
      }

      public static TameAnimalTrigger.TriggerInstance tamedAnimal() {
         return new TameAnimalTrigger.TriggerInstance(ContextAwarePredicate.f_285567_, ContextAwarePredicate.f_285567_);
      }

      public static TameAnimalTrigger.TriggerInstance tamedAnimal(EntityPredicate p_68849_) {
         return new TameAnimalTrigger.TriggerInstance(ContextAwarePredicate.f_285567_, EntityPredicate.wrap(p_68849_));
      }

      public boolean matches(LootContext pLootContext) {
         return this.entity.matches(pLootContext);
      }

      public JsonObject serializeToJson(SerializationContext p_68851_) {
         JsonObject jsonobject = super.serializeToJson(p_68851_);
         jsonobject.add("entity", this.entity.toJson(p_68851_));
         return jsonobject;
      }
   }
}