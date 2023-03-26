/**
 * Copyright (C) 2021 Kyant0
 *
 * @link https://github.com/Kyant0/MusicYou
 * @author Kyant0
 */

package org.dianqk.ruslin.ui.theme.palette.core

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import org.dianqk.ruslin.ui.theme.palette.colorspace.zcam.Zcam

data class ZcamLch(
    val L: Double,
    val C: Double,
    val h: Double,
) {

    @Composable
    fun toZcam(): Zcam = zcamLch(L = L, C = C, h = h)

    companion object {

        @Composable
        fun Color.toZcamLch(): ZcamLch = toRgb().toZcam().toZcamLch()

        fun Zcam.toZcamLch(): ZcamLch = ZcamLch(L = Jz, C = Cz, h = hz)
    }
}
