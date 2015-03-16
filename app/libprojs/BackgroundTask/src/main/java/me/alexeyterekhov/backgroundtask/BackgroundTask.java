package me.alexeyterekhov.backgroundtask;

import android.os.AsyncTask;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;


public class BackgroundTask <SourceType, ResultType> {
    // Modes of repeating failed sources
    public static final int REPEAT_ALL_FAILED = 1;
    public static final int REPEAT_ONE_WHILE_FAIL = 2;

    private Worker worker;
    private List<SourcePack> sourceData = new LinkedList<SourcePack>();

    private SourceHandler<SourceType, ResultType> sourceHandler;
    private ResultHandler<SourceType, ResultType> resultHandler;
    private ErrorHandler<SourceType> errorHandler;

    private int maxAttempts = 0; // 0 = Infinity
    private int delayMilliseconds = 0;
    private int mode = REPEAT_ALL_FAILED;
    private boolean shouldStop = false;
    private boolean running = false;

    private void run() {
        running = true;
        worker = new Worker();
        worker.execute();
    }

    public BackgroundTask(SourceHandler<SourceType, ResultType> sourceHandler,
                          ResultHandler<SourceType, ResultType> resultHandler,
                          ErrorHandler<SourceType> errorHandler) {
        this.sourceHandler = sourceHandler;
        this.resultHandler = resultHandler;
        this.errorHandler = errorHandler;
    }

    public void setMaxAttempts(int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }
    public void setRepeatOnFailMode(int mode) {
        this.mode = mode;
    }
    public void setDelayBetweenAttempts(int delayMilliseconds) {
        this.delayMilliseconds = delayMilliseconds;
    }

    public void pauseAfterCurrentSource() { shouldStop = true; }
    public void cancel() {
        shouldStop = true;
        sourceData.clear();
    }
    public boolean willPause() {
        return shouldStop;
    }
    public boolean isRunning() { return running; }
    public void resumeHandling() {
        shouldStop = false;
        if (!sourceData.isEmpty() && !running)
            run();
    }
    public void handle(Collection<SourceType> sources) {
        SourcePack sourcePack;
        for (SourceType source : sources) {
            sourcePack = new SourcePack();
            sourcePack.source = source;
            sourcePack.attempt = 0;
            sourceData.add(sourcePack);
        }
        shouldStop = false;
        if (!running)
            run();
    }
    public void handle(SourceType source) {
        SourcePack sourcePack = new SourcePack();
        sourcePack.source = source;
        sourcePack.attempt = 0;
        sourceData.add(sourcePack);
        shouldStop = false;
        if (!running)
            run();
    }

    private class SourcePack {
        public SourceType source = null;
        public int attempt = 0;
    }

    private class PublishProgressPack {
        public SourceType source;
        public ResultType okResult;
        public boolean error;
        public Exception exception;
        public boolean complete;
    }

    private class Worker extends AsyncTask<Void, PublishProgressPack, Void> {
        private int packSentCount = 0;
        private int packReceivedCount = 0;
        @Override
        protected Void doInBackground(Void... params) {
            SourcePack sourcePack;
            ResultType result;
            PublishProgressPack progressPack = new PublishProgressPack();

            while (!sourceData.isEmpty()) {
                if (shouldStop) {
                    running = false;
                    return null;
                }
                sourcePack = sourceData.remove(0);
                try {
                    result = sourceHandler.getResult(BackgroundTask.this, sourcePack.source);
                    progressPack.error = false;
                    progressPack.complete = false;
                    progressPack.okResult = result;
                    progressPack.source = sourcePack.source;
                    ++packSentCount;
                    publishProgress(progressPack);
                } catch (Exception e) {
                    progressPack.error = true;
                    progressPack.exception = e;
                    progressPack.source = sourcePack.source;
                    ++packSentCount;
                    publishProgress(progressPack);
                    if (delayMilliseconds > 0)
                        try { Thread.sleep(delayMilliseconds); } catch (InterruptedException x) {}
                    sourcePack.attempt++;
                    if (maxAttempts == 0 || sourcePack.attempt < maxAttempts) {
                        if (mode == REPEAT_ALL_FAILED)
                            sourceData.add(sourcePack);
                        else if (mode == REPEAT_ONE_WHILE_FAIL)
                            sourceData.add(0, sourcePack);
                    }
                }
                // waiting for sent result pack will be handled
                while (packSentCount > packReceivedCount)
                    try { Thread.sleep(10); } catch (Exception e) {}
            }
            running = false;

            progressPack.complete = true;
            ++packSentCount;
            publishProgress(progressPack);
            return null;
        }

        @Override
        protected void onProgressUpdate(PublishProgressPack... values) {
            super.onProgressUpdate(values);
            PublishProgressPack pack = values[0];
            if (!pack.complete) {
                if (pack.error) {
                    errorHandler.onError(BackgroundTask.this, pack.source, pack.exception);
                } else {
                    resultHandler.onNext(BackgroundTask.this, pack.source, pack.okResult);
                }
            } else
                resultHandler.onComplete(BackgroundTask.this);
            ++packReceivedCount;
        }
    }
}
