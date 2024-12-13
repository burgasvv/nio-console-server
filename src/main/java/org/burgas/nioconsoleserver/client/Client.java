package org.burgas.nioconsoleserver.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Client {

    public void connect(String host, Integer port, Scanner scanner)  {

        try (
                SocketChannel socketChannel = SocketChannel.open();
                Selector selector = Selector.open()
        ){
            socketChannel.connect(new InetSocketAddress(host, port));
            socketChannel.configureBlocking(false)
                    .register(selector, SelectionKey.OP_WRITE);

            while (socketChannel.isConnected()) {
                selector.select(
                        selectionKey -> {

                            if (selectionKey.isWritable()) {
                                try {
                                    //noinspection resource
                                    SocketChannel client = (SocketChannel) selectionKey.channel()
                                            .configureBlocking(false);

                                    String message = scanner.nextLine();
                                    System.out.println(message);

                                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                                    buffer.clear();

                                    buffer.put(message.getBytes(StandardCharsets.UTF_8));
                                    buffer.flip();
                                    client.write(buffer);
                                    client.register(selector, SelectionKey.OP_READ);

                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }

                            } else if (selectionKey.isReadable()) {

                                try {
                                    SocketChannel client = (SocketChannel) selectionKey.channel()
                                            .configureBlocking(false);

                                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                                    buffer.clear();

                                    if (client.read(buffer) == -1)
                                        client.close();

                                    else {
                                        buffer.flip();
                                        byte[] bytes = new byte[buffer.remaining()];
                                        buffer.get(bytes);
                                        System.out.println(new String(bytes, StandardCharsets.UTF_8));
                                    }
                                    client.register(selector, SelectionKey.OP_WRITE);

                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }
                );
            }

        } catch (IOException e) {
            System.out.println("Не удалось подключиться к серверу " + host + ":" + port);
        }
    }
}
