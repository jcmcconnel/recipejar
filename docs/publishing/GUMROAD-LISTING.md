# RecipeJar — Gumroad listing draft

Paste the sections below into Gumroad. Price is the planned desktop price. Do not list a mobile app here.

**Price:** $9.99 USD (one-time)

**Name on the listing:** RecipeJar (one word). Do not use recipejar.app. That domain is a different product.

---

## Title

RecipeJar

## Subtitle

Keeps your family’s recipes alive — offline, yours forever.

## Short description (Gumroad “tagline” / product summary)

A local, offline recipe organizer for Mac, Windows, and Linux. Your cookbook is a folder of HTML files you own, readable in any browser.

---

## Description

RecipeJar is a desktop app for the recipes you actually cook — the handwritten cards, the “ask Grandma” list, the one casserole everyone expects at Thanksgiving.

You keep them as ordinary HTML files in a folder on your computer. RecipeJar gives you an A–Z index, a reader, search, and a structured editor. When you save, it writes real HTML back to that folder. Open the same file in any browser if you want. Copy the folder to a USB stick or an iCloud Drive directory and it is still just files.

There is no RecipeJar account. No backend. No analytics. No ads. The app does not upload your cookbook.

This is the complete desktop v1 (macOS, Windows, Linux). Phone and tablet builds are prototypes only and are not for sale here.

**Not the same as:** the App Store app “Recipe Jar: Save & Cook” (Othman Shahrouri), or the web app at recipejar.app. Those are other people’s products. This RecipeJar is a local desktop organizer by James McConnel.

### What you do with it

1. Install the build for your operating system.
2. Open a recipe folder (or start a new one).
3. Browse A–Z, read, search, and edit.
4. Leave the files where you put them. They stay readable if you never open RecipeJar again.

### Why HTML

If the app goes away, the recipes do not. A grandchild can open `index.html` or a single recipe in a browser. That is the point.

---

## What’s included

- Packaged desktop app for **macOS (DMG)**, **Windows (MSI)**, and **Linux (Deb)** — download the file for your OS
- Bundled runtime in the packaged app (you do not install Java or Gradle)
- Sample workflow: point the app at your own folder and start saving recipes as HTML
- MIT-licensed software; you are paying for the packaged desktop build and support from the author
- Support by email: jcmcconnel@gmail.com

You are buying the desktop program. You are not buying cloud storage, a mobile app, or the recipejar.app website.

---

## Requirements

**Operating systems**

- macOS (Intel or Apple Silicon)
- Windows
- Linux (Debian/Ubuntu-style install via the `.deb`, or the packaged app image if offered)

**Java / JVM**

- Packaged Gumroad builds include a bundled runtime. End users do **not** need `JAVA_HOME`, a JDK, or Gradle.
- Developers building from source need JDK 17+ (required by the recipe WebView). That is not the Gumroad path.

**Disk and first launch**

- First launch may need the **internet** once. The reader downloads an embedded Chromium component into a local cache (`~/.cache/recipejar/` on Linux; the equivalent user cache on Mac and Windows). After that, the app works offline.
- If that download has not finished, you can still see HTML source or the welcome text. Restart after it finishes if the page view is blank.

**What you provide**

- A folder you own for the recipe files (home directory, external drive, or a synced folder such as iCloud Drive)

**Not required**

- An account
- An always-on network
- A phone

**macOS note**

Until the Mac build is notarized with Apple, Gatekeeper may block the first open. See `MAC-INSTALL.md` (or the install note shipped with the DMG): try to open the app once, then **System Settings → Privacy & Security → Open Anyway**. After notarization, a normal drag-to-Applications install is enough.

---

## FAQ

**Where do my recipes go?**  
Into the folder you open. They are HTML files on your disk. RecipeJar does not host them.

**Do I need the internet?**  
Only on first launch, so the recipe viewer can install its local Chromium embed. After that, no.

**Do I need Java?**  
Not for the packaged app. The installer ships its own runtime.

**Is there an iPhone or Android app?**  
Not for sale. Mobile is a prototype. This listing is desktop only.

**Can I sync to another computer?**  
Yes — sync the folder, not an account. iCloud Drive, Syncthing, Dropbox, a USB drive, whatever you already trust. RecipeJar just reads and writes the files.

**What if I stop using RecipeJar?**  
Keep the folder. Open the HTML in any browser.

**Is this recipejar.app?**  
No. recipejar.app is a different web app. We do not claim that domain.

**Is this “Recipe Jar: Save & Cook” on the App Store?**  
No. That app is by Othman Shahrouri. Ours is RecipeJar, one word, desktop, local files.

**What is the license?**  
MIT. Copyright James McConnel.

**How do I get help?**  
Email jcmcconnel@gmail.com. Say which OS and whether you used the DMG, MSI, or Deb.

**Mac says the app can’t be opened.**  
That is Gatekeeper, usually because the DMG is not notarized yet (or you have not approved this copy). Follow `MAC-INSTALL.md`. Do not turn Gatekeeper off for the whole Mac.

---

## Gumroad fields (checklist)

| Field | Value |
|-------|--------|
| Product name | RecipeJar |
| Price | $9.99 |
| Type | Digital download (one-time) |
| Versions / files | Attach the current Mac DMG, Windows MSI, and Linux Deb (label each file by OS) |
| Support email | jcmcconnel@gmail.com |
| Refunds | Your call; recommended: refund if the app never launched, after a quick OS/install check |
| Discover / categories | Productivity, or Food & Cooking if Gumroad offers it |
| Call out | Offline, no account, HTML files you own |

Do not promise App Store or Play Store builds on this product. Those, if they ship later, are planned around $6.99 and are a different listing.
