package multiServerClient;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MultiServer {

    public static void main(String[] args) {
        MultiServer multiServer = new MultiServer();
        multiServer.start();
    }

    public void start() {
        ServerSocket serverSocket = null;
        Socket socket = null;
        try {
            serverSocket = new ServerSocket(8000);
            while (true) {
                System.out.println("[클라이언트 연결대기중]");
                socket = serverSocket.accept();

                // client가 접속할때마다 새로운 스레드 생성
                ReceiveThread receiveThread = new ReceiveThread(socket);
                receiveThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (serverSocket!=null) {
                try {
                    serverSocket.close();
                    System.out.println("[서버종료]");
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("[서버소켓통신에러]");
                }
            }
        }
    }


}

class ReceiveThread extends Thread {

    static List<PrintWriter> list =
            Collections.synchronizedList(new ArrayList<PrintWriter>());

    Socket socket = null;
    BufferedReader in = null;
    PrintWriter out = null;

    public ReceiveThread (Socket socket) {
        this.socket = socket;
        try {
            out = new PrintWriter(socket.getOutputStream());
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            list.add(out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {

        String name = "";
        try {
            // 최초1회는 client이름을 수신
            name = in.readLine();
            System.out.println("[" + name + " 새연결생성]");
            sendAll("[" + name + "]님이 서버에 로그인하였습니다.");

            while (in != null) {
                String inputMsg = in.readLine();
                String filename=inputMsg;

                //파일 전송

                System.out.println(name+"님이 "+ filename+" 파일을 전송하였습니다.");
                System.out.println("전송받은 파일 내용: ");
                FileReader reader = new FileReader(filename);

                int ch;
                while ((ch = reader.read()) != -1) {
                    System.out.print((char) ch);
                }


                File file = new File(filename);






               //파일전송 기능 완료.
                if("quit".equals(inputMsg)) break;
                sendAll(name + "님께서 " + inputMsg+" 상대경로에 존재하는 파일을 서버로 전송완료하였습니다.");
            }
        } catch (IOException e) {
            System.out.println("[" + name + " 접속끊김]");
        } finally {
            sendAll("[" + name + "]님이 서버에서 로그아웃 하셨습니다");
            list.remove(out);
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("[" + name + " 연결종료]");
    }

    private void sendAll (String s) {
        for (PrintWriter out: list) {
            out.println(s);
            out.flush();
        }
    }
}

