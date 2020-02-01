import processing.core.PApplet

interface Drawable {
    fun draw(parent: PApplet)
}

class Visuals : PApplet() {

    override fun settings() {
        size(600, 1080)
    }

    override fun setup(){
        frameRate(10f)
    }

    override fun draw() {
        preRender()
        drawGrid()
    }

    private fun preRender() {
        background(255)
        stroke(0)
        translate(15f, 15f)
        scale(38f)
        strokeWeight(0.1f)
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