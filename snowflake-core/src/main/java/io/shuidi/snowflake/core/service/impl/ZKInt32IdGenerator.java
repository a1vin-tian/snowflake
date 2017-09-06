package io.shuidi.snowflake.core.service.impl;

import com.google.common.base.Preconditions;
import io.shuidi.snowflake.core.service.IDGenerator;
import io.shuidi.snowflake.core.util.sequence.RangeSequence;
import io.shuidi.snowflake.core.util.sequence.RangeStore;
import io.shuidi.snowflake.core.util.sequence.ZkRangeStore;
import org.apache.curator.framework.CuratorFramework;

import java.util.concurrent.TimeUnit;

/**
 * Author: Alvin Tian
 * Date: 2017/9/3 10:59
 */
public class ZKInt32IdGenerator implements IDGenerator<Integer> {

	private final CuratorFramework client;

	private RangeSequence rangeSequence;

	public ZKInt32IdGenerator(CuratorFramework client, String lockPath, String sequencePath, String name, long start, int rangeCount) {
		Preconditions.checkNotNull(client);
		Preconditions.checkNotNull(lockPath);
		Preconditions.checkNotNull(sequencePath);
		Preconditions.checkNotNull(name);
		this.client = client;
		client.start();
		RangeStore rangeStore = new ZkRangeStore(name, client, lockPath, sequencePath, 1, TimeUnit.SECONDS, start, rangeCount);
		try {
			start = rangeStore.getNextRange();
		} catch (InterruptedException e) {
			throw new IllegalStateException("ZKInt32IdGenerator 初始值获取失败!!!");
		}
		rangeSequence = new RangeSequence(1, start, rangeCount, rangeStore);
		rangeSequence.start();
	}

	@Override
	public Integer generateId() {
		return (int) rangeSequence.incrementAndGet();
	}

	public void stopRangeSeq(){
		rangeSequence.stop();
	}
}
