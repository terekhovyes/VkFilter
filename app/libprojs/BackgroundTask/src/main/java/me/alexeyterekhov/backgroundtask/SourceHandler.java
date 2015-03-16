package me.alexeyterekhov.backgroundtask;


/**
 This object handle "source object" in async thread and as result:
 --> return "result object" and it will be handled by ResultHandler
 --> throw exception while handling and then:
     1) it will be handled by ErrorHandler
     2) BackgroundTask will try handle it again later
 You can throw Exception manually if something went wrong to handle it again later
 */
public interface SourceHandler<SourceType, ResultType> {
    public ResultType getResult(BackgroundTask task,
                                SourceType from) throws Exception;
}
