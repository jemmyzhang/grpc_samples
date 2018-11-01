package pers.jz.grpc.pool.clientpool;

/**
 * @author Jemmy Zhang on 2018/11/1.
 * ref: https://www.jianshu.com/p/267078010c68
 */
public interface WorkCallBack<T> {
    void callback(T t);
}
