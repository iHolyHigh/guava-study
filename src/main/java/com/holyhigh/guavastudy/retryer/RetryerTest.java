package com.holyhigh.guavastudy.retryer;

import com.github.rholder.retry.*;

import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 重试器
 *
 * @author holyhigh
 * @version 0.0.1
 * @since 12/23/21 12:25 AM
 */
public class RetryerTest {
    public static void main(String[] args) throws ExecutionException, RetryException {
        final Retryer<Integer> retryer = RetryerBuilder.<Integer>newBuilder()
                .retryIfResult(i -> i < 10)
                .retryIfException()
                .retryIfRuntimeException()
                .retryIfExceptionOfType(IllegalArgumentException.class)
                .retryIfException(e -> e.getMessage().contains("error"))
                .withWaitStrategy(WaitStrategies.fixedWait(1, TimeUnit.SECONDS))
                .withStopStrategy(StopStrategies.stopAfterAttempt(20))
                // .withStopStrategy(StopStrategies.stopAfterDelay(10, TimeUnit.SECONDS))
                .withRetryListener(new MyRetryListener())
                .build();

        final Integer i = retryer.call(RetryerTest::testRetryer);
        System.out.println("最终返回值 = " + i);
    }

    private static AtomicInteger x = new AtomicInteger(0);

    private static Integer testRetryer() throws Exception {
        System.out.println("当前 x = " + x.addAndGet(1));
        int i = x.get();
        if (i == 10) {
            throw new Exception();
        }
        if (i == 11) {
            throw new RuntimeException();
        }
        if (i == 12) {
            throw new IllegalArgumentException();
        }
        if (i == 13) {
            throw new ArrayIndexOutOfBoundsException("error");
        }
        return i;
    }
}

class MyRetryListener implements RetryListener {

    @Override
    public <V> void onRetry(Attempt<V> attempt) {
        System.out.println(new Date() + " 第" + attempt.getAttemptNumber() + "次重试");
        System.out.println("距离第1次重试已经过了" + attempt.getDelaySinceFirstAttempt() / 1000 + "s");
        boolean result = attempt.hasResult();
        System.out.println("是否有返回值 " + result);
        if (result) {
            System.out.println("返回值 = " + attempt.getResult());
        }
        boolean exception = attempt.hasException();
        System.out.println("是否发生异常 " + exception);
        if (exception) {
            System.out.println("异常原因 = " + attempt.getExceptionCause().toString());
        }
        System.out.println("------------------------------------");
    }
}