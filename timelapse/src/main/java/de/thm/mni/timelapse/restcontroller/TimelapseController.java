package de.thm.mni.timelapse.restcontroller;

import de.thm.mni.timelapse.service.TimelapseService;
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
@RequestMapping("/timelapse")
public class TimelapseController {


    @Autowired
    TimelapseService timelapseService;

    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public  DeferredResult<ResponseEntity<byte[]>> createTimelapse(@RequestParam("file") MultipartFile file, @RequestParam(name = "framerate") int framerate) throws IOException, InterruptedException {
        DeferredResult<ResponseEntity<byte[]>> output = new DeferredResult<>();
        ExecutorService executorService = Executors.newFixedThreadPool(8);
        executorService.submit(() -> {
            File timelapse = null;
            try {
                timelapse = timelapseService.createTimelapse(file,framerate);
                final HttpHeaders headers = new HttpHeaders();
                headers.add("Content-Type","video/mp4");
                output.setResult(new ResponseEntity<>(IOUtils.toByteArray(timelapse.toURI()),headers,HttpStatus.OK));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return output;
    }


}
