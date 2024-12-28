package net.minecraft.advancements.critereon;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.JsonObject;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.storage.loot.LootContext;

public class KilledByCrossbowTrigger extends SimpleCriterionTrigger<KilledByCrossbowTrigger.TriggerInstance> {
   static final ResourceLocation f_46867_ = new ResourceLocation("killed_by_crossbow");

   public ResourceLocation m_7295_() {
      return f_46867_;
   }

   public KilledByCrossbowTrigger.TriggerInstance createInstance(JsonObject p_286238_, ContextAwarePredicate p_286227_, DeserializationContext p_286919_) {
      ContextAwarePredicate[] acontextawarepredicate = EntityPredicate.fromJsonArray(p_286238_, "victims", p_286919_);
      MinMaxBounds.Ints minmaxbounds$ints = MinMaxBounds.Ints.fromJson(p_286238_.get("unique_entity_types"));
      return new KilledByCrossbowTrigger.TriggerInstance(p_286227_, acontextawarepredicate, minmaxbounds$ints);
   }

   public void trigger(ServerPlayer pPlayer, Collection<Entity> pEntities) {
      List<LootContext> list = Lists.newArrayList();
      Set<EntityType<?>> set = Sets.newHashSet();

      for(Entity entity : pEntities) {
         set.add(entity.getType());
         list.add(EntityPredicate.createContext(pPlayer, entity));
      }

      this.trigger(pPlayer, (p_46881_) -> {
         return p_46881_.matches(list, set.size());
      });
   }

   public static class TriggerInstance extends AbstractCriterionTriggerInstance {
      private final ContextAwarePredicate[] victims;
      private final MinMaxBounds.Ints uniqueEntityTypes;

      public TriggerInstance(ContextAwarePredicate p_286398_, ContextAwarePredicate[] p_286510_, MinMaxBounds.Ints pUniqueEntityTypes) {
         super(KilledByCrossbowTrigger.f_46867_, p_286398_);
         this.victims = p_286510_;
         this.uniqueEntityTypes = pUniqueEntityTypes;
      }

      public static KilledByCrossbowTrigger.TriggerInstance crossbowKilled(EntityPredicate.Builder... pVictims) {
         ContextAwarePredicate[] acontextawarepredicate = new ContextAwarePredicate[pVictims.length];

         for(int i = 0; i < pVictims.length; ++i) {
            EntityPredicate.Builder entitypredicate$builder = pVictims[i];
            acontextawarepredicate[i] = EntityPredicate.wrap(entitypredicate$builder.build());
         }

         return new KilledByCrossbowTrigger.TriggerInstance(ContextAwarePredicate.f_285567_, acontextawarepredicate, MinMaxBounds.Ints.ANY);
      }

      public static KilledByCrossbowTrigger.TriggerInstance crossbowKilled(MinMaxBounds.Ints pUniqueEntityTypes) {
         ContextAwarePredicate[] acontextawarepredicate = new ContextAwarePredicate[0];
         return new KilledByCrossbowTrigger.TriggerInstance(ContextAwarePredicate.f_285567_, acontextawarepredicate, pUniqueEntityTypes);
      }

      public boolean matches(Collection<LootContext> pContexts, int pBounds) {
         if (this.victims.length > 0) {
            List<LootContext> list = Lists.newArrayList(pContexts);

            for(ContextAwarePredicate contextawarepredicate : this.victims) {
               boolean flag = false;
               Iterator<LootContext> iterator = list.iterator();

               while(iterator.hasNext()) {
                  LootContext lootcontext = iterator.next();
                  if (contextawarepredicate.matches(lootcontext)) {
                     iterator.remove();
                     flag = true;
                     break;
                  }
               }

               if (!flag) {
                  return false;
               }
            }
         }

         return this.uniqueEntityTypes.matches(pBounds);
      }

      public JsonObject serializeToJson(SerializationContext p_46896_) {
         JsonObject jsonobject = super.serializeToJson(p_46896_);
         jsonobject.add("victims", ContextAwarePredicate.toJson(this.victims, p_46896_));
         jsonobject.add("unique_entity_types", this.uniqueEntityTypes.m_55328_());
         return jsonobject;
      }
   }
}