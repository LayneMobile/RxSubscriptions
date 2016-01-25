/*
 * Copyright 2016 Layne Mobile, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package rxsubscriptions;

import android.util.Log;

import java.util.concurrent.atomic.AtomicReference;

import rx.Observable;
import rx.Observer;
import rx.Single;
import rx.Subscription;
import rx.subscriptions.CompositeSubscription;
import rx.subscriptions.Subscriptions;
import rxsubscriptions.internal.Util;
import rxsubscriptions.subscribers.WeakSubscriber;

public class RxSubscriptions {
    private static final String TAG = RxSubscriptions.class.getSimpleName();

    private final AtomicReference<LifecycleProducer> producer = new AtomicReference<LifecycleProducer>();
    private final LifecycleObservationCalculator observationCalculator;
    private final Thread thread = Thread.currentThread();
    // We never recycle the current node, so create one initially instead of from the pool here
    private SubscriptionNode current = new SubscriptionNode();

    protected RxSubscriptions(LifecycleObservationCalculator observationCalculator) {
        this.observationCalculator = observationCalculator;
    }

    public static RxSubscriptions observeActivity() {
        return new RxSubscriptions(LifecycleObservationCalculator.ACTIVITY);
    }

    public static RxSubscriptions observeFragment() {
        return new RxSubscriptions(LifecycleObservationCalculator.FRAGMENT);
    }

    public static RxSubscriptions observeUntilStop() {
        return new RxSubscriptions(LifecycleObservationCalculator.UNTIL_STOP);
    }

    public static RxSubscriptions observe(LifecycleObservationCalculator bindingCalculator) {
        return new RxSubscriptions(bindingCalculator);
    }

    public final boolean setProducer(LifecycleProducer producer) {
        if (this.producer.compareAndSet(null, producer)) {
            producer.asObservable().subscribe(new LifecycleObserver());
            return true;
        }
        return false;
    }

    public final Observable<Integer> lifecycleObservable() {
        LifecycleProducer producer = this.producer.get();
        if (producer == null) {
            throw new IllegalStateException(
                    "must set producer before calling lifecycleObservable()");
        }
        return producer.asObservable();
    }

    public <T> SubscriptionBuilder<T> with(Observable<T> observable) {
        return new SubscriptionBuilder<T>(this, observable);
    }

    public <T> SubscriptionBuilder<T> with(Single<T> single) {
        return new SubscriptionBuilder<T>(this, single);
    }

    public final int observeUntil() {
        int current = this.current.event;
        return observationCalculator.observeUntil(current);
    }

    @SuppressWarnings("unchecked") <T> Subscription subscribe(Object o, int observeUntil,
            WeakSubscriber<? super T> subscriber) {
        assertThread();
        // First verify that event has not already passed
        if (!canSubscribe(observeUntil)) {
            Log.w(TAG, "not subscribing to observable. Binding LifecycleEvent has already passed");
            subscriber.unsubscribe();
            return Subscriptions.unsubscribed();
        }
        // Subscribe and add to composite
        final CompositeSubscription cs = current.insertAndPopulate(observeUntil).cs;
        if (o instanceof Observable) {
            return Util.subscribeWithComposite((Observable<T>) o, subscriber, cs);
        } else if (o instanceof Single) {
            return Util.subscribeWithComposite((Single<T>) o, subscriber, cs);
        }
        return Subscriptions.unsubscribed();
    }

    private void assertThread() {
        if (thread != Thread.currentThread()) {
            throw new AssertionError("LifecycleSubscriptions must only be used on one thread");
        }
    }

    private boolean canSubscribe(int event) {
        return current.ordinal() < ordinal(event);
    }

    private static int ordinal(int event) {
        if (event >= Lifecycle.OnAttach && event <= Lifecycle.OnDetach) {
            return event;
        }
        return -1;
    }

    private final class LifecycleObserver implements Observer<Integer> {
        @Override public void onCompleted() {
            // should never be called
            assertThread();
            current = current.drain(Lifecycle.OnDetach);
        }

        @Override public void onError(Throwable throwable) {
            // should never be called
            assertThread();
            current = current.drain(Lifecycle.OnDetach);
        }

        @Override public void onNext(Integer lifecycleEvent) {
            assertThread();
            current = current.drain(lifecycleEvent);
        }
    }

    private static final class SubscriptionNode implements Subscription {
        private static final Object sPoolSync = new Object();
        private static SubscriptionNode sPool;
        private static int sPoolSize = 0;
        private static final int MAX_POOL_SIZE = 10;

        SubscriptionNode prev;
        SubscriptionNode next;
        int event;
        CompositeSubscription cs;

        int ordinal() {
            return RxSubscriptions.ordinal(event);
        }

        SubscriptionNode insertAndPopulate(int event) {
            SubscriptionNode node = insert(event);
            if (node.cs == null) {
                node.cs = new CompositeSubscription();
            }
            return node;
        }

        SubscriptionNode insert(int event) {
            final int ordinal = RxSubscriptions.ordinal(event);
            if (ordinal == ordinal()) {
                return this;
            } else if (ordinal < ordinal()) {
                SubscriptionNode current = this;
                while (current.prev != null) {
                    if (ordinal == current.prev.ordinal()) {
                        return current.prev;
                    } else if (ordinal > current.prev.ordinal()) {
                        SubscriptionNode node = obtain();
                        node.event = event;
                        node.prev = current.prev;
                        node.next = current;
                        current.prev.next = node;
                        current.prev = node;
                        return node;
                    }
                    current = current.prev;
                }
                SubscriptionNode node = obtain();
                node.event = event;
                node.next = current;
                current.prev = node;
                return node;
            } else {
                SubscriptionNode current = this;
                while (current.next != null) {
                    if (ordinal == current.next.ordinal()) {
                        return current.next;
                    } else if (ordinal < current.next.ordinal()) {
                        SubscriptionNode node = obtain();
                        node.event = event;
                        node.next = current.next;
                        node.prev = current;
                        current.next.prev = node;
                        current.next = node;
                        return node;
                    }
                    current = current.next;
                }
                SubscriptionNode node = obtain();
                node.event = event;
                node.prev = current;
                current.next = node;
                return node;
            }
        }

        SubscriptionNode drain(int event) {
            SubscriptionNode current = insert(event);
            SubscriptionNode head = findHead(current);
            while (head != current) {
                SubscriptionNode next = head.next;
                head.recycle();
                head = next;
            }
            current.prev = null;
            current.unsubscribe();
            return current;
        }

        @Override public boolean isUnsubscribed() {
            return cs == null || cs.isUnsubscribed();
        }

        @Override public void unsubscribe() {
            if (cs != null) {
                if (Log.isLoggable(TAG, Log.VERBOSE)) { Log.v(TAG, "unsubscribing " + this); }
                cs.unsubscribe();
                cs = null;
            }
        }

        @Override public String toString() {
            return "SubscriptionNode{" +
                    "event=" + event +
                    '}';
        }

        private void recycle() {
            next = null;
            prev = null;
            event = -1;
            unsubscribe();

            synchronized (sPoolSync) {
                if (sPoolSize < MAX_POOL_SIZE) {
                    next = sPool;
                    sPool = this;
                    sPoolSize++;
                }
            }
        }

        private static SubscriptionNode findHead(SubscriptionNode node) {
            SubscriptionNode head = node;
            while (head.prev != null) {
                head = head.prev;
            }
            return head;
        }

        private static SubscriptionNode obtain() {
            synchronized (sPoolSync) {
                if (sPool != null) {
                    SubscriptionNode n = sPool;
                    sPool = n.next;
                    n.next = null;
                    sPoolSize--;
                    return n;
                }
            }
            return new SubscriptionNode();
        }
    }
}
