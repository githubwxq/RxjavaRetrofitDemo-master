package com.queen.rxjavaretrofitdemo.subscribers;

import android.content.Context;
import android.widget.Toast;

import com.queen.rxjavaretrofitdemo.progress.ProgressCancelListener;
import com.queen.rxjavaretrofitdemo.progress.ProgressDialogHandler;

import java.net.ConnectException;
import java.net.SocketTimeoutException;

import rx.Subscriber;
  //回调上层只关心的数据 下沉做一系列的处理， base抽象处理一般情况 继承类处理特殊情况
/**    这个subscribers抽象类实现了接口所以能拿到 被订阅者的数据！！
 * 用于在Http请求开始时，自动显示一个ProgressDialog
 * 在Http请求结束是，关闭ProgressDialog
 * 调用者自己对请求数据进行处理
 * Created by liukun on 16/3/10.
 * <p/>
 * ！！！！对subscroibe的多功能封装 不再是 填充匿名内部类  处理onnext 等等方法 该如何处理而已 多天了接口
 * <p/>
 * 意味着 在某种状态下他会被实现！！！ 往往实现 是为了处理当类中的某些对象的方法调用解除耦合！！！
 * 像mainactivity实现mSubscriberOnNextListener 借口那么 Subscriber call方法调用时mainactivity方法就被调用了
 *
 * 还是通过接口回调的方法 传递数据  注册接口实现类方提供数据 实现接口类方拿到数据或响应 从而实现通信
 */
public class ProgressSubscriber<T> extends Subscriber<T> implements ProgressCancelListener {

    private SubscriberOnNextListener mSubscriberOnNextListener;
    private ProgressDialogHandler mProgressDialogHandler;

    private Context context;

    public ProgressSubscriber(SubscriberOnNextListener mSubscriberOnNextListener, Context context) {
        this.mSubscriberOnNextListener = mSubscriberOnNextListener;
        this.context = context;
        mProgressDialogHandler = new ProgressDialogHandler(context, this, true);
    }

    private void showProgressDialog() {
        if (mProgressDialogHandler != null) {
            mProgressDialogHandler.obtainMessage(ProgressDialogHandler.SHOW_PROGRESS_DIALOG).sendToTarget();
        }
    }

    private void dismissProgressDialog() {
        if (mProgressDialogHandler != null) {
            mProgressDialogHandler.obtainMessage(ProgressDialogHandler.DISMISS_PROGRESS_DIALOG).sendToTarget();
            mProgressDialogHandler = null;
        }
    }

    /**
     * 订阅开始时调用
     * 显示ProgressDialog
     */
    @Override
    public void onStart() {
        showProgressDialog();
    }

    /**
     * 完成，隐藏ProgressDialog
     */
    @Override
    public void onCompleted() {
        dismissProgressDialog();
        Toast.makeText(context, "Get Top Movie Completed", Toast.LENGTH_SHORT).show();
    }

    /**
     * 对错误进行统一处理
     * 隐藏ProgressDialog
     *
     * @param e
     */
    @Override
    public void onError(Throwable e) {
        if (e instanceof SocketTimeoutException) {
            Toast.makeText(context, "网络中断，请检查您的网络状态", Toast.LENGTH_SHORT).show();
        } else if (e instanceof ConnectException) {
            Toast.makeText(context, "网络中断，请检查您的网络状态", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "error:" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        dismissProgressDialog();

    }

    /**
     * 将onNext方法中的返回结果交给Activity或Fragment自己处理
     *
     * @param t 创建Subscriber时的泛型类型
     *          <p/>
     *          对onnext回调进行了再次回调
     *          订阅时执行onnext   onnext执行时用执行 onnext 在activity中被用到实现activity就只要实现onnext借口了
     *          <p/>
     *          最后activity是通过实现借口拿到数据的！！！！！！！
     */
    @Override
    public void onNext(T t) {

        if (mSubscriberOnNextListener != null) {
            mSubscriberOnNextListener.onNext(t);//给那个类实现借口那个类就能获取另一个类中的东西另一个类中的东西会以参数的形式传到实现借口的类中
        }

    }

    /**
     * 取消ProgressDialog的时候，取消对observable的订阅，同时也取消了http请求
     */
    @Override
    public void onCancelProgress() {
        if (!this.isUnsubscribed()) {
            this.unsubscribe();
        }
    }
}