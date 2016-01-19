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

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public final class Lifecycle {

    @IntDef({
            OnCreate,
            OnStart,
            OnResume,
            OnPause,
            OnStop,
            OnDestroy
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface ActivityEvent { }

    @IntDef({
            OnAttach,
            OnCreate,
            OnViewCreated,
            OnStart,
            OnResume,
            OnPause,
            OnStop,
            OnDestroyView,
            OnDestroy,
            OnDetach
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface FragmentEvent { }

    public static final int OnAttach = 0;
    public static final int OnCreate = 1;
    public static final int OnViewCreated = 2;
    public static final int OnStart = 3;
    public static final int OnResume = 4;
    public static final int OnPause = 5;
    public static final int OnStop = 6;
    public static final int OnDestroyView = 7;
    public static final int OnDestroy = 8;
    public static final int OnDetach = 9;
}
