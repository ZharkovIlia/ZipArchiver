package com.company;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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

    @Override
    boolean exec() {
        ArchiveCreator creator = new ArchiveCreator();
        Path target = Paths.get(getTargetArchiveName());
        try {
            creator.createZipOutputStream(target.normalize(),
                    getOptionValueMapping().getOrDefault("comment", null));
        } catch (IOException exc) {
            setErrorString("cannot create new archive: " + exc.toString());
            return false;
        }

        for (String file : getFiles()) {
            try {
                Files.walkFileTree(Paths.get(file).normalize(), creator);
            } catch (IOException exc) {
                setErrorString("error occurred during archiving " + file + ": " + exc.toString());
                try {
                    creator.closeZipOutputStream();
                    Files.delete(target);
                } catch (IOException e) {
                    setErrorString("cannot remove invalid archive " + target + ": " + exc.toString());
                }
                return false;
            }
        }
        try {
            creator.closeZipOutputStream();
        } catch (IOException exc) {
            setErrorString("cannot create new archive: " + exc.toString());
            try {
                Files.delete(target);
            } catch (IOException e) {
                setErrorString("cannot remove invalid archive " + target + ": " + exc.toString());
            }
            return false;
        }
        return true;
    }
}
