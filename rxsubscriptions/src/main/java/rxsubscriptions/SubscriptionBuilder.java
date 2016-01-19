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

import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.Single;
import rx.SingleSubscriber;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.observers.Subscribers;
import rxsubscriptions.internal.Util;
import rxsubscriptions.subscribers.ActionSubscriber;
import rxsubscriptions.subscribers.WeakSubscriber;

public final class SubscriptionBuilder<T> {
    private final RxSubscriptions subscriptions;
    private final Object o;
    private Scheduler subscribeOn;
    private int observeUntil = -1;
    private Scheduler observeOn;

    SubscriptionBuilder(RxSubscriptions subscriptions, Observable<T> observable) {
        this.subscriptions = subscriptions;
        this.o = observable;
    }

    SubscriptionBuilder(RxSubscriptions subscriptions, Single<T> single) {
        this.subscriptions = subscriptions;
        this.o = single;
    }

    public SubscriptionBuilder<T> subscribeOn(Scheduler subscribeOn) {
        this.subscribeOn = subscribeOn;
        return this;
    }

    public SubscriptionBuilder<T> observeUntil(int event) {
        this.observeUntil = event;
        return this;
    }

    public SubscriptionBuilder<T> observeOnMainThread() {
        return observeOn(AndroidSchedulers.mainThread());
    }

    public SubscriptionBuilder<T> observeOn(Scheduler observeOn) {
        this.observeOn = observeOn;
        return this;
    }

    public Subscription subscribe(Action1<? super T> onNext) {
        return subscribe(new ActionSubscriber<T>(onNext));
    }

    public Subscription subscribe(Action1<? super T> onNext, Action1<Throwable> onError) {
        return subscribe(new ActionSubscriber<T>(onNext, onError));
    }

    public Subscription subscribe(Action1<? super T> onNext, Action1<Throwable> onError,
            Action0 onCompleted) {
        return subscribe(new ActionSubscriber<T>(onNext, onError, onCompleted));
    }

    public Subscription subscribe(Observer<? super T> observer) {
        return subscribe(Subscribers.from(observer));
    }

    public Subscription subscribe(SingleSubscriber<? super T> subscriber) {
        return subscribe(Util.asSubscriber(subscriber));
    }

    public Subscription subscribe(Subscriber<? super T> subscriber) {
        // ObserveUntil
        int observeUntil = this.observeUntil;
        if (observeUntil == -1) {
            observeUntil = subscriptions.observeUntil();
        }
        // Actual subscribe
        return subscriptions.subscribe(configureSubscribe(), observeUntil, WeakSubscriber.create(subscriber));
    }

    @SuppressWarnings("unchecked")
    private Object configureSubscribe() {
        Object o = this.o;
        if (o instanceof Observable) {
            return configureSubscribe((Observable<T>) o);
        } else if (o instanceof Single) {
            return configureSubscribe((Single<T>) o);
        }
        return o;
    }

    private Observable<T> configureSubscribe(Observable<T> observable) {
        // SubscribeOn
        Scheduler subscribeOn = this.subscribeOn;
        observable = subscribeOn == null
                ? observable
                : observable.subscribeOn(subscribeOn);
        // ObserveOn
        Scheduler observeOn = this.observeOn;
        return observeOn == null
                ? observable
                : observable.observeOn(observeOn);
    }

    private Single<T> configureSubscribe(Single<T> single) {
        // SubscribeOn
        Scheduler subscribeOn = this.subscribeOn;
        single = subscribeOn == null
                ? single
                : single.subscribeOn(subscribeOn);
        // ObserveOn
        Scheduler observeOn = this.observeOn;
        return observeOn == null
                ? single
                : single.observeOn(observeOn);
    }
}
