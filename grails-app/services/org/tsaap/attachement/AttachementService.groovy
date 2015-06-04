package org.tsaap.attachement

import groovy.transform.ToString
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import org.tsaap.notes.Context
import org.tsaap.notes.Note
import org.tsaap.uploadImage.DataIdentifier
import org.tsaap.uploadImage.DataRecord
import org.tsaap.uploadImage.DataStore

import javax.imageio.ImageIO
import javax.imageio.ImageReader
import javax.imageio.stream.MemoryCacheImageInputStream
import javax.mail.internet.MimeUtility

class AttachementService {

    static transactional = false
    DataStore dataStore

    /**
     * Create an Attachement object from a HTTP request
     * with join file
     * @param file the file from the request
     * @return Attachement object
     */
    @Transactional
    Attachement createAttachementForMultipartFile(
            MultipartFile file) {
        if (!file || file.isEmpty()) {
            throw new AttachementUploadException("file.empty")
        }
        if (!file.name) {
            throw new AttachementUploadException("file.name.null")
        }

        return createAttachement(
                new AttachementDto(
                        size: file.size,
                        typeMime: file.contentType,
                        name: file.originalFilename,
                        originalFileName: file.originalFilename,
                        bytes: file.bytes
                ), 10)
    }




    @Transactional
    Attachement createAttachement(AttachementDto attachementDto,
                                  def maxSizeEnMega = 10) {

        if (attachementDto.size > 1024 * 1024 * maxSizeEnMega) {
            throw new AttachementUploadException("file.toobig")
        }
        Attachement attachement = new Attachement(
                size: attachementDto.size,
                typeMime: attachementDto.typeMime,
                name: attachementDto.name,
                originalName: attachementDto.originalFileName,
                toDelete: true
        )

        DataRecord dataRecord = dataStore.addRecord(attachementDto.inputStream)
        attachement.path = dataRecord.identifier.toString()
        if (attachement.imageIsDisplayable()) {
            attachement.dimension = determinerDimension(attachementDto.inputStream)
        }
        attachement.save()
        return attachement
    }

    /**
     * Create an Attachement from an ImageIds object
     * @param file ImageIds object type
     * @return Attachement object
     */
    @Transactional
    Attachement createAttachementForImageIds(
            ImageIds file) {

        Attachement attachement = new Attachement(
                size: file.size,
                typeMime: file.contentType,
                name: file.fileName,
                originalName: file.fileName,
                toDelete: true
        )
        attachement.path = file.dataSoreId
        attachement.save()
        return attachement
    }

    /**
     * Return File object matching to an Attachement
     * @param attachement the attachement
     * @return File object type
     */
    InputStream getInputStreamForAttachement(Attachement attachement) {
        DataRecord dataRecord = dataStore.getRecord(new DataIdentifier(attachement.path))
        dataRecord.stream
    }

    /**
     * Encode in base 64 an attachement
     * @param attachement attachement to encode
     * @return the encode attachement
     */
    String encodeToBase64(Attachement attachement) {
        ByteArrayOutputStream bos
        OutputStream b64os
        try {
            bos = new ByteArrayOutputStream()
            b64os = MimeUtility.encode(bos, 'base64')
            b64os << getInputStreamForAttachement(attachement)
            b64os.flush()
            bos.toString()
        } finally {
            bos?.close()
            b64os?.close()
        }
    }

    /**
     * Determine image dimensions
     * @param imageFile file to analyse
     * @param fileName file original name
     * @return file dimensions
     */
    private Dimension determinerDimension(InputStream inputStream) {

        ImageReader reader

        try {
            def memInputStream = new MemoryCacheImageInputStream(inputStream)
            def imageReaders = ImageIO.getImageReaders(memInputStream)

            if (imageReaders.hasNext()) {
                reader = imageReaders.next()
                reader.input = memInputStream
                return new Dimension(
                        width: reader.getWidth(reader.minIndex),
                        height: reader.getHeight(reader.minIndex)
                )
            }

        } finally {
            reader?.dispose()
        }
    }


    /**
     * Add file to attachment
     * @param file the file to attach
     * @param note the note
     * @return the attached attachment
     */
    Attachement addFileToNote(MultipartFile file, Note note) {
        Attachement attachement = createAttachementForMultipartFile(file)
        addNoteToAttachement(note,attachement)
        attachement
    }

    /**
     * Add a note to an attachment
     * @param myNote the note to add
     * @param myAttachement the attachment
     * @return the modified attachment
     */
    Attachement addNoteToAttachement(Note myNote, Attachement myAttachement) {
        myAttachement.context = myNote.context
        myAttachement.note = myNote
        myAttachement.toDelete = false
        myAttachement.save()
        myAttachement
    }

    Map<Note, Attachement> searchAttachementInNoteList(List<Note> noteList) {
        Map<Note, Attachement> result = new HashMap<Note, Attachement>()
        noteList.each {
            if (Attachement.findByNote(it)) {
                Attachement theAttachement = Attachement.findByNote(it)
                result.put(it, theAttachement)
            }
        }
        result
    }

    /**
     * Detach an attachment
     * @param myAttachement the attachment to detach
     * @return the detached attachment
     */
    Attachement detachAttachement(Attachement myAttachement) {
        if (myAttachement.context != null) {
            myAttachement.context = null
        }
        myAttachement.note = null
        myAttachement.toDelete = true
        myAttachement.save(flush: true)
        myAttachement
    }

    /**
     * Check if there are attachment to delete and delete them in this case.
     */
    def deleteAttachementAndFileInSystem() {
        def attachementToRemoveList = Attachement.findAllByToDelete(true)
        println attachementToRemoveList
        def iteratorAttachement = attachementToRemoveList.iterator()
        while(iteratorAttachement.hasNext()) {
            def attachementToDelete = iteratorAttachement.next()
            boolean deleteInSystem = true
            Attachement.findAllByPathAndIdNotEqual(attachementToDelete.path,attachementToDelete.id).each {
                if(!it.toDelete) {
                   deleteInSystem = false
                }
                else {
                    attachementToRemoveList.remove(it)
                    it.delete(flush: true)
                }
            }
            if(deleteInSystem) {
                String attachementPath = attachementToDelete.path
                String finalPath = "/opt/shared/tsaap-repo/${attachementPath.substring(0,2)}/${attachementPath.substring(2,4)}/" +
                        "${attachementPath.substring(4,6)}/$attachementPath"
                new File(finalPath).delete()
            }
            attachementToDelete.delete(flush: true)
        }
    }
}

/**
 * Class reprensenting an image already upload in DataStore
 */
@ToString
class ImageIds {
    String sourceId
    String fileName
    String dataSoreId
    String contentType
    Long size

}