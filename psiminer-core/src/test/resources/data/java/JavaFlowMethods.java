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

    public void breakAndContinue() {
        int j = 0;
        int k = 1;
        for (int i = 0; j < 10; k++) {
            {
                int b = 2;
                break;
            }
            {
                int c = 3;
                continue;
            }
            {
                int d = 4;
                return;
            }
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

    public void nestedIfs() {
        int a = 1;
        if (a > 1) {
            a = 2;
            if (a > 2) {
                a = 3;
            } else {
                a = 4;
            }
        } else {
            a = 5;
            if (a > 3) {
                a = 6;
            } else {
                a = 7;
            }
        }
        a = 8;
    }

    public void nestedFors() {
        int a = 1;
        for (; a < 1;) {
            a = 2;
            for (; a < 2;) {
                a = 3;
                for (; a < 3;) {
                    a = 4;
                }
                a = 5;
            }
            a = 6;
        }
        a = 7;
    }
}
