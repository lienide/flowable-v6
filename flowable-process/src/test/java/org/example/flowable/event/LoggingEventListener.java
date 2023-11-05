package org.example.flowable.event;

import org.flowable.common.engine.api.delegate.event.FlowableEvent;
import org.flowable.engine.delegate.event.BaseEntityEventListener;

public class LoggingEventListener extends BaseEntityEventListener {

    @Override
    protected void onCreate(FlowableEvent event) {
        super.onCreate(event);
    }

    @Override
    protected void onInitialized(FlowableEvent event) {
        super.onInitialized(event);
    }

    @Override
    protected void onDelete(FlowableEvent event) {
        super.onDelete(event);
    }

    @Override
    protected void onUpdate(FlowableEvent event) {
        super.onUpdate(event);
    }

    @Override
    protected void onEntityEvent(FlowableEvent event) {
        super.onEntityEvent(event);
    }
}
