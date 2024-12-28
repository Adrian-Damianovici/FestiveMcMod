package net.minecraft.advancements.critereon;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;

public class StatePropertiesPredicate {
   public static final StatePropertiesPredicate f_67658_ = new StatePropertiesPredicate(ImmutableList.of());
   private final List<StatePropertiesPredicate.PropertyMatcher> properties;

   private static StatePropertiesPredicate.PropertyMatcher m_67686_(String p_67687_, JsonElement p_67688_) {
      if (p_67688_.isJsonPrimitive()) {
         String s2 = p_67688_.getAsString();
         return new StatePropertiesPredicate.ExactPropertyMatcher(p_67687_, s2);
      } else {
         JsonObject jsonobject = GsonHelper.convertToJsonObject(p_67688_, "value");
         String s = jsonobject.has("min") ? m_67689_(jsonobject.get("min")) : null;
         String s1 = jsonobject.has("max") ? m_67689_(jsonobject.get("max")) : null;
         return (StatePropertiesPredicate.PropertyMatcher)(s != null && s.equals(s1) ? new StatePropertiesPredicate.ExactPropertyMatcher(p_67687_, s) : new StatePropertiesPredicate.RangedPropertyMatcher(p_67687_, s, s1));
      }
   }

   @Nullable
   private static String m_67689_(JsonElement p_67690_) {
      return p_67690_.isJsonNull() ? null : p_67690_.getAsString();
   }

   StatePropertiesPredicate(List<StatePropertiesPredicate.PropertyMatcher> p_67662_) {
      this.properties = ImmutableList.copyOf(p_67662_);
   }

   public <S extends StateHolder<?, S>> boolean matches(StateDefinition<?, S> pProperties, S pTargetProperty) {
      for(StatePropertiesPredicate.PropertyMatcher statepropertiespredicate$propertymatcher : this.properties) {
         if (!statepropertiespredicate$propertymatcher.match(pProperties, pTargetProperty)) {
            return false;
         }
      }

      return true;
   }

   public boolean matches(BlockState pState) {
      return this.matches(pState.getBlock().getStateDefinition(), pState);
   }

   public boolean matches(FluidState pState) {
      return this.matches(pState.getType().getStateDefinition(), pState);
   }

   public void checkState(StateDefinition<?, ?> pProperties, Consumer<String> pPropertyConsumer) {
      this.properties.forEach((p_67678_) -> {
         p_67678_.checkState(pProperties, pPropertyConsumer);
      });
   }

   public static StatePropertiesPredicate fromJson(@Nullable JsonElement pJson) {
      if (pJson != null && !pJson.isJsonNull()) {
         JsonObject jsonobject = GsonHelper.convertToJsonObject(pJson, "properties");
         List<StatePropertiesPredicate.PropertyMatcher> list = Lists.newArrayList();

         for(Map.Entry<String, JsonElement> entry : jsonobject.entrySet()) {
            list.add(m_67686_(entry.getKey(), entry.getValue()));
         }

         return new StatePropertiesPredicate(list);
      } else {
         return f_67658_;
      }
   }

   public JsonElement serializeToJson() {
      if (this == f_67658_) {
         return JsonNull.INSTANCE;
      } else {
         JsonObject jsonobject = new JsonObject();
         if (!this.properties.isEmpty()) {
            this.properties.forEach((p_67683_) -> {
               jsonobject.add(p_67683_.m_67726_(), p_67683_.m_7682_());
            });
         }

         return jsonobject;
      }
   }

   public static class Builder {
      private final List<StatePropertiesPredicate.PropertyMatcher> matchers = Lists.newArrayList();

      private Builder() {
      }

      public static StatePropertiesPredicate.Builder properties() {
         return new StatePropertiesPredicate.Builder();
      }

      public StatePropertiesPredicate.Builder hasProperty(Property<?> pProperty, String pValue) {
         this.matchers.add(new StatePropertiesPredicate.ExactPropertyMatcher(pProperty.getName(), pValue));
         return this;
      }

      public StatePropertiesPredicate.Builder hasProperty(Property<Integer> pProperty, int pValue) {
         return this.hasProperty(pProperty, Integer.toString(pValue));
      }

      public StatePropertiesPredicate.Builder hasProperty(Property<Boolean> pProperty, boolean pValue) {
         return this.hasProperty(pProperty, Boolean.toString(pValue));
      }

      public <T extends Comparable<T> & StringRepresentable> StatePropertiesPredicate.Builder hasProperty(Property<T> pProperty, T pValue) {
         return this.hasProperty(pProperty, pValue.getSerializedName());
      }

      public StatePropertiesPredicate build() {
         return new StatePropertiesPredicate(this.matchers);
      }
   }

   static class ExactPropertyMatcher extends StatePropertiesPredicate.PropertyMatcher {
      private final String f_67707_;

      public ExactPropertyMatcher(String p_67709_, String p_67710_) {
         super(p_67709_);
         this.f_67707_ = p_67710_;
      }

      protected <T extends Comparable<T>> boolean m_7517_(StateHolder<?, ?> p_67713_, Property<T> p_67714_) {
         T t = p_67713_.getValue(p_67714_);
         Optional<T> optional = p_67714_.getValue(this.f_67707_);
         return optional.isPresent() && t.compareTo(optional.get()) == 0;
      }

      public JsonElement m_7682_() {
         return new JsonPrimitive(this.f_67707_);
      }
   }

   abstract static class PropertyMatcher {
      private final String name;

      public PropertyMatcher(String p_67717_) {
         this.name = p_67717_;
      }

      public <S extends StateHolder<?, S>> boolean match(StateDefinition<?, S> pProperties, S pPropertyToMatch) {
         Property<?> property = pProperties.getProperty(this.name);
         return property == null ? false : this.m_7517_(pPropertyToMatch, property);
      }

      protected abstract <T extends Comparable<T>> boolean m_7517_(StateHolder<?, ?> p_67724_, Property<T> p_67725_);

      public abstract JsonElement m_7682_();

      public String m_67726_() {
         return this.name;
      }

      public void checkState(StateDefinition<?, ?> pState, Consumer<String> p_67723_) {
         Property<?> property = pState.getProperty(this.name);
         if (property == null) {
            p_67723_.accept(this.name);
         }

      }
   }

   static class RangedPropertyMatcher extends StatePropertiesPredicate.PropertyMatcher {
      @Nullable
      private final String f_67727_;
      @Nullable
      private final String f_67728_;

      public RangedPropertyMatcher(String p_67730_, @Nullable String p_67731_, @Nullable String p_67732_) {
         super(p_67730_);
         this.f_67727_ = p_67731_;
         this.f_67728_ = p_67732_;
      }

      protected <T extends Comparable<T>> boolean m_7517_(StateHolder<?, ?> p_67735_, Property<T> p_67736_) {
         T t = p_67735_.getValue(p_67736_);
         if (this.f_67727_ != null) {
            Optional<T> optional = p_67736_.getValue(this.f_67727_);
            if (!optional.isPresent() || t.compareTo(optional.get()) < 0) {
               return false;
            }
         }

         if (this.f_67728_ != null) {
            Optional<T> optional1 = p_67736_.getValue(this.f_67728_);
            if (!optional1.isPresent() || t.compareTo(optional1.get()) > 0) {
               return false;
            }
         }

         return true;
      }

      public JsonElement m_7682_() {
         JsonObject jsonobject = new JsonObject();
         if (this.f_67727_ != null) {
            jsonobject.addProperty("min", this.f_67727_);
         }

         if (this.f_67728_ != null) {
            jsonobject.addProperty("max", this.f_67728_);
         }

         return jsonobject;
      }
   }
}