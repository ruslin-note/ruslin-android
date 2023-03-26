/**
 * Copyright (C) 2021 Kyant0
 *
 * @link https://github.com/Kyant0/MusicYou
 * @author Kyant0
 */

package org.dianqk.ruslin.ui.theme.palette.colorspace.ciexyz

import org.dianqk.ruslin.ui.theme.palette.util.div
import org.dianqk.ruslin.ui.theme.palette.util.times

data class CieXyz(
    val x: Double,
    val y: Double,
    val z: Double,
) {

    inline val xyz: DoubleArray
        get() = doubleArrayOf(x, y, z)

    inline val luminance: Double
        get() = y

    operator fun times(luminance: Double): CieXyz = (xyz * luminance).asXyz()

    operator fun div(luminance: Double): CieXyz = (xyz / luminance).asXyz()

    companion object {

        internal fun DoubleArray.asXyz(): CieXyz = CieXyz(this[0], this[1], this[2])
    }
}
