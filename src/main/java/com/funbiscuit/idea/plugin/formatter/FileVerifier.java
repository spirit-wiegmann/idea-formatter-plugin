package com.funbiscuit.idea.plugin.formatter;

import com.intellij.codeInsight.actions.AbstractLayoutCodeProcessor;
import com.intellij.codeInsight.actions.RearrangeCodeProcessor;
import com.intellij.codeInsight.actions.ReformatCodeProcessor;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.PsiManager;

import java.util.UUID;

public class FileVerifier implements FileProcessor {
    private static final String PROCESS_RESULT_DRY_OK = "Formatted well";
    private static final String PROCESS_RESULT_DRY_FAIL = "Needs formatting";

    private final Project project;
    private final PsiDirectory projectPsiDir;
    private final FormatStatistics statistics;

    public FileVerifier(Project project, FormatStatistics statistics) {
        this.project = project;
        this.statistics = statistics;

        VirtualFile baseDir = ProjectUtil.guessProjectDir(project);
        if (baseDir == null) {
            throw new AppException("Failed to guess project dir");
        }
        projectPsiDir = PsiManager.getInstance(project).findDirectory(baseDir);
        if (projectPsiDir == null) {
            throw new AppException("Failed to find guessed project dir");
        }
    }

    @Override
    public String processFile(PsiFile originalFile) {
        String originalContent = originalFile.getText();

        PsiFile processedFile = createFileCopy(originalFile, originalContent);

        AbstractLayoutCodeProcessor processor = new ReformatCodeProcessor(processedFile, false);
        processor = new RearrangeCodeProcessor(processor);
        processor.run();

        if (processedFile.getText().equals(originalContent)) {
            statistics.fileProcessed(true);
            return PROCESS_RESULT_DRY_OK;
        } else {
            statistics.fileProcessed(false);
            return PROCESS_RESULT_DRY_FAIL;
        }
    }

    @Override
    public String actionMessage() {
        return "Checking";
    }

    private PsiFile createFileCopy(PsiFile originalFile, String content) {
        return ApplicationManager.getApplication().runWriteAction((Computable<PsiFile>) () -> {
            var psiCopy = PsiFileFactory.getInstance(project).createFileFromText(
                    UUID.randomUUID() + "." + originalFile.getFileType().getDefaultExtension(),
                    originalFile.getFileType(),
                    content
            );
            psiCopy.putUserData(PsiFileFactory.ORIGINAL_FILE, originalFile);

            return (PsiFile) projectPsiDir.add(psiCopy);
        });
    }
}
