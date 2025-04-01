package com.example.socket_server;

import com.example.socket_server.exercises.NumberChecking;
import com.example.socket_server.exercises.DigitOperations;
import com.example.socket_server.exercises.GcdLcm;
import com.example.socket_server.exercises.StringManipulation;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TCPServer {
    private static final int PORT = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
    private static final ExecutorService executorService = Executors.newCachedThreadPool();

    public static void main(String[] args) {
        try {
            // In thông tin môi trường
            System.out.println("Starting TCP Server...");
            System.out.println("PORT: " + PORT);
            System.out.println("JAVA_HOME: " + System.getenv("JAVA_HOME"));
            System.out.println("PWD: " + System.getenv("PWD"));
            
            // Tạo server socket với backlog
            ServerSocket serverSocket = new ServerSocket(PORT, 50);
            System.out.println("Server socket created successfully");
            System.out.println("Server listening on port: " + PORT);
            System.out.println("Server address: " + serverSocket.getInetAddress().getHostAddress());

            while (true) {
                try {
                    System.out.println("Waiting for client connection...");
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("New client connected from: " + clientSocket.getInetAddress().getHostAddress());
                    
                    executorService.execute(new ClientHandler(clientSocket));
                } catch (IOException e) {
                    System.err.println("Error accepting client connection: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            System.err.println("Fatal server error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static class ClientHandler implements Runnable {
        private final Socket clientSocket;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try (
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
            ) {
                System.out.println("Client handler started for: " + clientSocket.getInetAddress().getHostAddress());
                
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    System.out.println("Received from client: " + inputLine);
                    String response = processInput(inputLine);
                    System.out.println("Sending response: " + response);
                    out.println(response);
                }
            } catch (IOException e) {
                System.err.println("Error handling client: " + e.getMessage());
                e.printStackTrace();
            } finally {
                try {
                    clientSocket.close();
                    System.out.println("Client connection closed: " + clientSocket.getInetAddress().getHostAddress());
                } catch (IOException e) {
                    System.err.println("Error closing client socket: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }

        private String processInput(String input) {
            try {
                String[] parts = input.split("\\|");
                if (parts.length < 2) {
                    return "Lỗi: Định dạng không hợp lệ. Sử dụng: function|data";
                }

                int function = Integer.parseInt(parts[0]);
                String data = parts[1];

                switch (function) {
                    case 1: {
                        int number = Integer.parseInt(data);
                        return NumberChecking.checkNumber(number);
                    }
                    case 2: {
                        int number = Integer.parseInt(data);
                        return DigitOperations.calculateDigits(number);
                    }
                    case 3: {
                        String[] numbers = data.split("\\s+");
                        if (numbers.length != 2) {
                            return "Lỗi: Vui lòng nhập đúng hai số nguyên, cách nhau bởi dấu cách!";
                        }
                        int a = Integer.parseInt(numbers[0]);
                        int b = Integer.parseInt(numbers[1]);
                        return GcdLcm.calculate(a, b);
                    }
                    case 4:
                        return StringManipulation.reverseString(data);
                    case 5:
                        return StringManipulation.AdvancedOperations.processString(data);
                    case 6:
                        return StringManipulation.Analysis.analyzeString(data);
                    default:
                        return "Lỗi: Chức năng không tồn tại!";
                }
            } catch (NumberFormatException e) {
                return "Lỗi: Dữ liệu không hợp lệ!";
            } catch (Exception e) {
                return "Lỗi: " + e.getMessage();
            }
        }
    }
} 