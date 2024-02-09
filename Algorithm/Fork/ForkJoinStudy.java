package Algorithm.Fork;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

/**
 * ForkJoin 사용해보기
 * RecursiveTask를 상속하여 정수 배열의 합을 계산
 */
public class ForkJoinStudy extends RecursiveTask<Long> {

    private final int[] array;
    private final int start;
    private final int end;
    private static final int THRESHOLD = 20; // 20 이하는 분할하지 않음.

    public ForkJoinStudy(int[] array, int start, int end) {
        this.array = array;
        this.start = start;
        this.end = end;
    }
    
    @Override
    protected Long compute() {
        
        if (end - start <= THRESHOLD) {
            long sum = 0;
            for (int i = start; i < end; i++) {
                sum += array[i];
            }
            return sum;
        } 

        int mid = start + (end - start) / 2;
        ForkJoinStudy left = new ForkJoinStudy(array, start, mid);
        ForkJoinStudy right = new ForkJoinStudy(array, mid, end);

        // 스레드를 비동기로 실행
        left.fork();
        right.fork();
        
        return left.join() + right.join();
    }

    public static long sequentialSum(int[] array) {
        long sum = 0;

        for (int value : array) {
            sum += value;
        }

        return sum;
    }

    public static void main(String[] args) {
        
        // 초기 값 세팅
        int[] array = new int[1_000_000_000];
        for (int i = 0; i < array.length; i++) {
            array[i] = i++;
        }

        // ForkJoinPool 생성
        ForkJoinPool pool = new ForkJoinPool();

        long startTime = System.currentTimeMillis();

        ForkJoinStudy forkJoinStudy = new ForkJoinStudy(array, 0, array.length);
        long result = pool.invoke(forkJoinStudy);

        long endTime = System.currentTimeMillis();
        long timeElapsed = endTime - startTime;

        System.out.println("result : " + result);
        System.out.println("FrokJoin 실행시간 : " + timeElapsed);

        // 순차적으로 더하기
        startTime = System.currentTimeMillis();

        result = sequentialSum(array);

        endTime = System.currentTimeMillis();
        timeElapsed = endTime - startTime;
        System.out.println("result : " + result);
        System.out.println("Sequential 실행시간 : " + timeElapsed);

    }
}

