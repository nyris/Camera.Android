package io.nyris.camera

import java.io.Serializable

/**
 *
 *
 * @author Sidali Mellouk
 * Created by nyris GmbH
 * Copyright © 2018 nyris GmbH. All rights reserved.
 */

class Barcode : Serializable {
    var contents: String? = null
    var format: BarcodeFormat? = null
}