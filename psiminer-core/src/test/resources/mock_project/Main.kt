package mock_project

abstract class Main {
    init {
        val a = 10
    }

    abstract fun sumOfTwo(n1: Int, n2: Int): Int

    /**
     * This is JavaDoc
     * @param x
     * @param y
     */
    fun myStrangeFunction(x: Int, y: Int) {
        // test comment
        /*
        another comment
         */
        var mySuperVal = 0 + 123
        val myString = "asd"
        val a: Boolean = !mySuperVal
        val b = true
        val hashMap: MutableMap<String, Int> = HashMap()
        for (i in 0..x) {
            mySuperVal += y
            hashMap[myString] = i
            if (i > 0) {
            }
            hashMap.remove(myString)
        }
        val f = 5
    }

    fun set(aabb: AABB) {
        val v: Vec2 = aabb.lowerBound
        lowerBound.x = v.x
        lowerBound.y = v.y
        val v1: Vec2 = aabb.upperBound
        upperBound.x = v1.x
        upperBound.y = v1.y
    }

    fun f() {
        val a = 5
        val s = "asd"
        val t = false
        val s1: String? = null
        val c = 's'
        val d = 3.14
        val f = 0
        f()
    }
}