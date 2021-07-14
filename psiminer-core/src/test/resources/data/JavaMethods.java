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

    public void largeMethod() {
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
        int f = 5;
    }

    public void recursiveMethod() {
        int a = 5;
        int recursiveMethod = 0;
        recursiveMethod();
    }
}