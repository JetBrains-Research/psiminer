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

    public void nestedIfsTwoVariables() {
        int b = 0;
        int a = 1;
        if (b > 1) {
            a = 2;
            if (b > 2) {
                a = 3;
            } else {
                a = 4;
            }
        } else {
            a = 5;
            if (b > 3) {
                a = 6;
            } else {
                a = 7;
            }
        }
        a = 8;
        b = 9;
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

    public int multipleReturns() {
        int a = 1;
        if (a < 1) {
            return 0;
        }
        if (a > 1) {
            if (a > 2) {
                return 1;
            }
        }
        return 2;
    }

    /**
     * Method based on dispose() from RefactorInsight
     */
    public static void forEach(List<Disposable> logs) {
        for (Disposable log : logs) {
            Disposer.dispose(log);
        }
    }

    private RefactoringType type;
    public RefactoringInfo setType(RefactoringType type) {
        this.type = type;
        return this;
    }

    /**
     * Disposes the Vcs Log panel.
     */
    public static void disposeWithVcsLogManager(@NotNull Project project, @NotNull Disposable disposable) {
        Disposable connectionDisposable = Disposer.newDisposable();
        project.getMessageBus().connect(connectionDisposable)
                .subscribe(VcsProjectLog.VCS_PROJECT_LOG_CHANGED, new VcsProjectLog.ProjectLogListener() {
                    @Override
                    public void logCreated(@NotNull VcsLogManager manager) {
                    }

                    @Override
                    public void logDisposed(@NotNull VcsLogManager manager) {
                        Disposer.dispose(connectionDisposable);
                        Disposer.dispose(disposable);
                    }
                });
    }


    public void assertion(int a) {
        int b = 1;
        assert a == b;
        int c = 2;
    }
    public void correct(List<String> befores, String after, List<Pair<String, Boolean>> pathPair,
                        boolean skipAnnotationsLeft,
                        boolean skipAnnotationsMid, boolean skipAnnotationsRight) {
        assert pathPair.size() == lineMarkings.size();
        for (int i = 0; i < befores.size(); i++) {
            lineMarkings.get(i).correctLines(befores.get(i), null, after, skipAnnotationsLeft,
                    skipAnnotationsMid, skipAnnotationsRight);
            lineMarkings.get(i).getMoreSidedRange().leftPath = pathPair.get(i).first;
            if (pathPair.get(i).second) {
                lineMarkings.get(i).getMoreSidedRange().startLineRight = -1;
                lineMarkings.get(i).getMoreSidedRange().endLineRight = -1;
            }
        }
        prepareRanges(lineMarkings);
    }

    /**
     * Mine complete git repo for refactorings.
     *
     * @param repository GitRepository
     */
    public void mineAll(GitRepository repository) {
        int limit = Integer.MAX_VALUE;
        try {
            limit = Utils.getCommitCount(repository);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mineRepo(repository, limit);
        }
    }

    public void tryCatch(GitRepository repository) {
        int a = 1;
        try {
            int b = 2;
            int c = 3;
        } catch (Exception exception) {
            int d = 4;
        } finally {
            int e = 5;
        }
        int f = 6;
    }
    private MergeConflictType getMergeConflictType(VisualisationType type) {
        switch (type) {
            case LEFT:
                return new MergeConflictType(MergeConflictType.Type.MODIFIED, true, false);
            case RIGHT:
                return new MergeConflictType(MergeConflictType.Type.INSERTED, false, true);
            default:
                return new MergeConflictType(MergeConflictType.Type.MODIFIED, true, true);
        }
    }
}
