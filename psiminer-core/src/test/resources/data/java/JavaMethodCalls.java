class Main {
    void a(int aarg1) {}

    void b(int barg1, int barg2) {
        a(1);
    }

    void c() {
        a(2);
        b(3, 4);
    }
}
