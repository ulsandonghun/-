import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TcpIpServer2 {

    public static void main(String[] args) {
        ServerSocket serverSocket =null;

        try {
            serverSocket = new ServerSocket(7777);
            System.out.println(getTime() + "TCP서버가 준비되었습니다.");


        } catch (IOException e) {

            e.printStackTrace();
        }

        while (true) {
            try {
                System.out.println(getTime() + "서버가 연결요청을 기다립니다.");
                Socket socket = serverSocket.accept();
                System.out.println(getTime() + socket.getInetAddress() + "로부터 연결요청이 들어왔습니다.");

                System.out.println("getPort():" + socket.getPort());
                System.out.println("getLocalPort():" + socket.getLocalPort());


                //소켓의 출력스트림을 얻는다.
                OutputStream out = socket.getOutputStream();
                DataOutputStream dos = new DataOutputStream(out);

                //연결된 원격 소켓에 데이터를 보낸다.
                dos.writeUTF("[Notice]: TEST Message1 from server.");
                System.out.println(getTime() + "데이터를 전송했습니다.");

                dos.close();
                socket.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static String getTime() {
        SimpleDateFormat f = new SimpleDateFormat("[hh:mm:ss]");
        return f.format(new Date());
    }
}//class
