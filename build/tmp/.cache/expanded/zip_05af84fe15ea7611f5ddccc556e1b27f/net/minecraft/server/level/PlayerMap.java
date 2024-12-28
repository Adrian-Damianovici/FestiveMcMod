package net.minecraft.server.level;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import java.util.Set;

public final class PlayerMap {
   private final Object2BooleanMap<ServerPlayer> players = new Object2BooleanOpenHashMap<>();

   public Set<ServerPlayer> getAllPlayers(long p_183927_) {
      return this.players.keySet();
   }

   public void addPlayer(long p_8253_, ServerPlayer pPlayer, boolean pSkipPlayer) {
      this.players.put(pPlayer, pSkipPlayer);
   }

   public void removePlayer(long p_8250_, ServerPlayer pPlayer) {
      this.players.removeBoolean(pPlayer);
   }

   public void ignorePlayer(ServerPlayer pPlayer) {
      this.players.replace(pPlayer, true);
   }

   public void unIgnorePlayer(ServerPlayer pPlayer) {
      this.players.replace(pPlayer, false);
   }

   public boolean ignoredOrUnknown(ServerPlayer pPlayer) {
      return this.players.getOrDefault(pPlayer, true);
   }

   public boolean ignored(ServerPlayer pPlayer) {
      return this.players.getBoolean(pPlayer);
   }

   public void m_8245_(long p_8246_, long p_8247_, ServerPlayer p_8248_) {
   }
}