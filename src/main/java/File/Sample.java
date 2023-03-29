package File;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class Sample {

    public static void main(String[] args) throws IOException {
        try {
            File file = new File("test.txt");

            if (!file.exists()) {
                file.createNewFile();
            }

            // 3. Writer 생성
            FileOutputStream fos = new FileOutputStream(file);

            // 4. 파일에 쓰기
            fos.write("안녕하세요".getBytes());

            // 5. FileOutputStream close
            fos.close();

        } catch (Exception e) {
            e.getStackTrace();
        }
    }
}
