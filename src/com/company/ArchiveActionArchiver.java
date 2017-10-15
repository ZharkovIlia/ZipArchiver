package com.company;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;

import java.io.IOException;

class ArchiveActionArchiver extends ActionArchiver {
    ArchiveActionArchiver() {
        super(ActionType.ARCHIVE);
        options = new Options();
        options.addOption("help", false, "print this message");
        OptionGroup optionArchiveGroup = new OptionGroup();
        optionArchiveGroup.addOption(Option.builder()
                .longOpt("get-comment")
                .desc("get comment on the existing archive")
                .build());

        optionArchiveGroup.addOption(Option.builder()
                .longOpt("set-comment")
                .hasArg()
                .argName("COMMENT")
                .desc("set new comment on the existing archive")
                .build());

        optionArchiveGroup.addOption(new Option("help", false, "print this message"));

        optionArchiveGroup.setRequired(true);
        options.addOptionGroup(optionArchiveGroup);
    }

    @Override
    String getCLSyntax() {
        return getActionType().getNameOfOption() + " (-help|--set-comment|--get-comment) target";
    }

    @Override
    boolean exec() {
        return false;
    }
}
