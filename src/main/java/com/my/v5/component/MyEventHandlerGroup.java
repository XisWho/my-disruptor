package com.my.v5.component;

public class MyEventHandlerGroup<T> {

    private final MyDisruptor<T> disruptor;
    private final MyConsumerRepository<T> myConsumerRepository;
    private final MySequence[] sequences;

    public MyEventHandlerGroup(MyDisruptor<T> disruptor,
                               MyConsumerRepository<T> myConsumerRepository,
                               MySequence[] sequences) {
        this.disruptor = disruptor;
        this.myConsumerRepository = myConsumerRepository;
        this.sequences = sequences;
    }

    @SafeVarargs
    public final MyEventHandlerGroup<T> then(final MyEventHandler<T>... myEventHandlers) {
        return handleEventsWith(myEventHandlers);
    }

    @SafeVarargs
    public final MyEventHandlerGroup<T> handleEventsWith(final MyEventHandler<T>... handlers) {
        return disruptor.createEventProcessors(sequences, handlers);
    }

    @SafeVarargs
    public final MyEventHandlerGroup<T> thenHandleEventsWithWorkerPool(final MyWorkHandler<T>... handlers) {
        return handleEventsWithWorkerPool(handlers);
    }

    @SafeVarargs
    public final MyEventHandlerGroup<T> handleEventsWithWorkerPool(final MyWorkHandler<T>... handlers) {
        return disruptor.createWorkerPool(sequences, handlers);
    }

}
