package mock_data.train.mock_project

import java.util.Map;

public class Main {

    public Main() { int a = 10; }

    public abstract int sumOfTwo(int n1, int n2);

    @Override
    void show() { System.out.println("Child's show()"); }

    public void myStrangeFunction(int x, int y) {
        int mySuperVal = 0;
        String myString = "asd";
        boolean a = true;
        Map<String, Integer> hashMap = new HashMap<>();
        for (int i = 0; i <= x; ++i) {
            mySuperVal += y;
            hashMap[myString] = i;
            if (i > 0) {}
            hashMap.remove(i);
        }
    }

    public final void set(final AABB aabb) {
        Vec2 v = aabb.lowerBound;
        lowerBound.x = v.x;
        lowerBound.y = v.y;
        Vec2 v1 = aabb.upperBound;
        upperBound.x = v1.x;
        upperBound.y = v1.y;
    }

    public void f() {
        int a = 5;
        String s = "asd";
        Boolean t = false;
        String s1 = null;
        Char c = 's';
        Double d = 3.14;
        f();
    }
}