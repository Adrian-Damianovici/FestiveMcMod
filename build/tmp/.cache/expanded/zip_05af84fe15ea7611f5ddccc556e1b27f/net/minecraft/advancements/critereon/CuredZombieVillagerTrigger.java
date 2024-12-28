package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.storage.loot.LootContext;

public class CuredZombieVillagerTrigger extends SimpleCriterionTrigger<CuredZombieVillagerTrigger.TriggerInstance> {
   static final ResourceLocation f_24270_ = new ResourceLocation("cured_zombie_villager");

   public ResourceLocation m_7295_() {
      return f_24270_;
   }

   public CuredZombieVillagerTrigger.TriggerInstance createInstance(JsonObject pJson, ContextAwarePredicate p_286917_, DeserializationContext pDeserializationContext) {
      ContextAwarePredicate contextawarepredicate = EntityPredicate.fromJson(pJson, "zombie", pDeserializationContext);
      ContextAwarePredicate contextawarepredicate1 = EntityPredicate.fromJson(pJson, "villager", pDeserializationContext);
      return new CuredZombieVillagerTrigger.TriggerInstance(p_286917_, contextawarepredicate, contextawarepredicate1);
   }

   public void trigger(ServerPlayer pPlayer, Zombie pZombie, Villager pVillager) {
      LootContext lootcontext = EntityPredicate.createContext(pPlayer, pZombie);
      LootContext lootcontext1 = EntityPredicate.createContext(pPlayer, pVillager);
      this.trigger(pPlayer, (p_24285_) -> {
         return p_24285_.matches(lootcontext, lootcontext1);
      });
   }

   public static class TriggerInstance extends AbstractCriterionTriggerInstance {
      private final ContextAwarePredicate zombie;
      private final ContextAwarePredicate villager;

      public TriggerInstance(ContextAwarePredicate p_286338_, ContextAwarePredicate p_286686_, ContextAwarePredicate p_286773_) {
         super(CuredZombieVillagerTrigger.f_24270_, p_286338_);
         this.zombie = p_286686_;
         this.villager = p_286773_;
      }

      public static CuredZombieVillagerTrigger.TriggerInstance curedZombieVillager() {
         return new CuredZombieVillagerTrigger.TriggerInstance(ContextAwarePredicate.f_285567_, ContextAwarePredicate.f_285567_, ContextAwarePredicate.f_285567_);
      }

      public boolean matches(LootContext pZombie, LootContext pVillager) {
         if (!this.zombie.matches(pZombie)) {
            return false;
         } else {
            return this.villager.matches(pVillager);
         }
      }

      public JsonObject serializeToJson(SerializationContext p_24298_) {
         JsonObject jsonobject = super.serializeToJson(p_24298_);
         jsonobject.add("zombie", this.zombie.toJson(p_24298_));
         jsonobject.add("villager", this.villager.toJson(p_24298_));
         return jsonobject;
      }
   }
}