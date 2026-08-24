# Continuity SDK changelog

## Unreleased

### Changed

- Cross Device Resume app-context lifetime now defaults to five minutes and is capped at five
  minutes. When `AppContextManager.sendAppContext` is called for
  `ProtocolConstants.TYPE_RESUME_ACTIVITY`, a missing or greater-than-five-minute `lifeTime` is
  normalized to `300000` milliseconds. Explicit non-positive values reject the send through
  `IAppContextResponse.onContextResponseError`. Positive values below five minutes are preserved.
  Other app-context types retain their existing lifetime behavior.

### Consumer impact

- Cross Device Resume apps that previously relied on the 30-day default, a lifetime greater than
  five minutes, or a non-expiring value must now use a positive lifetime, refresh
  `lastUpdatedTime`, and resend the context before the five-minute window expires.
