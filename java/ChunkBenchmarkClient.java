
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ChunkBenchmarkClient {
  private static final int BLOCK_SIZE = 65536; // 64KB per block
  private static final String SERVER_IP = "192.168.168.96";
  private static final int SERVER_PORT = 9422;

  public static void main(String[] args) {
    try (Socket socket = new Socket(SERVER_IP, SERVER_PORT)) {
      DataOutputStream out = new DataOutputStream(socket.getOutputStream());
      DataInputStream in = new DataInputStream(socket.getInputStream());

      // --- Send request ---
      ByteBuffer requestBuffer = ByteBuffer.allocate(34).order(ByteOrder.BIG_ENDIAN);
      requestBuffer.putInt(1200); // Message type
      requestBuffer.putInt(26); // Packet length
      requestBuffer.putInt(1); // Packet version
      requestBuffer.putLong(0x1F6); // Chunk ID
      requestBuffer.putInt(1); // Chunk version
      requestBuffer.putShort((short) 0); // Chunk type (0 = standard)
      requestBuffer.putInt(0); // Offset
      requestBuffer.putInt(0x00400000); // Size (4 MB)

      out.write(requestBuffer.array());
      out.flush();

      // --- Receive response ---
      while (true) {
        byte[] header = in.readNBytes(8); // Type (4 bytes) + Packet length (4
                                          // bytes)
        System.out.println("read %s bytes".formatted(header.length));
        ByteBuffer headerBuffer = ByteBuffer.wrap(header).order(ByteOrder.BIG_ENDIAN);
        long msgType = headerBuffer.getInt();
        int packetLen = headerBuffer.getInt();

        System.out.println("msgType=" + msgType);
        System.out.println("packetLen=" + packetLen);
        if (msgType == 1201) { // Data message
          System.out.println("correct");
          byte[] meta = in.readNBytes(packetLen); // ChunkID(8)+Offset(4)+Size(4)+CRC(4)
          ByteBuffer metaBuffer = ByteBuffer.wrap(meta).order(ByteOrder.BIG_ENDIAN);
          long chunkId = metaBuffer.getLong();
          int offset = metaBuffer.getInt();
          int size = metaBuffer.getInt();
          int crc32 = metaBuffer.getInt();

          byte[] dataBlock = in.readNBytes(size); // receive data block
                                                  // (typically 65536 bytes)

          // Here you could verify CRC32 if needed

          System.out.println("Received block offset " + offset + ", size: " + size);
        } else if (msgType == 1000201L) { // Final status message
          byte[] statusMsg = in.readNBytes(9); // ChunkID (8 bytes) + Status (1
                                               // byte)
          ByteBuffer statusBuffer = ByteBuffer.wrap(statusMsg).order(ByteOrder.BIG_ENDIAN);
          long chunkId = statusBuffer.getLong();
          byte status = statusBuffer.get();
          System.out.println("Received final status for chunk " + chunkId + ": " + status);
          break;
        }
      }

    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
