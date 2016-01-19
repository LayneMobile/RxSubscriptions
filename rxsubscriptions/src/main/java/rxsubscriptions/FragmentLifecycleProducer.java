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


public final class FragmentLifecycleProducer extends LifecycleProducer {
    private FragmentLifecycleProducer() {}

    public static FragmentLifecycleProducer create() {
        return new FragmentLifecycleProducer();
    }

    @Override public final void onAttach() {
        super.onAttach();
    }

    @Override public final void onDestroyView() {
        super.onDestroyView();
    }

    @Override public final void onDetach() {
        super.onDetach();
    }

    @Override public final void onViewCreated() {
        super.onViewCreated();
    }
}
