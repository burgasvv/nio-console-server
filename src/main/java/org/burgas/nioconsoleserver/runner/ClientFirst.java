package org.burgas.nioconsoleserver.runner;

import org.burgas.nioconsoleserver.client.Client;

import java.util.Scanner;

public class ClientFirst {

    public static void main(String[] args)  {

        Client client = new Client();
        client.connect("127.0.0.1", 8888, new Scanner(System.in));
    }
}
