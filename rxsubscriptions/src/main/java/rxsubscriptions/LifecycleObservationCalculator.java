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


public interface LifecycleObservationCalculator {
    LifecycleObservationCalculator ACTIVITY = new LifecycleObservationCalculator() {
        @Override public int observeUntil(int current) {
            switch (current) {
                case Lifecycle.OnCreate:
                    return Lifecycle.OnDestroy;
                case Lifecycle.OnStart:
                    return Lifecycle.OnStop;
                case Lifecycle.OnResume:
                    return Lifecycle.OnPause;
                case Lifecycle.OnPause:
                    return Lifecycle.OnPause;
                case Lifecycle.OnStop:
                case Lifecycle.OnDestroy:
                default:
                    return Lifecycle.OnDestroy;
            }
        }
    };

    LifecycleObservationCalculator FRAGMENT = new LifecycleObservationCalculator() {
        @Override public int observeUntil(int current) {
            switch (current) {
                case Lifecycle.OnAttach:
                    return Lifecycle.OnDetach;
                case Lifecycle.OnCreate:
                    return Lifecycle.OnDestroy;
                case Lifecycle.OnViewCreated:
                    return Lifecycle.OnDestroyView;
                case Lifecycle.OnStart:
                    return Lifecycle.OnStop;
                case Lifecycle.OnResume:
                    return Lifecycle.OnPause;
                case Lifecycle.OnPause:
                    return Lifecycle.OnStop;
                case Lifecycle.OnStop:
                    return Lifecycle.OnDestroyView;
                case Lifecycle.OnDestroyView:
                    return Lifecycle.OnDestroy;
                case Lifecycle.OnDestroy:
                case Lifecycle.OnDetach:
                default:
                    return Lifecycle.OnDetach;
            }
        }
    };

    LifecycleObservationCalculator UNTIL_STOP = new LifecycleObservationCalculator() {
        @Override public int observeUntil(int current) {
            return Lifecycle.OnStop;
        }
    };

    int observeUntil(int current);
}
