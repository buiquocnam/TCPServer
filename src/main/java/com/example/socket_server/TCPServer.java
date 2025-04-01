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
import java.util.logging.Logger;
import java.util.logging.Level;

public class TCPServer {
    private static final Logger LOGGER = Logger.getLogger(TCPServer.class.getName());
    private static final int PORT = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
    private static final ExecutorService executorService = Executors.newCachedThreadPool();
    private static final int MAX_EMPTY_REQUESTS = 3;
    private static int emptyRequestCount = 0;

    public static void main(String[] args) {
        ServerSocket serverSocket = null;
        try {
            LOGGER.info("Starting TCP Server...");
            LOGGER.info("Server address: 0.0.0.0");
            LOGGER.info("Server listening on port: " + PORT);
            
            serverSocket = new ServerSocket(PORT);
            LOGGER.info("Server socket created successfully");
            
            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    String clientAddress = clientSocket.getInetAddress().getHostAddress();
                    
                    // Only log new connections from non-Render IPs
                    if (!clientAddress.startsWith("10.209.")) {
                        LOGGER.info("New client connected from: " + clientAddress);
                    }
                    
                    executorService.execute(new ClientHandler(clientSocket));
                } catch (IOException e) {
                    if (!serverSocket.isClosed()) {
                        LOGGER.log(Level.SEVERE, "Error accepting client connection", e);
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Server error", e);
        } finally {
            if (serverSocket != null && !serverSocket.isClosed()) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "Error closing server socket", e);
                }
            }
        }
    }

    private static class ClientHandler implements Runnable {
        private final Socket clientSocket;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            BufferedReader reader = null;
            PrintWriter writer = null;
            String clientAddress = null;
            
            try {
                clientAddress = clientSocket.getInetAddress().getHostAddress();
                
                reader = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream()));
                writer = new PrintWriter(
                    new OutputStreamWriter(clientSocket.getOutputStream()), true);

                // Set socket timeout
                clientSocket.setSoTimeout(30000); // 30 seconds timeout

                // Send welcome message and menu immediately
                writer.println("Chào mừng đến với TCP Server!");
                writer.println();
                sendMenu(writer);
                writer.flush();

                // Read the first line
                String firstLine = reader.readLine();
                if (firstLine == null || firstLine.trim().isEmpty()) {
                    synchronized(TCPServer.class) {
                        emptyRequestCount++;
                        if (emptyRequestCount >= MAX_EMPTY_REQUESTS) {
                            LOGGER.warning("Received multiple empty requests. This might be a health check.");
                            emptyRequestCount = 0;
                        }
                    }
                    sendHttpResponse(writer, "OK");
                    return;
                }

                // Reset empty request counter when we get a valid request
                synchronized(TCPServer.class) {
                    emptyRequestCount = 0;
                }

                // Check if it's an HTTP request
                if (firstLine.startsWith("GET ") || firstLine.startsWith("HEAD ")) {
                    handleHttpRequest(reader, writer);
                } else {
                    // Handle TCP message
                    handleTcpMessage(firstLine, reader, writer);
                }

            } catch (IOException e) {
                if (!clientSocket.isClosed()) {
                    LOGGER.log(Level.SEVERE, "Error handling client: " + e.getMessage(), e);
                }
            } finally {
                // Close resources
                try {
                    if (writer != null) writer.close();
                    if (reader != null) reader.close();
                    if (!clientSocket.isClosed()) {
                        clientSocket.close();
                        if (clientAddress != null && !clientAddress.startsWith("10.209.")) {
                            LOGGER.info("Client connection closed: " + clientAddress);
                        }
                    }
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "Error closing client resources", e);
                }
            }
        }

        private void handleHttpRequest(BufferedReader reader, PrintWriter writer) throws IOException {
            // Read all HTTP headers
            String line;
            StringBuilder headers = new StringBuilder();
            while ((line = reader.readLine()) != null && !line.isEmpty()) {
                headers.append(line).append("\n");
            }

            // Check if it's a health check request
            if (headers.toString().contains("User-Agent: Go-http-client/2.0")) {
                sendHttpResponse(writer, "OK");
                return;
            }

            // For other HTTP requests, send a more informative response
            String response = "TCP Server is running\n";
            response += "Server Time: " + new java.util.Date() + "\n";
            response += "Server Status: OK\n";
            sendHttpResponse(writer, response);
        }

        private void sendHttpResponse(PrintWriter writer, String body) {
            writer.println("HTTP/1.1 200 OK");
            writer.println("Content-Type: text/plain");
            writer.println("Content-Length: " + body.length());
            writer.println("Connection: close");
            writer.println("Cache-Control: no-cache, no-store, must-revalidate");
            writer.println("Pragma: no-cache");
            writer.println("Expires: 0");
            writer.println();
            writer.println(body);
            writer.flush();
        }

        private void handleTcpMessage(String firstLine, BufferedReader reader, PrintWriter writer) throws IOException {
            LOGGER.info("Received from client: " + firstLine);
            
            try {
                int currentFunction = 0;
                String line = firstLine;
                while (line != null && !line.trim().isEmpty()) {
                    try {
                        if (currentFunction == 0) {
                            // Người dùng đang chọn bài
                            currentFunction = Integer.parseInt(line.trim());
                            if (currentFunction >= 1 && currentFunction <= 6) {
                                writer.println(getPromptForFunction(currentFunction));
                                writer.flush();
                            } else {
                                currentFunction = 0;
                                writer.println("Lựa chọn không hợp lệ. Vui lòng chọn lại:\n");
                                sendMenu(writer);
                                writer.flush();
                            }
                        } else {
                            // Người dùng đang nhập dữ liệu cho bài đã chọn
                            String result = processFunction(currentFunction, line.trim());
                            writer.println(result);
                            writer.println(); // Add empty line to mark end of result
                            currentFunction = 0;
                            sendMenu(writer);
                            writer.flush();
                        }
                        line = reader.readLine();
                    } catch (Exception e) {
                        writer.println("Có lỗi xảy ra: " + e.getMessage());
                        writer.println(); // Add empty line to mark end of error
                        currentFunction = 0;
                        sendMenu(writer);
                        writer.flush();
                        line = reader.readLine();
                    }
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error processing TCP message", e);
                writer.println("Lỗi xử lý: " + e.getMessage());
                writer.println(); // Add empty line to mark end of error
                writer.flush();
            }
        }

        private void sendMenu(PrintWriter out) {
            StringBuilder menu = new StringBuilder();
            menu.append("MENU CHỨC NĂNG:\n\n");
            menu.append("1. Kiểm tra số (số nguyên tố, chính phương, hoàn hảo, Armstrong)\n");
            menu.append("2. Tính tổng và tích các chữ số\n");
            menu.append("3. Tìm UCLN và BCNN\n");
            menu.append("4. Đảo ngược chuỗi\n");
            menu.append("5. Xử lý chuỗi nâng cao\n");
            menu.append("6. Phân tích chuỗi\n\n");
            menu.append("Vui lòng chọn chức năng (1-6):\n");
            out.println(menu.toString());
            out.flush();
        }

        private String getPromptForFunction(int function) {
            switch (function) {
                case 1:
                    return "Nhập một số nguyên để kiểm tra:\n";
                case 2:
                    return "Nhập một số nguyên để tính tổng và tích các chữ số:\n";
                case 3:
                    return "Nhập hai số nguyên cách nhau bởi dấu cách để tìm UCLN và BCNN:\n";
                case 4:
                case 5:
                case 6:
                    return "Nhập chuỗi cần xử lý:\n";
                default:
                    return "Lựa chọn không hợp lệ\n";
            }
        }

        private String processFunction(int function, String data) {
            try {
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
                        String[] parts = data.split("\\s+");
                        if (parts.length != 2) {
                            throw new IllegalArgumentException("Vui lòng nhập đúng hai số nguyên cách nhau bởi dấu cách");
                        }
                        int a = Integer.parseInt(parts[0]);
                        int b = Integer.parseInt(parts[1]);
                        return GcdLcm.calculate(a, b);
                    }
                    case 4:
                        return StringManipulation.reverseString(data);
                    case 5:
                        return StringManipulation.AdvancedOperations.processString(data);
                    case 6:
                        return StringManipulation.Analysis.analyzeString(data);
                    default:
                        return "Lựa chọn không hợp lệ\n";
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Vui lòng nhập số nguyên hợp lệ");
            }
        }
    }
} 