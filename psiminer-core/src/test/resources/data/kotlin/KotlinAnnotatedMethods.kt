class Main {

    @Override
    fun overrideMethod(): String {
        return "test"
    }

    fun nonAnnotated() {
        val a = 5
    }

    @CustomAnnotation
    fun customAnnotatedMethod() {
        val a = true
    }
}