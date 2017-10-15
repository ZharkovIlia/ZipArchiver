package com.company;

import java.io.IOException;

import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

class ArchiveCreator extends SimpleFileVisitor<Path> {
    ArchiveCreator() {
        this.pathFromBegin = Paths.get("");
        this.zipOutputStream = null;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        pathFromBegin = pathFromBegin.resolve(dir.getFileName());
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        zipOutputStream.putNextEntry(new ZipEntry(pathFromBegin.resolve(file.getFileName()).toString()));
        Files.copy(file, zipOutputStream);
        zipOutputStream.closeEntry();
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        return super.visitFileFailed(file, exc);
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        if (pathFromBegin.getNameCount() == 1) {
            pathFromBegin = Paths.get("");
        } else {
            pathFromBegin = pathFromBegin.getParent();
        }
        return FileVisitResult.CONTINUE;
    }

    void setZipOutputStream(ZipOutputStream zos) {
        zipOutputStream = zos;
    }

    private ZipOutputStream zipOutputStream;
    private Path pathFromBegin;
}