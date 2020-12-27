# Palantir

Palantir is an Android application for the visualisation of protein structure in
augmented reality.

<div align="center">
  <img src="docs/preview.gif">
</div>

You can download it [here from the Google Play store](
https://play.google.com/store/apps/details?id=io.ningyuan.palantir)

## Development

These are reminders to myself.

- When making a new commit after a commit tagged as a release, increment the `build.gradle` `versionCode` by 1, and append the `versionName` with `-SNAPSHOT`.
  Also update the version name in `strings.xml`.
- When making a new commit to be tagged as a release, update the two version names as mentioned above.
