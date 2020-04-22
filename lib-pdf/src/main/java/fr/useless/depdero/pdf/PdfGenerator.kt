package fr.useless.depdero.pdf

import android.content.Context
import android.graphics.Bitmap
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.font.PDType1Font
import com.tom_roush.pdfbox.pdmodel.graphics.image.JPEGFactory
import com.tom_roush.pdfbox.util.PDFBoxResourceLoader
import fr.useless.depdero.core.model.Reason
import fr.useless.depdero.core.tool.toDMY
import fr.useless.depdero.core.tool.toH
import fr.useless.depdero.core.tool.tom
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*

class Certificate {

    lateinit var personName: String
    lateinit var birthDate: String
    lateinit var birthCity: String
    lateinit var streetAddress: String
    lateinit var zipCode: String
    lateinit var city: String

    lateinit var departureDate: Date

    val fullAddress : String
    get() = "$streetAddress, $zipCode $city"

    var reason : Reason? = null

    suspend fun generatePdfAsync(context: Context, bitmap: Bitmap, miniBitmap : Bitmap, creationDate: Date) = withContext(
        Dispatchers.IO) {
        async {
            generatePdf(context, bitmap, miniBitmap, creationDate)
        }
    }

    private fun generatePdf(context: Context,
                            bitmap: Bitmap,
                            miniBitmap: Bitmap,
                            creationDate: Date = Date()
    ): String {
        try {
            val document =
                PDDocument.load(context.assets.open("attestation-deplacement-fr.pdf"))
            val documentCatalog = document.documentCatalog
            val acroForm = documentCatalog.acroForm

            acroForm.setTextValue(NAME_FIELD, personName)
            acroForm.setTextValue(BIRTH_CITY_FIELD, birthCity)
            acroForm.setTextValue(BIRTH_DATE_FIELD, birthDate)
            acroForm.setTextValue(ADDRESS_FIELD, fullAddress)
            acroForm.setTextValue(CITY_FIELD, city)
            val date = departureDate
            acroForm.setTextValue(DATE_FIELD, date.toDMY())
            acroForm.setTextValue(HOUR_FIELD, date.toH())
            acroForm.setTextValue(MINUTE_FIELD, date.tom())

            reason?.let {
                val fieldName = reasonToPdfCheckBoxName(it)
                acroForm.setCheckValue(fieldName, true)
            }

            val bos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100 /*ignored for PNG*/, bos)
            val bitmapdata: ByteArray = bos.toByteArray()
            val bs = ByteArrayInputStream(bitmapdata)

            val page = documentCatalog.pages[1]
            val contentStream = PDPageContentStream(document, page, true, true)
            val pdImage = JPEGFactory.createFromStream(document, bs)
            val miniImagePd = JPEGFactory.createFromImage(document, miniBitmap)

            contentStream.drawImage(pdImage, 50f, 485f)
            contentStream.close()

            // Masquage de la signature
            val page1 = documentCatalog.pages[0]
            val contenStream1 = PDPageContentStream(document, page1, true, true)
            contenStream1.addRect(70f, 140f, 150f, 30f)
            contenStream1.setNonStrokingColor(255, 255, 255)
            contenStream1.fill()
            contenStream1.drawImage(miniImagePd, 425f, 153f)

            contenStream1.beginText()
            contenStream1.setNonStrokingColor(0, 0, 0)
            contenStream1.newLineAtOffset(458f, 144f)
            contenStream1.setFont(PDType1Font.HELVETICA, 8f)
            contenStream1.showText("Date de création :")
            contenStream1.endText()

            // Date de génération
            contenStream1.beginText()
            contenStream1.setNonStrokingColor(0, 0, 0)
            contenStream1.newLineAtOffset(452f, 136f)
            contenStream1.setFont(PDType1Font.HELVETICA, 8f)
            contenStream1.showText("${creationDate.toDMY()} à ${creationDate.toH()}h${creationDate.tom()}")
            contenStream1.endText()
            contenStream1.close()

            val horodatage = Date().time
            val path = context.filesDir.absolutePath + "/" + "attestation_deplacement_$horodatage.pdf"
            document.save(path)
            document.close()
            Timber.d("Saved to $path")

            return path
        } catch (exception: IOException) {
            Timber.e(exception)
        }
        return ""
    }

    class Builder

    companion object {
        const val NAME_FIELD = "Nom et prénom"
        const val BIRTH_DATE_FIELD = "Date de naissance"
        const val BIRTH_CITY_FIELD = "Lieu de naissance"
        const val ADDRESS_FIELD = "Adresse actuelle"
        const val CITY_FIELD = "Ville"
        const val DATE_FIELD = "Date"
        const val HOUR_FIELD = "Heure"
        const val MINUTE_FIELD = "Minute"

        private const val WORK_REASON = "Déplacements entre domicile et travail"
        private const val BUY_REASON = "Déplacements achats nécéssaires"
        private const val HEALTH_REASON = "Consultations et soins"
        private const val FAMILY_REASON = "Déplacements pour motif familial"
        private const val BRIEF_REASON = "Déplacements brefs (activité physique et animaux)"
        private const val JUSTICE_REASON = "Convcation judiciaire ou administrative"
        private const val MISSION_REASON = "Mission d'intérêt général"

        fun reasonToPdfCheckBoxName(reason: Reason) : String {
            return when(reason) {
                Reason.MISSIONS -> MISSION_REASON
                Reason.COURSES -> BUY_REASON
                Reason.FAMILLE -> FAMILY_REASON
                Reason.JUDICIAIRE -> JUSTICE_REASON
                Reason.SANTE -> HEALTH_REASON
                Reason.SPORT -> BRIEF_REASON
                Reason.TRAVAIL -> WORK_REASON
            }
        }

        fun loadPdfResources(context: Context) {
            PDFBoxResourceLoader.init(context)
        }
    }
}