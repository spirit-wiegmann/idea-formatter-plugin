package com.funbiscuit.idea.plugin.formatter;

import com.intellij.codeInsight.actions.AbstractLayoutCodeProcessor;
import com.intellij.codeInsight.actions.RearrangeCodeProcessor;
import com.intellij.codeInsight.actions.ReformatCodeProcessor;
import com.intellij.openapi.fileEditor.impl.NonProjectFileWritingAccessProvider;
import com.intellij.psi.PsiFile;

public class FileFormatter implements FileProcessor {
    private static final String PROCESS_RESULT_OK = "OK";

    private final FormatStatistics statistics;

    public FileFormatter(FormatStatistics statistics) {
        this.statistics = statistics;
    }

    @Override
    public String processFile(PsiFile originalFile) {
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
