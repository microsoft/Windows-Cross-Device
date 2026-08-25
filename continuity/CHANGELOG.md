# Continuity SDK changelog

## Unreleased

### Changed

- App-context lifetime now defaults to five minutes and is capped at five minutes except for
  Browser History. The SDK omits `lifeTime` from Browser History payloads so the receiver controls
  retention. When `AppContextManager.sendAppContext` is called for another context type, a missing
  `lifeTime` defaults to `300000` milliseconds, a greater value is capped at `300000`, and an
  explicit non-positive value rejects the send through
  `IAppContextResponse.onContextResponseError`.

### Consumer impact

- Apps sending non-Browser-History contexts that previously relied on the 30-day default, a
  lifetime greater than five minutes, or a non-expiring value must now use a positive lifetime,
  refresh `lastUpdatedTime`, and resend the context before the five-minute window expires. Browser
  History callers do not need to provide a lifetime.
