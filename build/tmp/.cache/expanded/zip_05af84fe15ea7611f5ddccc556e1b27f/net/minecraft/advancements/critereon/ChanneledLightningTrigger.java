package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;

public class ChanneledLightningTrigger extends SimpleCriterionTrigger<ChanneledLightningTrigger.TriggerInstance> {
   static final ResourceLocation f_21714_ = new ResourceLocation("channeled_lightning");

   public ResourceLocation m_7295_() {
      return f_21714_;
   }

   public ChanneledLightningTrigger.TriggerInstance createInstance(JsonObject p_286858_, ContextAwarePredicate p_286240_, DeserializationContext p_286562_) {
      ContextAwarePredicate[] acontextawarepredicate = EntityPredicate.fromJsonArray(p_286858_, "victims", p_286562_);
      return new ChanneledLightningTrigger.TriggerInstance(p_286240_, acontextawarepredicate);
   }

   public void trigger(ServerPlayer pPlayer, Collection<? extends Entity> pEntityTriggered) {
      List<LootContext> list = pEntityTriggered.stream().map((p_21720_) -> {
         return EntityPredicate.createContext(pPlayer, p_21720_);
      }).collect(Collectors.toList());
      this.trigger(pPlayer, (p_21730_) -> {
         return p_21730_.matches(list);
      });
   }

   public static class TriggerInstance extends AbstractCriterionTriggerInstance {
      private final ContextAwarePredicate[] victims;

      public TriggerInstance(ContextAwarePredicate p_286697_, ContextAwarePredicate[] p_286366_) {
         super(ChanneledLightningTrigger.f_21714_, p_286697_);
         this.victims = p_286366_;
      }

      public static ChanneledLightningTrigger.TriggerInstance channeledLightning(EntityPredicate... p_21747_) {
         return new ChanneledLightningTrigger.TriggerInstance(ContextAwarePredicate.f_285567_, Stream.of(p_21747_).map(EntityPredicate::wrap).toArray((p_286116_) -> {
            return new ContextAwarePredicate[p_286116_];
         }));
      }

      public boolean matches(Collection<? extends LootContext> pVictims) {
         for(ContextAwarePredicate contextawarepredicate : this.victims) {
            boolean flag = false;

            for(LootContext lootcontext : pVictims) {
               if (contextawarepredicate.matches(lootcontext)) {
                  flag = true;
                  break;
               }
            }

            if (!flag) {
               return false;
            }
         }

         return true;
      }

      public JsonObject serializeToJson(SerializationContext p_21743_) {
         JsonObject jsonobject = super.serializeToJson(p_21743_);
         jsonobject.add("victims", ContextAwarePredicate.toJson(this.victims, p_21743_));
         return jsonobject;
      }
   }
}