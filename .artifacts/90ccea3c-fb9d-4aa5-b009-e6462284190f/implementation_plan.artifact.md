# Implementation Plan - Reverting Qibla Offset & Syncing Logic

This plan restores the original visual offset for the Qiblah pointer and synchronizes the "Facing Qibla" logic to trigger exactly when the pointer is at the top of the screen. It also improves location filtering to handle invalid near-zero coordinates.

## User Review Required

> [!IMPORTANT]
> - **Restore Offset**: We will restore the `+ 103f` offset to the `pointerRotation` in `QiblahScreen.kt`. The user confirmed this was correct before.
> - **Sync Instruction Logic**: The "You are facing the Qibla!" message will now be triggered when the pointer is visually at the top (vertical), which means `(qiblahAngle - deviceBearing + 103)` is near 0.
> - **Aggressive Location Filter**: We will use a more robust check to ignore the `6.5e-317` placeholder coordinates that are still appearing on iOS.

## Proposed Changes

### [Common] [Qiblah]

#### [MODIFY] [QiblahScreen.kt](file:///Users/moehammadkhoyron/DriverApps/Bilal/composeApp/src/commonMain/kotlin/org/khoyron/bilal/ui/qiblah/QiblahScreen.kt)
- Restore `+ 103f` to `pointerRotation` and `rotationZ` for the pointer image.

#### [MODIFY] [QiblahViewModel.kt](file:///Users/moehammadkhoyron/DriverApps/Bilal/composeApp/src/commonMain/kotlin/org/khoyron/bilal/ui/qiblah/QiblahViewModel.kt)
- Update `handleLocation` with a stronger filter: `if (lat == 0.0 || lon == 0.0 || abs(lat) < 0.001) return`.
- Update `updateInstruction` to include the `103f` offset in its "Facing" calculation so the message matches the visual pointer.

## Verification Plan

### Automated Tests
- Build iOS and Android targets.

### Manual Verification
- Test on an iOS device:
    - Verify that the location is no longer `0.0, 0.0` or `6.5e-317`.
    - Verify that "You are facing the Qibla!" only appears when the Kaaba icon is strictly at the top of the screen.
    - Verify that the Qiblah pointer direction is "correct" again as it was before.
