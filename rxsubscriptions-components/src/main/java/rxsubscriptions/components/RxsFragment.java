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

package rxsubscriptions.components;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;

import rxsubscriptions.lifecycle.LifecycleProducer;
import rxsubscriptions.lifecycle.LifecycleSubscriptions;

public class RxsFragment extends Fragment {
    private final LifecycleProducer producer = LifecycleProducer.create();
    private final LifecycleSubscriptions subscriptions;

    public RxsFragment() {
        this.subscriptions = createSubscriptions();
        if (!subscriptions.setProducer(producer)) {
            throw new IllegalStateException("subclass must not set producer");
        }
    }

    protected LifecycleSubscriptions createSubscriptions() {
        return LifecycleSubscriptions.observeFragment();
    }

    @NonNull public LifecycleSubscriptions subscriptions() {
        return subscriptions;
    }

    @Override public void onAttach(Activity activity) {
        super.onAttach(activity);
        producer.onAttach();
    }

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        producer.onCreate();
    }

    @Override public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        producer.onViewCreated();
    }

    @Override public void onStart() {
        super.onStart();
        producer.onStart();
    }

    @Override public void onResume() {
        super.onResume();
        producer.onResume();
    }

    @Override public void onPause() {
        producer.onPause();
        super.onPause();
    }

    @Override public void onStop() {
        producer.onStop();
        super.onStop();
    }

    @Override public void onDestroyView() {
        producer.onDestroyView();
        super.onDestroyView();
    }

    @Override public void onDestroy() {
        producer.onDestroy();
        super.onDestroy();
    }

    @Override public void onDetach() {
        producer.onDetach();
        super.onDetach();
    }
}
