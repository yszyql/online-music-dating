package org.x.backend.utils;

/**
 * 线程工具类
 * 让不同用户在不同线程内使用他们各自的ID，通过ThreadLocal实现线程间数据的隔离。
 */
public class ThreadLocalUtil {

    // 定义ThreadLocal对象，用于存储每个线程的特定数据
    // 注意：此处使用原始类型，建议指定泛型类型以提高类型安全性
    public static final  ThreadLocal THREAD_LOCAL = new ThreadLocal();

    /**
     * 获取ThreadLocal中的数据
     * @param <T> 返回值类型，由调用者指定
     * @return 返回存储在ThreadLocal中的数据，若未存储则返回null
     */
    public static <T> T get(){
        // 从ThreadLocal中获取数据并强制转换为指定类型
        return (T) THREAD_LOCAL.get();
    }

    /**
     * 设置ThreadLocal中的数据
     * @param value 要存储在ThreadLocal中的值
     */
    public static void set(Object value){
        // 将指定的值存储到ThreadLocal中
        THREAD_LOCAL.set(value);
    }

    /**
     * 移除ThreadLocal中的数据
     * 调用此方法可防止内存泄漏，特别是在线程池环境中
     */
    public static void remove(){
        // 移除当前线程存储在ThreadLocal中的数据
        THREAD_LOCAL.remove();
    }

}