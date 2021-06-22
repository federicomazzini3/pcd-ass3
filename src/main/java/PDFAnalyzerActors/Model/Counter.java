package PDFAnalyzerActors.Model;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Counter {
    private Map<String, Integer> occurrences;
    private Lock mutex;
    private int processedWords;

    public Counter() {
        this.mutex = new ReentrantLock();
        this.occurrences = new HashMap<>();
        this.processedWords = 0;
    }

    /*
     * merge tra le occorrenze già nell'oggetto e quelle passate in input
     * (faccio la somma in caso le parole sono da entrambe le parti)
     */
    public void mergeOccurrence(Map<String, Integer> mapToMerge, int processedWords) {
        try {
            mutex.lock();
            mapToMerge.forEach((k, v) -> occurrences.merge(k, v, Integer::sum));
            this.processedWords += processedWords;
        } finally {
            mutex.unlock();
        }
    }

    public Map<String, Integer> getOccurrences() throws InterruptedException {
        try {
            mutex.lock();
            return new HashMap<>(this.occurrences);
        } finally {
            mutex.unlock();
        }
    }

    public int getProcessedWords() {
        try {
            mutex.lock();
            return this.processedWords;
        }finally {
            mutex.unlock();
        }
    }
}
