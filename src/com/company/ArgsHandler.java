package com.company;

import org.apache.commons.cli.*;

import java.util.Arrays;
import java.util.List;

class ArgsHandler {
    ArgsHandler(String[] args) {
        initializeSubcommands();
        HelpFormatter formatter = new HelpFormatter();
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd;

        String CLSyntaxSubcommands = programName + " ("
                + Arrays.stream(ActionArchiver.ActionType.values())
                .map(ActionArchiver.ActionType::getNameOfOption)
                .reduce("-help", (prev, next) -> prev + "|" + next)
                + ") [options]";

        String header = "simple ZIP archiver";

        if (args.length == 0) {
            formatter.printHelp(CLSyntaxSubcommands, header, subcommands, "");
            System.exit(1);
        }

        try {
            cmd = parser.parse(subcommands, Arrays.copyOfRange(args, 0, 1));
            if (cmd.hasOption("help")) {
                formatter.printHelp(CLSyntaxSubcommands, header, subcommands, "");
                System.exit(0);
            }
            for (ActionArchiver.ActionType actionType : ActionArchiver.ActionType.values()) {
                if ( !cmd.hasOption(actionType.getName())) {
                    continue;
                }
                this.action = ActionArchiver.getActionFromType(actionType);
                cmd = parser.parse(this.action.getOptions(),
                        Arrays.copyOfRange(args, 1, args.length));

                if (cmd.hasOption("help")) {
                    formatter.printHelp(programName + " " + this.action.getCLSyntax(), this.action.getOptions());
                    System.exit(0);
                }

                List<String> remainder = cmd.getArgList();
                if ( !remainder.isEmpty()) {
                    this.action.setTargetArchiveName(remainder.get(0));
                }

                for (Option opt : this.action.getOptions().getOptions()) {
                    if (opt.getOpt() != null && cmd.hasOption(opt.getOpt())) {
                        this.action.setOptionToValue(opt.getOpt(), cmd.getOptionValue(opt.getOpt()));
                    } else if (opt.getLongOpt() != null && cmd.hasOption(opt.getLongOpt())) {
                        this.action.setOptionToValue(opt.getLongOpt(), cmd.getOptionValue(opt.getLongOpt()));
                    }
                }

                if (remainder.size() > 1) {
                    this.action.setFiles(remainder.subList(1, remainder.size()));
                }

                ActionArchiver.ErrorType err = this.action.verify();
                if (err == ActionArchiver.ErrorType.ERROR) {
                    System.err.println(programName + ": error: " + this.action.getErrorString());
                    System.exit(1);
                } else if (err == ActionArchiver.ErrorType.WARNING) {
                    System.err.println(programName + ": warning: " + this.action.getErrorString());
                }
            }
        } catch (ParseException exc) {
            System.err.println(programName + ": " + exc.getMessage());
            System.exit(1);
        }
    }

    ActionArchiver getAction() {
        return action;
    }

    String getProgramName() {
        return programName;
    }

    private void initializeSubcommands() {
        subcommands = new Options();
        OptionGroup subcommandsGroup = new OptionGroup();

        for (ActionArchiver.ActionType actionType : ActionArchiver.ActionType.values()) {
            subcommandsGroup.addOption(new Option(actionType.getName(), false, actionType.getDescription()));
        }

        subcommandsGroup.addOption(new Option("help", false, "print this message"));
        subcommandsGroup.setRequired(true);
        subcommands.addOptionGroup(subcommandsGroup);
    }

    private final String programName = "ZipArchiver";
    private Options subcommands;
    private ActionArchiver action;
}
