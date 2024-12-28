package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;

/**
 * LootItemFunction that adds a list of attribute modifiers to the stacks.
 */
public class SetAttributesFunction extends LootItemConditionalFunction {
   final List<SetAttributesFunction.Modifier> modifiers;

   SetAttributesFunction(LootItemCondition[] p_80833_, List<SetAttributesFunction.Modifier> p_80834_) {
      super(p_80833_);
      this.modifiers = ImmutableList.copyOf(p_80834_);
   }

   public LootItemFunctionType getType() {
      return LootItemFunctions.SET_ATTRIBUTES;
   }

   /**
    * Get the parameters used by this object.
    */
   public Set<LootContextParam<?>> getReferencedContextParams() {
      return this.modifiers.stream().flatMap((p_279080_) -> {
         return p_279080_.amount.getReferencedContextParams().stream();
      }).collect(ImmutableSet.toImmutableSet());
   }

   /**
    * Called to perform the actual action of this function, after conditions have been checked.
    */
   public ItemStack run(ItemStack pStack, LootContext pContext) {
      RandomSource randomsource = pContext.getRandom();

      for(SetAttributesFunction.Modifier setattributesfunction$modifier : this.modifiers) {
         UUID uuid = setattributesfunction$modifier.id;
         if (uuid == null) {
            uuid = UUID.randomUUID();
         }

         EquipmentSlot equipmentslot = Util.getRandom(setattributesfunction$modifier.slots, randomsource);
         pStack.addAttributeModifier(setattributesfunction$modifier.attribute, new AttributeModifier(uuid, setattributesfunction$modifier.name, (double)setattributesfunction$modifier.amount.getFloat(pContext), setattributesfunction$modifier.operation), equipmentslot);
      }

      return pStack;
   }

   public static SetAttributesFunction.ModifierBuilder modifier(String pName, Attribute p_165237_, AttributeModifier.Operation pOperation, NumberProvider pAmount) {
      return new SetAttributesFunction.ModifierBuilder(pName, p_165237_, pOperation, pAmount);
   }

   public static SetAttributesFunction.Builder setAttributes() {
      return new SetAttributesFunction.Builder();
   }

   public static class Builder extends LootItemConditionalFunction.Builder<SetAttributesFunction.Builder> {
      private final List<SetAttributesFunction.Modifier> modifiers = Lists.newArrayList();

      protected SetAttributesFunction.Builder getThis() {
         return this;
      }

      public SetAttributesFunction.Builder withModifier(SetAttributesFunction.ModifierBuilder pModifierBuilder) {
         this.modifiers.add(pModifierBuilder.build());
         return this;
      }

      public LootItemFunction build() {
         return new SetAttributesFunction(this.getConditions(), this.modifiers);
      }
   }

   static class Modifier {
      final String name;
      final Attribute attribute;
      final AttributeModifier.Operation operation;
      final NumberProvider amount;
      @Nullable
      final UUID id;
      final EquipmentSlot[] slots;

      Modifier(String p_165250_, Attribute p_165251_, AttributeModifier.Operation p_165252_, NumberProvider p_165253_, EquipmentSlot[] p_165254_, @Nullable UUID p_165255_) {
         this.name = p_165250_;
         this.attribute = p_165251_;
         this.operation = p_165252_;
         this.amount = p_165253_;
         this.id = p_165255_;
         this.slots = p_165254_;
      }

      public JsonObject m_80865_(JsonSerializationContext p_80866_) {
         JsonObject jsonobject = new JsonObject();
         jsonobject.addProperty("name", this.name);
         jsonobject.addProperty("attribute", BuiltInRegistries.ATTRIBUTE.getKey(this.attribute).toString());
         jsonobject.addProperty("operation", m_80860_(this.operation));
         jsonobject.add("amount", p_80866_.serialize(this.amount));
         if (this.id != null) {
            jsonobject.addProperty("id", this.id.toString());
         }

         if (this.slots.length == 1) {
            jsonobject.addProperty("slot", this.slots[0].getName());
         } else {
            JsonArray jsonarray = new JsonArray();

            for(EquipmentSlot equipmentslot : this.slots) {
               jsonarray.add(new JsonPrimitive(equipmentslot.getName()));
            }

            jsonobject.add("slot", jsonarray);
         }

         return jsonobject;
      }

      public static SetAttributesFunction.Modifier m_80862_(JsonObject p_80863_, JsonDeserializationContext p_80864_) {
         String s = GsonHelper.getAsString(p_80863_, "name");
         ResourceLocation resourcelocation = new ResourceLocation(GsonHelper.getAsString(p_80863_, "attribute"));
         Attribute attribute = BuiltInRegistries.ATTRIBUTE.get(resourcelocation);
         if (attribute == null) {
            throw new JsonSyntaxException("Unknown attribute: " + resourcelocation);
         } else {
            AttributeModifier.Operation attributemodifier$operation = m_80869_(GsonHelper.getAsString(p_80863_, "operation"));
            NumberProvider numberprovider = GsonHelper.getAsObject(p_80863_, "amount", p_80864_, NumberProvider.class);
            UUID uuid = null;
            EquipmentSlot[] aequipmentslot;
            if (GsonHelper.isStringValue(p_80863_, "slot")) {
               aequipmentslot = new EquipmentSlot[]{EquipmentSlot.byName(GsonHelper.getAsString(p_80863_, "slot"))};
            } else {
               if (!GsonHelper.isArrayNode(p_80863_, "slot")) {
                  throw new JsonSyntaxException("Invalid or missing attribute modifier slot; must be either string or array of strings.");
               }

               JsonArray jsonarray = GsonHelper.getAsJsonArray(p_80863_, "slot");
               aequipmentslot = new EquipmentSlot[jsonarray.size()];
               int i = 0;

               for(JsonElement jsonelement : jsonarray) {
                  aequipmentslot[i++] = EquipmentSlot.byName(GsonHelper.convertToString(jsonelement, "slot"));
               }

               if (aequipmentslot.length == 0) {
                  throw new JsonSyntaxException("Invalid attribute modifier slot; must contain at least one entry.");
               }
            }

            if (p_80863_.has("id")) {
               String s1 = GsonHelper.getAsString(p_80863_, "id");

               try {
                  uuid = UUID.fromString(s1);
               } catch (IllegalArgumentException illegalargumentexception) {
                  throw new JsonSyntaxException("Invalid attribute modifier id '" + s1 + "' (must be UUID format, with dashes)");
               }
            }

            return new SetAttributesFunction.Modifier(s, attribute, attributemodifier$operation, numberprovider, aequipmentslot, uuid);
         }
      }

      private static String m_80860_(AttributeModifier.Operation p_80861_) {
         switch (p_80861_) {
            case ADDITION:
               return "addition";
            case MULTIPLY_BASE:
               return "multiply_base";
            case MULTIPLY_TOTAL:
               return "multiply_total";
            default:
               throw new IllegalArgumentException("Unknown operation " + p_80861_);
         }
      }

      private static AttributeModifier.Operation m_80869_(String p_80870_) {
         switch (p_80870_) {
            case "addition":
               return AttributeModifier.Operation.ADDITION;
            case "multiply_base":
               return AttributeModifier.Operation.MULTIPLY_BASE;
            case "multiply_total":
               return AttributeModifier.Operation.MULTIPLY_TOTAL;
            default:
               throw new JsonSyntaxException("Unknown attribute modifier operation " + p_80870_);
         }
      }
   }

   public static class ModifierBuilder {
      private final String name;
      private final Attribute attribute;
      private final AttributeModifier.Operation operation;
      private final NumberProvider amount;
      @Nullable
      private UUID id;
      private final Set<EquipmentSlot> slots = EnumSet.noneOf(EquipmentSlot.class);

      public ModifierBuilder(String pName, Attribute p_165264_, AttributeModifier.Operation pOperation, NumberProvider pAmount) {
         this.name = pName;
         this.attribute = p_165264_;
         this.operation = pOperation;
         this.amount = pAmount;
      }

      public SetAttributesFunction.ModifierBuilder forSlot(EquipmentSlot pSlot) {
         this.slots.add(pSlot);
         return this;
      }

      public SetAttributesFunction.ModifierBuilder withUuid(UUID pId) {
         this.id = pId;
         return this;
      }

      public SetAttributesFunction.Modifier build() {
         return new SetAttributesFunction.Modifier(this.name, this.attribute, this.operation, this.amount, this.slots.toArray(new EquipmentSlot[0]), this.id);
      }
   }

   public static class Serializer extends LootItemConditionalFunction.Serializer<SetAttributesFunction> {
      public void m_6170_(JsonObject p_80891_, SetAttributesFunction p_80892_, JsonSerializationContext p_80893_) {
         super.m_6170_(p_80891_, p_80892_, p_80893_);
         JsonArray jsonarray = new JsonArray();

         for(SetAttributesFunction.Modifier setattributesfunction$modifier : p_80892_.modifiers) {
            jsonarray.add(setattributesfunction$modifier.m_80865_(p_80893_));
         }

         p_80891_.add("modifiers", jsonarray);
      }

      public SetAttributesFunction m_6821_(JsonObject p_80883_, JsonDeserializationContext p_80884_, LootItemCondition[] p_80885_) {
         JsonArray jsonarray = GsonHelper.getAsJsonArray(p_80883_, "modifiers");
         List<SetAttributesFunction.Modifier> list = Lists.newArrayListWithExpectedSize(jsonarray.size());

         for(JsonElement jsonelement : jsonarray) {
            list.add(SetAttributesFunction.Modifier.m_80862_(GsonHelper.convertToJsonObject(jsonelement, "modifier"), p_80884_));
         }

         if (list.isEmpty()) {
            throw new JsonSyntaxException("Invalid attribute modifiers array; cannot be empty");
         } else {
            return new SetAttributesFunction(p_80885_, list);
         }
      }
   }
}