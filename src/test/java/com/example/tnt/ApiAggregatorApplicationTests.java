package com.example.tnt;

import org.junit.jupiter.api.Test;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

class ApiAggregatorApplicationTests {
	private final AtomicBoolean found = new AtomicBoolean(false);
	@Test
	void contextLoads() throws ExecutionException, InterruptedException {
		ScheduledExecutorService scheduler = Executors
				.newScheduledThreadPool(1);
		ScheduledFuture<String> future = scheduler.schedule(
				()->{
					found.set(false);
					System.out.println("beep " + found.get());


//					if (count.get()) {
//						scheduler.shutdown();
//					}
					return "finished";
				}, 2, TimeUnit.SECONDS);
		System.out.println(future.get());
	}
}
