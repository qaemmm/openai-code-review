package plus.gaga.middleware.sdk;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class OpenAiCodeReview {

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("呵呵哈哈哈或或11");

        ProcessBuilder processBuilder = new ProcessBuilder("git","diff","HEAD~1","HEAD");
        processBuilder.directory(new File("."));

        Process process = processBuilder.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

        String line;
        StringBuilder differCode = new StringBuilder();
        while((line=reader.readLine())!=null){
            differCode.append(line);
        }
        int exitCode = process.waitFor();
        System.out.println("Exit with code"+exitCode);
        System.out.println("评审代码"+differCode.toString());
    }

}
