package de.thm.mni.exifdata.helper;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class StreamGobbler implements Runnable {
    private InputStream inputStream;
    private List<String> result = new ArrayList<>();
    public StreamGobbler(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public List<String> getResult() {
        return result;
    }

    @Override
    public void run() {
        this.result = new BufferedReader(new InputStreamReader(inputStream)).lines().collect(Collectors.toList());
    }
}