package com.company;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

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

        optionArchiveGroup.addOption(Option.builder()
                .longOpt("append")
                .desc("append additional files")
                .build());

        optionArchiveGroup.addOption(new Option("help", false, "print this message"));

        optionArchiveGroup.setRequired(true);
        options.addOptionGroup(optionArchiveGroup);
    }

    @Override
    String getCLSyntax() {
        return getActionType().getNameOfOption() +
                " (-help|--set-comment|--get-comment|--append) target [files...]";
    }

    @Override
    boolean exec() {
        if (getOptionValueMapping().containsKey("append")) {
            return execAppend();
        } else if (getOptionValueMapping().containsKey("get-comment")) {
            return execGetComment();
        } else if (getOptionValueMapping().containsKey("set-comment")) {
            return execSetComment();
        }
        assert false;
        return true;
    }

    @Override
    ErrorType verify() {
        if (getTargetArchiveName() == null) {
            setErrorString("target is missed");
            return ErrorType.ERROR;
        }
        if ( getOptionValueMapping().containsKey("append") && getFiles().isEmpty()) {
            setErrorString("there should be at least one file");
            return ErrorType.ERROR;
        }
        if ( !getOptionValueMapping().containsKey("append") && !getFiles().isEmpty()) {
            setErrorString("next arguments were dropped: " + getFiles()
                    .stream()
                    .reduce((prev, next) -> prev + ", " + next)
                    .get());
            setFiles(new LinkedList<>());
            return ErrorType.WARNING;
        }
        return ErrorType.VALID;
    }

    private boolean execAppend() {
        Path target = Paths.get(getTargetArchiveName());
        Path curdir = (target.getNameCount() > 1 ? target.getParent() : Paths.get("."));
        Path tempFile = createTemporaryFile(curdir, target.getName(target.getNameCount() - 1).toString());
        if (tempFile == null) {
            return false;
        }

        ZipOutputStream zos = openZipOutputStream(tempFile);
        boolean success = true;
        if (zos == null) {
            success = false;
        }

        success = success && copyTargetToStream(zos) && writeFiles(zos);
        if ( !success) {
            closeZipOutputStream(zos);
            removeTemporaryFile(tempFile);
            return false;
        }
        success = closeZipOutputStream(zos) && moveTemporaryFileToTarget(tempFile, target);
        if ( !success) {
            removeTemporaryFile(tempFile);
            return false;
        }
        return true;
    }

    private boolean execGetComment() {
        ZipFile zf = openZipFile();
        if (zf == null) {
            return false;
        }
        Optional.ofNullable(zf.getComment()).ifPresent(System.out::println);
        return closeZipFile(zf);
    }

    private boolean execSetComment() {
        Path target = Paths.get(getTargetArchiveName());
        Path curdir = (target.getNameCount() > 1 ? target.getParent() : Paths.get("."));
        Path tempFile = createTemporaryFile(curdir, target.getName(target.getNameCount() - 1).toString());
        if (tempFile == null) {
            return false;
        }

        ZipOutputStream zos = openZipOutputStream(tempFile);
        if (zos == null) {
            removeTemporaryFile(tempFile);
            return false;
        }

        if ( !copyTargetToStream(zos)) {
            closeZipOutputStream(zos);
            removeTemporaryFile(tempFile);
            return false;
        }
        zos.setComment(getOptionValueMapping().get("set-comment"));
        boolean success = closeZipOutputStream(zos) && moveTemporaryFileToTarget(tempFile, target);
        if ( !success) {
            removeTemporaryFile(tempFile);
            return false;
        }
        return true;
    }

    private Path createTemporaryFile(Path dir, String name) {
        Path tempFile;
        try {
            tempFile = Files.createTempFile(dir, "." + name, "~");
        } catch (IOException exc) {
            setErrorString("cannot create temporary file: " + exc.toString());
            return null;
        }
        return tempFile;
    }

    private boolean removeTemporaryFile(Path tempFile) {
        try {
            Files.delete(tempFile);
        } catch (IOException exc) {
            setErrorString("cannot remove temporary file: " + exc.toString());
            return false;
        }
        return true;
    }

    private boolean moveTemporaryFileToTarget(Path tempFile, Path target) {
        try {
            Files.move(tempFile, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exc) {
            setErrorString("cannot update existing archive: " + exc.toString());
            return false;
        }
        return true;
    }

    private boolean copyTargetToStream(ZipOutputStream zos) {
        ZipFile zf = openZipFile();
        if (zf == null) {
            return false;
        }
        zos.setComment(zf.getComment());
        try {
            for (Enumeration<? extends ZipEntry> entries = zf.entries(); entries.hasMoreElements();) {
                ZipEntry entry = entries.nextElement();
                zos.putNextEntry(entry);
                IOUtils.copy(zf.getInputStream(entry), zos);
                zos.closeEntry();
            }
        } catch (IOException exc) {
            setErrorString("cannot copy zip archive: " + exc.toString());
            closeZipFile(zf);
            return false;
        }
        return true;
    }
}
