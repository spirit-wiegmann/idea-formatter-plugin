package com.funbiscuit.idea.plugin.formatter;

import com.intellij.application.options.CodeStyle;
import com.intellij.formatting.commandLine.StdIoMessageOutput;
import com.intellij.ide.RecentProjectsManager;
import com.intellij.ide.impl.OpenProjectTask;
import com.intellij.openapi.options.SchemeImportException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ex.ProjectManagerEx;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.source.codeStyle.CodeStyleSettingsLoader;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.function.Predicate;
import java.util.regex.Pattern;

@Command(mixinStandardHelpOptions = true,
        name = "formatter",
        description = "Formats and rearranges code according IDEA Code Style settings")
public class FormatCommand implements Callable<Integer> {
    private static final StdIoMessageOutput messageOutput = StdIoMessageOutput.INSTANCE;
    private static final String PROJECT_DIR_PREFIX = "idea.reformat.";
    private static final String PROJECT_DIR_SUFFIX = ".tmp";
    private static final String PROCESS_RESULT_BINARY_FILE = "Skipped, binary file.";
    private static final String PROCESS_RESULT_FAILED_OPEN = "Failed to open.";
    private static final String PROCESS_RESULT_FAILED_TO_PROCESS = "Failed to process.";

    @Option(names = {"-s", "--style"}, required = true, description = "A path to Intellij IDEA code style settings .xml file")
    private Path style;

    @Option(names = {"-r", "--recursive"}, description = "Scan directories recursively")
    private boolean recursive;

    @Option(names = {"-m", "--mask"}, split = ",", paramLabel = "<mask>",
            description = "A comma-separated list of file masks")
    private List<String> masks = List.of();

    @Option(names = {"-d", "--dry"}, description = "Perform a dry run: no file modifications, only exit status")
    private boolean dry;

    @Parameters(index = "1..*", paramLabel = "<file>", description = "A path to a file or a directory")
    private List<Path> files = List.of();

    @Parameters(index = "0", hidden = true)
    private String command;

    private Path projectPath;
    private Project project;
    private FileProcessor fileProcessor;

    @Override
    public Integer call() throws Exception {
        Predicate<String> fileNamePredicate = masks.stream()
                .map(m -> m
                        .replace(".", "\\.")
                        .replace("*", ".*")
                        .replace("?", ".")
                        .replace("+", "\\+"))
                .map(Pattern::compile)
                .map(Pattern::asMatchPredicate)
                .reduce(Predicate::or)
                .orElse(p -> true);

        createTempProject();

        loadSettings(style);

        FormatStatistics statistics = new FormatStatistics();
        if (dry) {
            fileProcessor = new FileVerifier(project, statistics);
        } else {
            fileProcessor = new FileFormatter(statistics);
        }

        for (var path : files) {
            try (var stream = Files.walk(path, recursive ? Integer.MAX_VALUE : 1)) {
                stream
                        .filter(Files::isRegularFile)
                        .filter(p -> fileNamePredicate.test(p.toString()))
                        .forEach(this::processPath);
            }
        }

        RecentProjectsManager.getInstance().removePath(projectPath.toString());

        messageOutput.info("Processed: %d%n".formatted(statistics.getProcessed()));

        return statistics.allValid() ? CommandLine.ExitCode.OK : CommandLine.ExitCode.SOFTWARE;
    }

    private void processPath(Path filePath) {
        messageOutput.info("%s %s... ".formatted(fileProcessor.actionMessage(), filePath));
        String result;
        Exception ex = null;
        try {
            result = processPathInternal(filePath);
        } catch (Exception e) {
            result = PROCESS_RESULT_FAILED_TO_PROCESS;
            ex = e;
        }
        messageOutput.info("%s%n".formatted(result));
        if (ex != null) {
            ex.printStackTrace();
        }
    }

    private String processPathInternal(Path filePath) {
        PsiManager psiManager = PsiManager.getInstance(project);

        var virtualFile = VirtualFileManager.getInstance().refreshAndFindFileByNioPath(filePath);
        if (virtualFile == null) {
            return PROCESS_RESULT_FAILED_OPEN;
        }

        virtualFile.refresh(false, false);

        if (virtualFile.getFileType().isBinary()) {
            return PROCESS_RESULT_BINARY_FILE;
        }

        var psiFile = psiManager.findFile(virtualFile);
        if (psiFile == null) {
            return PROCESS_RESULT_FAILED_OPEN;
        }

        return processPsiFile(psiFile);
    }


    private String processPsiFile(PsiFile originalFile) {
        return fileProcessor.processFile(originalFile);
    }

    private void loadSettings(Path stylePath) throws SchemeImportException {
        VirtualFile file = VfsUtil.findFile(stylePath, true);
        if (file == null) {
            throw new AppException("Failed to read style settings");
        }
        var codeStyle = new CodeStyleSettingsLoader().loadSettings(file);
        CodeStyle.setMainProjectSettings(project, codeStyle);
    }

    private void createTempProject() throws IOException {
        var projectUID = UUID.randomUUID().toString();
        projectPath = FileUtil
                .createTempDirectory(PROJECT_DIR_PREFIX, projectUID + PROJECT_DIR_SUFFIX)
                .toPath();

        project = ProjectManagerEx.getInstanceEx()
                .openProject(projectPath, OpenProjectTask.build().asNewProject());

        if (project == null) {
            throw new AppException("Failed to open temporary project");
        }
    }
}
