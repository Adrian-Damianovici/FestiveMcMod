package net.minecraft.world.level.storage.loot.providers.number;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.GsonAdapterFactory;
import net.minecraft.world.level.storage.loot.LootContext;

/**
 * A {@link NumberProvider} that provides a constant value.
 */
public final class ConstantValue implements NumberProvider {
   final float value;

   ConstantValue(float p_165690_) {
      this.value = p_165690_;
   }

   public LootNumberProviderType getType() {
      return NumberProviders.CONSTANT;
   }

   public float getFloat(LootContext pLootContext) {
      return this.value;
   }

   public static ConstantValue exactly(float pValue) {
      return new ConstantValue(pValue);
   }

   public boolean equals(Object pOther) {
      if (this == pOther) {
         return true;
      } else if (pOther != null && this.getClass() == pOther.getClass()) {
         return Float.compare(((ConstantValue)pOther).value, this.value) == 0;
      } else {
         return false;
      }
   }

   public int hashCode() {
      return this.value != 0.0F ? Float.floatToIntBits(this.value) : 0;
   }

   public static class InlineSerializer implements GsonAdapterFactory.InlineSerializer<ConstantValue> {
      public JsonElement m_142413_(ConstantValue p_165704_, JsonSerializationContext p_165705_) {
         return new JsonPrimitive(p_165704_.value);
      }

      public ConstantValue m_142268_(JsonElement p_165710_, JsonDeserializationContext p_165711_) {
         return new ConstantValue(GsonHelper.convertToFloat(p_165710_, "value"));
      }
   }

   public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<ConstantValue> {
      public void m_6170_(JsonObject p_165717_, ConstantValue p_165718_, JsonSerializationContext p_165719_) {
         p_165717_.addProperty("value", p_165718_.value);
      }

      public ConstantValue m_7561_(JsonObject p_165725_, JsonDeserializationContext p_165726_) {
         float f = GsonHelper.getAsFloat(p_165725_, "value");
         return new ConstantValue(f);
      }
   }
}