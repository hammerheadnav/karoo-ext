package io.hammerhead.karooext.aidl;

import io.hammerhead.karooext.aidl.IHandler;

interface IKarooExtension {
    String libVersion();

    oneway void startScan(String id, in IHandler handler);
    oneway void stopScan(String id);

    oneway void connectDevice(String id, String uid, in IHandler handler);
    oneway void disconnectDevice(String id);

    oneway void startStream(String id, String typeId, in IHandler handler);
    oneway void stopStream(String id);

    oneway void startView(String id, String typeId, in Bundle configBundle, in IHandler handler);
    oneway void stopView(String id);

    oneway void startMap(String id, in IHandler handler);
    oneway void stopMap(String id);

    oneway void startFit(String id, in IHandler handler);
    oneway void stopFit(String id);
}