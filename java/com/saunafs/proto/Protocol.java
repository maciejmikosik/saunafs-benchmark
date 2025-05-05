package com.saunafs.proto;

import static java.lang.reflect.Modifier.isPublic;
import static java.lang.reflect.Modifier.isStatic;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;

import java.io.DataInputStream;
import java.lang.reflect.Method;
import java.util.List;

import com.saunafs.common.Blob;
import com.saunafs.common.Size;
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
          var identifier = messageClass.getDeclaredAnnotation(Identifier.class);
          return code == identifier.code()
              && version == identifier.version();
        }).findFirst()
        .orElseThrow(() -> new RuntimeException("unknown code %d version %d"
            .formatted(code, version)));
  }

  public static int packetLengthFor(Message message) {
    int sizeOfVersionField = 4;
    int sizeOfMessageBody = stream(message.getClass().getDeclaredFields())
        .mapToInt(field -> {
          try {
            var value = field.get(message);
            return switch (value) {
              case Byte number -> 1;
              case Short number -> 2;
              case Integer number -> 4;
              case Long number -> 8;
              case Size number -> 4;
              case Blob blob -> 8 + blob.data.length;
              default -> throw new RuntimeException("unsupported " + field);
            };
          } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
          }
        })
        .sum();
    return sizeOfVersionField + sizeOfMessageBody;
  }

  private static final List<Class<? extends Message>> messageClasses = asList(
      ReadErasuredChunk.class,
      ReadData.class,
      ReadStatus.class);
}
