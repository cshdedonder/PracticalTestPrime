package graph

class MaskingSet<E>(private val baseCollection: Collection<E>) : Set<E> {

    private val masks: MutableSet<E> = HashSet()

    fun mask(element: E) {
        masks.add(element)
    }

    /**
     * Unmasks [element], return true is [element] was masked, false otherwise.
     */
    fun unmask(element: E): Boolean = masks.remove(element)

    override val size: Int
        get() = baseCollection.filter { it !in masks }.count()

    override fun contains(element: E): Boolean = (element !in masks) and (element in baseCollection)

    override fun containsAll(elements: Collection<E>): Boolean = elements.all { it in this }

    override fun isEmpty(): Boolean = size == 0

    /**
     * Make hard-copy of present data to allow online modifications.
     */
    override fun iterator(): Iterator<E> = baseCollection.filter { it !in masks }.iterator()
}