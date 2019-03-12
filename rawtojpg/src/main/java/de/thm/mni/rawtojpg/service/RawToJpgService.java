package de.thm.mni.rawtojpg.service;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executors;

@Service
public class RawToJpgService {

    public File convertRawToJpg(File rawFile) throws IOException, InterruptedException {
        ProcessBuilder builder = new ProcessBuilder();;
        String newFileName = FilenameUtils.removeExtension(rawFile.getAbsolutePath())+"_converted.jpg";
        builder.command("/bin/bash","-c", "dcraw -c -w "+ rawFile.getAbsolutePath() +" | convert - " + newFileName);
        Process process = builder.start();
        builder.start();
        int exitCode = process.waitFor();

        if(exitCode == 0) {
            return new File(newFileName);
        } else {
            return null;
        }
    }

    public byte[] grayscaleImage(File jpg) {
        HttpClient client = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost("http://grayscale:8081/grayscale");
        FileBody fileBody = new FileBody(jpg, ContentType.DEFAULT_BINARY);

        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        builder.addPart("file", fileBody);
        HttpEntity entity = builder.build();

        post.setEntity(entity);

        try {
            HttpResponse response = client.execute(post);
            return IOUtils.toByteArray(response.getEntity().getContent());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new byte[0];

    }
}
