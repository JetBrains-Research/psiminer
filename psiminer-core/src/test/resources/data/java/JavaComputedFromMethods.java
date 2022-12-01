class Main {

    public Main() {}

    void chainedVariables() {
        int a = 0;
        int b = a;
        int c = a + b;
        int d = a + b + c;
    }

    void chainedAssignment() {
        int a = 0;
        int b = 0;
        int c = 0;
        a = b = c = 1;
    }

    void multipleDeclarations() {
        int a = 0;
        int b = 0;
        int c = 0;
        int d = a + b, e = c * c;
    }

    void plusAssignment() {
        int a = 0;
        int b = 0;
        a += a + b;
    }
}
