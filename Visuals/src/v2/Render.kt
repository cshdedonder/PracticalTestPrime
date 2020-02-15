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

inline fun convert1(crossinline f: (Float) -> Unit): (Number) -> Unit = { p1 -> f(p1.toFloat()) }
inline fun convert2(crossinline f: (Float, Float) -> Unit): (Number, Number) -> Unit = { p1, p2 -> f(p1.toFloat(), p2.toFloat()) }
inline fun convert3(crossinline f: (Float, Float, Float) -> Unit): (Number, Number, Number) -> Unit = { p1, p2, p3 -> f(p1.toFloat(), p2.toFloat(), p3.toFloat()) }
inline fun convert4(crossinline f: (Float, Float, Float, Float) -> Unit): (Number, Number, Number, Number) -> Unit = { p1, p2, p3, p4 -> f(p1.toFloat(), p2.toFloat(), p3.toFloat(), p4.toFloat()) }

fun PApplet.translate(x: Number, y: Number) = convert2(::translate)(x, y)
fun PApplet.rect(x: Number, y: Number, w: Number, h: Number) = convert4(::rect)(x, y, w, h)

private const val BASE_WIDTH: Int = 1500
private const val BASE_HEIGHT: Int = 2700
private const val WIDTH_OFFSET: Int = 100
private const val HEIGHT_OFFSET: Int = 100
private const val WIDTH: Int = BASE_WIDTH + 2 * WIDTH_OFFSET
private const val HEIGHT: Int = BASE_HEIGHT + 2 * HEIGHT_OFFSET

private const val BASE_TILE_WIDTH: Int = 300
private const val BASE_TILE_HEIGHT: Int = 300
private const val TILE_WIDTH_OFFSET: Int = 10
private const val TILE_HEIGHT_OFFSET: Int = 10
private const val TILE_WIDTH: Int = BASE_TILE_WIDTH - 2 * TILE_WIDTH_OFFSET
private const val TILE_HEIGHT: Int = BASE_TILE_HEIGHT - 2 * TILE_HEIGHT_OFFSET


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
        // LOAD TILES
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

data class Point(val x: Float, val y: Float)

/**
 * Assumes ([x1], [y1]) is bottom control point and vertical switch.
 */
fun PApplet.bezierSwitch(x1: Number, y1: Number, x2: Number, y2: Number) {
    val p1 = Point(x1.toFloat(), y1.toFloat())
    val p2 = Point(x2.toFloat(), y2.toFloat())
    bezier(p1.x, p1.y, p1.x, p1.y - abs(p1.y - p2.y) / 3, p2.x, p2.y + abs(p1.y - p2.y) / 3, p2.x, p2.y)
}

open class Tile(val position: Position) : Drawable {

    override fun draw(parent: PApplet) {
        with(parent) {
            layer {
                translateToTile()
                strokeWeight(12f)
                rect(0, 0, TILE_WIDTH, TILE_HEIGHT)
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
    ENE -> Pair(TILE_WIDTH.toFloat(), TILE_HEIGHT * (1f / 3))
    ESE -> Pair(TILE_WIDTH.toFloat(), TILE_HEIGHT * (2f / 3))
    SSE -> Pair(TILE_WIDTH * (2f / 3), TILE_HEIGHT.toFloat())
    SSW -> Pair(TILE_WIDTH * (1f / 3), TILE_HEIGHT.toFloat())
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
                    bezierSwitch(x, y, centerX, centerY)
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
                    bezierSwitch(centerX, centerY, x, y)
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

fun Connection.draw(parent: PApplet) {
    with(parent) {
        layer {
            strokeWeight(8f)
            // TODO
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
                    bezierSwitch(centerX, centerY, x, y)
                }
                ENE -> arc(TILE_WIDTH.toFloat(), centerY, 2 * centerX, TILE_HEIGHT / 3f, PI, PI + HALF_PI)
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
                    arc(TILE_WIDTH.toFloat(), centerY, 2 * centerX, TILE_HEIGHT / 3f, PConstants.HALF_PI, PI)
                }
                SSE, SSW -> {
                    val (x, y) = this@drawOutput.relativeCoordinates()
                    bezierSwitch(x, y, centerX, centerY)
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