package com.tencent.dolphin.svrcore.client;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.NotImplementedException;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Singleton
public class InaccurateTimeoutManager implements TimeoutManager, Closeable {
	final HashedWheelTimer timer;
	
	public InaccurateTimeoutManager(int tickms) {
		timer = new HashedWheelTimer(tickms, TimeUnit.MILLISECONDS);
		timer.start();
	}
	
	@Inject
	public InaccurateTimeoutManager(Configuration configs) {
		this(configs.getInt("server.client.timeout_manager.tickms", 50));
	}

	@Override
	public Future<?> watch(final Runnable task, long timeout) {
		FutureAdapter adapter = new FutureAdapter(task);
		adapter.wrap = timer.newTimeout(adapter, timeout, TimeUnit.MILLISECONDS);
		return adapter;
	}

	@Override
	public void close() throws IOException {
		timer.stop();
	}
	
	static class FutureAdapter<T> implements Future<T>, TimerTask{
		Timeout wrap;
		final Runnable task;
		
		FutureAdapter(Runnable task){
			this.task = task;
		}

		@Override
		public boolean cancel(boolean mayInterruptIfRunning) {
			return wrap.cancel();
		}

		@Override
		public boolean isCancelled() {
			return wrap.isCancelled();
		}

		@Override
		public boolean isDone() {
			return wrap.isCancelled();
		}

		@Override
		public T get() throws InterruptedException, ExecutionException {
			throw new NotImplementedException();
		}

		@Override
		public T get(long timeout, TimeUnit unit) throws InterruptedException,
				ExecutionException, TimeoutException {
			throw new NotImplementedException();
		}

		@Override
		public void run(Timeout timeout) throws Exception {
			task.run();
		}
		
	}
}
