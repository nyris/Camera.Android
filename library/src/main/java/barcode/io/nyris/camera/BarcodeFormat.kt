package io.nyris.camera

import net.sourceforge.zbar.Symbol
import java.io.Serializable
import java.util.*

/**
 * BarcodeFormat.kt - Class that contain barcode format
 *
 * @author Sidali Mellouk
 * Created by nyris GmbH
 * Copyright © 2018 nyris GmbH. All rights reserved.
 */
class BarcodeFormat internal constructor(val id: Int, val name: String) : Serializable {
    companion object {
        val NONE = BarcodeFormat(Symbol.NONE, "NONE")
        val PARTIAL = BarcodeFormat(Symbol.PARTIAL, "PARTIAL")
        val EAN8 = BarcodeFormat(Symbol.EAN8, "EAN8")
        val UPCE = BarcodeFormat(Symbol.UPCE, "UPCE")
        val ISBN10 = BarcodeFormat(Symbol.ISBN10, "ISBN10")
        val UPCA = BarcodeFormat(Symbol.UPCA, "UPCA")
        val EAN13 = BarcodeFormat(Symbol.EAN13, "EAN13")
        val ISBN13 = BarcodeFormat(Symbol.ISBN13, "ISBN13")
        val I25 = BarcodeFormat(Symbol.I25, "I25")
        val DATABAR = BarcodeFormat(Symbol.DATABAR, "DATABAR")
        val DATABAR_EXP = BarcodeFormat(Symbol.DATABAR_EXP, "DATABAR_EXP")
        val CODABAR = BarcodeFormat(Symbol.CODABAR, "CODABAR")
        val CODE39 = BarcodeFormat(Symbol.CODE39, "CODE39")
        val PDF417 = BarcodeFormat(Symbol.PDF417, "PDF417")
        val QRCODE = BarcodeFormat(Symbol.QRCODE, "QRCODE")
        val CODE93 = BarcodeFormat(Symbol.CODE93, "CODE93")
        val CODE128 = BarcodeFormat(Symbol.CODE128, "CODE128")

        val ALL_FORMATS: MutableList<BarcodeFormat> = ArrayList()

        init {
            ALL_FORMATS.add(BarcodeFormat.PARTIAL)
            ALL_FORMATS.add(BarcodeFormat.EAN8)
            ALL_FORMATS.add(BarcodeFormat.UPCE)
            ALL_FORMATS.add(BarcodeFormat.ISBN10)
            ALL_FORMATS.add(BarcodeFormat.UPCA)
            ALL_FORMATS.add(BarcodeFormat.EAN13)
            ALL_FORMATS.add(BarcodeFormat.ISBN13)
            ALL_FORMATS.add(BarcodeFormat.I25)
            ALL_FORMATS.add(BarcodeFormat.DATABAR)
            ALL_FORMATS.add(BarcodeFormat.DATABAR_EXP)
            ALL_FORMATS.add(BarcodeFormat.CODABAR)
            ALL_FORMATS.add(BarcodeFormat.CODE39)
            ALL_FORMATS.add(BarcodeFormat.PDF417)
            ALL_FORMATS.add(BarcodeFormat.QRCODE)
            ALL_FORMATS.add(BarcodeFormat.CODE93)
            ALL_FORMATS.add(BarcodeFormat.CODE128)
        }

        fun getFormatById(id: Int): BarcodeFormat {
            for (format in ALL_FORMATS) {
                if (format.id == id) {
                    return format
                }
            }
            return BarcodeFormat.NONE
        }
    }
}
