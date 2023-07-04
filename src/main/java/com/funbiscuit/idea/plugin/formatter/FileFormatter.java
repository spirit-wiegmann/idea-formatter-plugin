package com.funbiscuit.idea.plugin.formatter;

import com.intellij.codeInsight.actions.AbstractLayoutCodeProcessor;
import com.intellij.codeInsight.actions.RearrangeCodeProcessor;
import com.intellij.codeInsight.actions.ReformatCodeProcessor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.impl.NonProjectFileWritingAccessProvider;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiUtilCore;

import java.util.Objects;

public class FileFormatter implements FileProcessor {
    private static final String PROCESS_RESULT_OK = "OK";
    private static final String PROCESS_RESULT_READ_ONLY = "Skipped, read only";

    private final FormatStatistics statistics;

    public FileFormatter(FormatStatistics statistics) {
        this.statistics = statistics;
    }

    @Override
    public String processFile(PsiFile originalFile) {
        FileDocumentManager documentManager = FileDocumentManager.getInstance();
        VirtualFile virtualFile = PsiUtilCore.getVirtualFile(originalFile);
        var document = documentManager.getDocument(Objects.requireNonNull(virtualFile));
        if (!documentManager.requestWriting(Objects.requireNonNull(document), null)) {
            return PROCESS_RESULT_READ_ONLY;
        }

        AbstractLayoutCodeProcessor processor = new ReformatCodeProcessor(originalFile, false);
        processor = new RearrangeCodeProcessor(processor);
        NonProjectFileWritingAccessProvider.disableChecksDuring(processor::run);

        statistics.fileProcessed(true);
        return PROCESS_RESULT_OK;
    }

    @Override
    public String actionMessage() {
        return "Formatting";
    }
}
