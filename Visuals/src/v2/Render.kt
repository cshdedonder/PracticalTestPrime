@file:Suppress("MemberVisibilityCanBePrivate", "ConstantConditionIf", "KDocUnresolvedReference")

package v2

import processing.core.PApplet
import processing.core.PApplet.*
import processing.core.PConstants
import v2.Compass16.*

interface Drawable {
    /**
     * Implementation is required to push and pop the transformation matrix.
     */
    fun draw(parent: PApplet)
}

private const val BASE_WIDTH: Float = 1500f
private const val BASE_HEIGHT: Float = 2700f
private const val WIDTH_OFFSET: Float = 100f
private const val HEIGHT_OFFSET: Float = 100f
private const val WIDTH: Float = BASE_WIDTH + 2 * WIDTH_OFFSET
private const val HEIGHT: Float = BASE_HEIGHT + 2 * HEIGHT_OFFSET

private const val BASE_TILE_WIDTH: Float = 300f
private const val BASE_TILE_HEIGHT: Float = 300f
private const val TILE_WIDTH_OFFSET: Float = 10f
private const val TILE_HEIGHT_OFFSET: Float = 10f
private const val TILE_WIDTH: Float = BASE_TILE_WIDTH - 2 * TILE_WIDTH_OFFSET
private const val TILE_HEIGHT: Float = BASE_TILE_HEIGHT - 2 * TILE_HEIGHT_OFFSET


object Main : PApplet() {

    private val fileName: String = "render-${System.currentTimeMillis()}.pdf"

    private val tiles: MutableList<Drawable> = ArrayList()

    override fun settings() {
        size(WIDTH.toInt(), HEIGHT.toInt(), PDF, fileName)
    }

    override fun setup() {
        defaultBoardInformation.informationMap.map { it.value.toTile(it.key) }.forEach { tiles.add(it) }
        noLoop()
    }

    override fun draw() {
        println("Saving as $fileName")
        println("Starting ...")

        background(255)

        translate(WIDTH_OFFSET, HEIGHT_OFFSET)
        tiles.forEach { it.draw(this) }

        translate(5 * BASE_TILE_WIDTH, 9 * BASE_TILE_HEIGHT)
        fill(0)
        textAlign(PConstants.RIGHT, PConstants.TOP)
        text("© Cedric De Donder, 2020", -30f, 20f)

        println("Finished.")
        exit()
    }

    fun run() = runSketch()
}

fun Information.toTile(position: Position): Tile = when (this) {
    is InputInformation -> InputTile(position, state, outputs)
    is OutputInformation -> OutputTile(position, expectedState, inputs)
    is ConnectorInformation -> ConnectorTile(position, connections)
    is UnaryGateInformation -> UnaryTile(position, functionDescription, input, outputs)
    is BinaryGateInformation -> BinaryTile(position, functionDescription, input1, input2, outputs)
    else -> throw IllegalArgumentException()
}

inline fun PApplet.layer(init: PApplet.() -> Unit) {
    pushMatrix()
    pushStyle()
    init()
    popStyle()
    popMatrix()
}

/**
 * Assumes ([x1], [y1]) is bottom control point and vertical switch.
 */
fun PApplet.bezierSwitchVertical(x1: Float, y1: Float, x2: Float, y2: Float) {
    bezier(x1, y1, x1, y1 - abs(y1 - y2) / 3, x2, y2 + abs(y1 - y2) / 3, x2, y2)
}

/**
 * Assumes ([x1], [y1]) is left control point for a horizontal switch.
 */
fun PApplet.bezierSwitchHorizontal(x1: Float, y1: Float, x2: Float, y2: Float) {
    bezier(x1, y1, x1 + abs(x1 - x2) / 3, y1, x2 - abs(x1 - x2) / 3, y2, x2, y2)
}

open class Tile(val position: Position) : Drawable {

    override fun draw(parent: PApplet) {
        with(parent) {
            layer {
                translateToTile()
                strokeWeight(12f)
                rect(0f, 0f, TILE_WIDTH, TILE_HEIGHT)
            }
        }
    }

    protected val centerX: Float = TILE_WIDTH / 2f
    protected val centerY: Float = TILE_HEIGHT / 2f

    /**
     * Translate to the 'real' tile canvas. If [relativeToBase] is true, initial position is assumed to be ([position.x]* [BASE_TILE_WIDTH], [position.y] * [BASE_TILE_HEIGHT]; is false, then (0,0) is assumed.
     */
    protected fun PApplet.translateToTile(relativeToBase: Boolean = false) = if (relativeToBase) {
        translate(TILE_WIDTH_OFFSET, TILE_HEIGHT_OFFSET)
    } else {
        translate(position.x * BASE_TILE_WIDTH + TILE_WIDTH_OFFSET, position.y * BASE_TILE_HEIGHT + TILE_HEIGHT_OFFSET)
    }
}

fun Compass16.relativeCoordinates(): Pair<Float, Float> = when (this) {
    NNE -> Pair(TILE_WIDTH * (2f / 3), 0f)
    ENE -> Pair(TILE_WIDTH, TILE_HEIGHT * (1f / 3))
    ESE -> Pair(TILE_WIDTH, TILE_HEIGHT * (2f / 3))
    SSE -> Pair(TILE_WIDTH * (2f / 3), TILE_HEIGHT)
    SSW -> Pair(TILE_WIDTH * (1f / 3), TILE_HEIGHT)
    WSW -> Pair(0f, TILE_HEIGHT * (2f / 3))
    WNW -> Pair(0f, TILE_HEIGHT * (1f / 3))
    NNW -> Pair(TILE_WIDTH * (1f / 3), 0f)
}

class InputTile(position: Position, val state: Boolean, val outputs: Set<Compass16>) : Tile(position) {

    override fun draw(parent: PApplet) {
        super.draw(parent)
        with(parent) {
            layer {
                translateToTile()
                strokeWeight(8f)
                outputs.forEach {
                    val (x, y) = it.relativeCoordinates()
                    bezierSwitchVertical(x, y, centerX, centerY)
                }
                val img = if (state)
                    loadImage("button-on.png")
                else
                    loadImage("button-off.png")
                imageMode(PConstants.CENTER)
                image(img, TILE_WIDTH / 2f, 100f, 130f, 130f)
            }
        }
    }
}

class OutputTile(position: Position, val state: Boolean, val inputs: Set<Compass16>) : Tile(position) {

    override fun draw(parent: PApplet) {
        super.draw(parent)
        with(parent) {
            layer {
                translateToTile()
                strokeWeight(8f)
                inputs.forEach {
                    val (x, y) = it.relativeCoordinates()
                    bezierSwitchVertical(centerX, centerY, x, y)
                }
                imageMode(PConstants.CENTER)
                if (state)
                    image(loadImage("light-on.jpg"), TILE_WIDTH / 2f, 175f, 130f, 190f)
                else
                    image(loadImage("light-off.jpg"), TILE_WIDTH / 2f, 175f, 130f, 190f)
            }
        }
    }
}

operator fun Compass16.inc(): Compass16 = when (this) {
    NNE -> ENE
    ENE -> ESE
    ESE -> SSE
    SSE -> SSW
    SSW -> WSW
    WSW -> WNW
    WNW -> NNW
    NNW -> NNE
}

fun Connection.draw(parent: PApplet) {
    with(parent) {
        layer {
            noFill()
            strokeWeight(8f)
            val (from, to) = this@draw
            val width = TILE_WIDTH
            val height = TILE_HEIGHT
            val width13 = width / 3f
            val height13 = height / 3f
            val width23 = 2 * width13
            val height23 = 2 * height13

            when (from) {
                NNE -> when (to) {
                    NNE, NNW -> throw IllegalArgumentException()
                    ENE -> arc(width, 0f, 2 * width13, 2 * height13, PConstants.HALF_PI, PConstants.PI)
                    ESE -> arc(width, 0f, 2 * width13, 2 * height23, PConstants.HALF_PI, PConstants.PI)
                    SSE -> line(width23, 0f, width23, height)
                    SSW -> bezierSwitchVertical(width13, height, width23, 0f)
                    WSW -> arc(0f, 0f, 2 * width23, 2 * height23, 0f, PConstants.HALF_PI)
                    WNW -> arc(0f, 0f, 2 * width23, 2 * height13, 0f, PConstants.HALF_PI)
                }
                ENE -> when (to) {
                    ENE, ESE -> throw IllegalArgumentException()
                    NNE -> arc(width, 0f, 2 * width13, 2 * height13, PConstants.HALF_PI, PConstants.PI)
                    SSE -> arc(width, height, 2 * width13, 2 * height23, PConstants.PI, PConstants.PI + PConstants.HALF_PI)
                    SSW -> arc(width, height, 2 * width23, 2 * height23, PConstants.PI, PConstants.PI + PConstants.HALF_PI)
                    WSW -> bezierSwitchHorizontal(0f, height23, width, height13)
                    WNW -> line(0f, height13, width, height13)
                    NNW -> arc(width, 0f, 2 * width23, 2 * height13, PConstants.HALF_PI, PConstants.PI)
                }
                ESE -> when (to) {
                    ESE, ENE -> throw IllegalArgumentException()
                    NNE -> arc(width, 0f, 2 * width13, 2 * height23, PConstants.HALF_PI, PConstants.PI)
                    SSE -> arc(width, height, 2 * width13, 2 * height13, PConstants.PI, PConstants.PI + PConstants.HALF_PI)
                    SSW -> arc(width, height, 2 * width23, 2 * height13, PConstants.PI, PConstants.PI + PConstants.HALF_PI)
                    WSW -> line(0f, height23, width, height23)
                    WNW -> bezierSwitchHorizontal(0f, height13, width, height23)
                    NNW -> arc(width, 0f, 2 * width23, 2 * height23, PConstants.HALF_PI, PConstants.PI)
                }
                SSE -> when (to) {
                    SSE, SSW -> throw IllegalArgumentException()
                    NNE -> line(width23, 0f, width23, height)
                    ENE -> arc(width, height, 2 * width13, 2 * height23, PConstants.PI, PConstants.PI + PConstants.HALF_PI)
                    ESE -> arc(width, height, 2 * width13, 2 * height13, PConstants.PI, PConstants.PI + PConstants.HALF_PI)
                    WSW -> arc(0f, height, 2 * width23, 2 * height13, PConstants.PI + PConstants.HALF_PI, PConstants.TWO_PI)
                    WNW -> arc(0f, height, 2 * width23, 2 * height23, PConstants.PI + PConstants.HALF_PI, PConstants.TWO_PI)
                    NNW -> bezierSwitchVertical(width23, height, width13, 0f)
                }
                SSW -> when (to) {
                    SSW, SSE -> throw IllegalArgumentException()
                    NNE -> bezierSwitchVertical(width13, height, width23, 0f)
                    ENE -> arc(width, height, 2 * width23, 2 * height23, PConstants.PI, PConstants.PI + PConstants.HALF_PI)
                    ESE -> arc(width, height, 2 * width23, 2 * height13, PConstants.PI, PConstants.PI + PConstants.HALF_PI)
                    WSW -> arc(0f, height, 2 * width13, 2 * height13, PConstants.PI + PConstants.HALF_PI, PConstants.TWO_PI)
                    WNW -> arc(width, height, 2 * width13, 2 * height23, PConstants.PI + PConstants.HALF_PI, PConstants.TWO_PI)
                    NNW -> line(width13, 0f, width13, height)
                }
                WSW -> when (to) {
                    WSW, WNW -> throw IllegalArgumentException()
                    NNE -> arc(0f, 0f, 2 * width23, 2 * height23, 0f, PConstants.HALF_PI)
                    ENE -> bezierSwitchHorizontal(0f, height23, width, height13)
                    ESE -> line(0f, height23, width, height23)
                    SSE -> arc(0f, height, 2 * width23, 2 * height13, PConstants.PI + PConstants.HALF_PI, PConstants.TWO_PI)
                    SSW -> arc(0f, height, 2 * width13, 2 * height13, PConstants.PI + PConstants.HALF_PI, PConstants.TWO_PI)
                    NNW -> arc(0f, 0f, 2 * width13, 2 * height23, 0f, PConstants.HALF_PI)
                }
                WNW -> when (to) {
                    WSW, WNW -> throw IllegalArgumentException()
                    NNE -> arc(0f, 0f, 2 * width23, 2 * height13, 0f, PConstants.HALF_PI)
                    ENE -> line(0f, height13, width, height13)
                    ESE -> bezierSwitchHorizontal(0f, height13, width, height23)
                    SSE -> arc(0f, height, 2 * width23, 2 * height23, PConstants.PI + PConstants.HALF_PI, PConstants.TWO_PI)
                    SSW -> arc(0f, height, 2 * width13, 2 * height23, PConstants.PI + PConstants.HALF_PI, PConstants.TWO_PI)
                    NNW -> arc(0f, 0f, 2 * width13, 2 * height13, 0f, PConstants.HALF_PI)
                }
                NNW -> when (to) {
                    NNW, NNE -> throw IllegalArgumentException()
                    ENE -> arc(width, 0f, 2 * width23, 2 * height13, PConstants.HALF_PI, PConstants.PI)
                    ESE -> arc(width, 0f, 2 * width23, 2 * height23, PConstants.HALF_PI, PConstants.PI)
                    SSE -> bezierSwitchVertical(width23, height, width13, 0f)
                    SSW -> line(width13, 0f, width13, height)
                    WSW -> arc(0f, 0f, 2 * width13, 2 * height23, 0f, PConstants.HALF_PI)
                    WNW -> arc(0f, 0f, 2 * width13, 2 * height13, 0f, PConstants.HALF_PI)
                }
            }
        }
    }
}

class ConnectorTile(position: Position, val connections: Set<Connection>) : Tile(position) {
    override fun draw(parent: PApplet) {
        super.draw(parent)
        with(parent) {
            layer {
                translateToTile()
                layer {
                    fill(120)
                    noStroke()
                    circle(TILE_WIDTH / 2f, 0.1f * TILE_HEIGHT, 10f)
                }
                connections.forEach { it.draw(this) }
            }
        }
    }
}

fun Compass16.drawInput(parent: PApplet, centerX: Float, centerY: Float) {
    with(parent) {
        layer {
            noFill()
            when (this@drawInput) {
                WNW -> {
                    arc(0f, centerY, 2 * centerX, TILE_HEIGHT / 3f, PI + HALF_PI, TWO_PI)
                }
                NNW, NNE -> {
                    val (x, y) = this@drawInput.relativeCoordinates()
                    bezierSwitchVertical(centerX, centerY, x, y)
                }
                ENE -> arc(TILE_WIDTH, centerY, 2 * centerX, TILE_HEIGHT / 3f, PI, PI + HALF_PI)
                else -> {
                }
            }
        }
    }
}

fun Compass16.drawOutput(parent: PApplet, centerX: Float, centerY: Float) {
    with(parent) {
        layer {
            noFill()
            when (this@drawOutput) {
                ESE -> {
                    arc(TILE_WIDTH, centerY, 2 * centerX, TILE_HEIGHT / 3f, PConstants.HALF_PI, PI)
                }
                SSE, SSW -> {
                    val (x, y) = this@drawOutput.relativeCoordinates()
                    bezierSwitchVertical(x, y, centerX, centerY)
                }
                WSW -> arc(0f, centerY, 2 * centerX, TILE_HEIGHT / 3f, 0f, PConstants.HALF_PI)
                else -> {
                }
            }
        }
    }
}

class UnaryTile(position: Position, val description: String, val input: Compass16, val outputs: Set<Compass16>) : Tile(position) {

    override fun draw(parent: PApplet) {
        super.draw(parent)
        with(parent) {
            layer {
                translateToTile()
                layer {
                    strokeWeight(8f)
                    input.drawInput(this, centerX, centerY)
                    outputs.forEach {
                        it.drawOutput(this, centerX, centerY)
                    }
                }
                fill(255)
                strokeWeight(4f)
                rect(centerX - 50f, centerY - 25f, 100f, 50f)
                fill(255)
                strokeWeight(4f)
                rect(centerX - 50f, centerY - 25f, 100f, 50f)
                fill(0)
                textAlign(PConstants.CENTER, PConstants.CENTER)
                textSize(32f)
                text(description, centerX - 50f, centerY - 25f, 100f, 50f)
            }
        }
    }
}

class BinaryTile(position: Position, val description: String, val input1: Compass16, val input2: Compass16, val outputs: Set<Compass16>) : Tile(position) {

    override fun draw(parent: PApplet) {
        super.draw(parent)
        with(parent) {
            layer {
                translateToTile()
                layer {
                    strokeWeight(8f)
                    input1.drawInput(this, centerX, centerY)
                    input2.drawInput(this, centerX, centerY)
                    outputs.forEach { it.drawOutput(this, centerX, centerY) }
                }
                fill(255)
                strokeWeight(4f)
                rect(centerX - 50f, centerY - 25f, 100f, 50f)
                fill(0)
                textAlign(PConstants.CENTER, PConstants.CENTER)
                textSize(32f)
                text(description, centerX - 50f, centerY - 25f, 100f, 50f)
            }
        }
    }
}

fun main() {
    Main.run()
}