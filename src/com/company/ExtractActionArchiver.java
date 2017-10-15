package com.company;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

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

    @Override
    boolean exec() {
        Path target = Paths.get(getTargetArchiveName());
        if ( !Files.exists(target)) {
            setErrorString(getTargetArchiveName() + " does not exist");
            return false;
        }
        Path targetDirectory;
        if (getOptionValueMapping().containsKey("d")) {
            targetDirectory = Paths.get(getOptionValueMapping().get("d"));
        } else {
            if (target.getNameCount() == 1) {
                targetDirectory = Paths.get("");
            } else {
                targetDirectory = target.getParent();
            }
        }
        ZipFile zf = openZipFile();
        if (zf == null) {
            return false;
        }
        try {
            for (Enumeration<? extends ZipEntry> entries = zf.entries(); entries.hasMoreElements();) {
                ZipEntry entry = entries.nextElement();
                Path path = targetDirectory.resolve(Paths.get(entry.getName()));
                if (path.getNameCount() > 1) {
                    Files.createDirectories(path.getParent());
                }
                Files.copy(new BufferedInputStream(zf.getInputStream(entry)), path);
            }
        } catch (IOException exc) {
            setErrorString("cannot extract zip archive: " + exc.toString());
            closeZipFile(zf);
            return false;
        }
        return closeZipFile(zf);
    }

    @Override
    ErrorType verify() {
        if (getTargetArchiveName() == null) {
            setErrorString("target is missed");
            return ErrorType.ERROR;
        }
        if ( !getFiles().isEmpty()) {
            setErrorString("next arguments were dropped: " + getFiles()
                    .stream()
                    .reduce((prev, next) -> prev + ", " + next)
                    .get());
            setFiles(new LinkedList<>());
            return ErrorType.WARNING;
        }
        return ErrorType.VALID;
    }
}
