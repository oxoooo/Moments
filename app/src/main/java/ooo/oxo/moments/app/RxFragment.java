/*
 * Moments - To the best Instagram client
 * Copyright (C) 2015  XiNGRZ <xxx@oxo.ooo>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program;  if not, see <http://www.gnu.org/licenses/>.
 */

package ooo.oxo.moments.app;

import android.support.v4.app.Fragment;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

public class RxFragment extends Fragment {

    private final CompositeSubscription subscriptions = new CompositeSubscription();


    protected <T> void subscribe(Observable<T> observable,
                                 Action1<T> onNext) {
        subscriptions.add(observable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onNext));
    }

    protected <T> void subscribe(Observable<T> observable,
                                 Action1<? super T> onNext,
                                 Action1<Throwable> onError) {
        subscriptions.add(observable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onNext, onError));
    }

    protected <T> void subscribe(Observable<T> observable,
                                 Action1<? super T> onNext,
                                 Action1<Throwable> onError,
                                 Action0 onComplete) {
        subscriptions.add(observable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onNext, onError, onComplete));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        subscriptions.unsubscribe();
    }

}
