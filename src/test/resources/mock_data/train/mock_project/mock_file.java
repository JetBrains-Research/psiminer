package mock_data.train.mock_project

import java.util.Map;

public class Main {
    public void myStrangeFunction(int x, int y) {
        int mySuperVal = 0;
        String myString = "asd";
        boolean a = true;
        Map<String, Integer> hashMap = new HashMap<>();
        for (int i = 0; i < x; ++i) {
            mySuperVal += y;
            hashMap[myString] = i;
            if (i > 0) {}
            hashMap.remove(i);
        }
    }
}