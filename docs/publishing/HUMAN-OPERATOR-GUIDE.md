# RecipeJar — Human Operator Guide

First-time publisher checklist for shipping RecipeJar.  
Assumes **zero** prior App Store / Play Console / online-sales experience.

Companion docs:

- [MAC-TOOLCHAIN.md](./MAC-TOOLCHAIN.md) — versions and paths already installed on the development Mac  
- Plan context: App Store + Play primary; desktop secondary; marketing site Phase 2  

**Mobile targets:** Android phones, **iPhone**, and **iPad** (one iOS app binary / universal family; iPad is an explicit product target, not an afterthought).  


**You (Human)** create accounts, pay fees, click Submit, and answer review questions.  
**Agent (AI)** drafts copy, policies, checklists, and builds binaries for you to upload.

---

## 1. Mindset & order of operations

Do work in this order so slow account verification runs in parallel with engineering:

| Week-ish | Focus | Who |
|----------|--------|-----|
| 0 | Finish Mac SDKs (done if [MAC-TOOLCHAIN.md](./MAC-TOOLCHAIN.md) checks pass) | Human + Agent |
| 0–1 | Apple Developer + Google Play enrollment; support email; seller name | **Human** |
| 0–1 | Privacy policy draft + store copy drafts | Agent → Human approve |
| 1–N | Mobile MVP engineering (Android/iOS targets, UI) | Agent |
| Near ready | Signing, internal test tracks, device QA | Shared |
| Launch | Store submission, pricing live, soft launch posts | **Human** (Agent checklists) |

**Expected waits:** Apple Developer can take hours to days after payment/identity. Google Play is usually faster after the $25 fee. Do not block coding on enrollment finishing—Simulator and local Android builds work without store accounts.

---

## 2. Accounts checklist (enrollment)

### Before you start

- [ ] Personal or business **email** you will keep for years (support + store contact)
- [ ] Phone number for 2FA
- [ ] Payment method for Apple (**$99/year**) and Google (**$25 one-time**)
- [ ] Legal identity for tax forms (SSN/ITIN or business EIN, depending on country)
- [ ] Bank account for payouts (when you enable paid apps)

### Apple (iOS / later macOS notarization)

1. Use or create an **Apple ID** at [appleid.apple.com](https://appleid.apple.com).
2. Enroll in the **Apple Developer Program** at [developer.apple.com/programs](https://developer.apple.com/programs/) (~$99/year).
3. Complete identity verification when prompted; wait for “Active” membership.
4. Sign in to [App Store Connect](https://appstoreconnect.apple.com).
5. Note your **Team ID** (Membership details)—you will need it for signing.
6. Enable **2FA** and store backup codes offline.

**RecipeJar defaults (change only if you decide otherwise):**

| Field | Suggested value |
|-------|-----------------|
| Seller / developer name | Your legal name or “RecipeJar” if entity exists |
| Bundle ID | `org.recipejar.app` (matches desktop packaging) |
| App name | RecipeJar |
| Price (target) | $6.99 USD one-time |

### Google Play (Android)

1. Create a Google account for publishing (can be the same support email).
2. Register at [play.google.com/console](https://play.google.com/console) (**$25** one-time).
3. Accept agreements; complete account details and identity checks.
4. Create an app entry when you have a first AAB (internal testing track is fine first).
5. Enable 2FA on the Google account.

| Field | Suggested value |
|-------|-----------------|
| Package name | `org.recipejar.app` (keep stable forever) |
| App name | RecipeJar |
| Default price | $6.99 USD one-time |
| Category | Food & Drink |

### Support contact

- [ ] Create `support@…` or a dedicated Gmail/alias you will monitor
- [ ] Use the **same** address in both store consoles and the privacy policy

---

## 3. Money, tax, and pricing setup

1. In **App Store Connect → Business / Agreements, Tax, and Banking**, accept paid apps agreement; add bank + tax forms.
2. In **Play Console → Payments profile**, complete merchant / tax info for paid apps.
3. Set base price **$6.99** on each store when the listing is ready (optional launch sale **$4.99** for 1–2 weeks).
4. Desktop (later): direct download can be free beta first, then ~**$9.99** via a merchant you choose (Gumroad/Paddle/etc.)—not required for mobile launch.

You do not need an LLC for v1; sole proprietor / individual is fine. Upgrade entity later if revenue or liability warrants it (tax pro optional).

---

## 4. Legal checklist (offline app)

Even offline apps need public legal links for store review.

- [ ] **Privacy policy URL** (GitHub Pages or similar is fine until Phase 2 site)
- [ ] Support URL or email shown on the listing
- [ ] Age rating questionnaire completed honestly (RecipeJar: no social, no ads, no user-generated public content → typically 4+ / Everyone)
- [ ] App Privacy (Apple) / Data safety (Google): declare **no** collection if true; if you add crash analytics later, update labels

**Offline / local-files talking points for forms:**

- Recipes live as HTML files the user chooses (folder / document picker).
- No account required for core use.
- No ad networks in v1.
- Optional author name is local preference, not a cloud profile.

Agent can draft `docs/publishing/PRIVACY-POLICY.md`; **you** publish it to a stable HTTPS URL before submission.

---

## 5. Mac setup (owned machine — not “what to buy”)

Hardware acquisition is **done**. Development Mac (verified):

| Item | Value |
|------|--------|
| Machine | MacBook Air **M4**, 16 GB RAM, ~256 GB SSD |
| OS | macOS **26.3** |
| Xcode | **26.6** (license accepted) |
| iOS Simulator | **iOS 26.5** runtimes / devices available |
| JDK | Temurin **21** user-local (`~/.local/jdks/…`) |
| Android SDK | `~/Library/Android/sdk` (API 35, build-tools 35, platform-tools) |
| Homebrew | Not required (install needs admin; skipped) |

Details and shell exports: [MAC-TOOLCHAIN.md](./MAC-TOOLCHAIN.md).

### Every new terminal

```bash
source ~/.zprofile   # JAVA_HOME + ANDROID_HOME
cd /path/to/recipejar
java -version        # 21.x
adb version          # platform-tools
```

### Quick health checks

```bash
xcodebuild -license check
xcrun simctl list devices available | head
./gradlew :shared:desktopTest
./gradlew :composeApp:compileKotlinDesktop
./gradlew :shared:compileDebugKotlinAndroid
```

### Disk (256 GB)

Keep **≥40–50 GB free**. If space gets tight:

- Xcode → Settings → Platforms: remove unused OS simulators  
- Delete `~/Library/Developer/Xcode/DerivedData`  
- Prune Gradle caches carefully under `~/.gradle/caches`  

### What “Archive” means (iOS, later)

When the iOS app target exists: open the Xcode project/workspace → select **Any iOS Device** → **Product → Archive**. That produces the binary you upload with Transporter / Organizer. You do **not** need this until TestFlight.

---

## 6. Signing overview

### Android

1. Create an **upload keystore** once (Agent can script; you store the password offline):

   ```bash
   keytool -genkey -v -keystore recipejar-upload.jks -keyalg RSA -keysize 2048 -validity 10000 -alias recipejar
   ```

2. Keep `.jks` + passwords in a password manager—not in git.
3. Configure signing in Gradle or Play App Signing (Google can hold the app signing key; you keep the upload key).
4. Build **AAB** for Play (`bundleRelease` when release build is configured).

Debug builds use the automatic debug keystore—fine for emulators and local devices.

### iOS

1. In Apple Developer → Certificates, Identifiers & Profiles: App ID `org.recipejar.app`.
2. Development certificate for device debugging; Distribution certificate for TestFlight/App Store.
3. Provisioning profiles bind App ID + certs + devices.
4. Xcode can manage signing automatically once you are signed in with your team (**Signing & Capabilities**).

### macOS desktop notarization (Phase 1C)

- Developer ID Application certificate  
- `xcrun notarytool` submit after packaging DMG  
- Not required for mobile store launch  

---

## 7. Build handoff (Agent produces → Human uploads)

| Artifact | Typical producer | You do |
|----------|------------------|--------|
| Debug APK / install on emulator | `./gradlew :composeApp:assembleDebug` (when app module is wired) | Install / smoke test |
| Release AAB | Agent + signing config | Upload to Play Console internal track |
| iOS Archive / IPA | Xcode Archive on this Mac | Upload to App Store Connect / TestFlight |
| Desktop DMG/MSI/Deb | `compose.desktop` packaging | Optional GitHub Release |
| Store listing copy | Agent drafts in `docs/publishing/` | Paste into consoles, edit voice |
| Privacy policy | Agent draft | Host URL, paste into both stores |

**Human-only clicks:** accept agreements, set price, submit for review, answer App Review questions, reply to users.

---

## 8. Store submission wizards (RecipeJar-oriented)

### Shared listing story

- **One-line:** RecipeJar keeps your family’s recipes alive—offline, yours forever, in HTML you can open anywhere.  
- **Not the lead:** “powerful CMS,” social discovery, cloud sync.  
- Screenshots should show index → recipe with notes → edit → offline ownership.

### Apple App Store Connect (high level)

1. My Apps → + → New App  
2. Bundle ID, name, primary language, SKU (e.g. `recipejar-ios`)  
3. Pricing: $6.99  
4. Privacy Policy URL  
5. Screenshots for required device sizes  
6. Build from TestFlight processed build  
7. Submit for Review  

### Google Play Console (high level)

1. Create app → name, language, free/paid  
2. Dashboard checklist: store listing, graphics, content rating, target audience, data safety  
3. Production or start with **Internal testing**  
4. Upload AAB → rollout  

Detailed field-by-field answers will live in `STORE-LISTING-EN.md` when drafted; use this guide for **order of operations**.

---

## 9. Review rejection playbook

| Issue | Mitigation |
|-------|------------|
| Missing privacy policy | Publish HTTPS policy before submit |
| File access purpose strings | Clear copy: “open your recipe folder” |
| Incomplete Data safety / App Privacy | Match real behavior; update if analytics added |
| Crash on launch | Internal testing first; fix before production |
| Misleading screenshots | Show real app UI, not mock marketing only |
| Payments | Use store IAP/billing only if you sell in-app; v1 is paid-upfront app |

---

## 10. Launch day runbook

1. Both listings **Ready for Sale** / production rolled out  
2. Support email open and filters checked  
3. Soft announce to friends/family (family history / cooking groups—value first, no spam)  
4. Watch crash reports and first reviews for 72 hours  
5. Keep a simple FAQ reply template ready  

---

## 11. Support runbook (first week)

Common questions:

- **Where are my recipes?** Local HTML files in the folder you opened—not on our servers.  
- **Can I sync to another device?** Use the folder sync tool you already trust (iCloud Drive, Syncthing, USB).  
- **Why HTML?** So grandchildren can open recipes in any browser if the app is gone.  

---

## 12. Phase 2 prep (website — deferred)

Not a launch blocker. Later: domain, home + privacy + support + store badges, SEO posts on digitizing family cookbooks.

---

## Quick reference — agent vs you

| Task | Owner |
|------|--------|
| Pay Apple/Google fees, 2FA, tax/bank | **Human** |
| Draft privacy, listings, icons briefs | Agent |
| Approve copy and host privacy URL | **Human** |
| Implement/features, Gradle mobile targets, tests | Agent |
| Device QA, store submit, review replies | **Human** |
| Mac SDK install (JDK/Android/Xcode platforms) | Shared (mostly done—see MAC-TOOLCHAIN) |
