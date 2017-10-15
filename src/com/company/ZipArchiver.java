package com.company;

public class ZipArchiver {
    public static void main(String[] args) {
        ArgsHandler argsHandler = new ArgsHandler(args);
        ActionArchiver action = argsHandler.getAction();

        boolean result = action.exec();
        if ( !result) {
            System.err.println(argsHandler.getProgramName() + ": " + action.getErrorString());
            System.exit(1);
        }
        System.exit(0);
    }
}
