package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;

public class V2688 extends NamespacedSchema {
   public V2688(int pVersionKey, Schema pParent) {
      super(pVersionKey, pParent);
   }

   public Map<String, Supplier<TypeTemplate>> registerEntities(Schema pSchema) {
      Map<String, Supplier<TypeTemplate>> map = super.registerEntities(pSchema);
      pSchema.register(map, "minecraft:glow_squid", () -> {
         return V100.equipment(pSchema);
      });
      pSchema.register(map, "minecraft:glow_item_frame", (p_264877_) -> {
         return DSL.optionalFields("Item", References.ITEM_STACK.in(pSchema));
      });
      return map;
   }
}