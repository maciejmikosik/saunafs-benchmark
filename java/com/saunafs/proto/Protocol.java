package com.saunafs.proto;

import static com.saunafs.common.Common.readStatic;
import static java.lang.reflect.Modifier.isPublic;
import static java.lang.reflect.Modifier.isStatic;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;

import java.io.DataInputStream;
import java.lang.reflect.Method;
import java.util.List;

import com.saunafs.proto.msg.ReadData;
import com.saunafs.proto.msg.ReadErasuredChunk;
import com.saunafs.proto.msg.ReadStatus;

public class Protocol {
  public static Method decoder(Class<? extends Message> messageClass) {
    return stream(messageClass.getDeclaredMethods())
        .filter(method -> isPublic(method.getModifiers()))
        .filter(method -> isStatic(method.getModifiers()))
        .filter(method -> method.getReturnType() == messageClass)
        .filter(method -> method.getParameterCount() == 1)
        .filter(method -> method.getParameters()[0].getType() == DataInputStream.class)
        .findFirst()
        .orElseThrow(() -> new RuntimeException("no decoder for " + messageClass));
  }

  public static Class<? extends Message> messageClass(int code, int version) {
    return messageClasses.stream()
        .filter(messageClass -> {
          try {
            return code == (int) readStatic(messageClass.getDeclaredField("code"))
                && version == (int) readStatic(messageClass.getDeclaredField("version"));
          } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
          }
        }).findFirst()
        .orElseThrow(() -> new RuntimeException("unknown code %d version %d"
            .formatted(code, version)));
  }

  private static final List<Class<? extends Message>> messageClasses = asList(
      ReadErasuredChunk.class,
      ReadData.class,
      ReadStatus.class);
}
