/*
 * Copyright (C) 2016-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.common.eventbus;

import com.badlogic.gdx.utils.reflect.Method;
import com.badlogic.gdx.utils.reflect.ReflectionException;

import java.util.Objects;

/**
 * A subscriber method on a specific object, plus the executor that should be
 * used for dispatching events to it.
 *
 * <p>
 * Two subscribers are equivalent when they refer to the same method on the same
 * object (not class). This property is used to ensure that no subscriber method
 * is registered more than once.
 *
 * @author Colin Decker
 */
class Subscriber {
    /**
     * Creates a {@code Subscriber} for {@code method} on {@code listener}.
     */
    static Subscriber create(EventBus bus, Object listener, Method method,
        int registryKey) {
        return new Subscriber(bus, listener, method, registryKey);
    }

    /** The event bus this subscriber belongs to. */
    private final EventBus eventBus;

    /** The object with the subscriber method. */
    private final Object listener;

    /** Subscriber method. */
    private final Method method;

    /** The pre-computed hash code. */
    private final int hashCode;

    /** The parameter hashCode of this subscriber used by the registry. */
    final int registryKey;

    private Subscriber(EventBus eventBus, Object listener, Method method,
        int registryKey) {
        this.eventBus = Objects.requireNonNull(eventBus);
        this.listener = Objects.requireNonNull(listener);
        method.setAccessible(true); // method.trySetAccessible()
        this.method = method;
        this.registryKey = registryKey;
        this.hashCode = computeHashCode();
    }

    /**
     * Dispatches {@code event} to this subscriber using the proper executor.
     */
    final void dispatchEvent(final Object event) {
        eventBus.executor().execute(() -> {
            try {
                invokeSubscriberMethod(event);
            } catch (ReflectionException e) {
                // do not publish if an exception occurs in the exception event handler
                if (!(event instanceof ExceptionEvent)) {
                    eventBus.post(
                        new ExceptionEvent(eventBus, getTarget(), method, event,
                            e.getCause()));
                }
            }
        });
    }

    /**
     * Invokes the subscriber method. This method can be overridden to make the
     * invocation synchronized.
     */
    void invokeSubscriberMethod(Object event) throws ReflectionException {
        try {
            Object target = getTarget();
            if (target == null) {// if the target object is no longer available,
                eventBus.unregister(this); // un-subscribe this subscriber!
                return;
            }
            method.invoke(target, event);
        } catch (IllegalArgumentException e) {
            throw new Error("Method rejected target/argument: " + event, e);
        } catch (ReflectionException e) {
            if (e.getCause() instanceof Error) {
                throw (Error) e.getCause();
            }
            throw e;
        }
    }

    Object getTarget() {
        return listener;
    }

    private int computeHashCode() {
        return (31 + method.hashCode()) * 31 + System.identityHashCode(
            getTarget());
    }

    @Override
    public final int hashCode() {
        return hashCode;
    }

    @Override
    public final boolean equals(Object obj) {
        if (obj instanceof Subscriber) {
            Subscriber that = (Subscriber) obj;
            // Use == so that different equal instances will still receive events.
            // We only guard against the case that the same object is registered multiple
            // times
            return getTarget() == that.getTarget() && method.equals(
                that.method);
        }
        return false;
    }

}
