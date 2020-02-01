import kotlin.math.min

interface ICompassRose4
interface ICompassRose8
interface ICompassRose16

enum class CompassRose4: ICompassRose4, ICompassRose8, ICompassRose16 {
    N, E, S, W
}

enum class CompassRose8: ICompassRose8, ICompassRose16 {
    NE, SE, SW, NW
}

enum class CompassRose16: ICompassRose16 {
    NNE, ENE, ESE, SSE, SSW, WSW, WNW, NNW;

    companion object {
        fun distanceBetween(start: CompassRose16, end: CompassRose16): Int {
            val values: Array<CompassRose16> = values()
            // TODO clean
            return min((values.size + values.indexOf(end) - values.indexOf(start)) % values.size, (values.size + values.indexOf(start) - values.indexOf(end)) % values.size)
        }
    }
}