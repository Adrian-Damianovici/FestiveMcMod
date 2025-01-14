package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class BlockEntitySignDoubleSidedEditableTextFix extends NamedEntityFix {
   public BlockEntitySignDoubleSidedEditableTextFix(Schema pOutputSchema, String pName, String pEntityName) {
      super(pOutputSchema, false, pName, References.BLOCK_ENTITY, pEntityName);
   }

   private static Dynamic<?> fixTag(Dynamic<?> p_278110_) {
      String s = "black";
      Dynamic<?> dynamic = p_278110_.emptyMap();
      dynamic = dynamic.set("messages", m_277057_(p_278110_, "Text"));
      dynamic = dynamic.set("filtered_messages", m_277057_(p_278110_, "FilteredText"));
      Optional<? extends Dynamic<?>> optional = p_278110_.get("Color").result();
      dynamic = dynamic.set("color", optional.isPresent() ? optional.get() : dynamic.createString("black"));
      Optional<? extends Dynamic<?>> optional1 = p_278110_.get("GlowingText").result();
      dynamic = dynamic.set("has_glowing_text", optional1.isPresent() ? optional1.get() : dynamic.createBoolean(false));
      Dynamic<?> dynamic1 = p_278110_.emptyMap();
      Dynamic<?> dynamic2 = m_276919_(p_278110_);
      dynamic1 = dynamic1.set("messages", dynamic2);
      dynamic1 = dynamic1.set("filtered_messages", dynamic2);
      dynamic1 = dynamic1.set("color", dynamic1.createString("black"));
      dynamic1 = dynamic1.set("has_glowing_text", dynamic1.createBoolean(false));
      p_278110_ = p_278110_.set("front_text", dynamic);
      return p_278110_.set("back_text", dynamic1);
   }

   private static <T> Dynamic<T> m_277057_(Dynamic<T> p_277452_, String p_277422_) {
      Dynamic<T> dynamic = p_277452_.createString(m_277204_());
      return p_277452_.createList(Stream.of(p_277452_.get(p_277422_ + "1").result().orElse(dynamic), p_277452_.get(p_277422_ + "2").result().orElse(dynamic), p_277452_.get(p_277422_ + "3").result().orElse(dynamic), p_277452_.get(p_277422_ + "4").result().orElse(dynamic)));
   }

   private static <T> Dynamic<T> m_276919_(Dynamic<T> p_277949_) {
      Dynamic<T> dynamic = p_277949_.createString(m_277204_());
      return p_277949_.createList(Stream.of(dynamic, dynamic, dynamic, dynamic));
   }

   private static String m_277204_() {
      return Component.Serializer.toJson(CommonComponents.EMPTY);
   }

   protected Typed<?> fix(Typed<?> pTyped) {
      return pTyped.update(DSL.remainderFinder(), BlockEntitySignDoubleSidedEditableTextFix::fixTag);
   }
}