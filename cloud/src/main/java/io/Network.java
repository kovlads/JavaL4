package io;

import java.io.*;
import java.net.Socket;

public class Network implements Closeable {
    private final BufferedInputStream bis;
    private final DataInputStream is;
    private final DataOutputStream os;
    private final Socket socket;
    private static Network instance;

    public static Network get() throws IOException {
        if (instance == null) {
            instance = new Network();
        }
        return instance;
    }

    private Network() throws IOException {
        socket = new Socket("localhost", 8189);
        os = new DataOutputStream(socket.getOutputStream());
        is = new DataInputStream(socket.getInputStream());
        bis = new BufferedInputStream(socket.getInputStream());
    }

    public void write(String message) throws IOException {
        os.writeUTF(message);
        os.flush();
    }

    public String read() throws IOException {
        return is.readUTF();
    }
    public int readBytes(byte[] byteArr) throws IOException {
        return bis.read(byteArr);
    }

    public void close() throws IOException {
        bis.close();
        is.close();
        os.close();
        socket.close();
    }

}
