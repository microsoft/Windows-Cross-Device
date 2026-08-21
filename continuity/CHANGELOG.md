# Continuity SDK changelog

## Unreleased

### Changed

- App-context lifetime now defaults to five minutes and is capped at five minutes. When
  `AppContextManager.sendAppContext` is called, a missing, non-positive, or greater-than-five-minute
  `lifeTime` is normalized to `300000` milliseconds. Positive values below five minutes are
  preserved.

### Consumer impact

- Apps that previously relied on the 30-day default, a lifetime greater than five minutes, or a
  non-expiring value must now refresh `lastUpdatedTime` and resend the context before the
  five-minute window expires.
