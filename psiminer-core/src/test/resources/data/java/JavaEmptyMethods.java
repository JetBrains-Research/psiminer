// abstract methods
interface Interface {
    void interfaceMethod();
}

abstract class AbstractClass {
    abstract public void abstractMethod();
}

// brackets only methods
class BracketsOnly {
    public void bracketsInline() {}

    public void bracketsOneLine() {
    }

    public void bracketsTwoLines()
    {
    }
}

// no-op methods
class NoOpMethods {
    public void commentInline() { /* no-op */}

    public void singleComment() {
        /*
        * no-op
        * */
    }

    public void multipleComments() {
        // method body
        // more useless comments
        // I think I could fit a poem here
    }
}

class NonEmptyMethods {
    public void notEmptyMethod() {
        System.out.println("Providing you with cat pictures");
    }
    public void notEmptyMethodInline() { System.out.println("Providing you with cat pictures"); }
}
