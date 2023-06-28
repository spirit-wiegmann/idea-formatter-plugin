package com.funbiscuit.idea.plugin.formatter;

import com.intellij.psi.PsiFile;

public interface FileProcessor {
    String processFile(PsiFile originalFile);

    String actionMessage();
}
