import java.util.Map;

class Main {

    public Main() { int a = 10; }

    public abstract int abstractMethod(int n1, int n2);

    @Override
    public String overrideMethod() {
        return "test"
    }

    public void emptyMethod() {

    }

    public void smallMethod() {
        int a = 5;
        String s = "asd";
        Boolean t = false;
    }

    /**
     * This is JavaDoc
     * @param x
     * @param y
     */
    public void largeMethod(int x, int y) {
        // test comment
        /*
            another comment
        */
        int mySuperVal = 0 + 123;
        String myString = "asd";
        boolean a = !mySuperVal;
        boolean b = true;
        Map<String, Integer> hashMap = new HashMap();
        for (int i = 0; i <= x; ++i) {
            mySuperVal += y;
            hashMap[myString] = i;
            if (i > 0) {}
            hashMap.remove(i);
        }
        float f = 5;
        Qwerty qwerty = new Qwerty()
    }

    public void recursiveMethod() {
        int a = 5;
        int recursiveMethod = 0;
        recursiveMethod();
    }

    /**
     * Returns the size of this big array in bytes.
     */
    public long sizeOf()
    {
        return SizeOf.sizeOf(array) + (segments * SIZE_OF_SEGMENT);
    }
}