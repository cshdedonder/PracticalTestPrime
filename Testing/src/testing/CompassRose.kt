package testing

import testing.CompassRose4.*

interface ICompassRose4
interface ICompassRose8
interface ICompassRose16

enum class CompassRose4 : ICompassRose4, ICompassRose8, ICompassRose16 {
    N, E, S, W
}

enum class CompassRose8 : ICompassRose8, ICompassRose16 {
    NE, SE, SW, NW
}

enum class CompassRose16: ICompassRose16 {
    NNE, ENE, ESE, SSE, SSW, WSW, WNW, NNW;

    val opposite: CompassRose16
        get() = when (this) {
            NNE -> SSE
            ENE -> WNW
            ESE -> WSW
            SSE -> NNE
            SSW -> NNW
            WSW -> ESE
            WNW -> ENE
            NNW -> SSW
        }

    fun reduceToCompassRose4(): CompassRose4 = when (this) {
        NNE -> N
        ENE -> E
        ESE -> E
        SSE -> S
        SSW -> S
        WSW -> W
        WNW -> W
        NNW -> N
    }

    fun rotate(rotation: Rotation): CompassRose16 = when (rotation) {
        Rotation.ROTATE_ZERO -> this
        Rotation.ROTATE_HALF_PI -> when (this) {
            NNE -> ESE
            ENE -> SSE
            ESE -> SSW
            SSE -> WSW
            SSW -> WNW
            WSW -> NNW
            WNW -> NNE
            NNW -> ENE
        }
        Rotation.ROTATE_PI -> rotate(Rotation.ROTATE_HALF_PI).rotate(Rotation.ROTATE_HALF_PI)
        Rotation.ROTATE_NEG_PI -> rotate(Rotation.ROTATE_PI).rotate(Rotation.ROTATE_HALF_PI)
    }
}

enum class Rotation {
    ROTATE_ZERO, ROTATE_HALF_PI, ROTATE_PI, ROTATE_NEG_PI
}