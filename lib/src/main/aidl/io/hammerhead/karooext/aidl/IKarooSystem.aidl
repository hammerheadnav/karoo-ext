package io.hammerhead.karooext.aidl;

import io.hammerhead.karooext.aidl.IHandler;

interface IKarooSystem {
    String libVersion();
    Bundle info();

    oneway void dispatchEffect(in Bundle bundle);

    oneway void addEventConsumer(String id, in Bundle bundle, in IHandler handler);
    oneway void removeEventConsumer(String id);
}