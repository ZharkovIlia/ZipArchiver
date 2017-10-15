package com.company;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;

import org.apache.commons.io.IOUtils;


import java.io.IOException;
import java.util.Enumeration;
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

    private boolean copyTargetToStream(ZipOutputStream zos) {
        ZipFile zf;
        try {
            zf = new ZipFile(getTargetArchiveName());
        } catch (IOException exc) {
            setErrorString("cannot read from zip file " + getTargetArchiveName() + ": " + exc.toString());
            return false;
        }
        try {
            for (Enumeration<? extends ZipEntry> entries = zf.entries(); entries.hasMoreElements();) {
                ZipEntry entry = entries.nextElement();
                zos.putNextEntry(entry);
                IOUtils.copy(zf.getInputStream(entry), zos);
                zos.closeEntry();
            }
        } catch (IOException exc) {
            setErrorString("cannot copy zip archive: " + exc.toString());
            try {
                zf.close();
            } catch (IOException e) {
                setErrorString("cannot close zip archive: " + e.toString());
            }
            return false;
        }
        return true;
    }
}
