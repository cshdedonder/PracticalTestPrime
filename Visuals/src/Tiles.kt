import CompassRose16.*
import processing.core.PApplet
import processing.core.PConstants
import processing.core.PConstants.HALF_PI
import kotlin.math.PI

data class Coordinate(val x: Float, val y: Float)

class ConnectorTile(private val topLeft: Coordinate, vararg connections: Pair<CompassRose16, CompassRose16>) : Drawable {

    private val connections: List<Pair<CompassRose16, CompassRose16>> = connections.asList()

    // TODO split in four cases: line, arc, ellips, bezier
    override fun draw(parent: PApplet) {
        with(parent) {
            pushMatrix()
            translate(topLeft.x + tileWidth / 2, topLeft.y + tileHeight / 2) // Move to the tile's centre
            for (c in connections) {
                pushMatrix()
                rotate(rotationMap.getValue(c.first).toFloat()) // Rotate to SSE position
                if (c.first !in secondarySet) { // Mirror if secondary node
                    translate(0.5f, 1.5f)
                    when (primaryDistanceMap.entries.filter { c in it.key }.map { it.value }.first()) {
                        1 -> arc(1f, 0f, 2f, 2f, PConstants.PI, PConstants.PI + HALF_PI)
                        2 -> arc(1f, 0f, 2f, 4f, PConstants.PI, PConstants.PI + HALF_PI)
                        3 -> line(0f, 0f, 0f, -3f)
                        4 -> bezier(0f, 0f, 0f, -1.5f, -1f, -1.5f, -1f, -3f)
                        5 -> arc(-2f, 0f, 4f, 4f, -HALF_PI, 0f)
                        6 -> arc(-2f, 0f, 4f, 2f, -HALF_PI, 0f)
                    }
                } else {
                    translate(-0.5f, 1.5f)
                    when (secondaryDistanceMap.entries.filter { c in it.key }.map { it.value }.first()) {
                        1 -> arc(-1f, 0f, 2f, 2f, -HALF_PI, 0f)
                        2 -> arc(-1f, 0f, 2f, 4f, -HALF_PI, 0f)
                        3 -> line(0f, 0f, 0f, -3f)
                        4 -> bezier(0f, 0f, 0f, -1.5f, 1f, -1.5f, 1f, 3f)
                        5 -> arc(2f, 0f, 4f, 4f, PConstants.PI, PConstants.PI + HALF_PI)
                        6 -> arc(2f, 0f, 4f, 2f, PConstants.PI, PConstants.PI + HALF_PI)
                    }
                }
                popMatrix()
            }
            popMatrix()
        }
    }

    companion object {
        // Base value is SSE
        private val rotationMap: Map<CompassRose16, Double> = mapOf(
                SSE to 0.0,
                SSW to 0.0,
                WSW to PI / 2,
                WNW to PI / 2,
                NNW to PI,
                NNE to PI,
                ENE to -PI / 2,
                ESE to -PI / 2
        )
        private val secondarySet: Set<CompassRose16> = setOf(SSW, WNW, NNE, ESE)

        private val primaryDistanceMap: Map<Set<Pair<CompassRose16, CompassRose16>>, Int> = mapOf(
                setOf(SSE to ESE, ENE to NNE, NNW to WNW, WSW to SSW) to 1,
                setOf(SSE to ENE, ENE to NNW, NNW to WSW, WSW to SSE) to 2,
                setOf(SSE to NNE, ENE to WNW, NNW to SSW, WSW to ESE) to 3,
                setOf(SSE to NNW, ENE to WSW, NNW to SSE, WSW to ENE) to 4,
                setOf(SSE to WNW, ENE to SSW, NNW to ESE, WSW to NNE) to 5,
                setOf(SSE to WSW, ENE to SSE, NNW to ENE, WSW to NNW) to 6
        )
        private val secondaryDistanceMap: Map<Set<Pair<CompassRose16, CompassRose16>>, Int> = mapOf(
                setOf(SSW to WSW, WNW to NNW, NNE to ENE, ESE to SSE) to 1,
                setOf(SSW to WNW, WNW to NNE, NNE to ESE, ESE to SSW) to 2,
                setOf(SSW to NNW, WNW to ENE, NNE to SSE, ESE to WSW) to 3,
                setOf(SSW to NNE, WNW to ESE, NNE to SSW, ESE to WNW) to 4,
                setOf(SSW to ENE, WNW to SSE, NNE to WSW, ESE to NNW) to 5,
                setOf(SSW to ESE, WNW to SSW, NNE to WNW, ESE to NNE) to 6
        )

        private const val unit = 1f
        private const val tileWidth = 3f
        private const val tileHeight = 3f
    }
}

interface ICompassRose4
interface ICompassRose8
interface ICompassRose16

enum class CompassRose4 : ICompassRose4, ICompassRose8, ICompassRose16 {
    N, E, S, W
}

enum class CompassRose8 : ICompassRose8, ICompassRose16 {
    NE, SE, SW, NW
}

enum class CompassRose16 : ICompassRose16 {
    NNE, ENE, ESE, SSE, SSW, WSW, WNW, NNW;

    companion object {
        fun distanceBetween(start: CompassRose16, end: CompassRose16): Int {
            val values: Array<CompassRose16> = values()
            // TODO clean
            return (values.size + (values.indexOf(end) - values.indexOf(start)) % values.size) % values.size

        }
    }
}