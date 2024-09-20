package artofillusion.picocli;

import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

import java.util.List;
import java.util.concurrent.Callable;

@Slf4j
@CommandLine.Command
public class Starter implements Callable<Integer> {

    @Override
    public Integer call() throws Exception {
        log.info("Starter called with splash: {} and old: {} {}", splash, old, files);
        return 0;
    }
    @CommandLine.Option(names = {"--nosplash"}, description = "No splash screen", negatable = true, defaultValue = "true")
    private boolean splash = false;

    @CommandLine.Option(names = {"--old"}, description = "No splash screen", negatable = true, defaultValue = "false")
    private boolean old = false;

    @CommandLine.Parameters
    private List<String> files;
}
