package net.minecraft.network;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import io.netty.util.ByteProcessor;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.time.Instant;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.core.IdMap;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Crypt;
import net.minecraft.util.CryptException;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class FriendlyByteBuf extends ByteBuf implements net.minecraftforge.common.extensions.IForgeFriendlyByteBuf {
   private static final int f_178335_ = 5;
   private static final int f_178336_ = 10;
   public static final int DEFAULT_NBT_QUOTA = 2097152;
   private final ByteBuf source;
   public static final short MAX_STRING_LENGTH = Short.MAX_VALUE;
   public static final int MAX_COMPONENT_STRING_LENGTH = 262144;
   private static final int PUBLIC_KEY_SIZE = 256;
   private static final int MAX_PUBLIC_KEY_HEADER_SIZE = 256;
   private static final int MAX_PUBLIC_KEY_LENGTH = 512;
   private static final Gson GSON = new Gson();

   public FriendlyByteBuf(ByteBuf pSource) {
      this.source = pSource;
   }

   public static int m_130053_(int p_130054_) {
      for(int i = 1; i < 5; ++i) {
         if ((p_130054_ & -1 << i * 7) == 0) {
            return i;
         }
      }

      return 5;
   }

   public static int m_178339_(long p_178340_) {
      for(int i = 1; i < 10; ++i) {
         if ((p_178340_ & -1L << i * 7) == 0L) {
            return i;
         }
      }

      return 10;
   }

   /** @deprecated */
   @Deprecated
   public <T> T readWithCodec(DynamicOps<Tag> pOps, Codec<T> pCodec) {
      CompoundTag compoundtag = this.m_130261_();
      return Util.getOrThrow(pCodec.parse(pOps, compoundtag), (p_261423_) -> {
         return new DecoderException("Failed to decode: " + p_261423_ + " " + compoundtag);
      });
   }

   /** @deprecated */
   @Deprecated
   public <T> void writeWithCodec(DynamicOps<Tag> pOps, Codec<T> pCodec, T pValue) {
      Tag tag = Util.getOrThrow(pCodec.encodeStart(pOps, pValue), (p_272384_) -> {
         return new EncoderException("Failed to encode: " + p_272384_ + " " + pValue);
      });
      this.writeNbt((CompoundTag)tag);
   }

   public <T> T readJsonWithCodec(Codec<T> pCodec) {
      JsonElement jsonelement = GsonHelper.fromJson(GSON, this.readUtf(), JsonElement.class);
      DataResult<T> dataresult = pCodec.parse(JsonOps.INSTANCE, jsonelement);
      return Util.getOrThrow(dataresult, (p_272382_) -> {
         return new DecoderException("Failed to decode json: " + p_272382_);
      });
   }

   public <T> void writeJsonWithCodec(Codec<T> pCodec, T pValue) {
      DataResult<JsonElement> dataresult = pCodec.encodeStart(JsonOps.INSTANCE, pValue);
      this.writeUtf(GSON.toJson(Util.getOrThrow(dataresult, (p_261421_) -> {
         return new EncoderException("Failed to encode: " + p_261421_ + " " + pValue);
      })));
   }

   public <T> void writeId(IdMap<T> pIdMap, T pValue) {
      int i = pIdMap.getId(pValue);
      if (i == -1) {
         throw new IllegalArgumentException("Can't find id for '" + pValue + "' in map " + pIdMap);
      } else {
         this.writeVarInt(i);
      }
   }

   public <T> void writeId(IdMap<Holder<T>> pIdMap, Holder<T> pValue, FriendlyByteBuf.Writer<T> pWriter) {
      switch (pValue.kind()) {
         case REFERENCE:
            int i = pIdMap.getId(pValue);
            if (i == -1) {
               throw new IllegalArgumentException("Can't find id for '" + pValue.value() + "' in map " + pIdMap);
            }

            this.writeVarInt(i + 1);
            break;
         case DIRECT:
            this.writeVarInt(0);
            pWriter.accept(this, pValue.value());
      }

   }

   @Nullable
   public <T> T readById(IdMap<T> pIdMap) {
      int i = this.readVarInt();
      return pIdMap.byId(i);
   }

   public <T> Holder<T> readById(IdMap<Holder<T>> pIdMap, FriendlyByteBuf.Reader<T> pReader) {
      int i = this.readVarInt();
      if (i == 0) {
         return Holder.direct(pReader.apply(this));
      } else {
         Holder<T> holder = pIdMap.byId(i - 1);
         if (holder == null) {
            throw new IllegalArgumentException("Can't find element with id " + i);
         } else {
            return holder;
         }
      }
   }

   public static <T> IntFunction<T> limitValue(IntFunction<T> pFunction, int pLimit) {
      return (p_182686_) -> {
         if (p_182686_ > pLimit) {
            throw new DecoderException("Value " + p_182686_ + " is larger than limit " + pLimit);
         } else {
            return pFunction.apply(p_182686_);
         }
      };
   }

   /**
    * Read a collection from this buffer. First a new collection is created given the number of elements using {@code
    * collectionFactory}.
    * Then every element is read using {@code elementReader}.
    * 
    * @see #writeCollection
    */
   public <T, C extends Collection<T>> C readCollection(IntFunction<C> pCollectionFactory, FriendlyByteBuf.Reader<T> pElementReader) {
      int i = this.readVarInt();
      C c = pCollectionFactory.apply(i);

      for(int j = 0; j < i; ++j) {
         c.add(pElementReader.apply(this));
      }

      return c;
   }

   /**
    * Write a collection to this buffer. Every element is encoded in order using {@code elementWriter}.
    * 
    * @see #readCollection
    */
   public <T> void writeCollection(Collection<T> pCollection, FriendlyByteBuf.Writer<T> pElementWriter) {
      this.writeVarInt(pCollection.size());

      for(T t : pCollection) {
         pElementWriter.accept(this, t);
      }

   }

   /**
    * Read a List from this buffer. First a new list is created given the number of elements.
    * Then every element is read using {@code elementReader}.
    * 
    * @see #writeCollection
    */
   public <T> List<T> readList(FriendlyByteBuf.Reader<T> pElementReader) {
      return this.readCollection(Lists::newArrayListWithCapacity, pElementReader);
   }

   /**
    * Read an IntList of VarInts from this buffer.
    * 
    * @see #writeIntIdList
    */
   public IntList readIntIdList() {
      int i = this.readVarInt();
      IntList intlist = new IntArrayList();

      for(int j = 0; j < i; ++j) {
         intlist.add(this.readVarInt());
      }

      return intlist;
   }

   /**
    * Write an IntList to this buffer. Every element is encoded as a VarInt.
    * 
    * @see #readIntIdList
    */
   public void writeIntIdList(IntList pItIdList) {
      this.writeVarInt(pItIdList.size());
      pItIdList.forEach((java.util.function.IntConsumer)this::writeVarInt);
   }

   /**
    * Read a Map from this buffer. First a new Map is created given the number of elements using {@code mapFactory}.
    * Then all keys and values are read using the given {@code keyReader} and {@code valueReader}.
    * 
    * @see #writeMap
    */
   public <K, V, M extends Map<K, V>> M readMap(IntFunction<M> pMapFactory, FriendlyByteBuf.Reader<K> pKeyReader, FriendlyByteBuf.Reader<V> pValueReader) {
      int i = this.readVarInt();
      M m = pMapFactory.apply(i);

      for(int j = 0; j < i; ++j) {
         K k = pKeyReader.apply(this);
         V v = pValueReader.apply(this);
         m.put(k, v);
      }

      return m;
   }

   /**
    * Read a Map from this buffer. First a new HashMap is created.
    * Then all keys and values are read using the given {@code keyReader} and {@code valueReader}.
    * 
    * @see #writeMap
    */
   public <K, V> Map<K, V> readMap(FriendlyByteBuf.Reader<K> pKeyReader, FriendlyByteBuf.Reader<V> pValueReader) {
      return this.readMap(Maps::newHashMapWithExpectedSize, pKeyReader, pValueReader);
   }

   /**
    * Write a Map to this buffer. First the size of the map is written as a VarInt.
    * Then all keys and values are written using the given {@code keyWriter} and {@code valueWriter}.
    * 
    * @see #readMap
    */
   public <K, V> void writeMap(Map<K, V> pMap, FriendlyByteBuf.Writer<K> pKeyWriter, FriendlyByteBuf.Writer<V> pValueWriter) {
      this.writeVarInt(pMap.size());
      pMap.forEach((p_236856_, p_236857_) -> {
         pKeyWriter.accept(this, p_236856_);
         pValueWriter.accept(this, p_236857_);
      });
   }

   /**
    * Read a VarInt N from this buffer, then reads N values by calling {@code reader}.
    */
   public void readWithCount(Consumer<FriendlyByteBuf> pReader) {
      int i = this.readVarInt();

      for(int j = 0; j < i; ++j) {
         pReader.accept(this);
      }

   }

   public <E extends Enum<E>> void writeEnumSet(EnumSet<E> pEnumSet, Class<E> pEnumClass) {
      E[] ae = pEnumClass.getEnumConstants();
      BitSet bitset = new BitSet(ae.length);

      for(int i = 0; i < ae.length; ++i) {
         bitset.set(i, pEnumSet.contains(ae[i]));
      }

      this.writeFixedBitSet(bitset, ae.length);
   }

   public <E extends Enum<E>> EnumSet<E> readEnumSet(Class<E> pEnumClass) {
      E[] ae = pEnumClass.getEnumConstants();
      BitSet bitset = this.readFixedBitSet(ae.length);
      EnumSet<E> enumset = EnumSet.noneOf(pEnumClass);

      for(int i = 0; i < ae.length; ++i) {
         if (bitset.get(i)) {
            enumset.add(ae[i]);
         }
      }

      return enumset;
   }

   public <T> void writeOptional(Optional<T> pOptional, FriendlyByteBuf.Writer<T> pWriter) {
      if (pOptional.isPresent()) {
         this.writeBoolean(true);
         pWriter.accept(this, pOptional.get());
      } else {
         this.writeBoolean(false);
      }

   }

   public <T> Optional<T> readOptional(FriendlyByteBuf.Reader<T> pReader) {
      return this.readBoolean() ? Optional.of(pReader.apply(this)) : Optional.empty();
   }

   @Nullable
   public <T> T readNullable(FriendlyByteBuf.Reader<T> pReader) {
      return (T)(this.readBoolean() ? pReader.apply(this) : null);
   }

   public <T> void writeNullable(@Nullable T pValue, FriendlyByteBuf.Writer<T> pWriter) {
      if (pValue != null) {
         this.writeBoolean(true);
         pWriter.accept(this, pValue);
      } else {
         this.writeBoolean(false);
      }

   }

   public <L, R> void writeEither(Either<L, R> pValue, FriendlyByteBuf.Writer<L> pLeftWriter, FriendlyByteBuf.Writer<R> pRightWriter) {
      pValue.ifLeft((p_236867_) -> {
         this.writeBoolean(true);
         pLeftWriter.accept(this, p_236867_);
      }).ifRight((p_236852_) -> {
         this.writeBoolean(false);
         pRightWriter.accept(this, p_236852_);
      });
   }

   public <L, R> Either<L, R> readEither(FriendlyByteBuf.Reader<L> pLeftReader, FriendlyByteBuf.Reader<R> pRightReader) {
      return this.readBoolean() ? Either.left(pLeftReader.apply(this)) : Either.right(pRightReader.apply(this));
   }

   public byte[] readByteArray() {
      return this.readByteArray(this.readableBytes());
   }

   public FriendlyByteBuf writeByteArray(byte[] pArray) {
      this.writeVarInt(pArray.length);
      this.writeBytes(pArray);
      return this;
   }

   public byte[] readByteArray(int pMaxLength) {
      int i = this.readVarInt();
      if (i > pMaxLength) {
         throw new DecoderException("ByteArray with size " + i + " is bigger than allowed " + pMaxLength);
      } else {
         byte[] abyte = new byte[i];
         this.readBytes(abyte);
         return abyte;
      }
   }

   /**
    * Writes an array of VarInts to the buffer, prefixed by the length of the array (as a VarInt).
    * 
    * @see #readVarIntArray
    */
   public FriendlyByteBuf writeVarIntArray(int[] pArray) {
      this.writeVarInt(pArray.length);

      for(int i : pArray) {
         this.writeVarInt(i);
      }

      return this;
   }

   /**
    * Reads an array of VarInts from this buffer.
    * 
    * @see #writeVarIntArray
    */
   public int[] readVarIntArray() {
      return this.readVarIntArray(this.readableBytes());
   }

   /**
    * Reads an array of VarInts with a maximum length from this buffer.
    * 
    * @see #writeVarIntArray
    */
   public int[] readVarIntArray(int pMaxLength) {
      int i = this.readVarInt();
      if (i > pMaxLength) {
         throw new DecoderException("VarIntArray with size " + i + " is bigger than allowed " + pMaxLength);
      } else {
         int[] aint = new int[i];

         for(int j = 0; j < aint.length; ++j) {
            aint[j] = this.readVarInt();
         }

         return aint;
      }
   }

   /**
    * Writes an array of longs to the buffer, prefixed by the length of the array (as a VarInt).
    * 
    * @see #readLongArray
    */
   public FriendlyByteBuf writeLongArray(long[] pArray) {
      this.writeVarInt(pArray.length);

      for(long i : pArray) {
         this.writeLong(i);
      }

      return this;
   }

   /**
    * Reads a length-prefixed array of longs from the buffer.
    */
   public long[] readLongArray() {
      return this.readLongArray((long[])null);
   }

   /**
    * Reads a length-prefixed array of longs from the buffer.
    * Will try to use the given long[] if possible. Note that if an array with the correct size is given, maxLength is
    * ignored.
    */
   public long[] readLongArray(@Nullable long[] pArray) {
      return this.readLongArray(pArray, this.readableBytes() / 8);
   }

   /**
    * Reads a length-prefixed array of longs with a maximum length from the buffer.
    * Will try to use the given long[] if possible. Note that if an array with the correct size is given, maxLength is
    * ignored.
    */
   public long[] readLongArray(@Nullable long[] pArray, int pMaxLength) {
      int i = this.readVarInt();
      if (pArray == null || pArray.length != i) {
         if (i > pMaxLength) {
            throw new DecoderException("LongArray with size " + i + " is bigger than allowed " + pMaxLength);
         }

         pArray = new long[i];
      }

      for(int j = 0; j < pArray.length; ++j) {
         pArray[j] = this.readLong();
      }

      return pArray;
   }

   @VisibleForTesting
   public byte[] m_178382_() {
      int i = this.writerIndex();
      byte[] abyte = new byte[i];
      this.getBytes(0, abyte);
      return abyte;
   }

   /**
    * Reads a BlockPos encoded as a long from the buffer.
    * 
    * @see #writeBlockPos
    */
   public BlockPos readBlockPos() {
      return BlockPos.of(this.readLong());
   }

   /**
    * Writes a BlockPos encoded as a long to the buffer.
    * 
    * @see #readBlockPos
    */
   public FriendlyByteBuf writeBlockPos(BlockPos pPos) {
      this.writeLong(pPos.asLong());
      return this;
   }

   /**
    * Reads a ChunkPos encoded as a long from the buffer.
    * 
    * @see #writeChunkPos
    */
   public ChunkPos readChunkPos() {
      return new ChunkPos(this.readLong());
   }

   /**
    * Writes a ChunkPos encoded as a long to the buffer.
    * 
    * @see #readChunkPos
    */
   public FriendlyByteBuf writeChunkPos(ChunkPos pChunkPos) {
      this.writeLong(pChunkPos.toLong());
      return this;
   }

   /**
    * Reads a SectionPos encoded as a long from the buffer.
    * 
    * @see #writeSectionPos
    */
   public SectionPos readSectionPos() {
      return SectionPos.of(this.readLong());
   }

   /**
    * Writes a SectionPos encoded as a long to the buffer.
    * 
    * @see #readSectionPos
    */
   public FriendlyByteBuf writeSectionPos(SectionPos pSectionPos) {
      this.writeLong(pSectionPos.asLong());
      return this;
   }

   public GlobalPos readGlobalPos() {
      ResourceKey<Level> resourcekey = this.readResourceKey(Registries.DIMENSION);
      BlockPos blockpos = this.readBlockPos();
      return GlobalPos.of(resourcekey, blockpos);
   }

   public void writeGlobalPos(GlobalPos pPos) {
      this.writeResourceKey(pPos.dimension());
      this.writeBlockPos(pPos.pos());
   }

   public Vector3f readVector3f() {
      return new Vector3f(this.readFloat(), this.readFloat(), this.readFloat());
   }

   public void writeVector3f(Vector3f pVector3f) {
      this.writeFloat(pVector3f.x());
      this.writeFloat(pVector3f.y());
      this.writeFloat(pVector3f.z());
   }

   public Quaternionf readQuaternion() {
      return new Quaternionf(this.readFloat(), this.readFloat(), this.readFloat(), this.readFloat());
   }

   public void writeQuaternion(Quaternionf pQuaternion) {
      this.writeFloat(pQuaternion.x);
      this.writeFloat(pQuaternion.y);
      this.writeFloat(pQuaternion.z);
      this.writeFloat(pQuaternion.w);
   }

   /**
    * Reads a Component encoded as a JSON string from the buffer.
    * 
    * @see #writeComponent
    */
   public Component readComponent() {
      Component component = Component.Serializer.fromJson(this.readUtf(262144));
      if (component == null) {
         throw new DecoderException("Received unexpected null component");
      } else {
         return component;
      }
   }

   /**
    * Writes a Component encoded as a JSON string to the buffer.
    * 
    * @see #readComponent
    */
   public FriendlyByteBuf writeComponent(Component pComponent) {
      return this.writeUtf(Component.Serializer.toJson(pComponent), 262144);
   }

   /**
    * Reads an enum of the given type T using the ordinal encoded as a VarInt from the buffer.
    * 
    * @see #writeEnum
    */
   public <T extends Enum<T>> T readEnum(Class<T> pEnumClass) {
      return (pEnumClass.getEnumConstants())[this.readVarInt()];
   }

   /**
    * Writes an enum of the given type T using the ordinal encoded as a VarInt to the buffer.
    * 
    * @see #readEnum
    */
   public FriendlyByteBuf writeEnum(Enum<?> pValue) {
      return this.writeVarInt(pValue.ordinal());
   }

   /**
    * Reads a compressed int from the buffer. To do so it maximally reads 5 byte-sized chunks whose most significant bit
    * dictates whether another byte should be read.
    * 
    * @see #writeVarInt
    */
   public int readVarInt() {
      int i = 0;
      int j = 0;

      byte b0;
      do {
         b0 = this.readByte();
         i |= (b0 & 127) << j++ * 7;
         if (j > 5) {
            throw new RuntimeException("VarInt too big");
         }
      } while((b0 & 128) == 128);

      return i;
   }

   /**
    * Reads a compressed long from the buffer. To do so it maximally reads 10 byte-sized chunks whose most significant
    * bit dictates whether another byte should be read.
    * 
    * @see #writeVarLong
    */
   public long readVarLong() {
      long i = 0L;
      int j = 0;

      byte b0;
      do {
         b0 = this.readByte();
         i |= (long)(b0 & 127) << j++ * 7;
         if (j > 10) {
            throw new RuntimeException("VarLong too big");
         }
      } while((b0 & 128) == 128);

      return i;
   }

   /**
    * Writes a UUID encoded as two longs to this buffer.
    * 
    * @see #readUUID
    */
   public FriendlyByteBuf writeUUID(UUID pUuid) {
      this.writeLong(pUuid.getMostSignificantBits());
      this.writeLong(pUuid.getLeastSignificantBits());
      return this;
   }

   /**
    * Reads a UUID encoded as two longs from this buffer.
    * 
    * @see #writeUUID
    */
   public UUID readUUID() {
      return new UUID(this.readLong(), this.readLong());
   }

   /**
    * Writes a compressed int to the buffer. The smallest number of bytes to fit the passed int will be written. Of each
    * such byte only 7 bits will be used to describe the actual value since its most significant bit dictates whether
    * the next byte is part of that same int. Micro-optimization for int values that are usually small.
    */
   public FriendlyByteBuf writeVarInt(int p_130131_) {
      while((p_130131_ & -128) != 0) {
         this.writeByte(p_130131_ & 127 | 128);
         p_130131_ >>>= 7;
      }

      this.writeByte(p_130131_);
      return this;
   }

   /**
    * Writes a compressed long to the buffer. The smallest number of bytes to fit the passed long will be written. Of
    * each such byte only 7 bits will be used to describe the actual value since its most significant bit dictates
    * whether the next byte is part of that same long. Micro-optimization for long values that are usually small.
    */
   public FriendlyByteBuf writeVarLong(long pValue) {
      while((pValue & -128L) != 0L) {
         this.writeByte((int)(pValue & 127L) | 128);
         pValue >>>= 7;
      }

      this.writeByte((int)pValue);
      return this;
   }

   public FriendlyByteBuf writeNbt(@Nullable CompoundTag p_130080_) {
      if (p_130080_ == null) {
         this.writeByte(0);
      } else {
         try {
            NbtIo.write(p_130080_, new ByteBufOutputStream(this));
         } catch (IOException ioexception) {
            throw new EncoderException(ioexception);
         }
      }

      return this;
   }

   /**
    * Reads a NBT CompoundTag from this buffer.
    * {@code null} is a valid value and may be returned.
    * 
    * This method will read a maximum of 0x200000 bytes.
    * 
    * @see #writeNbt
    * @see #readAnySizeNbt
    * @see #readNbt(NbtAccounter)
    */
   @Nullable
   public CompoundTag readNbt() {
      return this.readNbt(new NbtAccounter(2097152L));
   }

   @Nullable
   public CompoundTag m_130261_() {
      return this.readNbt(NbtAccounter.f_128917_);
   }

   @Nullable
   public CompoundTag readNbt(NbtAccounter pNbtAccounter) {
      int i = this.readerIndex();
      byte b0 = this.readByte();
      if (b0 == 0) {
         return null;
      } else {
         this.readerIndex(i);

         try {
            return NbtIo.read(new ByteBufInputStream(this), pNbtAccounter);
         } catch (IOException ioexception) {
            throw new EncoderException(ioexception);
         }
      }
   }

   /**
    * Writes an ItemStack to this buffer.
    * 
    * @see #readItem
    */
   public FriendlyByteBuf writeItem(ItemStack pStack) {
      return writeItemStack(pStack, true);
   }

   /**
    * Most ItemStack serialization is Server to Client,and doesn't need to know the FULL tag details.
    * One exception is items from the creative menu, which must be sent from Client to Server with their full NBT.
    * If you want to send the FULL tag set limitedTag to false
    */
   public FriendlyByteBuf writeItemStack(ItemStack pStack, boolean limitedTag) {
      if (pStack.isEmpty()) {
         this.writeBoolean(false);
      } else {
         this.writeBoolean(true);
         Item item = pStack.getItem();
         this.writeId(BuiltInRegistries.ITEM, item);
         this.writeByte(pStack.getCount());
         CompoundTag compoundtag = null;
         if (item.isDamageable(pStack) || item.shouldOverrideMultiplayerNbt()) {
            compoundtag = limitedTag ? pStack.getShareTag() : pStack.getTag();
         }

         this.writeNbt(compoundtag);
      }

      return this;
   }

   /**
    * Reads an ItemStack from this buffer.
    * 
    * @see #writeItem
    */
   public ItemStack readItem() {
      if (!this.readBoolean()) {
         return ItemStack.EMPTY;
      } else {
         Item item = this.readById(BuiltInRegistries.ITEM);
         int i = this.readByte();
         ItemStack itemstack = new ItemStack(item, i);
         itemstack.readShareTag(this.readNbt());
         return itemstack;
      }
   }

   /**
    * Reads a String with a maximum length of {@code Short.MAX_VALUE}.
    * 
    * @see #readUtf(int)
    * @see #writeUtf
    */
   public String readUtf() {
      return this.readUtf(32767);
   }

   /**
    * Reads a string with a maximum length from this buffer.
    * 
    * @see #writeUtf
    */
   public String readUtf(int pMaxLength) {
      int i = m_236870_(pMaxLength);
      int j = this.readVarInt();
      if (j > i) {
         throw new DecoderException("The received encoded string buffer length is longer than maximum allowed (" + j + " > " + i + ")");
      } else if (j < 0) {
         throw new DecoderException("The received encoded string buffer length is less than zero! Weird string!");
      } else {
         String s = this.toString(this.readerIndex(), j, StandardCharsets.UTF_8);
         this.readerIndex(this.readerIndex() + j);
         if (s.length() > pMaxLength) {
            throw new DecoderException("The received string length is longer than maximum allowed (" + s.length() + " > " + pMaxLength + ")");
         } else {
            return s;
         }
      }
   }

   /**
    * Writes a String with a maximum length of {@code Short.MAX_VALUE}.
    * 
    * @see #readUtf
    */
   public FriendlyByteBuf writeUtf(String p_130071_) {
      return this.writeUtf(p_130071_, 32767);
   }

   /**
    * Writes a String with a maximum length.
    * 
    * @see #readUtf
    */
   public FriendlyByteBuf writeUtf(String pString, int pMaxLength) {
      if (pString.length() > pMaxLength) {
         throw new EncoderException("String too big (was " + pString.length() + " characters, max " + pMaxLength + ")");
      } else {
         byte[] abyte = pString.getBytes(StandardCharsets.UTF_8);
         int i = m_236870_(pMaxLength);
         if (abyte.length > i) {
            throw new EncoderException("String too big (was " + abyte.length + " bytes encoded, max " + i + ")");
         } else {
            this.writeVarInt(abyte.length);
            this.writeBytes(abyte);
            return this;
         }
      }
   }

   private static int m_236870_(int p_236871_) {
      return p_236871_ * 3;
   }

   /**
    * Read a ResourceLocation using its String representation.
    * 
    * @see #writeResourceLocation
    */
   public ResourceLocation readResourceLocation() {
      return new ResourceLocation(this.readUtf(32767));
   }

   /**
    * Write a ResourceLocation using its String representation.
    * 
    * @see #readResourceLocation
    */
   public FriendlyByteBuf writeResourceLocation(ResourceLocation pResourceLocation) {
      this.writeUtf(pResourceLocation.toString());
      return this;
   }

   public <T> ResourceKey<T> readResourceKey(ResourceKey<? extends Registry<T>> pRegistryKey) {
      ResourceLocation resourcelocation = this.readResourceLocation();
      return ResourceKey.create(pRegistryKey, resourcelocation);
   }

   public void writeResourceKey(ResourceKey<?> pResourceKey) {
      this.writeResourceLocation(pResourceKey.location());
   }

   /**
    * Read a timestamp as milliseconds since the unix epoch.
    * 
    * @see #writeDate
    */
   public Date readDate() {
      return new Date(this.readLong());
   }

   /**
    * Write a timestamp as milliseconds since the unix epoch.
    * 
    * @see #readDate
    */
   public FriendlyByteBuf writeDate(Date pTime) {
      this.writeLong(pTime.getTime());
      return this;
   }

   public Instant readInstant() {
      return Instant.ofEpochMilli(this.readLong());
   }

   public void writeInstant(Instant pInstant) {
      this.writeLong(pInstant.toEpochMilli());
   }

   public PublicKey readPublicKey() {
      try {
         return Crypt.byteToPublicKey(this.readByteArray(512));
      } catch (CryptException cryptexception) {
         throw new DecoderException("Malformed public key bytes", cryptexception);
      }
   }

   public FriendlyByteBuf writePublicKey(PublicKey pPublicKey) {
      this.writeByteArray(pPublicKey.getEncoded());
      return this;
   }

   /**
    * Read a BlockHitResult.
    * 
    * @see #writeBlockHitResult
    */
   public BlockHitResult readBlockHitResult() {
      BlockPos blockpos = this.readBlockPos();
      Direction direction = this.readEnum(Direction.class);
      float f = this.readFloat();
      float f1 = this.readFloat();
      float f2 = this.readFloat();
      boolean flag = this.readBoolean();
      return new BlockHitResult(new Vec3((double)blockpos.getX() + (double)f, (double)blockpos.getY() + (double)f1, (double)blockpos.getZ() + (double)f2), direction, blockpos, flag);
   }

   /**
    * Write a BlockHitResult.
    * 
    * @see #readBlockHitResult
    */
   public void writeBlockHitResult(BlockHitResult pResult) {
      BlockPos blockpos = pResult.getBlockPos();
      this.writeBlockPos(blockpos);
      this.writeEnum(pResult.getDirection());
      Vec3 vec3 = pResult.getLocation();
      this.writeFloat((float)(vec3.x - (double)blockpos.getX()));
      this.writeFloat((float)(vec3.y - (double)blockpos.getY()));
      this.writeFloat((float)(vec3.z - (double)blockpos.getZ()));
      this.writeBoolean(pResult.isInside());
   }

   /**
    * Read a BitSet as a long[].
    * 
    * @see #writeBitSet
    */
   public BitSet readBitSet() {
      return BitSet.valueOf(this.readLongArray());
   }

   /**
    * Write a BitSet as a long[].
    * 
    * @see #readBitSet
    */
   public void writeBitSet(BitSet pBitSet) {
      this.writeLongArray(pBitSet.toLongArray());
   }

   public BitSet readFixedBitSet(int pSize) {
      byte[] abyte = new byte[Mth.positiveCeilDiv(pSize, 8)];
      this.readBytes(abyte);
      return BitSet.valueOf(abyte);
   }

   public void writeFixedBitSet(BitSet pBitSet, int pSize) {
      if (pBitSet.length() > pSize) {
         throw new EncoderException("BitSet is larger than expected size (" + pBitSet.length() + ">" + pSize + ")");
      } else {
         byte[] abyte = pBitSet.toByteArray();
         this.writeBytes(Arrays.copyOf(abyte, Mth.positiveCeilDiv(pSize, 8)));
      }
   }

   public GameProfile readGameProfile() {
      UUID uuid = this.readUUID();
      String s = this.readUtf(16);
      GameProfile gameprofile = new GameProfile(uuid, s);
      gameprofile.getProperties().putAll(this.readGameProfileProperties());
      return gameprofile;
   }

   public void writeGameProfile(GameProfile pGameProfile) {
      this.writeUUID(pGameProfile.getId());
      this.writeUtf(pGameProfile.getName());
      this.writeGameProfileProperties(pGameProfile.getProperties());
   }

   public PropertyMap readGameProfileProperties() {
      PropertyMap propertymap = new PropertyMap();
      this.readWithCount((p_236809_) -> {
         Property property = this.readProperty();
         propertymap.put(property.getName(), property);
      });
      return propertymap;
   }

   public void writeGameProfileProperties(PropertyMap pGameProfileProperties) {
      this.writeCollection(pGameProfileProperties.values(), FriendlyByteBuf::writeProperty);
   }

   public Property readProperty() {
      String s = this.readUtf();
      String s1 = this.readUtf();
      if (this.readBoolean()) {
         String s2 = this.readUtf();
         return new Property(s, s1, s2);
      } else {
         return new Property(s, s1);
      }
   }

   public void writeProperty(Property p_236806_) {
      this.writeUtf(p_236806_.getName());
      this.writeUtf(p_236806_.getValue());
      if (p_236806_.hasSignature()) {
         this.writeBoolean(true);
         this.writeUtf(p_236806_.getSignature());
      } else {
         this.writeBoolean(false);
      }

   }

   public int capacity() {
      return this.source.capacity();
   }

   public ByteBuf capacity(int p_130120_) {
      return this.source.capacity(p_130120_);
   }

   public int maxCapacity() {
      return this.source.maxCapacity();
   }

   public ByteBufAllocator alloc() {
      return this.source.alloc();
   }

   public ByteOrder order() {
      return this.source.order();
   }

   public ByteBuf order(ByteOrder pEndianness) {
      return this.source.order(pEndianness);
   }

   public ByteBuf unwrap() {
      return this.source.unwrap();
   }

   public boolean isDirect() {
      return this.source.isDirect();
   }

   public boolean isReadOnly() {
      return this.source.isReadOnly();
   }

   public ByteBuf asReadOnly() {
      return this.source.asReadOnly();
   }

   public int readerIndex() {
      return this.source.readerIndex();
   }

   public ByteBuf readerIndex(int p_130343_) {
      return this.source.readerIndex(p_130343_);
   }

   public int writerIndex() {
      return this.source.writerIndex();
   }

   public ByteBuf writerIndex(int p_130527_) {
      return this.source.writerIndex(p_130527_);
   }

   public ByteBuf setIndex(int p_130417_, int p_130418_) {
      return this.source.setIndex(p_130417_, p_130418_);
   }

   public int readableBytes() {
      return this.source.readableBytes();
   }

   public int writableBytes() {
      return this.source.writableBytes();
   }

   public int maxWritableBytes() {
      return this.source.maxWritableBytes();
   }

   public boolean isReadable() {
      return this.source.isReadable();
   }

   public boolean isReadable(int pSize) {
      return this.source.isReadable(pSize);
   }

   public boolean isWritable() {
      return this.source.isWritable();
   }

   public boolean isWritable(int pSize) {
      return this.source.isWritable(pSize);
   }

   public ByteBuf clear() {
      return this.source.clear();
   }

   public ByteBuf markReaderIndex() {
      return this.source.markReaderIndex();
   }

   public ByteBuf resetReaderIndex() {
      return this.source.resetReaderIndex();
   }

   public ByteBuf markWriterIndex() {
      return this.source.markWriterIndex();
   }

   public ByteBuf resetWriterIndex() {
      return this.source.resetWriterIndex();
   }

   public ByteBuf discardReadBytes() {
      return this.source.discardReadBytes();
   }

   public ByteBuf discardSomeReadBytes() {
      return this.source.discardSomeReadBytes();
   }

   public ByteBuf ensureWritable(int p_130139_) {
      return this.source.ensureWritable(p_130139_);
   }

   public int ensureWritable(int pSize, boolean pForce) {
      return this.source.ensureWritable(pSize, pForce);
   }

   public boolean getBoolean(int pIndex) {
      return this.source.getBoolean(pIndex);
   }

   public byte getByte(int pIndex) {
      return this.source.getByte(pIndex);
   }

   public short getUnsignedByte(int pIndex) {
      return this.source.getUnsignedByte(pIndex);
   }

   public short getShort(int pIndex) {
      return this.source.getShort(pIndex);
   }

   public short getShortLE(int pIndex) {
      return this.source.getShortLE(pIndex);
   }

   public int getUnsignedShort(int pIndex) {
      return this.source.getUnsignedShort(pIndex);
   }

   public int getUnsignedShortLE(int pIndex) {
      return this.source.getUnsignedShortLE(pIndex);
   }

   public int getMedium(int pIndex) {
      return this.source.getMedium(pIndex);
   }

   public int getMediumLE(int pIndex) {
      return this.source.getMediumLE(pIndex);
   }

   public int getUnsignedMedium(int pIndex) {
      return this.source.getUnsignedMedium(pIndex);
   }

   public int getUnsignedMediumLE(int pIndex) {
      return this.source.getUnsignedMediumLE(pIndex);
   }

   public int getInt(int pIndex) {
      return this.source.getInt(pIndex);
   }

   public int getIntLE(int pIndex) {
      return this.source.getIntLE(pIndex);
   }

   public long getUnsignedInt(int pIndex) {
      return this.source.getUnsignedInt(pIndex);
   }

   public long getUnsignedIntLE(int pIndex) {
      return this.source.getUnsignedIntLE(pIndex);
   }

   public long getLong(int pIndex) {
      return this.source.getLong(pIndex);
   }

   public long getLongLE(int pIndex) {
      return this.source.getLongLE(pIndex);
   }

   public char getChar(int pIndex) {
      return this.source.getChar(pIndex);
   }

   public float getFloat(int pIndex) {
      return this.source.getFloat(pIndex);
   }

   public double getDouble(int pIndex) {
      return this.source.getDouble(pIndex);
   }

   public ByteBuf getBytes(int p_130163_, ByteBuf p_130164_) {
      return this.source.getBytes(p_130163_, p_130164_);
   }

   public ByteBuf getBytes(int p_130166_, ByteBuf p_130167_, int p_130168_) {
      return this.source.getBytes(p_130166_, p_130167_, p_130168_);
   }

   public ByteBuf getBytes(int p_130170_, ByteBuf p_130171_, int p_130172_, int p_130173_) {
      return this.source.getBytes(p_130170_, p_130171_, p_130172_, p_130173_);
   }

   public ByteBuf getBytes(int p_130191_, byte[] p_130192_) {
      return this.source.getBytes(p_130191_, p_130192_);
   }

   public ByteBuf getBytes(int p_130194_, byte[] p_130195_, int p_130196_, int p_130197_) {
      return this.source.getBytes(p_130194_, p_130195_, p_130196_, p_130197_);
   }

   public ByteBuf getBytes(int p_130179_, ByteBuffer p_130180_) {
      return this.source.getBytes(p_130179_, p_130180_);
   }

   public ByteBuf getBytes(int p_130175_, OutputStream p_130176_, int p_130177_) throws IOException {
      return this.source.getBytes(p_130175_, p_130176_, p_130177_);
   }

   public int getBytes(int pIndex, GatheringByteChannel pOut, int pLength) throws IOException {
      return this.source.getBytes(pIndex, pOut, pLength);
   }

   public int getBytes(int pIndex, FileChannel pOut, long pPosition, int pLength) throws IOException {
      return this.source.getBytes(pIndex, pOut, pPosition, pLength);
   }

   public CharSequence getCharSequence(int pIndex, int pLength, Charset pCharset) {
      return this.source.getCharSequence(pIndex, pLength, pCharset);
   }

   public ByteBuf setBoolean(int p_130362_, boolean p_130363_) {
      return this.source.setBoolean(p_130362_, p_130363_);
   }

   public ByteBuf setByte(int p_130365_, int p_130366_) {
      return this.source.setByte(p_130365_, p_130366_);
   }

   public ByteBuf setShort(int p_130438_, int p_130439_) {
      return this.source.setShort(p_130438_, p_130439_);
   }

   public ByteBuf setShortLE(int p_130441_, int p_130442_) {
      return this.source.setShortLE(p_130441_, p_130442_);
   }

   public ByteBuf setMedium(int p_130432_, int p_130433_) {
      return this.source.setMedium(p_130432_, p_130433_);
   }

   public ByteBuf setMediumLE(int p_130435_, int p_130436_) {
      return this.source.setMediumLE(p_130435_, p_130436_);
   }

   public ByteBuf setInt(int p_130420_, int p_130421_) {
      return this.source.setInt(p_130420_, p_130421_);
   }

   public ByteBuf setIntLE(int p_130423_, int p_130424_) {
      return this.source.setIntLE(p_130423_, p_130424_);
   }

   public ByteBuf setLong(int p_130426_, long p_130427_) {
      return this.source.setLong(p_130426_, p_130427_);
   }

   public ByteBuf setLongLE(int p_130429_, long p_130430_) {
      return this.source.setLongLE(p_130429_, p_130430_);
   }

   public ByteBuf setChar(int p_130404_, int p_130405_) {
      return this.source.setChar(p_130404_, p_130405_);
   }

   public ByteBuf setFloat(int p_130414_, float p_130415_) {
      return this.source.setFloat(p_130414_, p_130415_);
   }

   public ByteBuf setDouble(int p_130411_, double p_130412_) {
      return this.source.setDouble(p_130411_, p_130412_);
   }

   public ByteBuf setBytes(int p_130368_, ByteBuf p_130369_) {
      return this.source.setBytes(p_130368_, p_130369_);
   }

   public ByteBuf setBytes(int p_130371_, ByteBuf p_130372_, int p_130373_) {
      return this.source.setBytes(p_130371_, p_130372_, p_130373_);
   }

   public ByteBuf setBytes(int p_130375_, ByteBuf p_130376_, int p_130377_, int p_130378_) {
      return this.source.setBytes(p_130375_, p_130376_, p_130377_, p_130378_);
   }

   public ByteBuf setBytes(int p_130396_, byte[] p_130397_) {
      return this.source.setBytes(p_130396_, p_130397_);
   }

   public ByteBuf setBytes(int p_130399_, byte[] p_130400_, int p_130401_, int p_130402_) {
      return this.source.setBytes(p_130399_, p_130400_, p_130401_, p_130402_);
   }

   public ByteBuf setBytes(int p_130384_, ByteBuffer p_130385_) {
      return this.source.setBytes(p_130384_, p_130385_);
   }

   public int setBytes(int pIndex, InputStream pIn, int pLength) throws IOException {
      return this.source.setBytes(pIndex, pIn, pLength);
   }

   public int setBytes(int pIndex, ScatteringByteChannel pIn, int pLength) throws IOException {
      return this.source.setBytes(pIndex, pIn, pLength);
   }

   public int setBytes(int pIndex, FileChannel pIn, long pPosition, int pLength) throws IOException {
      return this.source.setBytes(pIndex, pIn, pPosition, pLength);
   }

   public ByteBuf setZero(int p_130444_, int p_130445_) {
      return this.source.setZero(p_130444_, p_130445_);
   }

   public int setCharSequence(int pIndex, CharSequence pCharSequence, Charset pCharset) {
      return this.source.setCharSequence(pIndex, pCharSequence, pCharset);
   }

   public boolean readBoolean() {
      return this.source.readBoolean();
   }

   public byte readByte() {
      return this.source.readByte();
   }

   public short readUnsignedByte() {
      return this.source.readUnsignedByte();
   }

   public short readShort() {
      return this.source.readShort();
   }

   public short readShortLE() {
      return this.source.readShortLE();
   }

   public int readUnsignedShort() {
      return this.source.readUnsignedShort();
   }

   public int readUnsignedShortLE() {
      return this.source.readUnsignedShortLE();
   }

   public int readMedium() {
      return this.source.readMedium();
   }

   public int readMediumLE() {
      return this.source.readMediumLE();
   }

   public int readUnsignedMedium() {
      return this.source.readUnsignedMedium();
   }

   public int readUnsignedMediumLE() {
      return this.source.readUnsignedMediumLE();
   }

   public int readInt() {
      return this.source.readInt();
   }

   public int readIntLE() {
      return this.source.readIntLE();
   }

   public long readUnsignedInt() {
      return this.source.readUnsignedInt();
   }

   public long readUnsignedIntLE() {
      return this.source.readUnsignedIntLE();
   }

   public long readLong() {
      return this.source.readLong();
   }

   public long readLongLE() {
      return this.source.readLongLE();
   }

   public char readChar() {
      return this.source.readChar();
   }

   public float readFloat() {
      return this.source.readFloat();
   }

   public double readDouble() {
      return this.source.readDouble();
   }

   public ByteBuf readBytes(int pLength) {
      return this.source.readBytes(pLength);
   }

   public ByteBuf readSlice(int pLength) {
      return this.source.readSlice(pLength);
   }

   public ByteBuf readRetainedSlice(int pLength) {
      return this.source.readRetainedSlice(pLength);
   }

   public ByteBuf readBytes(ByteBuf p_130289_) {
      return this.source.readBytes(p_130289_);
   }

   public ByteBuf readBytes(ByteBuf p_130291_, int p_130292_) {
      return this.source.readBytes(p_130291_, p_130292_);
   }

   public ByteBuf readBytes(ByteBuf p_130294_, int p_130295_, int p_130296_) {
      return this.source.readBytes(p_130294_, p_130295_, p_130296_);
   }

   public ByteBuf readBytes(byte[] p_130310_) {
      return this.source.readBytes(p_130310_);
   }

   public ByteBuf readBytes(byte[] p_130312_, int p_130313_, int p_130314_) {
      return this.source.readBytes(p_130312_, p_130313_, p_130314_);
   }

   public ByteBuf readBytes(ByteBuffer p_130301_) {
      return this.source.readBytes(p_130301_);
   }

   public ByteBuf readBytes(OutputStream p_130298_, int p_130299_) throws IOException {
      return this.source.readBytes(p_130298_, p_130299_);
   }

   public int readBytes(GatheringByteChannel pOut, int pLength) throws IOException {
      return this.source.readBytes(pOut, pLength);
   }

   public CharSequence readCharSequence(int pLength, Charset pCharset) {
      return this.source.readCharSequence(pLength, pCharset);
   }

   public int readBytes(FileChannel pOut, long pPosition, int pLength) throws IOException {
      return this.source.readBytes(pOut, pPosition, pLength);
   }

   public ByteBuf skipBytes(int p_130447_) {
      return this.source.skipBytes(p_130447_);
   }

   public ByteBuf writeBoolean(boolean p_130468_) {
      return this.source.writeBoolean(p_130468_);
   }

   public ByteBuf writeByte(int p_130470_) {
      return this.source.writeByte(p_130470_);
   }

   public ByteBuf writeShort(int p_130520_) {
      return this.source.writeShort(p_130520_);
   }

   public ByteBuf writeShortLE(int p_130522_) {
      return this.source.writeShortLE(p_130522_);
   }

   public ByteBuf writeMedium(int p_130516_) {
      return this.source.writeMedium(p_130516_);
   }

   public ByteBuf writeMediumLE(int p_130518_) {
      return this.source.writeMediumLE(p_130518_);
   }

   public ByteBuf writeInt(int p_130508_) {
      return this.source.writeInt(p_130508_);
   }

   public ByteBuf writeIntLE(int p_130510_) {
      return this.source.writeIntLE(p_130510_);
   }

   public ByteBuf writeLong(long p_130512_) {
      return this.source.writeLong(p_130512_);
   }

   public ByteBuf writeLongLE(long p_130514_) {
      return this.source.writeLongLE(p_130514_);
   }

   public ByteBuf writeChar(int p_130499_) {
      return this.source.writeChar(p_130499_);
   }

   public ByteBuf writeFloat(float p_130506_) {
      return this.source.writeFloat(p_130506_);
   }

   public ByteBuf writeDouble(double p_130504_) {
      return this.source.writeDouble(p_130504_);
   }

   public ByteBuf writeBytes(ByteBuf p_130472_) {
      return this.source.writeBytes(p_130472_);
   }

   public ByteBuf writeBytes(ByteBuf p_130474_, int p_130475_) {
      return this.source.writeBytes(p_130474_, p_130475_);
   }

   public ByteBuf writeBytes(ByteBuf p_130477_, int p_130478_, int p_130479_) {
      return this.source.writeBytes(p_130477_, p_130478_, p_130479_);
   }

   public ByteBuf writeBytes(byte[] p_130493_) {
      return this.source.writeBytes(p_130493_);
   }

   public ByteBuf writeBytes(byte[] p_130495_, int p_130496_, int p_130497_) {
      return this.source.writeBytes(p_130495_, p_130496_, p_130497_);
   }

   public ByteBuf writeBytes(ByteBuffer p_130484_) {
      return this.source.writeBytes(p_130484_);
   }

   public int writeBytes(InputStream pIn, int pLength) throws IOException {
      return this.source.writeBytes(pIn, pLength);
   }

   public int writeBytes(ScatteringByteChannel pIn, int pLength) throws IOException {
      return this.source.writeBytes(pIn, pLength);
   }

   public int writeBytes(FileChannel pIn, long pPosition, int pLength) throws IOException {
      return this.source.writeBytes(pIn, pPosition, pLength);
   }

   public ByteBuf writeZero(int p_130524_) {
      return this.source.writeZero(p_130524_);
   }

   public int writeCharSequence(CharSequence pCharSequence, Charset pCharset) {
      return this.source.writeCharSequence(pCharSequence, pCharset);
   }

   public int indexOf(int pFromIndex, int pToIndex, byte pValue) {
      return this.source.indexOf(pFromIndex, pToIndex, pValue);
   }

   public int bytesBefore(byte pValue) {
      return this.source.bytesBefore(pValue);
   }

   public int bytesBefore(int pLength, byte pValue) {
      return this.source.bytesBefore(pLength, pValue);
   }

   public int bytesBefore(int pIndex, int pLength, byte pValue) {
      return this.source.bytesBefore(pIndex, pLength, pValue);
   }

   public int forEachByte(ByteProcessor pProcessor) {
      return this.source.forEachByte(pProcessor);
   }

   public int forEachByte(int pIndex, int pLength, ByteProcessor pProcessor) {
      return this.source.forEachByte(pIndex, pLength, pProcessor);
   }

   public int forEachByteDesc(ByteProcessor pProcessor) {
      return this.source.forEachByteDesc(pProcessor);
   }

   public int forEachByteDesc(int pIndex, int pLength, ByteProcessor pProcessor) {
      return this.source.forEachByteDesc(pIndex, pLength, pProcessor);
   }

   public ByteBuf copy() {
      return this.source.copy();
   }

   public ByteBuf copy(int pIndex, int pLength) {
      return this.source.copy(pIndex, pLength);
   }

   public ByteBuf slice() {
      return this.source.slice();
   }

   public ByteBuf retainedSlice() {
      return this.source.retainedSlice();
   }

   public ByteBuf slice(int pIndex, int pLength) {
      return this.source.slice(pIndex, pLength);
   }

   public ByteBuf retainedSlice(int pIndex, int pLength) {
      return this.source.retainedSlice(pIndex, pLength);
   }

   public ByteBuf duplicate() {
      return this.source.duplicate();
   }

   public ByteBuf retainedDuplicate() {
      return this.source.retainedDuplicate();
   }

   public int nioBufferCount() {
      return this.source.nioBufferCount();
   }

   public ByteBuffer nioBuffer() {
      return this.source.nioBuffer();
   }

   public ByteBuffer nioBuffer(int pIndex, int pLength) {
      return this.source.nioBuffer(pIndex, pLength);
   }

   public ByteBuffer internalNioBuffer(int pIndex, int pLength) {
      return this.source.internalNioBuffer(pIndex, pLength);
   }

   public ByteBuffer[] nioBuffers() {
      return this.source.nioBuffers();
   }

   public ByteBuffer[] nioBuffers(int pIndex, int pLength) {
      return this.source.nioBuffers(pIndex, pLength);
   }

   public boolean hasArray() {
      return this.source.hasArray();
   }

   public byte[] array() {
      return this.source.array();
   }

   public int arrayOffset() {
      return this.source.arrayOffset();
   }

   public boolean hasMemoryAddress() {
      return this.source.hasMemoryAddress();
   }

   public long memoryAddress() {
      return this.source.memoryAddress();
   }

   public String toString(Charset pCharset) {
      return this.source.toString(pCharset);
   }

   public String toString(int pIndex, int pLength, Charset pCharset) {
      return this.source.toString(pIndex, pLength, pCharset);
   }

   public int hashCode() {
      return this.source.hashCode();
   }

   public boolean equals(Object pOther) {
      return this.source.equals(pOther);
   }

   public int compareTo(ByteBuf pOther) {
      return this.source.compareTo(pOther);
   }

   public String toString() {
      return this.source.toString();
   }

   public ByteBuf retain(int p_130353_) {
      return this.source.retain(p_130353_);
   }

   public ByteBuf retain() {
      return this.source.retain();
   }

   public ByteBuf touch() {
      return this.source.touch();
   }

   public ByteBuf touch(Object p_130462_) {
      return this.source.touch(p_130462_);
   }

   public int refCnt() {
      return this.source.refCnt();
   }

   public boolean release() {
      return this.source.release();
   }

   public boolean release(int pDecrement) {
      return this.source.release(pDecrement);
   }

   @FunctionalInterface
   public interface Reader<T> extends Function<FriendlyByteBuf, T> {
      default FriendlyByteBuf.Reader<Optional<T>> asOptional() {
         return (p_236878_) -> {
            return p_236878_.readOptional(this);
         };
      }
   }

   @FunctionalInterface
   public interface Writer<T> extends BiConsumer<FriendlyByteBuf, T> {
      default FriendlyByteBuf.Writer<Optional<T>> asOptional() {
         return (p_236881_, p_236882_) -> {
            p_236881_.writeOptional(p_236882_, this);
         };
      }
   }
}
