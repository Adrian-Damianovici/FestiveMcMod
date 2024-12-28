package net.minecraft.world.item;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.Level;

public class SuspiciousStewItem extends Item {
   public static final String EFFECTS_TAG = "Effects";
   public static final String f_151226_ = "EffectId";
   public static final String f_151227_ = "EffectDuration";
   public static final int DEFAULT_DURATION = 160;

   public SuspiciousStewItem(Item.Properties pProperties) {
      super(pProperties);
   }

   public static void m_43258_(ItemStack p_43259_, MobEffect p_43260_, int p_43261_) {
      CompoundTag compoundtag = p_43259_.getOrCreateTag();
      ListTag listtag = compoundtag.getList("Effects", 9);
      CompoundTag compoundtag1 = new CompoundTag();
      compoundtag1.putInt("EffectId", MobEffect.m_19459_(p_43260_));
      net.minecraftforge.common.ForgeHooks.saveMobEffect(compoundtag1, "forge:effect_id", p_43260_);
      compoundtag1.putInt("EffectDuration", p_43261_);
      listtag.add(compoundtag1);
      compoundtag.put("Effects", listtag);
   }

   private static void listPotionEffects(ItemStack pStack, Consumer<MobEffectInstance> pOutput) {
      CompoundTag compoundtag = pStack.getTag();
      if (compoundtag != null && compoundtag.contains("Effects", 9)) {
         ListTag listtag = compoundtag.getList("Effects", 10);

         for(int i = 0; i < listtag.size(); ++i) {
            CompoundTag compoundtag1 = listtag.getCompound(i);
            int j;
            if (compoundtag1.contains("EffectDuration", 99)) {
               j = compoundtag1.getInt("EffectDuration");
            } else {
               j = 160;
            }

            MobEffect mobeffect = MobEffect.m_19453_(compoundtag1.getInt("EffectId"));
            mobeffect = net.minecraftforge.common.ForgeHooks.loadMobEffect(compoundtag1, "forge:effect_id", mobeffect);
            if (mobeffect != null) {
               pOutput.accept(new MobEffectInstance(mobeffect, j));
            }
         }
      }

   }

   /**
    * Allows items to add custom lines of information to the mouseover description.
    */
   public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
      super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
      if (pIsAdvanced.isCreative()) {
         List<MobEffectInstance> list = new ArrayList<>();
         listPotionEffects(pStack, list::add);
         PotionUtils.addPotionTooltip(list, pTooltipComponents, 1.0F);
      }

   }

   /**
    * Called when the player finishes using this Item (E.g. finishes eating.). Not called when the player stops using
    * the Item before the action is complete.
    */
   public ItemStack finishUsingItem(ItemStack pStack, Level pLevel, LivingEntity pEntityLiving) {
      ItemStack itemstack = super.finishUsingItem(pStack, pLevel, pEntityLiving);
      listPotionEffects(itemstack, pEntityLiving::addEffect);
      return pEntityLiving instanceof Player && ((Player)pEntityLiving).getAbilities().instabuild ? itemstack : new ItemStack(Items.BOWL);
   }
}
