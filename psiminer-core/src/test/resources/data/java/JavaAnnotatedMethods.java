class Main {
    @Override
    public String overrideMethod() {
        return "test"
    }

    public void nonAnnotated() {
        int a = 5;
    }

    @CustomAnnotation
    public void customAnnotatedMethod() {
        boolean a = true;
    }
}