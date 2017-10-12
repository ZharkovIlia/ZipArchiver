package com.company;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

class CreateActionArchiver extends ActionArchiver {
    CreateActionArchiver() {
        super(ActionType.CREATE);
        options = new Options();
        options.addOption("help", false, "print this message");
        options.addOption(Option.builder()
                .longOpt("comment")
                .hasArg()
                .argName("COMMENT")
                .desc("set comment on a new created archive")
                .build());
    }

    @Override
    String getCLSyntax() {
        return getActionType().getNameOfOption() + " [options] target file [files...]";
    }
}
