package com.saunafs.proto;

import static com.saunafs.proto.Description.description;
import static java.lang.reflect.Modifier.isFinal;
import static java.lang.reflect.Modifier.isPublic;
import static java.lang.reflect.Modifier.isStatic;
import static java.util.Arrays.stream;

import java.util.List;

import com.saunafs.common.Common;
import com.saunafs.common.Size;
import com.saunafs.proto.msg.ReadData;
import com.saunafs.proto.msg.ReadErasuredChunk;
import com.saunafs.proto.msg.ReadStatus;

public class Protocol {
  public static final Description SAU_CLTOCS_READ = description()
      .identifier("SAU_CLTOCS_READ")
      .code(ReadErasuredChunk.code)
      .version(ReadErasuredChunk.version)
      .field(long.class, "chunkId")
      .field(int.class, "chunkVersion")
      .field(short.class, "chunkType")
      .field(int.class, "offset")
      .field(Size.class, "requestedSize");

  public static final Description SAU_CSTOCL_READ_STATUS = description()
      .identifier("SAU_CSTOCL_READ_STATUS")
      .code(ReadStatus.code)
      .version(ReadStatus.version)
      .field(long.class, "chunkId")
      .field(byte.class, "status")
      .decoder(ReadStatus::readStatus);

  public static final Description SAU_CSTOCL_READ_DATA = description()
      .identifier("SAU_CSTOCL_READ_DATA")
      .code(ReadData.code)
      .version(ReadData.version)
      .field(long.class, "chunkId")
      .field(int.class, "offset")
      .field(int.class, "size")
      .field(int.class, "crc")
      .field(byte[].class, "data")
      .decoder(ReadData::readData);

  public static final List<Description> PROTOCOL = findDescriptions();

  private static List<Description> findDescriptions() {
    return stream(Protocol.class.getDeclaredFields())
        .filter(field -> isPublic(field.getModifiers()))
        .filter(field -> isStatic(field.getModifiers()))
        .filter(field -> isFinal(field.getModifiers()))
        .filter(field -> field.getType() == Description.class)
        .map(Common::readStatic)
        .map(Description.class::cast)
        .toList();
  }
}
