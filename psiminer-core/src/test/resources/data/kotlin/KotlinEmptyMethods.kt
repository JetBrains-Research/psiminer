// abstract methods
internal interface Interface {
    fun interfaceMethod()
}

internal abstract class AbstractClass {
    abstract fun abstractMethod()
} // brackets only methods

internal class BracketsOnly {
    fun bracketsInline() {}
    fun bracketsOneLine() {

    }
    fun bracketsTwoLines()
    {

    }
}

// no-op methods
internal class NoOpMethods {
    fun commentInline() { /* no-op */ }

    fun singleComment() {
        /*
        * no-op
        * */
    }

    fun multipleComments() {
        // method body
        // more useless comments
        // I think I could fit a poem here
    }
}

internal class NonEmptyMethods {
    fun notEmptyMethod() {
        println("Providing you with cat pictures")
    }

    fun notEmptyMethodInline() {
        println("Providing you with cat pictures")
    }
}