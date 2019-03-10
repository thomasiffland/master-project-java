package de.thm.mni.grayscale.service;

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

@Service
public class GrayscaleService {

    public File convertToGrayscaleImage(File colorImage) throws IOException, InterruptedException {
        ProcessBuilder builder = new ProcessBuilder();
        String newFileName = FilenameUtils.removeExtension(colorImage.getAbsolutePath())+"_grayscale.jpg";
        String command =  "convert " + colorImage.getAbsolutePath() +" -colorspace Gray "+ newFileName;
        builder.command("/bin/bash","-c",command);
        Process process = builder.start();
        builder.start();
        int exitCode = process.waitFor();
        if(exitCode == 0) {
            return new File(newFileName);
        } else {
            return null;
        }
    }

    public byte[] resizeGrayscaleImage(File grayscaleImage, String size) {
        HttpClient client = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost("http://localhost:8083/resize");
        FileBody fileBody = new FileBody(grayscaleImage, ContentType.DEFAULT_BINARY);
        StringBody sizeStringBody = new StringBody(size, ContentType.MULTIPART_FORM_DATA);

        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        builder.addPart("file", fileBody);
        builder.addPart("size", sizeStringBody);
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
