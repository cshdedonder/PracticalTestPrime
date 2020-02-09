package graph

import graph.Compass4.*
import graph.RotationAngle.*

enum class RotationAngle {
    ZERO, HALF_PI, PI, THREE_HALF_PI
}

interface Compass {
    /**
     * Rotate the compass direction clockwise by [angle].
     */
    fun rotate(angle: RotationAngle): Compass
}

enum class Compass4 : Compass {
    NORTH {
        override fun rotate(angle: RotationAngle): Compass4 = when (angle) {
            ZERO -> NORTH
            HALF_PI -> EAST
            PI -> SOUTH
            THREE_HALF_PI -> WEST
        }
    },
    EAST {
        override fun rotate(angle: RotationAngle): Compass4 = when (angle) {
            ZERO -> EAST
            HALF_PI -> SOUTH
            PI -> WEST
            THREE_HALF_PI -> NORTH
        }
    },
    SOUTH {
        override fun rotate(angle: RotationAngle): Compass4 = when (angle) {
            ZERO -> SOUTH
            HALF_PI -> WEST
            PI -> NORTH
            THREE_HALF_PI -> EAST
        }
    },
    WEST {
        override fun rotate(angle: RotationAngle): Compass4 = when (angle) {
            ZERO -> WEST
            HALF_PI -> NORTH
            PI -> EAST
            THREE_HALF_PI -> SOUTH
        }
    };
}

enum class Compass16 : Compass {
    NNE, ENE, ESE, SSE, SSW, WSW, WNW, NNW;

    override fun rotate(angle: RotationAngle): Compass16 = values()[(values().indexOf(this) + (2 * RotationAngle.values().indexOf(angle))) % values().size]

    val compass4: Compass4
        get() = when (this) {
            NNE -> NORTH
            ENE -> EAST
            ESE -> EAST
            SSE -> SOUTH
            SSW -> SOUTH
            WSW -> WEST
            WNW -> WEST
            NNW -> NORTH
        }

    val opposite: Compass16
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
}