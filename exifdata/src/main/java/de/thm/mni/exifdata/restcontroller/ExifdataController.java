package de.thm.mni.exifdata.restcontroller;

import de.thm.mni.exifdata.service.ExifdataService;
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
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("/exifdata")
public class ExifdataController {

    public static final String IMAGE_SUBFOLDER = "/tmp/images/";
    private static final File IMAGE_SUBFOLDER_FILEHANDLE = new File(IMAGE_SUBFOLDER);

    @Autowired
    ExifdataService exifdataService;


    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public DeferredResult<ResponseEntity<String>> getAllExifdata(@RequestParam("file") MultipartFile file) throws IOException, InterruptedException {
        DeferredResult<ResponseEntity<String>> output = new DeferredResult<>();
        ExecutorService executorService = Executors.newFixedThreadPool(8);
        executorService.submit(() -> {
            System.out.println(Thread.currentThread().getId());
            File image;
            try {
                image =  writeImageAndReturnFileHandle(file);
                List<String> exifdata = exifdataService.readExifdataFromImage(image);
                final HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.IMAGE_JPEG);
                output.setResult(new ResponseEntity<>(String.join("\n",exifdata), HttpStatus.OK));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return output;
    }


    @RequestMapping(value = "/filtered",method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public DeferredResult<ResponseEntity<String>> getExifdataFiltered(@RequestParam("file") MultipartFile file,@RequestParam("filter") String filter) throws IOException, InterruptedException {
        DeferredResult<ResponseEntity<String>> output = new DeferredResult<>();
        ExecutorService executorService = Executors.newFixedThreadPool(8);
        executorService.submit(() -> {
            System.out.println(Thread.currentThread().getId());
            File image;
            try {
                image =  writeImageAndReturnFileHandle(file);
                List<String> exifdata = exifdataService.readExifdataFromImageFiltered(image,filter);
                output.setResult(new ResponseEntity<>(String.join("\n",exifdata), HttpStatus.OK));
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
