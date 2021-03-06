package org.python.ant;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.python.Version;
import org.python.bootstrap.Import;
import org.python.core.PySystemState;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * Compiles all python files in a directory to bytecode, and writes them to another directory,
 * possibly the same one.
 */
public class JycompileAntTask extends MatchingTask {
    protected org.apache.tools.ant.types.Path src;
    protected File destDir;
    protected Set<File> toExpose = new HashSet<>();

    @Override
    public void execute() throws BuildException {
        checkParameters();
        toExpose.clear();

        for (String srcEntry : src.list()) {
            File srcDir = getProject().resolveFile(srcEntry);
            if (!srcDir.exists()) {
                throw new BuildException("srcdir '" + srcDir.getPath() + "' does not exist!",
                                         getLocation());
            }
            File[] files = srcDir.listFiles();
            filter(files);
        }
        process(toExpose);
    }

    private void filter(File[] files) {
        for (File src : files) {
            if (src.isDirectory()) {
                Path p = src.toPath();
                if (p.endsWith(Import.CACHEDIR) || p.getFileName().startsWith("test")) {
                    continue;
                }
                filter(src.listFiles());
            }
            if (!src.getName().endsWith(".py")) {
                continue;
            }
            Path classPath = src.toPath().resolveSibling(
                    Paths.get(Import.CACHEDIR, src.getName().substring(0, src.getName().length() - 3) + "." + Version.PY_CACHE_TAG + ".class"));
            File classFile = classPath.toFile();
            if (classFile.exists() && classFile.lastModified() > src.lastModified())
                continue;
            toExpose.add(src);
        }
    }

    public void process(Set<File> toCompile) throws BuildException {
        if (toCompile.size() == 0) {
            return;
        } else if (toCompile.size() > 1) {
            log("Compiling " + toCompile.size() + " files");
        } else if (toCompile.size() == 1) {
            log("Compiling 1 file");
        }
        /* Initialize a basic system state, this is required to use dynalink in compiler */
        Properties props = new Properties();
        props.setProperty(PySystemState.PYTHON_CACHEDIR_SKIP, "true");
        PySystemState.initialize(System.getProperties(), props);
        for (File src : toCompile) {
            try {
                String name = getModuleName(src);
                String compiledFilePath = name.replace('.', File.separatorChar);
                Path classPath = Paths.get(compiledFilePath);
                if (src.getName().endsWith("__init__.py")) {
                    classPath = classPath.resolve(Paths.get(Import.CACHEDIR, "__init__"));
                } else {
                    Path cache = Paths.get(Import.CACHEDIR, classPath.getFileName().toString());
                    if (classPath.getParent() == null) {
                        classPath = cache;
                    } else {
                        classPath = classPath.getParent().resolve(cache);
                    }
                }
                File compiled = Paths.get(destDir.toString(),
                        classPath.getParent().toString(),
                        classPath.getFileName().toString() + "."  + Version.PY_CACHE_TAG + ".class").toFile();
                compile(src, compiled, name);
            } catch (RuntimeException e) {
                log("Could not compile " + src);
                throw e;
            }
        }
    }

    /**
     * Compiles the python file <code>src</code> to bytecode filling in <code>moduleName</code> a
     * its name, and stores it in <code>compiled</code>. This is called by process for every file
     * that's compiled, so subclasses can override this method to affect or track the compilation.
     */
    protected void compile(File src, File compiled, String moduleName) {
        byte[] bytes;
        try {
            bytes = Import.compileSource(moduleName, new FileInputStream(src), moduleName);
        } catch (Exception e) {
            throw new BuildException("Compile failed; see the compiler error output for details.");
        }
        File dir = compiled.getParentFile();
        if (!dir.exists() && !compiled.getParentFile().mkdirs()) {
            throw new BuildException("Unable to make directory for compiled file: " + compiled);
        }
        try (FileOutputStream fop = new FileOutputStream(compiled)) {
            fop.write(bytes);
        } catch (IOException e) {
            throw new BuildException("Unable to write to source cache file due to " + e);
        }
    }

    protected static final String getModuleName(File f) {
        String name = f.getName();
        int dot = name.lastIndexOf('.');
        if (dot != -1) {
            name = name.substring(0, dot);
        }

        // name the __init__ module after its package
        File dir = f.getParentFile();
        if (name.equals("__init__")) {
            name = dir.getName();
            dir = dir.getParentFile();
        }

        // Make the compiled classfile's name fully qualified with a package by walking up the
        // directory tree looking for __init__.py files. Don't check for __init__.${cache_tag}.class since
        // we're compiling source here and the existence of a class file without corresponding
        // source probably doesn't indicate a package.
        while (dir != null && (new File(dir, "__init__.py").exists())) {
            name = dir.getName() + "." + name;
            dir = dir.getParentFile();
        }
        return name;
    }

    /**
     * Set the source directories to find the class files to be exposed.
     */
    public void setSrcdir(org.apache.tools.ant.types.Path srcDir) {
        if (src == null) {
            src = srcDir;
        } else {
            src.append(srcDir);
        }
    }

    /**
     * Gets the source dirs to find the class files to be exposed.
     */
    public org.apache.tools.ant.types.Path getSrcdir() {
        return src;
    }

    /**
     * Set the destination directory into which the Java source files should be compiled.
     *
     * @param destDir
     *            the destination director
     */
    public void setDestdir(File destDir) {
        this.destDir = destDir;
    }

    /**
     * Gets the destination directory into which the java source files should be compiled.
     *
     * @return the destination directory
     */
    public File getDestdir() {
        return destDir;
    }

    /**
     * Check that all required attributes have been set and nothing silly has been entered.
     */
    protected void checkParameters() throws BuildException {
        if (src == null || src.size() == 0) {
            throw new BuildException("srcdir attribute must be set!", getLocation());
        }
        if (destDir != null && !destDir.isDirectory()) {
            throw new BuildException("destination directory '" + destDir + "' does not exist "
                    + "or is not a directory", getLocation());
        }
    }
}
