@file:Suppress("MemberVisibilityCanBePrivate", "ConstantConditionIf")

package v2

import processing.core.PApplet
import processing.core.PConstants

interface Drawable {
    /**
     * Implementation is required to push and pop the transformation matrix.
     */
    fun draw(parent: PApplet)
}

fun PApplet.translate(x: Int, y: Int) = translate(x.toFloat(), y.toFloat())
fun PApplet.square(x: Int, y: Int, size: Int) = square(x.toFloat(), y.toFloat(), size.toFloat())

private const val WIDTH: Int = 1700 // +50px either side
private const val HEIGHT: Int = 2900 // +50px either side
private const val WIDTH_OFFSET: Float = 100f
private const val HEIGHT_OFFSET: Float = 100f
private const val TILE_WIDTH: Int = 300
private const val TILE_HEIGHT: Int = 300

object Main : PApplet() {

    private const val display: Boolean = false

    private val fileName: String = "render-${System.currentTimeMillis()}.pdf"

    private val tiles: MutableList<Drawable> = ArrayList()

    override fun settings() {
        if (display)
            size(WIDTH, HEIGHT)
        else
            size(WIDTH, HEIGHT, PDF, fileName)
    }

    override fun setup() {
        // LOAD DRAWABLES
        defaultBoardInformation.informationMap.map { it.value.toTile(it.key) }.forEach { tiles.add(it) }
        // SETUP DRAW ENV
        noLoop()
    }

    override fun draw() {
        println("Saving as $fileName")
        println("Starting ...")

        background(255)
        translate(WIDTH_OFFSET, HEIGHT_OFFSET)
        tiles.forEach { it.draw(this) }

        println("Finished.")
        if (!display)
            exit()
    }

    fun run() = runSketch()
}

fun Information.toTile(position: Position): Tile = when (this) {
    is InputInformation -> InputTile(position, state, outputs)
    is OutputInformation -> OutputTile(position, expectedState, inputs)
    else -> throw IllegalArgumentException()
    // TODO expand
}

open class Tile(val position: Position) : Drawable {

    override fun draw(parent: PApplet) {
        with(parent) {
            pushMatrix()
            translate(position.x * TILE_WIDTH, position.y * TILE_HEIGHT)
            strokeWeight(12f)
            square(0, 0, TILE_HEIGHT)
            popMatrix()
        }
    }

}

class InputTile(position: Position, val state: Boolean, val outputs: Set<Compass16>) : Tile(position) {

    override fun draw(parent: PApplet) {
        super.draw(parent)
        with(parent) {
            pushMatrix()
            translate(position.x * TILE_WIDTH, position.y * TILE_HEIGHT)
            // TODO add lines
            // line(150f, 100f, 150f, 300f)
            val img = if (state)
                loadImage("button-on.png")
            else
                loadImage("button-off.png")
            imageMode(PConstants.CENTER)
            image(img, 150f, 100f, 140f, 140f)
            popMatrix()
        }
    }
}

class OutputTile(position: Position, val state: Boolean, val inputs: Set<Compass16>) : Tile(position) {

    override fun draw(parent: PApplet) {
        super.draw(parent)
        with(parent) {
            pushMatrix()
            translate(position.x * TILE_WIDTH, position.y * TILE_HEIGHT)
            // TODO add lines
            imageMode(PConstants.CENTER)
            if (state)
                image(loadImage("light-on.jpg"), 150f, 180f, 130f, 190f)
            else
                image(loadImage("light-off.jpg"), 150f, 180f, 130f, 190f)
            popMatrix()
        }
    }
}

class ConnectorTile(position: Position, val connections: Set<Connection>) : Tile(position) {
    override fun draw(parent: PApplet) {
        super.draw(parent)
        with(parent) {
            pushMatrix()
            translate(position.x * TILE_WIDTH, position.y * TILE_HEIGHT)
            // TODO
            popMatrix()
        }
    }
}

// TODO Binary and Unary gate tiles

fun main() {
    Main.run()
}