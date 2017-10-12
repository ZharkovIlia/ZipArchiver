package com.company;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

class ExtractActionArchiver extends ActionArchiver {
    ExtractActionArchiver() {
        super(ActionType.EXTRACT);
        options = new Options();
        options.addOption("help", false, "print this message");
        options.addOption(Option.builder("d")
                .hasArg()
                .argName("DIRECTORY")
                .desc("directory where to extract files")
                .build());
    }
    @Override
    String getCLSyntax() {
        return getActionType().getNameOfOption() + " [options] target";
    }
}
