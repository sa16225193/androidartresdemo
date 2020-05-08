package com.example.chapter_2.binderpool;

import android.os.IBinder;

import static com.example.chapter_2.binderpool.BinderPool.BINDER_COMPUTE;
import static com.example.chapter_2.binderpool.BinderPool.BINDER_SECURITY_CENTER;

/**
 * Created by liuyong on 2020-05-08
 */
public class BinderPoolImpl extends IBinderPool.Stub {

    public BinderPoolImpl() {
        super();
    }

    @Override
    public IBinder queryBinder(int binderCode) {
        IBinder binder = null;
        switch (binderCode) {
            case BINDER_SECURITY_CENTER: {
                binder = new SecurityCenterImpl();
                break;
            }
            case BINDER_COMPUTE: {
                binder = new ComputeImpl();
                break;
            }
            default:
                break;
        }

        return binder;
    }
}
