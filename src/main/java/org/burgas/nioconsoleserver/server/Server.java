package org.burgas.nioconsoleserver.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

public class Server {

    public void start(String host, Integer port) throws IOException {

        try (
                ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
                Selector selector = Selector.open()
        ){
            serverSocketChannel
                    .bind(new InetSocketAddress(host, port))
                    .configureBlocking(false).
                    register(selector, SelectionKey.OP_ACCEPT);

            System.out.println("Сервер: " + serverSocketChannel.getLocalAddress() + " успешно запущен");
            AtomicReference<String> message = new AtomicReference<>("");

            while (serverSocketChannel.isOpen()) {
                selector.select(
                        selectionKey -> {

                            if (selectionKey.isAcceptable()) {
                                //noinspection resource
                                ServerSocketChannel channel = (ServerSocketChannel) selectionKey.channel();

                                try {
                                    SocketChannel client = channel.accept();
                                    System.out.println("Клиент подключен: " + client.getRemoteAddress());
                                    client.configureBlocking(false)
                                            .register(selector, SelectionKey.OP_READ);

                                } catch (IOException e) {
                                    System.out.println("Возникли трудности с подключением клиента");
                                }

                            } else if (selectionKey.isReadable()) {

                                SocketChannel client = (SocketChannel) selectionKey.channel();
                                try {
                                    client.configureBlocking(false);
                                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                                    buffer.clear();

                                    if (client.read(buffer) == -1)
                                        client.close();

                                    else {
                                        buffer.flip();
                                        byte[] bytes = new byte[buffer.remaining()];
                                        buffer.get(bytes);
                                        message.set(new String(bytes, StandardCharsets.UTF_8));
                                        System.out.println(message.get());
                                    }
                                    client.register(selector, SelectionKey.OP_WRITE);

                                } catch (IOException e) {
                                    try {
                                        System.out.println("Клиент " + client.getRemoteAddress() + " отключился");
                                        client.close();

                                    } catch (IOException ex) {
                                        throw new RuntimeException(ex);
                                    }
                                }

                            } else if (selectionKey.isWritable()) {

                                SocketChannel client = (SocketChannel) selectionKey.channel();
                                try {
                                    client.configureBlocking(false);
                                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                                    buffer.clear();

                                    String serverAnswer = "Ответ сервера клиенту: " + client.getRemoteAddress() +
                                                          " на сообщение: " + message.get();

                                    buffer.put(serverAnswer.getBytes(StandardCharsets.UTF_8));
                                    buffer.flip();
                                    client.write(buffer);
                                    System.out.println("Ответ отправлен клиенту: " + client.getRemoteAddress());
                                    client.register(selector, SelectionKey.OP_READ);

                                } catch (IOException e) {
                                    try {
                                        System.out.println("Клиент " + client.getRemoteAddress() + " отключился");
                                        client.close();

                                    } catch (IOException ex) {
                                        throw new RuntimeException(ex);
                                    }
                                }
                            }
                        }
                );
            }
        }
    }
}
