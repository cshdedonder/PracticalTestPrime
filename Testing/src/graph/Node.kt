package graph


open class Node {

    protected val id: Int = ++counter

    override fun toString(): String {
        return "Node #$id"
    }

    companion object {
        private var counter: Int = 0
    }
}

class InputNode(val state: Boolean) : Node() {

    override fun toString(): String {
        return "InputNode #$id {state: $state}"
    }
}

class OutputNode(val expectedState: Boolean) : Node() {

    override fun toString(): String {
        return "OutputNode #$id {expectedState: $expectedState}"
    }
}

abstract class GatedNode : Node()

interface UnaryGateFunction {
    fun calculate(input: Boolean): Boolean
}

object NOTGateFunction : UnaryGateFunction {
    override fun calculate(input: Boolean): Boolean = !input

    override fun toString(): String = "NOT"
}

class UnaryGatedNode(val gateFunction: UnaryGateFunction) : GatedNode() {
    override fun toString(): String {
        return "UnaryGatedNode #$id {function: $gateFunction}"
    }
}

interface BinaryGateFunction {
    fun calculate(input1: Boolean, input2: Boolean): Boolean
}

infix fun Boolean.nand(other: Boolean): Boolean = !(this and other)
infix fun Boolean.nor(other: Boolean): Boolean = !(this or other)
infix fun Boolean.xnor(other: Boolean): Boolean = !(this xor other)

object ANDGateFunction : BinaryGateFunction {
    override fun calculate(input1: Boolean, input2: Boolean): Boolean = input1 and input2

    override fun toString(): String {
        return "AND"
    }
}

object ORGateFunction : BinaryGateFunction {
    override fun calculate(input1: Boolean, input2: Boolean): Boolean = input1 or input2

    override fun toString(): String {
        return "OR"
    }
}

object NANDGateFunction : BinaryGateFunction {
    override fun calculate(input1: Boolean, input2: Boolean): Boolean = input1 nand input2

    override fun toString(): String {
        return "NAND"
    }
}

object NORGateFunction : BinaryGateFunction {
    override fun calculate(input1: Boolean, input2: Boolean): Boolean = input1 nor input2

    override fun toString(): String {
        return "NOR"
    }
}

object XORGateFunction : BinaryGateFunction {
    override fun calculate(input1: Boolean, input2: Boolean): Boolean = input1 xor input2

    override fun toString(): String {
        return "XOR"
    }
}

object XNORGateFunction : BinaryGateFunction {
    override fun calculate(input1: Boolean, input2: Boolean): Boolean = input1 xnor input2

    override fun toString(): String {
        return "XNOR"
    }
}

class BinaryGatedNode(val gateFunction: BinaryGateFunction) : GatedNode() {

    override fun toString(): String {
        return "BinaryGatedNode #id {function: $gateFunction}"
    }
}