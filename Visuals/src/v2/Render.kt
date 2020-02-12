package v2

import processing.core.PApplet

interface Drawable {
    fun draw(parent: PApplet)
}

fun PApplet.draw(d: Drawable) {
    pushMatrix()
    d.draw(this)
    popMatrix()
}

object Main : PApplet() {

    private const val WIDTH: Int = 1600 // +50px either side
    private const val HEIGHT: Int = 2800 // +50px either side
    private const val TILE_WIDTH: Int = 300
    private const val TILE_HEIGHT: Int = 300

    private val fileName: String = "render-${System.currentTimeMillis()}.pdf"

    override fun settings() {
        size(WIDTH, HEIGHT, PDF, fileName)
    }

    override fun draw() {
        background(120)
        exit()
    }

    fun run() = runSketch()
}

fun main() {
    Main.run()
}