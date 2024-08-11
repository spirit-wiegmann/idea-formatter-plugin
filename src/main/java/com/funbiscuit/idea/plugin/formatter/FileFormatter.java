package com.funbiscuit.idea.plugin.formatter;

import com.funbiscuit.idea.plugin.formatter.report.FileInfo;
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

    @Override
    public void processFile(PsiFile originalFile, FileInfo fileInfo) {
        String originalContent = originalFile.getText();
        FileDocumentManager documentManager = FileDocumentManager.getInstance();
        VirtualFile virtualFile = PsiUtilCore.getVirtualFile(originalFile);
        var document = documentManager.getDocument(Objects.requireNonNull(virtualFile));
        if (!documentManager.requestWriting(Objects.requireNonNull(document), null)) {
            fileInfo.addWarning(ProcessStatuses.SKIPPED_READ_ONLY);
            return;
        }

        AbstractLayoutCodeProcessor processor = new ReformatCodeProcessor(originalFile, false);
        // processor = new RearrangeCodeProcessor(processor);
        NonProjectFileWritingAccessProvider.disableChecksDuring(processor::run);
        FileDocumentManager.getInstance().saveDocument(document);

        if (originalFile.getText().equals(originalContent)) {
            fileInfo.addInfo(ProcessStatuses.FORMATTED_WELL);
        } else {
            fileInfo.addInfo(ProcessStatuses.FORMATTED);
        }
    }
}
