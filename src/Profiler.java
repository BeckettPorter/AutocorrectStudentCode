import java.time.Instant;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Profiler
 * <p>
 * A java profiler that allows for measuring time elapsed while a program is running.
 * </p>
 * @author Beckett Porter
 * @author ChatGPT
 */

public class Profiler {
    private static final Map<String, Instant> timers = new HashMap<>();

    public static void start(String name) {
        timers.put(name, Instant.now());
        System.out.println("[Profiler] " + name + " started.");
    }

    public static void end(String name) {
        Instant startTime = timers.get(name);
        if (startTime == null) {
            System.out.println("[Profiler] No start time found for: " + name);
            return;
        }
        Duration elapsed = Duration.between(startTime, Instant.now());
        System.out.println("[Profiler] " + name + " ended. Elapsed: " + elapsed.toMillis() + " ms");
        timers.remove(name);
    }
}