package net.minecraft.network.protocol.handshake;

import net.minecraft.SharedConstants;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientIntentionPacket implements Packet<ServerHandshakePacketListener> {
   private static final int MAX_HOST_LENGTH = 255;
   private final int protocolVersion;
   private final String hostName;
   private final int port;
   private final ConnectionProtocol intention;
   private String fmlVersion = net.minecraftforge.network.NetworkConstants.NETVERSION;

   public ClientIntentionPacket(String p_134726_, int p_134727_, ConnectionProtocol p_134728_) {
      this.protocolVersion = SharedConstants.getCurrentVersion().getProtocolVersion();
      this.hostName = p_134726_;
      this.port = p_134727_;
      this.intention = p_134728_;
   }

   public ClientIntentionPacket(FriendlyByteBuf pBuffer) {
      this.protocolVersion = pBuffer.readVarInt();
      String hostName = pBuffer.readUtf(255);
      this.port = pBuffer.readUnsignedShort();
      this.intention = ConnectionProtocol.m_129583_(pBuffer.readVarInt());
      this.fmlVersion = net.minecraftforge.network.NetworkHooks.getFMLVersion(hostName);
      this.hostName = hostName.split("\0")[0];
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeVarInt(this.protocolVersion);
      pBuffer.writeUtf(this.hostName + "\0"+ net.minecraftforge.network.NetworkConstants.NETVERSION+"\0");
      pBuffer.writeShort(this.port);
      pBuffer.writeVarInt(this.intention.m_129582_());
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ServerHandshakePacketListener pHandler) {
      pHandler.handleIntention(this);
   }

   public ConnectionProtocol m_134735_() {
      return this.intention;
   }

   public int m_134738_() {
      return this.protocolVersion;
   }

   public String m_179802_() {
      return this.hostName;
   }

   public int m_179803_() {
      return this.port;
   }

   public String getFMLVersion() {
      return this.fmlVersion;
   }
}
