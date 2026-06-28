package visualizer.learning.api;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Executes synchronous learning API operations with read-timeout enforcement.
 * Used by all {@link LearningApiClient} implementations.
 */
final class LearningApiTimeouts {

    private LearningApiTimeouts() {
    }

    /**
     * Runs a task synchronously and enforces {@link LearningApiConfig#getReadTimeoutMs()}.
     *
     * @param config    API configuration
     * @param operation logical operation name for error messages
     * @param task      operation to execute
     * @param <T>       result type
     * @return task result
     * @throws LearningApiException when the task fails or times out
     */
    static <T> T execute(LearningApiConfig config, String operation, Callable<T> task)
            throws LearningApiException {
        ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
            Thread thread = new Thread(r, "learning-api-" + operation);
            thread.setDaemon(true);
            return thread;
        });
        try {
            Future<T> future = executor.submit(task);
            return future.get(config.getReadTimeoutMs(), TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            throw new LearningApiException("Operation timed out: " + operation, e);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof LearningApiException) {
                throw (LearningApiException) cause;
            }
            throw new LearningApiException("Operation failed: " + operation, cause);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new LearningApiException("Operation interrupted: " + operation, e);
        } finally {
            executor.shutdownNow();
        }
    }
}
