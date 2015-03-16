package me.alexeyterekhov.backgroundtask;


/**
 This object get every "result object" (from SourceHandler)
 and handle it in UI-thread so you can
 change your UI with obtained result
 onComplete invokes when all objects are handled
 */
public interface ResultHandler <SourceType, ResultType> {
    public void onNext(BackgroundTask task, SourceType source, ResultType result);
    public void onComplete(BackgroundTask task);
}
