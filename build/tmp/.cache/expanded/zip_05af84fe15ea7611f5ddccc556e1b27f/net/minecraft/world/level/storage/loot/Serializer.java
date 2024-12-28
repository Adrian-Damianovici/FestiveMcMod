package net.minecraft.world.level.storage.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;

public interface Serializer<T> {
   void m_6170_(JsonObject p_79325_, T p_79326_, JsonSerializationContext p_79327_);

   T m_7561_(JsonObject p_79323_, JsonDeserializationContext p_79324_);
}