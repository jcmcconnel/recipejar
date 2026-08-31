# RecipeJar support

Desktop app only (Mac, Windows, Linux). Mobile is not for sale yet.

**Email:** [jcmcconnel@gmail.com](mailto:jcmcconnel@gmail.com)

Say which OS you are on, which installer you used (DMG / MSI / Deb), and what you expected to happen. If a recipe file is involved, you can attach it — it never leaves your machine unless you send it.

This is not the App Store app “Recipe Jar: Save & Cook,” and it is not recipejar.app.

---

## Where your recipes live

RecipeJar does not keep a copy in the cloud. When you **Open repository** (or set the folder in **Tools → Preferences…**), you are choosing a directory on your computer.

Inside that folder:

- Each recipe is an HTML file
- The app updates `index.html` / category indexes when you save
- You can open any of those files in Safari, Chrome, Firefox, or Edge without RecipeJar

If you cannot find a recipe, look at the folder path in Preferences — that is the only cookbook RecipeJar is using.

Last folder, last recipe, and an optional author name are remembered in local preferences on that computer. They are not an account.

---

## How to sync (iCloud, USB, another PC)

RecipeJar has no sync button. Sync the **folder**.

**USB / AirDrop / a disk**

1. Quit RecipeJar so files are finished writing.
2. Copy the whole recipe folder to the stick or disk.
3. On the other machine, copy it off (or leave it on the drive) and **Open repository** on that copy.

**iCloud Drive (Mac)**

1. Move or create the recipe folder under iCloud Drive (for example `iCloud Drive/Recipes`).
2. Open that same folder in RecipeJar.
3. On another Mac signed into the same Apple ID, wait until Finder shows the files downloaded, then open that folder.

Keep one person editing a given file at a time. iCloud is a file sync, not a multi-user editor. If you see conflict copies (`Recipe 2.html`), keep the one you want and delete the duplicate.

**Other folder sync (Dropbox, Syncthing, etc.)**

Same idea: the recipe directory is just files. Point RecipeJar at the synced folder. Do not sync the app’s cache (`~/.cache/recipejar/` and friends) — that is the viewer install, not your cookbook.

**Backup**

Copy the folder. Time Machine, a zip on a drive, whatever you already use. If the folder is gone, RecipeJar cannot get it back.

---

## Why HTML

The recipes should outlive the program.

HTML is readable in any browser, on any computer, without RecipeJar, without an account, and without a particular phone OS. You can print from the browser. You can email one file. You can hand a grandchild a USB stick in twenty years and they can still open dinner.

That is what “offline, yours forever” means here. The app is a convenient editor and index. The folder is the product you keep.
