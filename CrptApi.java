import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CrptApi {
    private final long intervalInMillis;
    private final int requestLimit;
    private final AtomicInteger requestCount;
    private final Lock lock;
    private long lastResetTimeMillis;

    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        this.intervalInMillis = timeUnit.toMillis(1);
        this.requestLimit = requestLimit;
        this.requestCount = new AtomicInteger(0);
        this.lock = new ReentrantLock();
        this.lastResetTimeMillis = System.currentTimeMillis();
    }

    public void createDocument(String documentJson, String signature) {
        lock.lock();
        try {
            long currentTimeMillis = System.currentTimeMillis();
            long elapsedTimeSinceReset = currentTimeMillis - lastResetTimeMillis;

            if (elapsedTimeSinceReset >= intervalInMillis) {
                requestCount.set(0);
                lastResetTimeMillis = currentTimeMillis;
            }

            if (requestCount.get() >= requestLimit) {
                try {
                    long sleepTime = intervalInMillis - elapsedTimeSinceReset;
                    TimeUnit.MILLISECONDS.sleep(sleepTime);
                    requestCount.set(0);
                    lastResetTimeMillis = System.currentTimeMillis();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }

            System.out.println("Creating document: " + documentJson);
            System.out.println("Signature: " + signature);

            requestCount.incrementAndGet();
        } finally {
            lock.unlock();
        }
    }

    public static void main(String[] args) {
        CrptApi crptApi = new CrptApi(TimeUnit.SECONDS, 10);
        String documentJson = "{\"description\":{\"participantInn\":\"string\"},\"doc_id\":\"string\",\"doc_status\":\"string\",\"doc_type\":\"LP_INTRODUCE_GOODS\",\"importRequest\":true,\"owner_inn\":\"string\",\"participant_inn\":\"string\",\"producer_inn\":\"string\",\"production_date\":\"2020-01-23\",\"production_type\":\"string\",\"products\":[{\"certificate_document\":\"string\",\"certificate_document_date\":\"2020-01-23\",\"certificate_document_number\":\"string\",\"owner_inn\":\"string\",\"producer_inn\":\"string\",\"production_date\":\"2020-01-23\",\"tnved_code\":\"string\",\"uit_code\":\"string\",\"uitu_code\":\"string\"}],\"reg_date\":\"2020-01-23\",\"reg_number\":\"string\"}";
        String signature = "exampleSignature";

        for (int i = 0; i < 15; i++) {
            crptApi.createDocument(documentJson, signature);
        }
    }
}
