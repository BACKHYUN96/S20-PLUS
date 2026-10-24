# v1.8.0 / code 45 — ABYSS reference controls

- Main play orb: 80dp default, stronger cyan edge and separate white reflections. Custom theme play size remains authoritative.
- Previous/next/shuffle/repeat retain transparent native accessible buttons.
- Volume: thin outlined circles, centered group. A–B and lyrics: low outlined capsules within 48dp touch targets.
- Main picker/favorite removed; Library already has file/folder import and track long-press favorite actions.
- Elapsed/total times render at opposite edges. Scrubbing descriptions retain normal TextView rendering and accessibility text.
- Central lower background receives dark water shading; portal rim gains a layered cyan edge.
- Bubbles use faint perimeter, localized reflection and translucent gradient instead of the thick ring sprite.
- Jelly bell contraction amplitude increases, with existing delayed tentacle motion. This is artistic kinematics, not a fluid simulation.

No background assets regenerated in this release. This is a visual refinement toward the reference, not pixel-identical reconstruction. It does not yet introduce independently articulated 3D jellyfish or new rock geometry.

## Device checks
Install over existing app using the same signing identity; do not uninstall/reset.
Check portrait/landscape, large text, A–B expansion, lyric opening, volume, elapsed/total labels while dragging, library import and long-press favorite.
Check play orb/outlined capsules and readable lower controls against the reference. Test animation with music playing and paused. GPU frame rate and physical-device layout are not verified by JVM tests.
