package io.nyris.camera

/**
 * IBarcodeListener.kt - Barcode listener triggered when there are barcode
 *
 * @author Sidali Mellouk
 * Created by nyris GmbH
 * Copyright © 2018 nyris GmbH. All rights reserved.
 */
interface IBarcodeListener {
    fun onBarcode(barcode: Barcode)
}