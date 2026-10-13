# Development workflow

The build is only verified by GitHub Actions (there is no local Android SDK in
every environment), so **never push UI changes straight to `main` unchecked**.

1. Work on a branch: `git checkout -b my-change`
2. Push it and open a pull request to `main`. The `Build APK` workflow runs on PRs.
3. Merge only when the run is green. On failure, open the run: the
   "Gradle build errors" annotation lists the exact `e: file:...` compiler lines.
4. After merging to `main`, CI commits a fresh `app-debug.apk`.

## Common failure causes (seen in this repo)

- **Unresolved reference to an extension/infix function** (`togetherWith`,
  `roundToInt`, `rotate`, `animateContentSize`, ...). Fully-qualified names do
  *not* make an infix operator work: add the `import` line.
- Do not bump Gradle past 9.3.1 (Compose compiler plugin breaks).

## Mixture Calculator layout (ui/screens/MixtureCalculatorScreen.kt)

UI only: state lives in `MixtureCalculatorViewModel`, math in `domain/mixture/*`.
Layout changes should not touch those. Keep the `calc_*` test tags stable.
Steps: 1 Formula, 2 Re-balance, 3 Report; each has a pinned `StepSummaryBar`
and a `StepFooter` (Back / Next). Rarely used tools go in `CollapsibleCard`.
