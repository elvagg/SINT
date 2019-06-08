package org.opwo.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import groovy.xml.MarkupBuilder

import java.nio.file.NoSuchFileException
import java.security.MessageDigest
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.Path
import java.io.File
import java.time.*
import java.time.format.DateTimeFormatter


class Metalink extends DefaultTask {
    private String fileSet
    private String url
    private String outputFile
    private ArrayList<Path> filePaths = new ArrayList<Path>()

    def generateMD5(byte[] content) {
        return MessageDigest.getInstance("MD5").digest(content).encodeHex().toString()
    }

    def writeFile() {
        def fw = new FileWriter(outputFile)
        def xml = new groovy.xml.MarkupBuilder(fw)
        def projectdir = Paths.get(fileSet)
        def filePaths = this.filePaths
        def baseUrl = this.url
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        try {
            Files.walk(Paths.get(this.fileSet)).filter(Files.&isRegularFile).forEach(filePaths.&add)
        } catch(NoSuchFileException ex){
            println "Files not found"
        }

        xml.mkp.xmlDeclaration(version: "1.0", encoding: "utf-8")
        xml.metalink("xmlns":"urn:ietf:params:xml:ns:metalink"){
            LocalDateTime date = LocalDateTime.now()
            published(date.format(formatter))
            for(filePath in filePaths) {
                File file = new File(filePath.toString())
                String relative = projectdir.relativize(filePath)
                byte[] fileBytes = Files.readAllBytes(filePath)
                URL finalUrl = new URL(baseUrl + "/" + relative.replace("\\", "/"))
                xml.file("name": file.name) {
                    size(fileBytes.length)
                    hash("type": "md5", generateMD5(fileBytes))
                    xml.url(finalUrl)
                }
            }
        }
    }


    @TaskAction
    def mainTask() {
        if (url == null){
            setUrl(project.properties['serverFilesUrl'])
        }
        this.writeFile()
    }

    def setFileSet(String fileSet) {
        File f = new File(fileSet)
        this.fileSet = f.getCanonicalPath()
        println "Content of fileSet under path " + this.fileSet + " has been written to metalink file zad-opwo.xml in current directory."
    }

    def setUrl(String url) { this.url = url }

    def setOutputFile(String outputFile) {
        File f = new File(outputFile)
        this.outputFile = f.getPath()
    }
}