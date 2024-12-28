package net.minecraft.world.effect;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;

public class MobEffect implements net.minecraftforge.common.extensions.IForgeMobEffect {
   /** Contains a Map of the AttributeModifiers registered by potions */
   private final Map<Attribute, AttributeModifier> attributeModifiers = Maps.newHashMap();
   private final MobEffectCategory category;
   private final int color;
   @Nullable
   private String descriptionId;
   private Supplier<MobEffectInstance.FactorData> factorDataFactory = () -> {
      return null;
   };

   @Nullable
   public static MobEffect m_19453_(int p_19454_) {
      return BuiltInRegistries.MOB_EFFECT.byId(p_19454_);
   }

   public static int m_19459_(MobEffect p_19460_) {
      return BuiltInRegistries.MOB_EFFECT.getId(p_19460_);
   }

   public static int m_216882_(@Nullable MobEffect p_216883_) {
      return BuiltInRegistries.MOB_EFFECT.getId(p_216883_);
   }

   protected MobEffect(MobEffectCategory pCategory, int pColor) {
      this.category = pCategory;
      this.color = pColor;
      initClient();
   }

   public Optional<MobEffectInstance.FactorData> createFactorData() {
      return Optional.ofNullable(this.factorDataFactory.get());
   }

   public void applyEffectTick(LivingEntity pLivingEntity, int pAmplifier) {
      if (this == MobEffects.REGENERATION) {
         if (pLivingEntity.getHealth() < pLivingEntity.getMaxHealth()) {
            pLivingEntity.heal(1.0F);
         }
      } else if (this == MobEffects.POISON) {
         if (pLivingEntity.getHealth() > 1.0F) {
            pLivingEntity.hurt(pLivingEntity.damageSources().magic(), 1.0F);
         }
      } else if (this == MobEffects.WITHER) {
         pLivingEntity.hurt(pLivingEntity.damageSources().wither(), 1.0F);
      } else if (this == MobEffects.HUNGER && pLivingEntity instanceof Player) {
         ((Player)pLivingEntity).causeFoodExhaustion(0.005F * (float)(pAmplifier + 1));
      } else if (this == MobEffects.SATURATION && pLivingEntity instanceof Player) {
         if (!pLivingEntity.level().isClientSide) {
            ((Player)pLivingEntity).getFoodData().eat(pAmplifier + 1, 1.0F);
         }
      } else if ((this != MobEffects.HEAL || pLivingEntity.isInvertedHealAndHarm()) && (this != MobEffects.HARM || !pLivingEntity.isInvertedHealAndHarm())) {
         if (this == MobEffects.HARM && !pLivingEntity.isInvertedHealAndHarm() || this == MobEffects.HEAL && pLivingEntity.isInvertedHealAndHarm()) {
            pLivingEntity.hurt(pLivingEntity.damageSources().magic(), (float)(6 << pAmplifier));
         }
      } else {
         pLivingEntity.heal((float)Math.max(4 << pAmplifier, 0));
      }

   }

   public void applyInstantenousEffect(@Nullable Entity pSource, @Nullable Entity pIndirectSource, LivingEntity pLivingEntity, int pAmplifier, double pHealth) {
      if ((this != MobEffects.HEAL || pLivingEntity.isInvertedHealAndHarm()) && (this != MobEffects.HARM || !pLivingEntity.isInvertedHealAndHarm())) {
         if (this == MobEffects.HARM && !pLivingEntity.isInvertedHealAndHarm() || this == MobEffects.HEAL && pLivingEntity.isInvertedHealAndHarm()) {
            int j = (int)(pHealth * (double)(6 << pAmplifier) + 0.5D);
            if (pSource == null) {
               pLivingEntity.hurt(pLivingEntity.damageSources().magic(), (float)j);
            } else {
               pLivingEntity.hurt(pLivingEntity.damageSources().indirectMagic(pSource, pIndirectSource), (float)j);
            }
         } else {
            this.applyEffectTick(pLivingEntity, pAmplifier);
         }
      } else {
         int i = (int)(pHealth * (double)(4 << pAmplifier) + 0.5D);
         pLivingEntity.heal((float)i);
      }

   }

   public boolean m_6584_(int p_19455_, int p_19456_) {
      if (this == MobEffects.REGENERATION) {
         int k = 50 >> p_19456_;
         if (k > 0) {
            return p_19455_ % k == 0;
         } else {
            return true;
         }
      } else if (this == MobEffects.POISON) {
         int j = 25 >> p_19456_;
         if (j > 0) {
            return p_19455_ % j == 0;
         } else {
            return true;
         }
      } else if (this == MobEffects.WITHER) {
         int i = 40 >> p_19456_;
         if (i > 0) {
            return p_19455_ % i == 0;
         } else {
            return true;
         }
      } else {
         return this == MobEffects.HUNGER;
      }
   }

   /**
    * Returns {@code true} if the potion has an instant effect instead of a continuous one (e.g. Harming)
    */
   public boolean isInstantenous() {
      return false;
   }

   protected String getOrCreateDescriptionId() {
      if (this.descriptionId == null) {
         this.descriptionId = Util.makeDescriptionId("effect", BuiltInRegistries.MOB_EFFECT.getKey(this));
      }

      return this.descriptionId;
   }

   /**
    * Returns the name of the effect.
    */
   public String getDescriptionId() {
      return this.getOrCreateDescriptionId();
   }

   public Component getDisplayName() {
      return Component.translatable(this.getDescriptionId());
   }

   public MobEffectCategory getCategory() {
      return this.category;
   }

   /**
    * Returns the color of the potion liquid.
    */
   public int getColor() {
      return this.color;
   }

   /**
    * Adds an attribute modifier to this effect. This method can be called for more than one attribute. The attributes
    * are applied to an entity when the potion effect is active and removed when it stops.
    */
   public MobEffect addAttributeModifier(Attribute pAttribute, String pUuid, double pAmount, AttributeModifier.Operation pOperation) {
      AttributeModifier attributemodifier = new AttributeModifier(UUID.fromString(pUuid), this::getDescriptionId, pAmount, pOperation);
      this.attributeModifiers.put(pAttribute, attributemodifier);
      return this;
   }

   public MobEffect setFactorDataFactory(Supplier<MobEffectInstance.FactorData> pFactorDataFactory) {
      this.factorDataFactory = pFactorDataFactory;
      return this;
   }

   public Map<Attribute, AttributeModifier> getAttributeModifiers() {
      return this.attributeModifiers;
   }

   public void removeAttributeModifiers(LivingEntity p_19469_, AttributeMap pAttributeMap, int p_19471_) {
      for(Map.Entry<Attribute, AttributeModifier> entry : this.attributeModifiers.entrySet()) {
         AttributeInstance attributeinstance = pAttributeMap.getInstance(entry.getKey());
         if (attributeinstance != null) {
            attributeinstance.removeModifier(entry.getValue());
         }
      }

   }

   public void addAttributeModifiers(LivingEntity p_19478_, AttributeMap pAttributeMap, int pAmplifier) {
      for(Map.Entry<Attribute, AttributeModifier> entry : this.attributeModifiers.entrySet()) {
         AttributeInstance attributeinstance = pAttributeMap.getInstance(entry.getKey());
         if (attributeinstance != null) {
            AttributeModifier attributemodifier = entry.getValue();
            attributeinstance.removeModifier(attributemodifier);
            attributeinstance.addPermanentModifier(new AttributeModifier(attributemodifier.getId(), this.getDescriptionId() + " " + pAmplifier, this.m_7048_(pAmplifier, attributemodifier), attributemodifier.getOperation()));
         }
      }

   }

   public double m_7048_(int p_19457_, AttributeModifier p_19458_) {
      return p_19458_.getAmount() * (double)(p_19457_ + 1);
   }

   /**
    * Get if the potion is beneficial to the player. Beneficial potions are shown on the first row of the HUD
    */
   public boolean isBeneficial() {
      return this.category == MobEffectCategory.BENEFICIAL;
   }

   // FORGE START
   private Object effectRenderer;

   /*
      DO NOT CALL, IT WILL DISAPPEAR IN THE FUTURE
      Call RenderProperties.getEffectRenderer instead
    */
   public Object getEffectRendererInternal() {
      return effectRenderer;
   }

   private void initClient() {
      // Minecraft instance isn't available in datagen, so don't call initializeClient if in datagen
      if (net.minecraftforge.fml.loading.FMLEnvironment.dist == net.minecraftforge.api.distmarker.Dist.CLIENT && !net.minecraftforge.fml.loading.FMLLoader.getLaunchHandler().isData()) {
         initializeClient(properties -> {
            this.effectRenderer = properties;
         });
      }
   }

   public void initializeClient(java.util.function.Consumer<net.minecraftforge.client.extensions.common.IClientMobEffectExtensions> consumer) {
   }
   // END FORGE

}
