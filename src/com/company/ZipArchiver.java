package com.company;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipFile;

public class ZipArchiver {
    public static void main(String[] args) {
        ArgsHandler argsHandler = new ArgsHandler(args);
        ActionArchiver action = argsHandler.getAction();
        System.out.println(action.getOptionValueMapping());
        System.out.println(action.getTargetArchiveName());
        System.out.println(action.getFiles());

        boolean result = action.exec();
        if ( !result) {
            System.err.println(argsHandler.getProgramName() + ": " + action.getErrorString());
            System.exit(1);
        }
        System.exit(0);
    }
}
