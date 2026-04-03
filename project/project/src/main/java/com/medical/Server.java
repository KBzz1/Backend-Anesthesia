package com.medical;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

//public class Server {
//    @Component
//    public class MedicalDeviceTcpServer {
//
//        private static final int PORT = 8888; // 设备配置的端口
//        private ServerSocket serverSocket;
//
//        @PostConstruct
//        public void startServer() {
//            new Thread(() -> {
//                try {
//                    serverSocket = new ServerSocket(PORT);
//
//                    while (true) {
//                        Socket clientSocket = serverSocket.accept();
//
//                        // 为每个设备创建独立线程处理
//                        new Thread(() -> handleDevice(clientSocket)).start();
//                    }
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
//            }).start();
//        }
//
//        private void handleDevice(Socket socket) {
//            try (BufferedInputStream in = new BufferedInputStream(socket.getInputStream());
//                 BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream())) {
//
//                byte[] buffer = new byte[4096];
//                int bytesRead;
//
//                while ((bytesRead = in.read(buffer)) != -1) {
//                    // 读取到的原始数据
//                    byte[] data = Arrays.copyOf(buffer, bytesRead);
//
//                    // 打印十六进制数据（用于调试）
//                    log.info("收到数据: {}", bytesToHex(data));
//
//                    // 解析数据（根据设备协议）
//                    parseDeviceData(data);
//
//                    // 如果需要应答
//                    // out.write(ackResponse);
//                    // out.flush();
//                }
//            } catch (IOException e) {
//                log.error("处理设备数据异常", e);
//            } finally {
//                try {
//                    socket.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//
//        private void parseDeviceData(byte[] data) {
//            // 根据设备协议文档解析
//            // 例如：心率、血压、血氧等参数
//        }
//
//        private String bytesToHex(byte[] bytes) {
//            StringBuilder sb = new StringBuilder();
//            for (byte b : bytes) {
//                sb.append(String.format("%02X ", b));
//            }
//            return sb.toString();
//        }
//
//        @PreDestroy
//        public void stopServer() {
//            try {
//                if (serverSocket != null) {
//                    serverSocket.close();
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//}
