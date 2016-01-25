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
import rx.subjects.BehaviorSubject;

public abstract class LifecycleProducer {
    private final BehaviorSubject<Integer> subject = BehaviorSubject.create();

    LifecycleProducer() {}

    void onAttach() {
        subject.onNext(Lifecycle.OnAttach);
    }

    public final void onCreate() {
        subject.onNext(Lifecycle.OnCreate);
    }

    void onViewCreated() {
        subject.onNext(Lifecycle.OnViewCreated);
    }

    public final void onStart() {
        subject.onNext(Lifecycle.OnStart);
    }

    public final void onResume() {
        subject.onNext(Lifecycle.OnResume);
    }

    public final void onPause() {
        subject.onNext(Lifecycle.OnPause);
    }

    public final void onStop() {
        subject.onNext(Lifecycle.OnStop);
    }

    void onDestroyView() {
        subject.onNext(Lifecycle.OnDestroyView);
    }

    public final void onDestroy() {
        subject.onNext(Lifecycle.OnDestroy);
    }

    void onDetach() {
        subject.onNext(Lifecycle.OnDetach);
    }

    public Observable<Integer> asObservable() {
        return subject.asObservable();
    }
}
