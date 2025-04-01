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
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("TCP Server đang chạy trên port: " + PORT);
            System.out.println("Địa chỉ server: " + serverSocket.getInetAddress().getHostAddress());

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client mới kết nối từ: " + clientSocket.getInetAddress().getHostAddress());
                
                // Xử lý client trong thread riêng
                executorService.execute(new ClientHandler(clientSocket));
            }
        } catch (IOException e) {
            System.err.println("Lỗi server: " + e.getMessage());
            e.printStackTrace();
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
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    // Xử lý dữ liệu từ client
                    String response = processInput(inputLine);
                    out.println(response);
                }
            } catch (IOException e) {
                System.err.println("Lỗi xử lý client: " + e.getMessage());
                e.printStackTrace();
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private String processInput(String input) {
            try {
                // Phân tích input để xác định chức năng và dữ liệu
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