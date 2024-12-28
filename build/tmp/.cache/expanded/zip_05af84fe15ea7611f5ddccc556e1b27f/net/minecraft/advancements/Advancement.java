package net.minecraft.advancements;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.apache.commons.lang3.ArrayUtils;

public class Advancement {
   @Nullable
   private final Advancement parent;
   @Nullable
   private final DisplayInfo display;
   private final AdvancementRewards rewards;
   private final ResourceLocation f_138301_;
   private final Map<String, Criterion> criteria;
   private final String[][] requirements;
   private final Set<Advancement> f_138304_ = Sets.newLinkedHashSet();
   private final Component f_138305_;
   private final boolean sendsTelemetryEvent;

   public Advancement(ResourceLocation p_286878_, @Nullable Advancement p_286496_, @Nullable DisplayInfo p_286499_, AdvancementRewards pRewards, Map<String, Criterion> pCriteria, String[][] p_286882_, boolean pSendsTelemetryEvent) {
      this.f_138301_ = p_286878_;
      this.display = p_286499_;
      this.criteria = ImmutableMap.copyOf(pCriteria);
      this.parent = p_286496_;
      this.rewards = pRewards;
      this.requirements = p_286882_;
      this.sendsTelemetryEvent = pSendsTelemetryEvent;
      if (p_286496_ != null) {
         p_286496_.m_138317_(this);
      }

      if (p_286499_ == null) {
         this.f_138305_ = Component.literal(p_286878_.toString());
      } else {
         Component component = p_286499_.getTitle();
         ChatFormatting chatformatting = p_286499_.getFrame().getChatColor();
         Component component1 = ComponentUtils.mergeStyles(component.copy(), Style.EMPTY.withColor(chatformatting)).append("\n").append(p_286499_.getDescription());
         Component component2 = component.copy().withStyle((p_138316_) -> {
            return p_138316_.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, component1));
         });
         this.f_138305_ = ComponentUtils.wrapInSquareBrackets(component2).withStyle(chatformatting);
      }

   }

   public Advancement.Builder m_138313_() {
      return new Advancement.Builder(this.parent == null ? null : this.parent.m_138327_(), this.display, this.rewards, this.criteria, this.requirements, this.sendsTelemetryEvent);
   }

   @Nullable
   public Advancement m_138319_() {
      return this.parent;
   }

   public Advancement m_264348_() {
      return m_264636_(this);
   }

   public static Advancement m_264636_(Advancement p_265545_) {
      Advancement advancement = p_265545_;

      while(true) {
         Advancement advancement1 = advancement.m_138319_();
         if (advancement1 == null) {
            return advancement;
         }

         advancement = advancement1;
      }
   }

   @Nullable
   public DisplayInfo m_138320_() {
      return this.display;
   }

   public boolean m_285828_() {
      return this.sendsTelemetryEvent;
   }

   public AdvancementRewards m_138321_() {
      return this.rewards;
   }

   public String toString() {
      return "SimpleAdvancement{id=" + this.m_138327_() + ", parent=" + (this.parent == null ? "null" : this.parent.m_138327_()) + ", display=" + this.display + ", rewards=" + this.rewards + ", criteria=" + this.criteria + ", requirements=" + Arrays.deepToString(this.requirements) + ", sendsTelemetryEvent=" + this.sendsTelemetryEvent + "}";
   }

   public Iterable<Advancement> m_138322_() {
      return this.f_138304_;
   }

   public Map<String, Criterion> m_138325_() {
      return this.criteria;
   }

   public int m_138326_() {
      return this.requirements.length;
   }

   public void m_138317_(Advancement p_138318_) {
      this.f_138304_.add(p_138318_);
   }

   public ResourceLocation m_138327_() {
      return this.f_138301_;
   }

   public boolean equals(Object pOther) {
      if (this == pOther) {
         return true;
      } else if (!(pOther instanceof Advancement)) {
         return false;
      } else {
         Advancement advancement = (Advancement)pOther;
         return this.f_138301_.equals(advancement.f_138301_);
      }
   }

   public int hashCode() {
      return this.f_138301_.hashCode();
   }

   public String[][] m_138329_() {
      return this.requirements;
   }

   public Component m_138330_() {
      return this.f_138305_;
   }

   public static class Builder implements net.minecraftforge.common.extensions.IForgeAdvancementBuilder {
      @Nullable
      private ResourceLocation f_138332_;
      @Nullable
      private Advancement parent;
      @Nullable
      private DisplayInfo display;
      private AdvancementRewards rewards = AdvancementRewards.EMPTY;
      private Map<String, Criterion> criteria = Maps.newLinkedHashMap();
      @Nullable
      private String[][] requirements;
      private RequirementsStrategy requirementsStrategy = RequirementsStrategy.f_15978_;
      private final boolean sendsTelemetryEvent;

      Builder(@Nullable ResourceLocation p_286422_, @Nullable DisplayInfo p_286485_, AdvancementRewards p_286364_, Map<String, Criterion> p_286544_, String[][] p_286283_, boolean p_286626_) {
         this.f_138332_ = p_286422_;
         this.display = p_286485_;
         this.rewards = p_286364_;
         this.criteria = p_286544_;
         this.requirements = p_286283_;
         this.sendsTelemetryEvent = p_286626_;
      }

      private Builder(boolean p_286780_) {
         this.sendsTelemetryEvent = p_286780_;
      }

      public static Advancement.Builder advancement() {
         return new Advancement.Builder(true);
      }

      public static Advancement.Builder recipeAdvancement() {
         return new Advancement.Builder(false);
      }

      public Advancement.Builder parent(Advancement p_138399_) {
         this.parent = p_138399_;
         return this;
      }

      public Advancement.Builder parent(ResourceLocation pParentId) {
         this.f_138332_ = pParentId;
         return this;
      }

      public Advancement.Builder display(ItemStack pStack, Component pTitle, Component pDescription, @Nullable ResourceLocation pBackground, FrameType pFrame, boolean pShowToast, boolean pAnnounceToChat, boolean pHidden) {
         return this.display(new DisplayInfo(pStack, pTitle, pDescription, pBackground, pFrame, pShowToast, pAnnounceToChat, pHidden));
      }

      public Advancement.Builder display(ItemLike pItem, Component pTitle, Component pDescription, @Nullable ResourceLocation pBackground, FrameType pFrame, boolean pShowToast, boolean pAnnounceToChat, boolean pHidden) {
         return this.display(new DisplayInfo(new ItemStack(pItem.asItem()), pTitle, pDescription, pBackground, pFrame, pShowToast, pAnnounceToChat, pHidden));
      }

      public Advancement.Builder display(DisplayInfo pDisplay) {
         this.display = pDisplay;
         return this;
      }

      public Advancement.Builder rewards(AdvancementRewards.Builder pRewardsBuilder) {
         return this.rewards(pRewardsBuilder.build());
      }

      public Advancement.Builder rewards(AdvancementRewards pRewards) {
         this.rewards = pRewards;
         return this;
      }

      public Advancement.Builder m_138386_(String p_138387_, CriterionTriggerInstance p_138388_) {
         return this.addCriterion(p_138387_, new Criterion(p_138388_));
      }

      public Advancement.Builder addCriterion(String pKey, Criterion pCriterion) {
         if (this.criteria.containsKey(pKey)) {
            throw new IllegalArgumentException("Duplicate criterion " + pKey);
         } else {
            this.criteria.put(pKey, pCriterion);
            return this;
         }
      }

      public Advancement.Builder requirements(RequirementsStrategy p_138361_) {
         this.requirementsStrategy = p_138361_;
         return this;
      }

      public Advancement.Builder requirements(String[][] p_143952_) {
         this.requirements = p_143952_;
         return this;
      }

      public boolean m_138392_(Function<ResourceLocation, Advancement> p_138393_) {
         if (this.f_138332_ == null) {
            return true;
         } else {
            if (this.parent == null) {
               this.parent = p_138393_.apply(this.f_138332_);
            }

            return this.parent != null;
         }
      }

      public Advancement build(ResourceLocation pId) {
         if (!this.m_138392_((p_138407_) -> {
            return null;
         })) {
            throw new IllegalStateException("Tried to build incomplete advancement!");
         } else {
            if (this.requirements == null) {
               this.requirements = this.requirementsStrategy.m_15985_(this.criteria.keySet());
            }

            return new Advancement(pId, this.parent, this.display, this.rewards, this.criteria, this.requirements, this.sendsTelemetryEvent);
         }
      }

      public Advancement save(Consumer<Advancement> pOutput, String pId) {
         Advancement advancement = this.build(new ResourceLocation(pId));
         pOutput.accept(advancement);
         return advancement;
      }

      public JsonObject m_138400_() {
         if (this.requirements == null) {
            this.requirements = this.requirementsStrategy.m_15985_(this.criteria.keySet());
         }

         JsonObject jsonobject = new JsonObject();
         if (this.parent != null) {
            jsonobject.addProperty("parent", this.parent.m_138327_().toString());
         } else if (this.f_138332_ != null) {
            jsonobject.addProperty("parent", this.f_138332_.toString());
         }

         if (this.display != null) {
            jsonobject.add("display", this.display.serializeToJson());
         }

         jsonobject.add("rewards", this.rewards.serializeToJson());
         JsonObject jsonobject1 = new JsonObject();

         for(Map.Entry<String, Criterion> entry : this.criteria.entrySet()) {
            jsonobject1.add(entry.getKey(), entry.getValue().serializeToJson());
         }

         jsonobject.add("criteria", jsonobject1);
         JsonArray jsonarray1 = new JsonArray();

         for(String[] astring : this.requirements) {
            JsonArray jsonarray = new JsonArray();

            for(String s : astring) {
               jsonarray.add(s);
            }

            jsonarray1.add(jsonarray);
         }

         jsonobject.add("requirements", jsonarray1);
         jsonobject.addProperty("sends_telemetry_event", this.sendsTelemetryEvent);
         return jsonobject;
      }

      public void m_138394_(FriendlyByteBuf p_138395_) {
         if (this.requirements == null) {
            this.requirements = this.requirementsStrategy.m_15985_(this.criteria.keySet());
         }

         p_138395_.writeNullable(this.f_138332_, FriendlyByteBuf::writeResourceLocation);
         p_138395_.writeNullable(this.display, (p_214831_, p_214832_) -> {
            p_214832_.serializeToNetwork(p_214831_);
         });
         Criterion.m_11420_(this.criteria, p_138395_);
         p_138395_.writeVarInt(this.requirements.length);

         for(String[] astring : this.requirements) {
            p_138395_.writeVarInt(astring.length);

            for(String s : astring) {
               p_138395_.writeUtf(s);
            }
         }

         p_138395_.writeBoolean(this.sendsTelemetryEvent);
      }

      public String toString() {
         return "Task Advancement{parentId=" + this.f_138332_ + ", display=" + this.display + ", rewards=" + this.rewards + ", criteria=" + this.criteria + ", requirements=" + Arrays.deepToString(this.requirements) + ", sends_telemetry_event=" + this.sendsTelemetryEvent + "}";
      }

      /** @deprecated Forge: use {@linkplain #fromJson(JsonObject, DeserializationContext, net.minecraftforge.common.crafting.conditions.ICondition.IContext) overload with context}. */
      @Deprecated
      public static Advancement.Builder m_138380_(JsonObject p_138381_, DeserializationContext p_138382_) {
         return fromJson(p_138381_, p_138382_, net.minecraftforge.common.crafting.conditions.ICondition.IContext.EMPTY);
      }

      public static Advancement.Builder fromJson(JsonObject p_138381_, DeserializationContext p_138382_, net.minecraftforge.common.crafting.conditions.ICondition.IContext context) {
         if ((p_138381_ = net.minecraftforge.common.crafting.ConditionalAdvancement.processConditional(p_138381_, context)) == null) return null;
         ResourceLocation resourcelocation = p_138381_.has("parent") ? new ResourceLocation(GsonHelper.getAsString(p_138381_, "parent")) : null;
         DisplayInfo displayinfo = p_138381_.has("display") ? DisplayInfo.fromJson(GsonHelper.getAsJsonObject(p_138381_, "display")) : null;
         AdvancementRewards advancementrewards = p_138381_.has("rewards") ? AdvancementRewards.deserialize(GsonHelper.getAsJsonObject(p_138381_, "rewards")) : AdvancementRewards.EMPTY;
         Map<String, Criterion> map = Criterion.criteriaFromJson(GsonHelper.getAsJsonObject(p_138381_, "criteria"), p_138382_);
         if (map.isEmpty()) {
            throw new JsonSyntaxException("Advancement criteria cannot be empty");
         } else {
            JsonArray jsonarray = GsonHelper.getAsJsonArray(p_138381_, "requirements", new JsonArray());
            String[][] astring = new String[jsonarray.size()][];

            for(int i = 0; i < jsonarray.size(); ++i) {
               JsonArray jsonarray1 = GsonHelper.convertToJsonArray(jsonarray.get(i), "requirements[" + i + "]");
               astring[i] = new String[jsonarray1.size()];

               for(int j = 0; j < jsonarray1.size(); ++j) {
                  astring[i][j] = GsonHelper.convertToString(jsonarray1.get(j), "requirements[" + i + "][" + j + "]");
               }
            }

            if (astring.length == 0) {
               astring = new String[map.size()][];
               int k = 0;

               for(String s2 : map.keySet()) {
                  astring[k++] = new String[]{s2};
               }
            }

            for(String[] astring1 : astring) {
               if (astring1.length == 0 && map.isEmpty()) {
                  throw new JsonSyntaxException("Requirement entry cannot be empty");
               }

               for(String s : astring1) {
                  if (!map.containsKey(s)) {
                     throw new JsonSyntaxException("Unknown required criterion '" + s + "'");
                  }
               }
            }

            for(String s1 : map.keySet()) {
               boolean flag1 = false;

               for(String[] astring2 : astring) {
                  if (ArrayUtils.contains(astring2, s1)) {
                     flag1 = true;
                     break;
                  }
               }

               if (!flag1) {
                  throw new JsonSyntaxException("Criterion '" + s1 + "' isn't a requirement for completion. This isn't supported behaviour, all criteria must be required.");
               }
            }

            boolean flag = GsonHelper.getAsBoolean(p_138381_, "sends_telemetry_event", false);
            return new Advancement.Builder(resourcelocation, displayinfo, advancementrewards, map, astring, flag);
         }
      }

      public static Advancement.Builder m_138401_(FriendlyByteBuf p_138402_) {
         ResourceLocation resourcelocation = p_138402_.readNullable(FriendlyByteBuf::readResourceLocation);
         DisplayInfo displayinfo = p_138402_.readNullable(DisplayInfo::fromNetwork);
         Map<String, Criterion> map = Criterion.m_11431_(p_138402_);
         String[][] astring = new String[p_138402_.readVarInt()][];

         for(int i = 0; i < astring.length; ++i) {
            astring[i] = new String[p_138402_.readVarInt()];

            for(int j = 0; j < astring[i].length; ++j) {
               astring[i][j] = p_138402_.readUtf();
            }
         }

         boolean flag = p_138402_.readBoolean();
         return new Advancement.Builder(resourcelocation, displayinfo, AdvancementRewards.EMPTY, map, astring, flag);
      }

      public Map<String, Criterion> m_138405_() {
         return this.criteria;
      }
   }
}
