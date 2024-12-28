package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class FlowerBlock extends BushBlock implements SuspiciousEffectHolder {
   protected static final float AABB_OFFSET = 3.0F;
   protected static final VoxelShape SHAPE = Block.box(5.0D, 0.0D, 5.0D, 11.0D, 10.0D, 11.0D);
   private final MobEffect f_53508_;
   private final int f_53509_;

   private final java.util.function.Supplier<MobEffect> suspiciousStewEffectSupplier;

   public FlowerBlock(java.util.function.Supplier<MobEffect> effectSupplier, int pEffectDuration, BlockBehaviour.Properties pProperties) {
      super(pProperties);
      this.f_53508_ = null;
      this.suspiciousStewEffectSupplier = effectSupplier;
      this.f_53509_ = pEffectDuration;
   }

   /** @deprecated FORGE: Use supplier version instead */
   @Deprecated
   public FlowerBlock(MobEffect pSuspiciousStewEffect, int pEffectDuration, BlockBehaviour.Properties pProperties) {
      super(pProperties);
      this.f_53508_ = pSuspiciousStewEffect;
      if (pSuspiciousStewEffect.isInstantenous()) {
         this.f_53509_ = pEffectDuration;
      } else {
         this.f_53509_ = pEffectDuration * 20;
      }
      this.suspiciousStewEffectSupplier = net.minecraftforge.registries.ForgeRegistries.MOB_EFFECTS.getDelegateOrThrow(pSuspiciousStewEffect);

   }

   public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
      Vec3 vec3 = pState.getOffset(pLevel, pPos);
      return SHAPE.move(vec3.x, vec3.y, vec3.z);
   }

   public MobEffect m_53521_() {
      if (true) return this.suspiciousStewEffectSupplier.get();
      return this.f_53508_;
   }

   public int m_53522_() {
      if (this.f_53508_ == null && !this.suspiciousStewEffectSupplier.get().isInstantenous()) return this.f_53509_ * 20;
      return this.f_53509_;
   }
}
