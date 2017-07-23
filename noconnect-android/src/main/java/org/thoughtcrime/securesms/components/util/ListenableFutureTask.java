/**
 * Copyright (C) 2014 Open Whisper Systems
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.thoughtcrime.securesms.components.util;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import javax.annotation.Nullable;

public class ListenableFutureTask<V> extends FutureTask<V> {

	private final List<FutureTaskListener<V>> listeners = new LinkedList<>();

	@Nullable
	private final Object identifier;

	public ListenableFutureTask(Callable<V> callable) {
		this(callable, null);
	}

	private ListenableFutureTask(Callable<V> callable,
			@Nullable Object identifier) {
		super(callable);
		this.identifier = identifier;
	}

	public ListenableFutureTask(final V result) {
		this(result, null);
	}

	private ListenableFutureTask(final V result, @Nullable Object identifier) {
		super(new Callable<V>() {
			@Override
			public V call() throws Exception {
				return result;
			}
		});
		this.identifier = identifier;
		this.run();
	}

	public synchronized void addListener(FutureTaskListener<V> listener) {
		if (this.isDone()) {
			callback(listener);
		} else {
			this.listeners.add(listener);
		}
	}

	public synchronized void removeListener(FutureTaskListener<V> listener) {
		this.listeners.remove(listener);
	}

	@Override
	protected synchronized void done() {
		callback();
	}

	private void callback() {
		for (FutureTaskListener<V> listener : listeners) {
			callback(listener);
		}
	}

	private void callback(FutureTaskListener<V> listener) {
		if (listener != null) {
			try {
				listener.onSuccess(get());
			} catch (InterruptedException e) {
				throw new AssertionError(e);
			} catch (ExecutionException e) {
				listener.onFailure(e);
			}
		}
	}

	@Override
	public boolean equals(Object other) {
		if (other != null && other instanceof ListenableFutureTask &&
				this.identifier != null) {
			return identifier.equals(other);
		} else {
			return super.equals(other);
		}
	}

	@Override
	public int hashCode() {
		if (identifier != null) return identifier.hashCode();
		else return super.hashCode();
	}

}
