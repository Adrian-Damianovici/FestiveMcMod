package net.minecraft.network;

import com.mojang.logging.LogUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import java.io.IOException;
import java.util.List;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.util.profiling.jfr.JvmProfiler;
import org.slf4j.Logger;

/**
 * Main netty packet decoder. Reads the packet ID as a VarInt and creates the corresponding packet
 * based on the current {@link ConnectionProtocol}.
 */
public class PacketDecoder extends ByteToMessageDecoder {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final PacketFlow f_130530_;

   public PacketDecoder(PacketFlow p_130533_) {
      this.f_130530_ = p_130533_;
   }

   protected void decode(ChannelHandlerContext pContext, ByteBuf pIn, List<Object> pOut) throws Exception {
      int i = pIn.readableBytes();
      if (i != 0) {
         FriendlyByteBuf friendlybytebuf = new FriendlyByteBuf(pIn);
         int j = friendlybytebuf.readVarInt();
         Packet<?> packet = pContext.channel().attr(Connection.f_129461_).get().m_178321_(this.f_130530_, j, friendlybytebuf);
         if (packet == null) {
            throw new IOException("Bad packet id " + j);
         } else {
            int k = pContext.channel().attr(Connection.f_129461_).get().m_129582_();
            JvmProfiler.INSTANCE.onPacketReceived(k, j, pContext.channel().remoteAddress(), i);
            if (friendlybytebuf.readableBytes() > 0) {
               throw new IOException("Packet " + pContext.channel().attr(Connection.f_129461_).get().m_129582_() + "/" + j + " (" + packet.getClass().getSimpleName() + ") was larger than I expected, found " + friendlybytebuf.readableBytes() + " bytes extra whilst reading packet " + j);
            } else {
               pOut.add(packet);
               if (LOGGER.isDebugEnabled()) {
                  LOGGER.debug(Connection.PACKET_RECEIVED_MARKER, " IN: [{}:{}] {}", pContext.channel().attr(Connection.f_129461_).get(), j, packet.getClass().getName());
               }

            }
         }
      }
   }
}