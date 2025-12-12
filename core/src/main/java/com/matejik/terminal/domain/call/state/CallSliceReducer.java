package com.matejik.terminal.domain.call.state;

import java.util.List;

public final class CallSliceReducer {

  private CallSliceReducer() {}

  public static CallSlice reduce(CallSlice slice, CallAction action) {
    return switch (action) {
      case CallAction.ReplaceActiveCalls replace -> replace(slice, replace.calls());
      case CallAction.UpsertCall upsert -> CallSlice.upsert(slice, upsert.call());
      case CallAction.RemoveCall remove -> CallSlice.remove(slice, remove.callId());
      case CallAction.SelectCall select -> select(slice, select.callId());
      case CallAction.ClearSelection ignore -> slice.withSelectedCallId(null);
    };
  }

  private static CallSlice replace(CallSlice slice, List<CallSlice.CallView> calls) {
    return CallSlice.replaceAll(slice, calls);
  }

  private static CallSlice select(CallSlice slice, String callId) {
    if (callId == null) {
      return slice;
    }
    if (!slice.hasCall(callId)) {
      return slice;
    }
    return slice.withSelectedCallId(callId);
  }
}
