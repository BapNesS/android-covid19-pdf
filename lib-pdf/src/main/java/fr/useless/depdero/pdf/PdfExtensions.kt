package fr.useless.depdero.pdf

import com.tom_roush.pdfbox.pdmodel.interactive.form.PDAcroForm
import com.tom_roush.pdfbox.pdmodel.interactive.form.PDCheckbox
import com.tom_roush.pdfbox.pdmodel.interactive.form.PDTextField

fun PDAcroForm.setTextValue(fieldName: String, value: String) {
    (getField(fieldName) as? PDTextField)?.let {
        it.value = value
        it.isReadonly = true
    }
}

fun PDAcroForm.setCheckValue(fieldName : String, checked: Boolean) {
    (getField(fieldName) as? PDCheckbox)?.let {
        if (checked) { it.check() } else { it.unCheck() }
        it.isReadonly = true
    }
}