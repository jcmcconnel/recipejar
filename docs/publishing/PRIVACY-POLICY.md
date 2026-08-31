# RecipeJar Privacy Policy

**Last updated:** August 19, 2026

RecipeJar is a local, offline recipe organizer for your computer. It keeps your family’s recipes as ordinary HTML files in a folder you choose and own. You can open those files in any web browser, with or without RecipeJar.

This policy is for the **desktop app** (macOS, Windows, and Linux). Android and iOS builds exist only as prototypes and are not offered for sale.

RecipeJar is made by James McConnel. Support: [jcmcconnel@gmail.com](mailto:jcmcconnel@gmail.com).

This is **not** the App Store app “Recipe Jar: Save & Cook,” and it is **not** the website at recipejar.app. Those are other products.

---

## What RecipeJar does not do

RecipeJar does **not**:

- Create accounts or ask you to sign in
- Run a backend or cloud for your recipes
- Collect analytics, crash reports, or usage telemetry
- Show ads or include ad networks
- Upload your recipes, photos, or folder paths to James or to any service
- Sell, rent, or share personal information (there is none to share)

There is no RecipeJar server that stores your cookbook.

---

## Where your recipes live

You point RecipeJar at a folder on your computer (or on a drive you attach). Recipes are saved there as HTML files, plus an index the app maintains in that same folder.

Those files are yours. Copy them, back them up, put the folder in iCloud Drive, drop it on a USB stick, or open a recipe in Safari, Chrome, or Firefox. RecipeJar does not keep a second copy in the cloud.

If you delete the folder, the recipes are gone. RecipeJar cannot recover them.

---

## What stays on this computer

RecipeJar may store a few **local** preferences so the next launch is less annoying:

- The last recipe folder you opened
- The last recipe you had open in that folder
- An optional author name, if you set one (written into recipe metadata when you save)

On typical systems this lives in the ordinary Java user-preferences store for the `recipejar` node. It does not leave the machine.

To show a recipe as a page (not raw HTML), the desktop app uses an embedded Chromium viewer (KCEF). That viewer’s files and cache live under a user cache directory, usually `~/.cache/recipejar/` on Linux and the equivalent cache location on macOS and Windows. That cache is local. It is not a RecipeJar account.

---

## Network use

Day-to-day use is offline. Your recipes are not sent anywhere.

**First launch** of the desktop app may use the internet once to download the embedded Chromium viewer into the local cache above. That download is so the reader can render HTML. It is not a signup, and it does not include your recipes.

After that, you can use RecipeJar with no network. If the viewer has not finished installing, the app can still show HTML source or welcome text instead of a rendered page.

RecipeJar does not phone home to check a license or report that you opened a file.

---

## Children

RecipeJar is a desktop tool for organizing recipes. It is not a social network and has no in-app communication. It does not knowingly collect personal information from anyone, including children.

---

## Payments

If you buy a packaged desktop build (for example through Gumroad or Paddle), that merchant processes the payment. RecipeJar the app never sees your card number. Their privacy policy applies to the checkout, not to your recipe folder.

---

## Source and license

RecipeJar is released under the MIT License. Copyright James McConnel. The license is a software license, not a claim on your recipes. Your HTML files remain yours.

---

## Changes

If this policy has to change (for example if a future version added optional crash reporting), the new text will be posted here with a new date. The desktop app described above does not collect data today.

---

## Contact

Questions about this policy or the app:

**James McConnel**  
[jcmcconnel@gmail.com](mailto:jcmcconnel@gmail.com)

This page is meant to be hosted as a static document (GitHub Pages is fine). Hosting the policy does not give RecipeJar access to your recipes.
