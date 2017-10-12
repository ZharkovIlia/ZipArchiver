package com.company;
import java.io.IOException;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.Arrays;

public class ZipArchiver {

    public static void main(String[] args) {
        ArgsHandler argsHandler = new ArgsHandler(args);
        ActionArchiver action = argsHandler.getAction();
        System.out.println(action.getOptionValueMapping());
        System.out.println(action.getTargetArchiveName());
        System.out.println(action.getFiles());
        /*FileWalker fw = new FileWalker();
        try {
            Files.walkFileTree(Paths.get("/home/ilya/Parallels/work/strace"), fw);
        } catch (IOException exc) {
            ;
        }*/
    }
}
