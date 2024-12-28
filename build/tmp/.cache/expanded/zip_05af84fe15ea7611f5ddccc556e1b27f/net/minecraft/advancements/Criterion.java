package net.minecraft.advancements;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

public class Criterion {
   @Nullable
   private final CriterionTriggerInstance trigger;

   public Criterion(CriterionTriggerInstance p_11415_) {
      this.trigger = p_11415_;
   }

   public Criterion() {
      this.trigger = null;
   }

   public void m_11423_(FriendlyByteBuf p_11424_) {
   }

   public static Criterion criterionFromJson(JsonObject pJson, DeserializationContext pContext) {
      ResourceLocation resourcelocation = new ResourceLocation(GsonHelper.getAsString(pJson, "trigger"));
      CriterionTrigger<?> criteriontrigger = CriteriaTriggers.getCriterion(resourcelocation);
      if (criteriontrigger == null) {
         throw new JsonSyntaxException("Invalid criterion trigger: " + resourcelocation);
      } else {
         CriterionTriggerInstance criteriontriggerinstance = criteriontrigger.createInstance(GsonHelper.getAsJsonObject(pJson, "conditions", new JsonObject()), pContext);
         return new Criterion(criteriontriggerinstance);
      }
   }

   public static Criterion m_11429_(FriendlyByteBuf p_11430_) {
      return new Criterion();
   }

   public static Map<String, Criterion> criteriaFromJson(JsonObject pJson, DeserializationContext pContext) {
      Map<String, Criterion> map = Maps.newHashMap();

      for(Map.Entry<String, JsonElement> entry : pJson.entrySet()) {
         map.put(entry.getKey(), criterionFromJson(GsonHelper.convertToJsonObject(entry.getValue(), "criterion"), pContext));
      }

      return map;
   }

   public static Map<String, Criterion> m_11431_(FriendlyByteBuf p_11432_) {
      return p_11432_.readMap(FriendlyByteBuf::readUtf, Criterion::m_11429_);
   }

   public static void m_11420_(Map<String, Criterion> p_11421_, FriendlyByteBuf p_11422_) {
      p_11422_.writeMap(p_11421_, FriendlyByteBuf::writeUtf, (p_145258_, p_145259_) -> {
         p_145259_.m_11423_(p_145258_);
      });
   }

   @Nullable
   public CriterionTriggerInstance m_11416_() {
      return this.trigger;
   }

   public JsonElement serializeToJson() {
      if (this.trigger == null) {
         throw new JsonSyntaxException("Missing trigger");
      } else {
         JsonObject jsonobject = new JsonObject();
         jsonobject.addProperty("trigger", this.trigger.m_7294_().toString());
         JsonObject jsonobject1 = this.trigger.serializeToJson(SerializationContext.f_64768_);
         if (jsonobject1.size() != 0) {
            jsonobject.add("conditions", jsonobject1);
         }

         return jsonobject;
      }
   }
}