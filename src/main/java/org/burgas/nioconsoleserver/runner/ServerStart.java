package org.burgas.nioconsoleserver.runner;

import org.burgas.nioconsoleserver.server.Server;

import java.io.IOException;

public class ServerStart {

    public static void main(String[] args) throws IOException {

        Server server = new Server();
        server.start("localhost", 8888);
    }
}
