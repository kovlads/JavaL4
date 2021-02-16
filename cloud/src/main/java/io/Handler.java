package io;

import java.io.*;
import java.net.Socket;

public class Handler implements Runnable{
    private String serverDir = "./";
    private String userName;
    private final IoFileCommandServer server;
    private final Socket socket;

    public Handler(IoFileCommandServer server, Socket socket) {
        this.server = server;
        this.socket = socket;
        userName = "user";
    }

    @Override
    public void run() {
        try(DataOutputStream os = new DataOutputStream(socket.getOutputStream());
                DataInputStream is = new DataInputStream(socket.getInputStream());
                BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream())) {
            while (true) {
                String message = is.readUTF();
                System.out.println("received message: " + message);
                if (message.equals("ls")) {
                    File dir = new File(serverDir);
                    StringBuilder sb = new StringBuilder(userName).append(": files ->  \n");
                    File[] files = dir.listFiles();
                    if (files != null) {
                        for (File file : files) {
                            if (file == null) {
                                continue;
                            }
                            sb.append(file.getName()).append(" | ");
                            if (file.isFile()) {
                                sb.append("[FILE] | ").append(file.length()).append(" bytes");
                            } else {
                                sb.append("[DIR]");
                            }
                            sb.append("\n");
                        }
                    }
                    os.writeUTF(sb.toString());
                    os.flush();
                }
                else if (message.startsWith("cd ")) {
                    String path = message.split(" ", 2)[1];

                    File dir = new File(path);
                    File dirAcc = new File(serverDir + "/" + path);
                    if (dir.exists()) {
                        serverDir = path;
                    } else if (dirAcc.exists()) {
                        serverDir = serverDir + "/" + path;
                    } else if (path.equals("..")) {
                        serverDir = new File(serverDir).getParent();
                    } else {
                        os.writeUTF("user: WRONG PATH\n");
                        os.flush();
                    }


                }
                else if (message.startsWith("get ")) {
                    String fileName = message.split(" ", 2)[1];
                    File file = new File(fileName);
                    if (file.exists()) {
                        os.writeUTF("get " + fileName + " " + file.length() + "\n");
                        os.flush();
                         
                        InputStream fileIs = new FileInputStream(file);
                        byte[] buffer = new byte[8192];
                        int ptr = 0;
                        while ((ptr = fileIs.read(buffer)) != -1) {
                            bos.write(buffer, 0, ptr);
                        }
                        fileIs.close();
                        bos.flush();
                    } else {
                        os.writeUTF("user: file not found\n");
                        os.flush();
                    }
                }
                else if (message.equals("/quit")) {
                    os.writeUTF(message);
                    os.flush();
                    break;
                }
                else {
                    os.writeUTF("user: UNKNOWN COMMAND\n");
                    os.flush();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
