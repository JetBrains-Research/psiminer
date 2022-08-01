class Main {

    public Main() {}

    public void straightWriteMethod() {
        int a = 1;
        int b = 2;
        int c = 3;
        int d = 4;
    }

    public void straightReadWriteMethod() {
        int a = 1;
        int b = a;
        b = 2 * a;
        int c = a + b;
        int d = c * c;
    }

    public void ifMethod() {
        int a = 1;
        if (a > 1) {
            int b = 2;
        } else if (a < 0) {
            int c = 3;
        } else {
            int d = 4;
        }
        int e = 5;
    }

    public void multipleDeclarations() {
        {
            int a = 1;
        }
        for (int a = 2; a <= 20; a++) {
            a *= 2;
        }
        int a = 3;
        a--;
    }

}
