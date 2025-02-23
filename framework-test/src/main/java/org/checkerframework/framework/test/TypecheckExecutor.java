package org.checkerframework.framework.test;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import org.checkerframework.framework.test.diagnostics.JavaDiagnosticReader;
import org.checkerframework.framework.test.diagnostics.TestDiagnostic;
import org.plumelib.util.StringsPlume;

/** Used by the Checker Framework test suite to run the framework and generate a test result. */
public class TypecheckExecutor {

  /** Creates a new TypecheckExecutor. */
  public TypecheckExecutor() {}

  /**
   * Runs a typechecking test using the given configuration and returns the test result.
   *
   * @param configuration the test configuration
   * @return the test result
   */
  public TypecheckResult runTest(TestConfiguration configuration) {
    try {
      CompilationResult result = compile(configuration);
      return interpretResults(configuration, result);
    } catch (OutOfMemoryError e) {
      String message =
          String.format(
              "Max memory = %d, total memory = %d, free memory = %d.",
              Runtime.getRuntime().maxMemory(),
              Runtime.getRuntime().totalMemory(),
              Runtime.getRuntime().freeMemory());
      System.out.println(message);
      System.err.println(message);
      throw new Error(message, e);
    }
  }

  /**
   * Using the settings from the input configuration, compile all source files in the configuration,
   * and return the result in a CompilationResult.
   */
  public CompilationResult compile(TestConfiguration configuration) {
    String dOption = configuration.getOptions().get("-d");
    if (dOption == null) {
      throw new Error("-d not supplied");
    }
    TestUtilities.ensureDirectoryExists(dOption);

    final StringWriter javacOutput = new StringWriter();
    DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();

    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    try (StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null)) {
      Iterable<? extends JavaFileObject> javaFiles =
          fileManager.getJavaFileObjects(configuration.getTestSourceFiles().toArray(new File[] {}));

      // Even though the method compiler.getTask takes a list of processors, it fails if
      // processors are passed this way with the message:
      //   error: Class names, 'org.checkerframework.checker.interning.InterningChecker', are
      //   only accepted if annotation processing is explicitly requested
      // Therefore, we now add them to the beginning of the options list.
      final List<String> options = new ArrayList<>();
      options.add("-processor");
      options.add(String.join(",", configuration.getProcessors()));

      List<String> nonJvmOptions = new ArrayList<>();
      for (String option : configuration.getFlatOptions()) {
        if (!option.startsWith("-J-")) {
          nonJvmOptions.add(option);
        }
      }
      nonJvmOptions.add("-Xmaxerrs");
      nonJvmOptions.add("100000");
      nonJvmOptions.add("-Xmaxwarns");
      nonJvmOptions.add("100000");
      nonJvmOptions.add("-Xlint:deprecation");

      nonJvmOptions.add("-ApermitMissingJdk");
      nonJvmOptions.add("-Anocheckjdk"); // temporary, for backward compatibility

      // -Anomsgtext is needed to ensure expected errors can be matched.
      // Note: Since "-Anomsgtext" is always added to the non-JVM options,
      //  we are passing `true` as the `noMsgText` argument to all invocations
      //  of `TestDiagnosticUtils.fromJavaxDiagnosticList`.
      nonJvmOptions.add("-Anomsgtext");

      options.addAll(nonJvmOptions);

      if (configuration.shouldEmitDebugInfo()) {
        System.out.println("Running test using the following invocation:");
        System.out.println(
            "javac "
                + String.join(" ", options)
                + " "
                + StringsPlume.join(" ", configuration.getTestSourceFiles()));
      }

      JavaCompiler.CompilationTask task =
          compiler.getTask(
              javacOutput, fileManager, diagnostics, options, new ArrayList<String>(), javaFiles);

      /*
       * In Eclipse, std out and std err for multiple tests appear as one
       * long stream. When selecting a specific failed test, one sees the
       * expected/unexpected messages, but not the std out/err messages from
       * that particular test. Can we improve this somehow?
       */
      final Boolean compiledWithoutError = task.call();
      javacOutput.flush();
      return new CompilationResult(
          compiledWithoutError, javacOutput.toString(), javaFiles, diagnostics.getDiagnostics());
    } catch (IOException e) {
      throw new Error(e);
    }
  }

  /**
   * Reads the expected diagnostics for the given configuration and creates a TypecheckResult which
   * contains all of the missing and expected diagnostics.
   */
  public TypecheckResult interpretResults(
      TestConfiguration config, CompilationResult compilationResult) {
    List<TestDiagnostic> expectedDiagnostics = readDiagnostics(config, compilationResult);
    return TypecheckResult.fromCompilationResults(config, compilationResult, expectedDiagnostics);
  }

  /**
   * A subclass can override this to filter out errors or add new expected errors. This method is
   * called immediately before results are checked.
   */
  protected List<TestDiagnostic> readDiagnostics(
      TestConfiguration config, CompilationResult compilationResult) {
    List<TestDiagnostic> expectedDiagnostics;
    if (config.getDiagnosticFiles() == null || config.getDiagnosticFiles().isEmpty()) {
      expectedDiagnostics =
          JavaDiagnosticReader.readJavaSourceFiles(compilationResult.getJavaFileObjects());
    } else {
      expectedDiagnostics = JavaDiagnosticReader.readDiagnosticFiles(config.getDiagnosticFiles());
    }

    return expectedDiagnostics;
  }
}
