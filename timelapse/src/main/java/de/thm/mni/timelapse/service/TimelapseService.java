package de.thm.mni.timelapse.service;

import com.sun.org.apache.xpath.internal.operations.Mult;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

@Service
public class TimelapseService {

    private static final String IMAGE_SUBFOLDER = "/tmp/images/";
    private static final File IMAGE_SUBFOLDER_FILEHANDLE = new File(IMAGE_SUBFOLDER);

    public File createTimelapse(MultipartFile file, int framerate) throws IOException, InterruptedException {
        File zip = writeZipAndReturnFileHandle(file);
        System.out.println("zipfile: " +zip.getAbsolutePath());

        File unzipped = unzip(zip);
        System.out.println("unzipped to: " + unzipped.getAbsolutePath());
        ProcessBuilder builder = new ProcessBuilder();
        String command = "ffmpeg -r " + framerate + " -pattern_type glob -i '*.png' -vcodec libx264 timelapse.mp4";
        builder.command("/bin/bash", "-c", command);
        Process process = builder.directory(unzipped).start();
        builder.start();
        int exitCode = process.waitFor();
        if (exitCode == 0) {
            return new File(unzipped.getAbsolutePath()+"/timelapse.mp4");
        } else {
            return null;
        }
    }

    private File writeZipAndReturnFileHandle(MultipartFile file) throws IOException {
        String uuid = UUID.randomUUID().toString();
        String originalFileExtension = FilenameUtils.getExtension(file.getOriginalFilename());
        if (!IMAGE_SUBFOLDER_FILEHANDLE.exists()) {
            IMAGE_SUBFOLDER_FILEHANDLE.mkdir();
        }
        File zip = new File(IMAGE_SUBFOLDER + uuid + "." + originalFileExtension);
        zip.createNewFile();
        FileOutputStream fileOutputStream = new FileOutputStream(zip);
        fileOutputStream.write(file.getBytes());
        fileOutputStream.close();
        return zip;
    }

    private File unzip(File zip) throws IOException, InterruptedException {
        File folderToExtractTo = new File(FilenameUtils.removeExtension(zip.getAbsolutePath()));
        folderToExtractTo.mkdirs();
        ProcessBuilder builder = new ProcessBuilder();
        String command = "unzip " + zip.getAbsolutePath() + " -d " + folderToExtractTo.getAbsolutePath();
        builder.command("/bin/bash", "-c", command);
        Process process = builder.start();
        builder.start();
        process.waitFor();
        int exitCode = process.waitFor();
        if (exitCode == 0) {
            return folderToExtractTo;
        } else {
            return null;
        }
    }
}
