package com.example.socket_server;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class TCPClient {
    private static final String SERVER_HOST = "tcpserver-adpb.onrender.com";
    private static final int SERVER_PORT = 10000;
    private static final int TIMEOUT = 10000; // 10 seconds timeout

    public static void main(String[] args) {
        Socket socket = null;
        BufferedReader reader = null;
        PrintWriter writer = null;
        Scanner scanner = null;

        try {
            System.out.println("Đang kết nối đến server...");
            System.out.println("Server: " + SERVER_HOST);
            System.out.println("Port: " + SERVER_PORT);
            
            socket = new Socket();
            socket.setSoTimeout(TIMEOUT);
            socket.connect(new java.net.InetSocketAddress(SERVER_HOST, SERVER_PORT), TIMEOUT);
            
            System.out.println("Đã kết nối thành công đến server!");
            System.out.println("Local address: " + socket.getLocalAddress());
            System.out.println("Local port: " + socket.getLocalPort());
            System.out.println("Remote address: " + socket.getRemoteSocketAddress());

            // Tạo các luồng đọc/ghi
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
            scanner = new Scanner(System.in);

            // Đọc menu từ server
            String menu = reader.readLine();
            System.out.println(menu);

            // Vòng lặp chính để tương tác với server
            while (true) {
                try {
                    // Đọc prompt từ server
                    String prompt = reader.readLine();
                    if (prompt == null || prompt.trim().isEmpty()) {
                        System.out.println("Server đã đóng kết nối.");
                        break;
                    }
                    System.out.print(prompt);

                    // Đọc input từ người dùng
                    String input = scanner.nextLine();
                    if (input.trim().equalsIgnoreCase("exit")) {
                        System.out.println("Đang thoát...");
                        break;
                    }
                    
                    // Gửi input đến server
                    writer.println(input);
                    writer.flush();

                    // Đọc kết quả từ server
                    String result = reader.readLine();
                    if (result == null || result.trim().isEmpty()) {
                        System.out.println("Server đã đóng kết nối.");
                        break;
                    }
                    System.out.println(result);

                    // Đọc menu tiếp theo
                    menu = reader.readLine();
                    if (menu == null || menu.trim().isEmpty()) {
                        System.out.println("Server đã đóng kết nối.");
                        break;
                    }
                    System.out.println(menu);

                } catch (IOException e) {
                    System.out.println("Lỗi kết nối: " + e.getMessage());
                    break;
                }
            }

        } catch (java.net.SocketTimeoutException e) {
            System.out.println("Lỗi: Kết nối bị timeout sau " + TIMEOUT + " giây");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Lỗi kết nối: " + e.getMessage());
            System.out.println("Chi tiết lỗi:");
            e.printStackTrace();
            System.out.println("\nGợi ý khắc phục:");
            System.out.println("1. Kiểm tra service có đang chạy trên Render không");
            System.out.println("2. Kiểm tra cấu hình port trên Render");
            System.out.println("3. Thử tắt tạm thời firewall");
            System.out.println("4. Kiểm tra logs của service trên Render");
        } finally {
            // Đóng các tài nguyên
            try {
                if (scanner != null) scanner.close();
                if (writer != null) writer.close();
                if (reader != null) reader.close();
                if (socket != null) socket.close();
                System.out.println("Đã đóng kết nối!");
            } catch (IOException e) {
                System.out.println("Lỗi khi đóng kết nối: " + e.getMessage());
            }
        }
    }
} 