package com.company;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipOutputStream;

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
        ZipOutputStream zos;
        try {
            zos = new ZipOutputStream(new BufferedOutputStream(Files.newOutputStream(target.normalize())));
            zos.setComment(getOptionValueMapping().getOrDefault("comment", null));
        } catch (IOException exc) {
            setErrorString("cannot create new archive: " + exc.toString());
            return false;
        }
        creator.setZipOutputStream(zos);

        boolean success = writeFiles(zos);
        success = closeZipOutputStream(zos) && success;
        if ( !success) {
            try {
                Files.delete(target);
            } catch (IOException exc) {
                setErrorString("cannot remove invalid archive " + target + ": " + exc.toString());
            }
        }
        return success;
    }

    @Override
    ErrorType verify() {
        if (getTargetArchiveName() == null) {
            setErrorString("target is missed");
            return ErrorType.ERROR;
        }
        if (getFiles().isEmpty()) {
            setErrorString("there should be at least one file");
            return ErrorType.ERROR;
        }
        return ErrorType.VALID;
    }
}
