package com.company;

import org.apache.commons.cli.Options;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

abstract class ActionArchiver {
    enum ActionType {
        ARCHIVE("archive"),
        CREATE("create"),
        EXTRACT("extract");

        private final String name;
        ActionType(String name) {
            this.name = name;
        }

        String getName() {
            return name;
        }

        String getNameOfOption() {
            return "-" + name;
        }

        String getDescription() {
            switch (this) {
                case ARCHIVE:
                    return "provide some operations on existing archive";
                case CREATE:
                    return "create new archive";
                case EXTRACT:
                    return "extract existing archive";
            }
            assert false;
            return null;
        }

    }

    enum ErrorType {
        WARNING,
        ERROR,
        VALID
    }

    static ActionArchiver getActionFromType(ActionType type) {
        switch (type) {
            case EXTRACT:
                return new ExtractActionArchiver();
            case ARCHIVE:
                return new ArchiveActionArchiver();
            case CREATE:
                return new CreateActionArchiver();
        }
        assert false;
        return null;
    }

    abstract String getCLSyntax();
    abstract boolean exec();
    abstract ErrorType verify();

    ActionArchiver(ActionType type) {
        this.type = type;
        optionValues = new TreeMap<>();
        options = null;
        target = null;
        files = new LinkedList<>();
        errorString = "unexpected error";
    }

    final Options getOptions() {
        return options;
    }

    final void setTargetArchiveName(String target) {
        this.target = target;
    }

    final String getTargetArchiveName() {
        return target;
    }

    final void setFiles(List<String> files) {
        this.files = files;
    }

    final List<String> getFiles() {
        return files;
    }

    final void setOptionToValue(String option, String value) {
        optionValues.put(option, value);
    }

    final Map<String, String> getOptionValueMapping() {
        return optionValues;
    }

    final ActionType getActionType() {
        return type;
    }

    final void setErrorString(String error) {
        errorString = error;
    }

    final String getErrorString() {
        return errorString;
    }

    ZipFile openZipFile() {
        ZipFile zf;
        try {
            zf = new ZipFile(getTargetArchiveName());
        } catch (IOException exc) {
            setErrorString("cannot read from zip file " + getTargetArchiveName() + ": " + exc.toString());
            return null;
        }
        return zf;
    }

    boolean writeFiles(ZipOutputStream zos) {
        ArchiveCreator creator = new ArchiveCreator();
        creator.setZipOutputStream(zos);
        for (String file : getFiles()) {
            try {
                Files.walkFileTree(Paths.get(file).normalize(), creator);
            } catch (IOException exc) {
                setErrorString("error occurred during archiving " + file + ": " + exc.toString());
                return false;
            }
        }
        return true;
    }

    boolean closeZipOutputStream(ZipOutputStream zos) {
        try {
            zos.close();
        } catch (IOException exc) {
            setErrorString("cannot close zip archive: " + exc.toString());
            return false;
        }
        return true;
    }

    boolean closeZipFile(ZipFile zf) {
        try {
            zf.close();
        } catch (IOException exc) {
            setErrorString("cannot close zip archive: " + exc.toString());
            return false;
        }
        return true;
    }

    protected Options options;

    private String target;
    private List<String> files;
    private Map<String, String> optionValues;
    private final ActionType type;
    private String errorString;
}
