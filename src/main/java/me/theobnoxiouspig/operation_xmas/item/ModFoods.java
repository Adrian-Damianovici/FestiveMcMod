package me.theobnoxiouspig.operation_xmas.item;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;

public class ModFoods {
    public static final FoodProperties ICECREAM_SANDWICH = new FoodProperties.Builder().alwaysEat().nutrition(8)
            .saturationMod(2)
            .effect(() -> new MobEffectInstance(MobEffects.DIG_SPEED, 2000, 0), 0.75f)
            .build();
}
