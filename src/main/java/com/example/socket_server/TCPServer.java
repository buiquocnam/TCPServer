package com.example.socket_server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;
import java.util.logging.Level;

public class TCPServer {
    private static final Logger LOGGER = Logger.getLogger(TCPServer.class.getName());
    private static final int PORT = Integer.parseInt(System.getenv().getOrDefault("PORT", "10000"));
    private static final ExecutorService executorService = Executors.newCachedThreadPool();

    public static void main(String[] args) {
        try {
            LOGGER.info("Starting TCP Server...");
            LOGGER.info("Server address: 0.0.0.0");
            LOGGER.info("Server listening on port: " + PORT);
            
            ServerSocket serverSocket = new ServerSocket(PORT);
            LOGGER.info("Server socket created successfully");
            
            while (true) {
                try {
                    LOGGER.info("Waiting for client connection...");
                    Socket clientSocket = serverSocket.accept();
                    String clientAddress = clientSocket.getInetAddress().getHostAddress();
                    LOGGER.info("New client connected from: " + clientAddress);
                    
                    executorService.execute(new ClientHandler(clientSocket));
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "Error accepting client connection", e);
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Server error", e);
        }
    }

    private static class ClientHandler implements Runnable {
        private final Socket clientSocket;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try {
                LOGGER.info("Client handler started for: " + clientSocket.getInetAddress().getHostAddress());
                
                BufferedReader reader = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter writer = new PrintWriter(
                    new OutputStreamWriter(clientSocket.getOutputStream()), true);

                // Read the first line to determine if it's an HTTP request
                String firstLine = reader.readLine();
                if (firstLine == null) {
                    LOGGER.warning("Received empty request");
                    return;
                }

                // Check if it's an HTTP request
                if (firstLine.startsWith("GET ") || firstLine.startsWith("HEAD ")) {
                    // Handle HTTP health check
                    handleHttpRequest(reader, writer);
                } else {
                    // Handle TCP message
                    handleTcpMessage(firstLine, reader, writer);
                }

                clientSocket.close();
                LOGGER.info("Client connection closed: " + clientSocket.getInetAddress().getHostAddress());
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Error handling client: " + e.getMessage(), e);
            }
        }

        private void handleHttpRequest(BufferedReader reader, PrintWriter writer) throws IOException {
            // Read all HTTP headers
            String line;
            while ((line = reader.readLine()) != null && !line.isEmpty()) {
                LOGGER.info("Received from client: " + line);
            }

            // Send HTTP response for health check
            writer.println("HTTP/1.1 200 OK");
            writer.println("Content-Type: text/plain");
            writer.println("Content-Length: 2");
            writer.println();
            writer.println("OK");
            writer.flush();
        }

        private void handleTcpMessage(String firstLine, BufferedReader reader, PrintWriter writer) throws IOException {
            LOGGER.info("Received from client: " + firstLine);
            
            // Process TCP message
            String[] parts = firstLine.split("\\|");
            if (parts.length != 2) {
                String errorMsg = "Lỗi: Định dạng không hợp lệ. Sử dụng: function|data";
                LOGGER.warning(errorMsg);
                writer.println(errorMsg);
                writer.flush();
                return;
            }

            String function = parts[0];
            String data = parts[1];
            String response = processRequest(function, data);
            
            LOGGER.info("Sending response: " + response);
            writer.println(response);
            writer.flush();
        }

        private String processRequest(String function, String data) {
            switch (function) {
                case "login":
                    return handleLogin(data);
                case "register":
                    return handleRegister(data);
                case "getUserInfo":
                    return handleGetUserInfo(data);
                case "updateUserInfo":
                    return handleUpdateUserInfo(data);
                case "getAllUsers":
                    return handleGetAllUsers();
                case "deleteUser":
                    return handleDeleteUser(data);
                default:
                    return "Lỗi: Chức năng không tồn tại";
            }
        }

        private String handleLogin(String data) {
            try {
                String[] parts = data.split(",");
                if (parts.length != 2) {
                    return "Lỗi: Định dạng dữ liệu không hợp lệ";
                }
                String username = parts[0];
                String password = parts[1];
                
                // TODO: Implement actual login logic
                return "Đăng nhập thành công";
            } catch (Exception e) {
                return "Lỗi: " + e.getMessage();
            }
        }

        private String handleRegister(String data) {
            try {
                String[] parts = data.split(",");
                if (parts.length != 3) {
                    return "Lỗi: Định dạng dữ liệu không hợp lệ";
                }
                String username = parts[0];
                String password = parts[1];
                String email = parts[2];
                
                // TODO: Implement actual registration logic
                return "Đăng ký thành công";
            } catch (Exception e) {
                return "Lỗi: " + e.getMessage();
            }
        }

        private String handleGetUserInfo(String data) {
            try {
                // TODO: Implement actual user info retrieval logic
                return "Thông tin người dùng";
            } catch (Exception e) {
                return "Lỗi: " + e.getMessage();
            }
        }

        private String handleUpdateUserInfo(String data) {
            try {
                // TODO: Implement actual user info update logic
                return "Cập nhật thông tin thành công";
            } catch (Exception e) {
                return "Lỗi: " + e.getMessage();
            }
        }

        private String handleGetAllUsers() {
            try {
                // TODO: Implement actual user list retrieval logic
                return "Danh sách người dùng";
            } catch (Exception e) {
                return "Lỗi: " + e.getMessage();
            }
        }

        private String handleDeleteUser(String data) {
            try {
                // TODO: Implement actual user deletion logic
                return "Xóa người dùng thành công";
            } catch (Exception e) {
                return "Lỗi: " + e.getMessage();
            }
        }
    }
} 