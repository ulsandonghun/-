package multiServerClient;

import javax.swing.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class MultiServer {
    private List<PrintWriter> clientOutputs;
    private JTextArea chatArea;
    private String serverRepositoryPath;

    public static void main(String[] args) {
        MultiServer multiServer = new MultiServer();
        multiServer.start();
    }

    public void start() {
        clientOutputs = new ArrayList<>();
        chatArea = new JTextArea();
        serverRepositoryPath = "C:\\WinterSchool-spring\\분산시스템과제\\server\\";

        // Specify the server repository directory

        JFrame frame = new JFrame("MultiServer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 400);
        frame.setLayout(null);

        JScrollPane scrollPane = new JScrollPane(chatArea);
        scrollPane.setBounds(20, 20, 360, 320);
        frame.add(scrollPane);

        frame.setVisible(true);

        ServerSocket serverSocket = null;
        Socket socket = null;
        try {
            serverSocket = new ServerSocket(8000);
            while (true) {
                chatArea.append("[클라이언트 연결 대기중]\n");
                socket = serverSocket.accept();

                // client가 접속할 때마다 새로운 스레드 생성
                ReceiveThread receiveThread = new ReceiveThread(socket);
                receiveThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                    chatArea.append("[서버 종료]\n");
                } catch (IOException e) {
                    e.printStackTrace();
                    chatArea.append("[서버 소켓 통신 에러]\n");
                }
            }
        }
    }

    private class ReceiveThread extends Thread {
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;
        private FileOutputStream fout;

        public ReceiveThread(Socket socket) {
            this.socket = socket;
            try {
                out = new PrintWriter(socket.getOutputStream());
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                clientOutputs.add(out);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            String name = "";
            try {
                name = in.readLine();
                chatArea.append("[" + name + " 새 연결 생성]\n");
                sendAll("[" + name + "]님이 서버에 로그인하였습니다.");

                while (in != null) {
                    String inputMsg = in.readLine();
                    //클라이언트가 보낸 String에서 Check 메세지가 오면 UPdate 프로토콜 시작
                    if(inputMsg.substring(0,5).equals("CHECK")){
                        String checkfile=inputMsg.substring(5,inputMsg.length());

                        chatArea.append(name+"님의 요청으로"+checkfile+"에 대한 파일 업데이트탐지를 진행합니다.\n");

                        // 파일 변경 감지 로직을 구현
                        boolean isUpdated = detectFileUpdate(name, checkfile);

                        if (isUpdated) {
                            chatArea.append(checkfile + " 파일이 변경되었습니다.\n");
                            // 변경된 파일에 대한 추가 동작 수행
                            // 예: 변경된 파일 복사, 로그 작성 등
                        } else {
                            chatArea.append(checkfile + " 파일은 변경되지 않았습니다.\n");
                        }


                        continue;

                    }








                    String filename = inputMsg;

                    chatArea.append(name + "님이 " + filename + " 파일을 전송하였습니다.\n");
                    chatArea.append("전송받은 파일 내용:\n");

                    FileInputStream fis = new FileInputStream(filename);

                    byte[] byteBuff = new byte[9999];
                    int nRLen = fis.read(byteBuff);
                    String strBuff = new String(byteBuff, 0, nRLen);

                    chatArea.append(String.format("읽은 바이트 수[%d]:\n읽은 내용:\n%s\n", nRLen, strBuff));

                    File file = new File(serverRepositoryPath + name + "_" + filename);
                    FileOutputStream fos = new FileOutputStream(file);
                    fos.write(byteBuff, 0, nRLen);
                    fos.close();

                    sendAll(name +
                            "님께서 " + inputMsg + " 파일을 ServerRepository로 전송 완료하였습니다.");
                    chatArea.append("서버 저장소에 저장 완료.\n");
                    if ("quit".equals(inputMsg)) break;
                }
            } catch (IOException e) {
                chatArea.append("[" + name + " 접속 끊김]\n");
            } finally {
                sendAll("[" + name + "]님이 서버에서 로그아웃 하셨습니다");
                clientOutputs.remove(out);
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            chatArea.append("[" + name + " 연결 종료]\n");
        }

        private void sendAll(String s) {
            for (PrintWriter out : clientOutputs) {
                out.println(s);
                out.flush();
            }
        }

        private boolean detectFileUpdate(String name, String checkfile) {
            String filePath = serverRepositoryPath + name + "_" + checkfile;
            File file = new File(filePath);

            if (file.exists()) {
                long previousModifiedTime = file.lastModified();

                try {
                    // 일정 시간 동안 대기
                    Thread.sleep(1000);

                    file = new File(filePath);
                    long currentModifiedTime = file.lastModified();

                    return currentModifiedTime > previousModifiedTime;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            return false;
        }
    }
}


