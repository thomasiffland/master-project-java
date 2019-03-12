package de.thm.mni.resize.service;

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
public class ResizeService {

    public File resizeImage(File image, String size) throws IOException, InterruptedException {
        ProcessBuilder builder = new ProcessBuilder();
        String newFileName = FilenameUtils.removeExtension(image.getAbsolutePath())+"_resized.jpg";
        String command =  "convert " + image.getAbsolutePath() +" -resize " + size + " " + newFileName;
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

    public String generateSizeStringFromPercent(File jpg, String percent) {
        HttpClient client = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost("http://exifdata:8082/exifdata/filtered");
        FileBody fileBody = new FileBody(jpg, ContentType.DEFAULT_BINARY);
        StringBody filter = new StringBody("Image Height", ContentType.DEFAULT_BINARY);

        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        builder.addPart("file", fileBody);
        builder.addPart("filter", filter);
        HttpEntity entity = builder.build();

        post.setEntity(entity);

        try {
            HttpResponse response = client.execute(post);
            String returnValue = new String(IOUtils.toByteArray(response.getEntity().getContent()));
            float imageHeight = Float.parseFloat(returnValue.split(":")[1].trim());
            float newImageHeight = imageHeight * (Float.parseFloat(percent) / 100f);
            return newImageHeight + "x" + newImageHeight;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new String();

    }
}
