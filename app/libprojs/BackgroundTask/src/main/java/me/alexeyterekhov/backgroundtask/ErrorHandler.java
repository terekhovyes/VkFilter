package me.alexeyterekhov.backgroundtask;


/**
 In this object you can handle any exceptions
 thrown in SourceHandler. Working in UI thread
 so you can interact with your UI here
 */
public interface ErrorHandler <SourceType> {
    public void onError(BackgroundTask task,
                        SourceType failedSource,
                        Exception e);
}
