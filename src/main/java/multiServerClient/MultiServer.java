package multiServerClient;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
    FileOutputStream fout=null;

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

                //파일 전송 기능 추가

                System.out.println(name+"님이 "+ filename+" 파일을 전송하였습니다.");
                System.out.println("전송받은 파일 내용: ");
                // file open..

                // 소켓에서 받은 file 정보로 파일 입력 받기
                FileInputStream fis = new FileInputStream(filename);

                // 파일의 내용을 byte단위로 읽어옵니다.그래서
                // 읽어서 저장할 버퍼 byte 배열 설정
                byte[] byteBuff = new byte[9999];

                // 파일을 읽고 읽은 크기를 nRLen 에 저장한다.
                int nRLen = fis.read(byteBuff);

                // 출력을 위해서 byte배열을 문자열로 변환
                String strBuff = new String(byteBuff, 0, nRLen);

                // 읽은 내용을 출력 합니다.
                System.out.printf("읽은 바이트수[%d] : \n읽은 내용 :  \n%s \n", nRLen, strBuff);

                // 사용이 끝나면 파일 스트림을 닫습니다.
                fis.close();

                //파일 input 완료.



                // 기존의 파일이 없으면 만들어지고 있으면 덮어쓰게 되어 기존 파일내용이 지워진다.
                FileOutputStream fos = new FileOutputStream("ServerRepository.txt");

                //기존 파일에 내용을 추가 할려면 두번째 인자로 true를 적어 준다. true를 추가해도 없으면 만든다.
                //FileOutputStream fos1 = new FileOutputStream("c:/temp/java/test/test.txt",true);

                // 파일에 저장할 내용
                String strText = strBuff;



                // 문자열을 바이트배열로 변환해서 파일에 저장한다.
                fos.write(strText.getBytes());

                // 사용이 끝나면 파일 스트림을 닫습니다.
                fos.close();

               //파일전송 기능 완료.
                if("quit".equals(inputMsg)) break;
                sendAll(name + "님께서 " + inputMsg+" 상대경로에 존재하는 파일을 ServerRepository로 전송완료하였습니다.");
                System.out.println("서버 저장소에 저장 완료.");
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

