/**
 * Copyright (c) 2016-present, RxJava Contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 */

package io.reactivex.rxjava3.observables;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.annotations.*;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.internal.functions.*;
import io.reactivex.rxjava3.internal.operators.observable.*;
import io.reactivex.rxjava3.internal.util.ConnectConsumer;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * A {@code ConnectableObservable} resembles an ordinary {@link Observable}, except that it does not begin
 * emitting items when it is subscribed to, but only when its {@link #connect} method is called. In this way you
 * can wait for all intended {@link Observer}s to {@link Observable#subscribe} to the {@code Observable}
 * before the {@code Observable} begins emitting items.
 * <p>
 * <img width="640" height="510" src="https://github.com/ReactiveX/RxJava/wiki/images/rx-operators/publishConnect.v3.png" alt="">
 * <p>
 * When the upstream terminates, the {@code ConnectableObservable} remains in this terminated state and,
 * depending on the actual underlying implementation, relays cached events to late {@link Observer}s.
 * In order to reuse and restart this {@code ConnectableObservable}, the {@link #reset()} method has to be called.
 * When called, this {@code ConnectableObservable} will appear as fresh, unconnected source to new {@link Observer}s.
 * Disposing the connection will reset the {@code ConnectableFlowable} to its fresh state and there is no need to call
 * {@code reset()} in this case.
 * <p>
 * Note that although {@link #connect()} and {@link #reset()} are safe to call from multiple threads, it is recommended
 * a dedicated thread or business logic manages the connection or resetting of a {@code ConnectableObservable} so that
 * there is no unwanted signal loss due to early {@code connect()} or {@code reset()} calls while {@code Observer}s are
 * still being subscribed to to this {@code ConnectableObservable} to receive signals from the get go.
 *
 * @see <a href="https://github.com/ReactiveX/RxJava/wiki/Connectable-Observable-Operators">RxJava Wiki:
 *      Connectable Observable Operators</a>
 * @param <T>
 *          the type of items emitted by the {@code ConnectableObservable}
 */
public abstract class ConnectableObservable<T> extends Observable<T> {

    /**
     * Instructs the {@code ConnectableObservable} to begin emitting the items from its underlying
     * {@link Observable} to its {@link Observer}s.
     * <dl>
     *  <dt><b>Scheduler:</b></dt>
     *  <dd>The behavior is determined by the implementor of this abstract class.</dd>
     * </dl>
     *
     * @param connection
     *          the action that receives the connection subscription before the subscription to source happens
     *          allowing the caller to synchronously disconnect a synchronous source
     * @throws NullPointerException if {@code connection} is {@code null}
     * @see <a href="http://reactivex.io/documentation/operators/connect.html">ReactiveX documentation: Connect</a>
     */
    @SchedulerSupport(SchedulerSupport.NONE)
    public abstract void connect(@NonNull Consumer<? super Disposable> connection);

    /**
     * Resets this ConnectableObservable into its fresh state if it has terminated
     * or has been disposed.
     * <p>
     * Calling this method on a fresh or active {@code ConnectableObservable} has no effect.
     * <dl>
     *  <dt><b>Scheduler:</b></dt>
     *  <dd>The behavior is determined by the implementor of this abstract class.</dd>
     * </dl>
     * @since 3.0.0
     */
    @SchedulerSupport(SchedulerSupport.NONE)
    public abstract void reset();

    /**
     * Instructs the {@code ConnectableObservable} to begin emitting the items from its underlying
     * {@link Observable} to its {@link Observer}s.
     * <p>
     * To disconnect from a synchronous source, use the {@link #connect(Consumer)} method.
     * <dl>
     *  <dt><b>Scheduler:</b></dt>
     *  <dd>The behavior is determined by the implementor of this abstract class.</dd>
     * </dl>
     *
     * @return the subscription representing the connection
     * @see <a href="http://reactivex.io/documentation/operators/connect.html">ReactiveX documentation: Connect</a>
     */
    @NonNull
    @SchedulerSupport(SchedulerSupport.NONE)
    public final Disposable connect() {
        ConnectConsumer cc = new ConnectConsumer();
        connect(cc);
        return cc.disposable;
    }

    /**
     * Returns an {@code Observable} that stays connected to this {@code ConnectableObservable} as long as there
     * is at least one subscription to this {@code ConnectableObservable}.
     * <dl>
     *  <dt><b>Scheduler:</b></dt>
     *  <dd>This {@code refCount} overload does not operate on any particular {@link Scheduler}.</dd>
     * </dl>
     * @return an {@link Observable}
     * @see <a href="http://reactivex.io/documentation/operators/refcount.html">ReactiveX documentation: RefCount</a>
     * @see #refCount(int)
     * @see #refCount(long, TimeUnit)
     * @see #refCount(int, long, TimeUnit)
     */
    @NonNull
    @CheckReturnValue
    @SchedulerSupport(SchedulerSupport.NONE)
    public Observable<T> refCount() {
        return RxJavaPlugins.onAssembly(new ObservableRefCount<>(this));
    }

    /**
     * Connects to the upstream {@code ConnectableObservable} if the number of subscribed
     * observers reaches the specified count and disconnect if all subscribers have unsubscribed.
     * <dl>
     *  <dt><b>Scheduler:</b></dt>
     *  <dd>This {@code refCount} overload does not operate on any particular {@link Scheduler}.</dd>
     * </dl>
     * <p>History: 2.1.14 - experimental
     * @param subscriberCount the number of subscribers required to connect to the upstream
     * @return the new Observable instance
     * @since 2.2
     */
    @CheckReturnValue
    @SchedulerSupport(SchedulerSupport.NONE)
    @NonNull
    public final Observable<T> refCount(int subscriberCount) {
        return refCount(subscriberCount, 0, TimeUnit.NANOSECONDS, Schedulers.trampoline());
    }

    /**
     * Connects to the upstream {@code ConnectableObservable} if the number of subscribed
     * observers reaches 1 and disconnect after the specified
     * timeout if all subscribers have unsubscribed.
     * <dl>
     *  <dt><b>Scheduler:</b></dt>
     *  <dd>This {@code refCount} overload operates on the {@code computation} {@link Scheduler}.</dd>
     * </dl>
     * <p>History: 2.1.14 - experimental
     * @param timeout the time to wait before disconnecting after all subscribers unsubscribed
     * @param unit the time unit of the timeout
     * @return the new Observable instance
     * @throws NullPointerException if {@code unit} is {@code null}
     * @see #refCount(long, TimeUnit, Scheduler)
     * @since 2.2
     */
    @CheckReturnValue
    @SchedulerSupport(SchedulerSupport.COMPUTATION)
    @NonNull
    public final Observable<T> refCount(long timeout, @NonNull TimeUnit unit) {
        return refCount(1, timeout, unit, Schedulers.computation());
    }

    /**
     * Connects to the upstream {@code ConnectableObservable} if the number of subscribed
     * observers reaches 1 and disconnect after the specified
     * timeout if all subscribers have unsubscribed.
     * <dl>
     *  <dt><b>Scheduler:</b></dt>
     *  <dd>This {@code refCount} overload operates on the specified {@link Scheduler}.</dd>
     * </dl>
     * <p>History: 2.1.14 - experimental
     * @param timeout the time to wait before disconnecting after all subscribers unsubscribed
     * @param unit the time unit of the timeout
     * @param scheduler the target scheduler to wait on before disconnecting
     * @return the new Observable instance
     * @throws NullPointerException if {@code unit} or {@code scheduler} is {@code null}
     * @since 2.2
     */
    @CheckReturnValue
    @SchedulerSupport(SchedulerSupport.CUSTOM)
    @NonNull
    public final Observable<T> refCount(long timeout, @NonNull TimeUnit unit, @NonNull Scheduler scheduler) {
        return refCount(1, timeout, unit, scheduler);
    }

    /**
     * Connects to the upstream {@code ConnectableObservable} if the number of subscribed
     * observers reaches the specified count and disconnect after the specified
     * timeout if all subscribers have unsubscribed.
     * <dl>
     *  <dt><b>Scheduler:</b></dt>
     *  <dd>This {@code refCount} overload operates on the {@code computation} {@link Scheduler}.</dd>
     * </dl>
     * <p>History: 2.1.14 - experimental
     * @param subscriberCount the number of subscribers required to connect to the upstream
     * @param timeout the time to wait before disconnecting after all subscribers unsubscribed
     * @param unit the time unit of the timeout
     * @return the new Observable instance
     * @throws NullPointerException if {@code unit} or {@code scheduler} is {@code null}
     * @see #refCount(int, long, TimeUnit, Scheduler)
     * @since 2.2
     */
    @CheckReturnValue
    @SchedulerSupport(SchedulerSupport.COMPUTATION)
    @NonNull
    public final Observable<T> refCount(int subscriberCount, long timeout, @NonNull TimeUnit unit) {
        return refCount(subscriberCount, timeout, unit, Schedulers.computation());
    }

    /**
     * Connects to the upstream {@code ConnectableObservable} if the number of subscribed
     * observers reaches the specified count and disconnect after the specified
     * timeout if all subscribers have unsubscribed.
     * <dl>
     *  <dt><b>Scheduler:</b></dt>
     *  <dd>This {@code refCount} overload operates on the specified {@link Scheduler}.</dd>
     * </dl>
     * <p>History: 2.1.14 - experimental
     * @param subscriberCount the number of subscribers required to connect to the upstream
     * @param timeout the time to wait before disconnecting after all subscribers unsubscribed
     * @param unit the time unit of the timeout
     * @param scheduler the target scheduler to wait on before disconnecting
     * @return the new Observable instance
     * @throws NullPointerException if {@code unit} or {@code scheduler} is {@code null}
     * @throws IllegalArgumentException if {@code subscriberCount} is non-positive
     * @since 2.2
     */
    @CheckReturnValue
    @SchedulerSupport(SchedulerSupport.CUSTOM)
    @NonNull
    public final Observable<T> refCount(int subscriberCount, long timeout, @NonNull TimeUnit unit, @NonNull Scheduler scheduler) {
        ObjectHelper.verifyPositive(subscriberCount, "subscriberCount");
        Objects.requireNonNull(unit, "unit is null");
        Objects.requireNonNull(scheduler, "scheduler is null");
        return RxJavaPlugins.onAssembly(new ObservableRefCount<>(this, subscriberCount, timeout, unit, scheduler));
    }

    /**
     * Returns an Observable that automatically connects (at most once) to this ConnectableObservable
     * when the first Observer subscribes.
     * <p>
     * <img width="640" height="348" src="https://raw.github.com/wiki/ReactiveX/RxJava/images/rx-operators/autoConnect.o.png" alt="">
     * <p>
     * The connection happens after the first subscription and happens at most once
     * during the lifetime of the returned Observable. If this ConnectableObservable
     * terminates, the connection is never renewed, no matter how Observers come
     * and go. Use {@link #refCount()} to renew a connection or dispose an active
     * connection when all {@code Observer}s have disposed their {@code Disposable}s.
     * <p>
     * This overload does not allow disconnecting the connection established via
     * {@link #connect(Consumer)}. Use the {@link #autoConnect(int, Consumer)} overload
     * to gain access to the {@code Disposable} representing the only connection.
     * <dl>
     *  <dt><b>Scheduler:</b></dt>
     *  <dd>{@code autoConnect} overload does not operate on any particular {@link Scheduler}.</dd>
     * </dl>
     *
     * @return an Observable that automatically connects to this ConnectableObservable
     *         when the first Observer subscribes
     */
    @NonNull
    @CheckReturnValue
    @SchedulerSupport(SchedulerSupport.NONE)
    public Observable<T> autoConnect() {
        return autoConnect(1);
    }

    /**
     * Returns an Observable that automatically connects (at most once) to this ConnectableObservable
     * when the specified number of Observers subscribe to it.
     * <p>
     * <img width="640" height="348" src="https://raw.github.com/wiki/ReactiveX/RxJava/images/rx-operators/autoConnect.o.png" alt="">
     * <p>
     * The connection happens after the given number of subscriptions and happens at most once
     * during the lifetime of the returned Observable. If this ConnectableObservable
     * terminates, the connection is never renewed, no matter how Observers come
     * and go. Use {@link #refCount()} to renew a connection or dispose an active
     * connection when all {@code Observer}s have disposed their {@code Disposable}s.
     * <p>
     * This overload does not allow disconnecting the connection established via
     * {@link #connect(Consumer)}. Use the {@link #autoConnect(int, Consumer)} overload
     * to gain access to the {@code Disposable} representing the only connection.
     * <dl>
     *  <dt><b>Scheduler:</b></dt>
     *  <dd>{@code autoConnect} overload does not operate on any particular {@link Scheduler}.</dd>
     * </dl>
     *
     * @param numberOfSubscribers the number of subscribers to await before calling connect
     *                            on the ConnectableObservable. A non-positive value indicates
     *                            an immediate connection.
     * @return an Observable that automatically connects to this ConnectableObservable
     *         when the specified number of Subscribers subscribe to it
     */
    @NonNull
    @CheckReturnValue
    @SchedulerSupport(SchedulerSupport.NONE)
    public Observable<T> autoConnect(int numberOfSubscribers) {
        return autoConnect(numberOfSubscribers, Functions.emptyConsumer());
    }

    /**
     * Returns an Observable that automatically connects (at most once) to this ConnectableObservable
     * when the specified number of Subscribers subscribe to it and calls the
     * specified callback with the Subscription associated with the established connection.
     * <p>
     * <img width="640" height="348" src="https://raw.github.com/wiki/ReactiveX/RxJava/images/rx-operators/autoConnect.o.png" alt="">
     * <p>
     * The connection happens after the given number of subscriptions and happens at most once
     * during the lifetime of the returned Observable. If this ConnectableObservable
     * terminates, the connection is never renewed, no matter how Observers come
     * and go. Use {@link #refCount()} to renew a connection or dispose an active
     * connection when all {@code Observer}s have disposed their {@code Disposable}s.
     * <dl>
     *  <dt><b>Scheduler:</b></dt>
     *  <dd>{@code autoConnect} overload does not operate on any particular {@link Scheduler}.</dd>
     * </dl>
     *
     * @param numberOfSubscribers the number of subscribers to await before calling connect
     *                            on the ConnectableObservable. A non-positive value indicates
     *                            an immediate connection.
     * @param connection the callback Consumer that will receive the Subscription representing the
     *                   established connection
     * @return an Observable that automatically connects to this ConnectableObservable
     *         when the specified number of Subscribers subscribe to it and calls the
     *         specified callback with the Subscription associated with the established connection
     * @throws NullPointerException if {@code connection} is {@code null}
     */
    @NonNull
    @CheckReturnValue
    @SchedulerSupport(SchedulerSupport.NONE)
    public Observable<T> autoConnect(int numberOfSubscribers, @NonNull Consumer<? super Disposable> connection) {
        Objects.requireNonNull(connection, "connection is null");
        if (numberOfSubscribers <= 0) {
            this.connect(connection);
            return RxJavaPlugins.onAssembly(this);
        }
        return RxJavaPlugins.onAssembly(new ObservableAutoConnect<>(this, numberOfSubscribers, connection));
    }
}
