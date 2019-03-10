package de.thm.mni.exifdata.service;

import de.thm.mni.exifdata.helper.StreamGobbler;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

@Service
public class ExifdataService {

    public List<String> readExifdataFromImage(File image) throws IOException, InterruptedException {
        ProcessBuilder builder = new ProcessBuilder();
        String command =  "exiftool " + image.getAbsolutePath();
        builder.command("/bin/bash", "-c", command);
        Process process = builder.start();
        StreamGobbler streamGobbler =
                new StreamGobbler(process.getInputStream());
        Executors.newSingleThreadExecutor().submit(streamGobbler);
        int exitCode = process.waitFor();
        if(exitCode == 0) {
            return streamGobbler.getResult();
        } else {
            return null;
        }
    }

    public List<String> readExifdataFromImageFiltered(File image,String filter) throws IOException, InterruptedException {
        ProcessBuilder builder = new ProcessBuilder();
        String command =  "exiftool " + image.getAbsolutePath()  + "| grep '" + filter + "'";
        builder.command("/bin/bash", "-c", command);
        Process process = builder.start();
        StreamGobbler streamGobbler =
                new StreamGobbler(process.getInputStream());
        Executors.newSingleThreadExecutor().submit(streamGobbler);
        int exitCode = process.waitFor();
        if(exitCode == 0) {
            return streamGobbler.getResult();
        } else {
            return null;
        }
    }
}
