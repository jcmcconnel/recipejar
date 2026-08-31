# RecipeJar — Mac install

For the desktop DMG. Packaged builds include their own runtime; you do not install Java.

Until Apple Developer enrollment and notarization are done, macOS will treat the app as an unidentified download. That is Gatekeeper doing its job. Use the **un-notarized** section below. After the DMG is notarized and stapled, use the short **notarized** section.

Do not turn off Gatekeeper for the whole Mac. Approve this one app.

---

## Install (both cases)

1. Open the `RecipeJar-*.dmg` you downloaded.
2. Drag **RecipeJar** into **Applications** (or another folder you prefer).
3. Eject the disk image.
4. Open RecipeJar from Applications — not from inside the DMG.

First launch may take a while. The recipe reader may download its Chromium embed into a local cache. That needs the internet **once**. After that, the app works offline.

Then: **Open repository** and choose the folder where you want the HTML files.

---

## If the DMG is notarized

Apple has checked this build. A first-open dialog may still say the app was downloaded from the internet.

1. Click **Open**.
2. If macOS asks again, confirm **Open**.
3. You should not need **Open Anyway**.

If you still get a block, the file may be damaged, or you may have an older un-notarized copy. Re-download, or follow the un-notarized steps once.

---

## If the DMG is not notarized yet (interim)

macOS Sequoia 15, Tahoe 26, and current shipping macOS hide the override in System Settings. The old Control-click → Open confirmation is unreliable on these versions.

### Open Anyway (current macOS)

The **Open Anyway** button appears only after macOS has blocked the app once, and only for about an hour.

1. Double-click RecipeJar in Applications. When you see that it cannot be opened / Apple could not verify it, dismiss the dialog. Do **not** choose Move to Trash.
2. Open **Apple menu → System Settings → Privacy & Security**.
3. Scroll to **Security**.
4. You should see that RecipeJar was blocked. Click **Open Anyway**.
5. Authenticate (password or Touch ID).
6. Confirm **Open Anyway** / **Open** on the follow-up dialog.

After that, this copy is an exception. Later launches are a normal double-click.

If you do not see **Open Anyway**, open the app once more, dismiss the warning, and return to Privacy & Security immediately.

### Older macOS (Sonoma 14 and earlier)

If Control-click still offers **Open**:

1. Control-click (or right-click) RecipeJar in Applications.
2. Choose **Open**.
3. Confirm **Open** in the dialog.

You can still use System Settings → Privacy & Security → Open Anyway if that path is there.

---

## What the warnings mean

| You see | What it is | What to do |
|---------|------------|------------|
| “Apple could not verify RecipeJar” / “cannot be opened” | Gatekeeper; unsigned or not-yet-notarized build | Open Anyway, above |
| “Downloaded from the internet” on a notarized build | Quarantine on a first open | Click Open |
| Blank recipe page, welcome text, or raw HTML | Viewer cache not finished | Stay online on first run; quit and reopen when the status banner finishes |
| App never appears in Privacy & Security | You have not triggered a block yet, or the hour expired | Double-click the app once, then go back |

RecipeJar does not need you to allow “Apps from anywhere” system-wide.

---

## After it opens

- **Tools → Preferences…** — set the recipe folder and optional author name.
- Recipes are HTML in that folder, not in iCloud unless you put the folder there yourself.
- Help: [jcmcconnel@gmail.com](mailto:jcmcconnel@gmail.com). Mention “Mac install” and whether you used Open Anyway.

This install guide is for RecipeJar by James McConnel (desktop). It is not for “Recipe Jar: Save & Cook” on the App Store, and not for recipejar.app.
