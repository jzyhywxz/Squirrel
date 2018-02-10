package com.zzw.squirrel.daemon;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

/**
 * Created by zzw on 2018/2/1.
 */

public interface IDaemonInterface extends IInterface {
    public String getServerName() throws RemoteException;
    public void stopServer() throws RemoteException;
    public boolean isServerStopped() throws RemoteException;

    /**
     * Local-side IPC implementation stub class.
     */
    public static abstract class Stub extends Binder implements IDaemonInterface {
        static final int TRANSACTION_getServerName = (IBinder.FIRST_CALL_TRANSACTION + 0);
        static final int TRANSACTION_stopServer = (IBinder.FIRST_CALL_TRANSACTION + 1);
        static final int TRANSACTION_isServerStopped = (IBinder.FIRST_CALL_TRANSACTION + 2);
        private static final String DESCRIPTOR = "com.zzw.squirrel.util.IDaemonInterface";

        /**
         * Construct the stub at attach it to the interface.
         */
        public Stub() {
            this.attachInterface(this, DESCRIPTOR);
        }

        /**
         * Cast an IBinder object into an com.zzw.daemon.IDaemonInterface interface,
         * generating a proxy if needed.
         */
        public static IDaemonInterface asInterface(IBinder obj) {
            if ((obj == null)) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (((iin != null) && (iin instanceof IDaemonInterface))) {
                return ((IDaemonInterface) iin);
            }
            return new IDaemonInterface.Stub.Proxy(obj);
        }

        @Override
        public IBinder asBinder() {
            return this;
        }

        @Override
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case INTERFACE_TRANSACTION: {
                    reply.writeString(DESCRIPTOR);
                    return true;
                }
                case TRANSACTION_getServerName: {
                    data.enforceInterface(DESCRIPTOR);
                    String _result = this.getServerName();
                    reply.writeNoException();
                    reply.writeString(_result);
                    return true;
                }
                case TRANSACTION_stopServer: {
                    data.enforceInterface(DESCRIPTOR);
                    this.stopServer();
                    reply.writeNoException();
                    return true;
                }
                case TRANSACTION_isServerStopped: {
                    data.enforceInterface(DESCRIPTOR);
                    boolean _result = this.isServerStopped();
                    reply.writeNoException();
                    reply.writeInt((_result ? 1 : 0));
                    return true;
                }
            }
            return super.onTransact(code, data, reply, flags);
        }

        private static class Proxy implements IDaemonInterface {
            private IBinder mRemote;

            Proxy(IBinder remote) {
                mRemote = remote;
            }

            @Override
            public IBinder asBinder() {
                return mRemote;
            }

            public String getInterfaceDescriptor() {
                return DESCRIPTOR;
            }

            @Override
            public String getServerName() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                String _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    mRemote.transact(Stub.TRANSACTION_getServerName, _data, _reply, 0);
                    _reply.readException();
                    _result = _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }

            @Override
            public void stopServer() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    mRemote.transact(Stub.TRANSACTION_stopServer, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override
            public boolean isServerStopped() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                boolean _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    mRemote.transact(Stub.TRANSACTION_isServerStopped, _data, _reply, 0);
                    _reply.readException();
                    _result = (0 != _reply.readInt());
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }
        }
    }
}
