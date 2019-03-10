package de.thm.mni.rawtojpg.restcontroller;

import de.thm.mni.rawtojpg.service.RawToJpgService;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;

@RestController
@RequestMapping("/rawtojpg")
public class RawToJpgController {
    public static final String IMAGE_SUBFOLDER = "/tmp/images/";
    private static final File IMAGE_SUBFOLDER_FILEHANDLE = new File(IMAGE_SUBFOLDER);

    @Autowired
    RawToJpgService rawToJpgService;

    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public DeferredResult<ResponseEntity<byte[]>> convertRawToJpg(@RequestParam("file") MultipartFile file) throws IOException, InterruptedException {
        DeferredResult<ResponseEntity<byte[]>> output = new DeferredResult<>();
        ExecutorService executorService = Executors.newFixedThreadPool(8);
        executorService.submit(() -> {
            System.out.println(Thread.currentThread().getId());
            File rawImage = null;
            File jpgImage = null;
            try {
                rawImage = writeRawImageAndReturnFileHandle(file);
                jpgImage = rawToJpgService.convertRawToJpg(rawImage);
                final HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.IMAGE_JPEG);
                output.setResult(new ResponseEntity<>(IOUtils.toByteArray(jpgImage.toURI()),headers,HttpStatus.OK));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return output;
    }

    @RequestMapping(path = "/grayscale",method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public DeferredResult<ResponseEntity<byte[]>> convertRawToJpgGrayscale(@RequestParam("file") MultipartFile file) throws IOException, InterruptedException {
        DeferredResult<ResponseEntity<byte[]>> output = new DeferredResult<>();
        ExecutorService executorService = Executors.newFixedThreadPool(8);
        executorService.submit(() -> {
            System.out.println(Thread.currentThread().getId());
            File rawImage = null;
            File jpgImage = null;
            try {
                rawImage = writeRawImageAndReturnFileHandle(file);
                jpgImage = rawToJpgService.convertRawToJpg(rawImage);
                byte[] grayscale = rawToJpgService.grayscaleImage(jpgImage);
                final HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.IMAGE_JPEG);
                output.setResult(new ResponseEntity<>(grayscale,headers,HttpStatus.OK));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return output;
    }

    private File writeRawImageAndReturnFileHandle(MultipartFile file) throws IOException {
        String uuid = UUID.randomUUID().toString();
        String originalFileExtension = FilenameUtils.getExtension(file.getOriginalFilename());
        File rawImage = new File(IMAGE_SUBFOLDER + uuid + "."+originalFileExtension);
        if(!IMAGE_SUBFOLDER_FILEHANDLE.exists()) {
            IMAGE_SUBFOLDER_FILEHANDLE.mkdir();
        }
        System.out.println(IMAGE_SUBFOLDER_FILEHANDLE.getAbsolutePath());
        rawImage.createNewFile();
        FileOutputStream fileOutputStream = new FileOutputStream(rawImage);
        fileOutputStream.write(file.getBytes());
        fileOutputStream.close();
        return rawImage;
    }
}
