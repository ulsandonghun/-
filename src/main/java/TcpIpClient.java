import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.Socket;

public class TcpIpClient {
    public static void main(String[] args) {
        try {

            String serverIP = "127.0.0.1";

            System.out.println("서버에 연결중입니다. 서버 IP: " +serverIP);

            //소켓을 생성
            //생성즉시 서버 소켓이랑 연결됨.
            Socket socket = new Socket(serverIP, 7777);

            //소켓의 입력 스트림을 얻는다.
            InputStream in = socket.getInputStream();
            DataInputStream dis = new DataInputStream(in);

            //서버소켓으로부터 받은 데이터를 출력한다.
            System.out.println("서버로부터 받은 메세지" + dis.readUTF());
            System.out.println("연결을 종료합니다.");

            //스트림과 소켓 종료
            dis.close();
            socket.close();;
            System.out.println("연결이 종료되었습니다.");


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
