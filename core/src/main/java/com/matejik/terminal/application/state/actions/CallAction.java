package com.matejik.terminal.application.state.actions;

import com.matejik.terminal.application.state.CallSlice.CallView;
import java.util.List;
import java.util.Objects;

public sealed interface CallAction extends AppAction
    permits CallAction.ReplaceActiveCalls,
        CallAction.UpsertCall,
        CallAction.RemoveCall,
        CallAction.SelectCall,
        CallAction.ClearSelection {

  record ReplaceActiveCalls(List<CallView> calls) implements CallAction {

    public ReplaceActiveCalls {
      Objects.requireNonNull(calls, "calls");
    }
  }

  record UpsertCall(CallView call) implements CallAction {

    public UpsertCall {
      Objects.requireNonNull(call, "call");
    }
  }

  record RemoveCall(String callId) implements CallAction {

    public RemoveCall {
      Objects.requireNonNull(callId, "callId");
    }
  }

  record SelectCall(String callId) implements CallAction {

    public SelectCall {
      Objects.requireNonNull(callId, "callId");
    }
  }

  record ClearSelection() implements CallAction {}
}
