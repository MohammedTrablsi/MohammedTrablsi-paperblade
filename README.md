# Paper Blade — Android

A fullscreen WebView wrapper around `app/src/main/assets/index.html` (the whole game is that one file).
Controllers (GameSir, Xbox, PlayStation, any HID gamepad) are forwarded natively to the game, and every
button can be remapped in-game under **Options** (title screen, or Pause → Options / Select button).

## Get the APK — easiest: let GitHub build it
1. Create a new GitHub repository and push this folder (the `android/` contents must be the repo root).
2. Open the repo's **Actions** tab → "Build Paper Blade APK" runs automatically (or press *Run workflow*).
3. Download the `PaperBlade-apk` artifact → `PaperBlade.apk` → copy to your phone → install
   (allow "install from unknown sources" when asked).

## Or build locally with Android Studio
1. Android Studio → *Open* → pick this folder. Let it sync (it downloads Gradle + the Android SDK bits).
2. **Build → Build Bundle(s) / APK(s) → Build APK(s)**. The APK is at `app/build/outputs/apk/debug/app-debug.apk`.
3. Or from a terminal: `gradle assembleDebug` (with the Android SDK installed and `ANDROID_HOME` set).

## Updating the game
Replace `app/src/main/assets/index.html` with a newer copy of the game and rebuild.

## Default controller layout (standard / Xbox naming — GameSir in X-input mode matches this)
| Action          | Keyboard        | Gamepad        |
|-----------------|-----------------|----------------|
| Move            | A / D, ← →      | D-pad / left stick |
| Attack          | J               | X or R1        |
| Deflect / Guard | K (tap / hold)  | L1 or L2       |
| Dodge           | L               | B              |
| Jump            | Space           | A              |
| Heal            | H               | Y              |
| Pause           | P               | Start          |
| Options         | O               | Select         |

Tip: if your GameSir is in a mode where buttons come through under different names, just remap them in Options — it records whatever the controller actually sends.
