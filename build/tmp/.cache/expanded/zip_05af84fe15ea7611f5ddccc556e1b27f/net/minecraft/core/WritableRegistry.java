package net.minecraft.core;

import com.mojang.serialization.Lifecycle;
import net.minecraft.resources.ResourceKey;

public interface WritableRegistry<T> extends Registry<T> {
   Holder<T> m_203704_(int p_206368_, ResourceKey<T> p_206369_, T p_206370_, Lifecycle p_206371_);

   Holder.Reference<T> register(ResourceKey<T> pKey, T pValue, Lifecycle pLifecycle);

   boolean isEmpty();

   HolderGetter<T> createRegistrationLookup();
}