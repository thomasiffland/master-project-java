package de.thm.mni.resize.restcontroller;

import de.thm.mni.resize.service.ResizeService;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("/resize")
public class ResizeController {
    public static final String IMAGE_SUBFOLDER = "/tmp/images/";
    private static final File IMAGE_SUBFOLDER_FILEHANDLE = new File(IMAGE_SUBFOLDER);

    @Autowired
    ResizeService resizeService;

    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public DeferredResult<ResponseEntity<byte[]>> resize(@RequestParam("file") MultipartFile file, @RequestParam("size") String size) throws IOException, InterruptedException {
        DeferredResult<ResponseEntity<byte[]>> output = new DeferredResult<>();
        ExecutorService executorService = Executors.newFixedThreadPool(8);
        executorService.submit(() -> {
            System.out.println(Thread.currentThread().getId());
            File image = null;
            File resizedImage = null;
            try {
                image = writeImageAndReturnFileHandle(file);
                resizedImage = resizeService.resizeImage(image,size);
                final HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.IMAGE_JPEG);
                output.setResult(new ResponseEntity<>(IOUtils.toByteArray(resizedImage.toURI()),headers,HttpStatus.OK));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return output;
    }

    @RequestMapping(path = "/percent",method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public DeferredResult<ResponseEntity<byte[]>> resizePercent(@RequestParam("file") MultipartFile file, @RequestParam("percent") String percent) throws IOException, InterruptedException {
        DeferredResult<ResponseEntity<byte[]>> output = new DeferredResult<>();
        ExecutorService executorService = Executors.newFixedThreadPool(8);
        executorService.submit(() -> {
            System.out.println(Thread.currentThread().getId());
            File image = null;
            File resizedImage = null;
            try {
                image = writeImageAndReturnFileHandle(file);
                String sizeString = resizeService.generateSizeStringFromPercent(image,percent);
                resizedImage = resizeService.resizeImage(image,sizeString);
                final HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.IMAGE_JPEG);
                output.setResult(new ResponseEntity<>(IOUtils.toByteArray(resizedImage.toURI()),headers,HttpStatus.OK));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return output;
    }
    private File writeImageAndReturnFileHandle(MultipartFile file) throws IOException {
        String uuid = UUID.randomUUID().toString();
        String originalFileExtension = FilenameUtils.getExtension(file.getOriginalFilename());
        if(!IMAGE_SUBFOLDER_FILEHANDLE.exists()) {
            IMAGE_SUBFOLDER_FILEHANDLE.mkdir();
        }
        File image = new File(IMAGE_SUBFOLDER + uuid + "." + originalFileExtension);
        image.createNewFile();
        FileOutputStream fileOutputStream = new FileOutputStream(image);
        fileOutputStream.write(file.getBytes());
        fileOutputStream.close();
        return image;
    }
}
