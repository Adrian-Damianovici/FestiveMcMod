package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class PlayerTrigger extends SimpleCriterionTrigger<PlayerTrigger.TriggerInstance> {
   final ResourceLocation f_222614_;

   public PlayerTrigger(ResourceLocation p_222616_) {
      this.f_222614_ = p_222616_;
   }

   public ResourceLocation m_7295_() {
      return this.f_222614_;
   }

   public PlayerTrigger.TriggerInstance createInstance(JsonObject p_286310_, ContextAwarePredicate p_286629_, DeserializationContext p_286901_) {
      return new PlayerTrigger.TriggerInstance(this.f_222614_, p_286629_);
   }

   public void trigger(ServerPlayer pPlayer) {
      this.trigger(pPlayer, (p_222625_) -> {
         return true;
      });
   }

   public static class TriggerInstance extends AbstractCriterionTriggerInstance {
      public TriggerInstance(ResourceLocation p_286413_, ContextAwarePredicate p_286749_) {
         super(p_286413_, p_286749_);
      }

      public static PlayerTrigger.TriggerInstance located(LocationPredicate p_222636_) {
         return new PlayerTrigger.TriggerInstance(CriteriaTriggers.LOCATION.f_222614_, EntityPredicate.wrap(EntityPredicate.Builder.entity().located(p_222636_).build()));
      }

      public static PlayerTrigger.TriggerInstance located(EntityPredicate p_222634_) {
         return new PlayerTrigger.TriggerInstance(CriteriaTriggers.LOCATION.f_222614_, EntityPredicate.wrap(p_222634_));
      }

      public static PlayerTrigger.TriggerInstance sleptInBed() {
         return new PlayerTrigger.TriggerInstance(CriteriaTriggers.SLEPT_IN_BED.f_222614_, ContextAwarePredicate.f_285567_);
      }

      public static PlayerTrigger.TriggerInstance raidWon() {
         return new PlayerTrigger.TriggerInstance(CriteriaTriggers.RAID_WIN.f_222614_, ContextAwarePredicate.f_285567_);
      }

      public static PlayerTrigger.TriggerInstance avoidVibration() {
         return new PlayerTrigger.TriggerInstance(CriteriaTriggers.AVOID_VIBRATION.f_222614_, ContextAwarePredicate.f_285567_);
      }

      public static PlayerTrigger.TriggerInstance tick() {
         return new PlayerTrigger.TriggerInstance(CriteriaTriggers.TICK.f_222614_, ContextAwarePredicate.f_285567_);
      }

      public static PlayerTrigger.TriggerInstance walkOnBlockWithEquipment(Block pBlock, Item pEquipment) {
         return located(EntityPredicate.Builder.entity().equipment(EntityEquipmentPredicate.Builder.equipment().feet(ItemPredicate.Builder.item().of(pEquipment).build()).build()).steppingOn(LocationPredicate.Builder.location().setBlock(BlockPredicate.Builder.block().of(pBlock).build()).build()).build());
      }
   }
}