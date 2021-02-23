package com.atguigu.gmall.index.service;



import org.apache.commons.lang.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;


import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class TestService {

    @Autowired
    private StringRedisTemplate redisTemplate;


    @Autowired
    private RedissonClient redissonClient;

    public void testRedisson() {

        //拿到锁
        RLock lock = redissonClient.getLock("lock2");
        //获取锁
        lock.lock();

        String value = redisTemplate.opsForValue().get("num");

        int num = Integer.parseInt(value);

        redisTemplate.opsForValue().set("num", String.valueOf(++num));

        System.out.println(num);

        //释放锁
        lock.unlock();

    }

    public void testRedisson2(){
        RLock lock = this.redissonClient.getLock("lock3"); // 只要锁的名称相同就是同一把锁
        lock.lock(); // 加锁

        // 查询redis中的num值
        String value = this.redisTemplate.opsForValue().get("num");
        // 没有该值return
        if (StringUtils.isBlank(value)) {
            return;
        }
        // 有值就转成成int
        int num = Integer.parseInt(value);
        // 把redis中的num值+1
        this.redisTemplate.opsForValue().set("num", String.valueOf(++num));

        lock.unlock(); // 解锁
    }



    public synchronized void testLock() {

        String value = redisTemplate.opsForValue().get("lock");

        if (StringUtils.isBlank(value)) {
            return;
        }

        int num = Integer.valueOf(value);
        redisTemplate.opsForValue().set("lock", String.valueOf(++num));

    }

    public void testLock2() {
        Boolean lock = redisTemplate.opsForValue().setIfAbsent("lock", "1111");

        if (lock) {

            String value = redisTemplate.opsForValue().get("num");

            if (StringUtils.isBlank(value)) {
                return;
            }
            Integer num = Integer.valueOf(value);

            redisTemplate.opsForValue().set("num", String.valueOf(++num));

            //释放锁
            redisTemplate.delete("num");
        } else {
            try {
                Thread.sleep(1000);
                testLock2();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    public void testLock3() {

        String uuid = UUID.randomUUID().toString();
        //设置过期时间，防止进入循环时，出现异常，导致锁无法释放
        Boolean lock = redisTemplate.opsForValue().setIfAbsent("lock", uuid, 20, TimeUnit.SECONDS);
        if (lock) {

            String value = redisTemplate.opsForValue().get("num");

            if (StringUtils.isBlank(value)) {
                return;
            }
            Integer num = Integer.valueOf(value);
            redisTemplate.opsForValue().set("num", String.valueOf(++num));

            // 因为业务还没执行完，锁过期了，如果直接删除，会删别的锁  通过uuid来判断，防止删除了别的锁

            if (StringUtils.equals(redisTemplate.opsForValue().get("lock"), uuid)) {

                redisTemplate.delete("lock");
            }
        } else {
            try {
                Thread.sleep(1000);
                testLock3();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    public void testLock4() {

        String uuid = UUID.randomUUID().toString();
        //设置过期时间，防止进入循环时，出现异常，导致锁无法释放
        Boolean lock = redisTemplate.opsForValue().setIfAbsent("lock", uuid, 20, TimeUnit.SECONDS);
        if (lock) {

            String value = redisTemplate.opsForValue().get("num");

            if (StringUtils.isBlank(value)) {
                return;
            }
            Integer num = Integer.valueOf(value);
            redisTemplate.opsForValue().set("num", String.valueOf(++num));

            // 因为业务还没执行完，锁过期了，如果直接删除，会删别的锁  通过uuid来判断，防止删除了别的锁
            //上面通过if判断 然后删除 操作不具备原子性
            //所以采用lua脚本

            String script = "if(redis.call('get',KEYS[1]) == ARGV[1]) then return redis.call('del',KEYS[1])  else return 0 end";
            redisTemplate.execute(new DefaultRedisScript<>(script, Boolean.class), Arrays.asList("lock"),uuid);

        } else {
            try {
                Thread.sleep(1000);
                testLock4();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    public void testLock5() {

        boolean lock = addLock("lock", "1234567", 30L);
        if (lock) {
            String value = redisTemplate.opsForValue().get("k1");

            if (value == null) {
                return;
            }
            Integer num = Integer.valueOf(value);

            redisTemplate.opsForValue().set("k1", String.valueOf(++num));

            testSubLock("1234567");

            unLock("lock", "1234567");
        }

    }

    public void testSubLock(String uuid) {

        boolean lock = addLock("lock", uuid, 30L);

        if (lock) {
            System.out.println("获取到了锁...");
            unLock("lock", uuid);
        }
    }


    private boolean addLock(String lockName, String uuid, Long ttlTime) {

        //加可重入锁，加锁的同时还要设置时间
        String script = "if(redis.call('exists',KEYS[1]) == 0 or redis.call('hexists',KEYS[1],ARGV[1])) " +
                "then" +
                " redis.call('hincrby',KEYS[1],ARGV[1],1);redis.call('expire',KEYS[1],ARGV[2])" +
                "return 1" +
                " else" +
                " return 0" +
                " end";

        Boolean execute = redisTemplate.execute(new DefaultRedisScript<>(script, Boolean.class), Arrays.asList(lockName), uuid,ttlTime);

        //说明锁被别人占用着
        if (!execute) {
            try {
                Thread.sleep(1000);
                addLock(lockName, uuid, ttlTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return execute;
    }

    private void unLock(String lockName, String uuid) {

        //释放可重入锁
        //首先判断是否为自己的锁
        //如果大于0，则减减
        //如果为0.说明可以释放锁


        String script = "if(redis.call('hexists',KEYS[1],ARGV[1])==0) " +
                "then return nil" +
                " elseif(redis.call('hincrby',KEYS[1],ARGV[1],-1) > 0 )" +
                "then return 0 else redis.call('del',KEYS[1]) return 1 end";

        Integer execute = redisTemplate.execute(new DefaultRedisScript<>(script, Integer.class), Arrays.asList(lockName), uuid);

        if (execute == null) {
            throw new IllegalMonitorStateException("试图解锁，但不存在锁 : " + lockName);
        }
    }


}
