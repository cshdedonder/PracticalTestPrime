package v1

import processing.core.PApplet
import v1.CompassRose16.*

interface Drawable {
    fun draw(parent: PApplet)
}

class Visuals : PApplet() {

    private val tileList: List<ConnectorTile> = listOf(
            connector(0,1, NNW to SSW, NNE to ENE),
            connector(0,2, NNW to SSW, ENE to SSE),
            connector(1,2, NNW to WNW, NNE to ENE),
            connector(2, 1, NNE to ENE),
            connector(3,1, WNW to SSW, NNW to SSE),
            connector(4,1, NNW to SSE),
            connector(2, 2, WNW to SSW),
            connector(4, 2, WSW to SSW, NNE to SSE),
            connector(3,3, NNW to WNW, ESE to SSE),
            connector(2,6, WNW to SSE, NNW to ENE, ESE to SSW),
            connector(3, 6, NNW to ENE, WNW to SSW, WSW to ESE)
    )

    private fun connector(x: Int, y: Int, vararg connections: Pair<CompassRose16, CompassRose16>) = ConnectorTile(Coordinate(x * 3f, y * 3f), *connections)

    override fun settings() {
        size(600, 1080)
    }

    override fun setup(){
        frameRate(30f)
    }

    override fun draw() {
        preRender()
        drawGrid()
        tileList.forEach { it.draw(this) }
    }

    private fun preRender() {
        background(255)
        stroke(0)
        translate(15f, 15f)
        scale(38f)
        strokeWeight(0.1f)
        noFill()
    }

    private fun drawGrid(){
        for(i: Int in 0..9){
            line(0f, i*3f, 15f, i*3f)
        }
        for(i: Int in 0..5){
            line(i*3f, 0f, i*3f, 27f)
        }
    }

    companion object {
        fun run() = Visuals().runSketch()
    }
}

fun main() = Visuals.run()