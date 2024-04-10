# TRIfA Material

<img src="https://github.com/Zoxcore/trifa_material/releases/download/nightly/screenshot-macos.png" width="90%">

Automated screenshots:<br>
<img src="https://github.com/Zoxcore/trifa_material/releases/download/nightly/screenshot-macos.png" height="200"></a>
<img src="https://github.com/Zoxcore/trifa_material/releases/download/nightly/screenshot-windows.png" height="200"></a>
<img src="https://github.com/Zoxcore/trifa_material/releases/download/nightly/screenshot-linux.png" height="200"></a>

## Build Status

**Github:** [![Android CI](https://github.com/Zoxcore/trifa_material/workflows/Nightly/badge.svg)](https://github.com/Zoxcore/trifa_material/actions?query=workflow%3A%22Nightly%22)
[![Last release](https://img.shields.io/github/v/release/Zoxcore/trifa_material)](https://github.com/Zoxcore/trifa_material/releases/latest)
[![Translations](https://hosted.weblate.org/widget/trifa-a-tox-client-for-android/trifa-material/svg-badge.svg)](https://hosted.weblate.org/projects/trifa-a-tox-client-for-android/trifa-material/)
<a href="https://github.com/Zoxcore/trifa_material/blob/master/LICENSE">
<img src="https://img.shields.io/badge/license-GPLv3%2B-blue.svg" alt="GPLv3+" />
</a>

## Development Snapshot Version
the latest Development Snapshot can be downloaded from [here](https://github.com/Zoxcore/trifa_material/releases/tag/nightly)

<a href="https://github.com/Zoxcore/trifa_material/releases/download/nightly/trifa-material_nightly-x86_64.appimage
"><img src="https://raw.githubusercontent.com/Zoxcore/trifa_material/master/images/on_github_nightly.png" width="200"></a>

## Installation & starting the App [Linux, Windows and MacOS]

<a href="https://github.com/Zoxcore/trifa_material/releases/latest/"><img src="https://raw.githubusercontent.com/Zoxcore/trifa_material/master/images/Tux.svg" height="45"></a>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="https://github.com/Zoxcore/trifa_material/releases/latest/"><img src="https://raw.githubusercontent.com/Zoxcore/trifa_material/master/images/Windows_logo_-_2012.svg" height="45"></a>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="https://github.com/Zoxcore/trifa_material/releases/latest/"><img src="https://raw.githubusercontent.com/Zoxcore/trifa_material/master/images/Apple_logo_black.svg" height="45"></a>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="https://github.com/Zoxcore/trifa_material/releases/latest/"><img src="https://raw.githubusercontent.com/Zoxcore/trifa_material/master/images/Raspberry_Pi_Logo.svg" height="45"></a>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="https://github.com/Zoxcore/trifa_material/releases/latest/"><img src="https://raw.githubusercontent.com/Zoxcore/trifa_material/master/images/Apple_M1.svg.png" height="45"></a>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="https://github.com/Zoxcore/trifa_material/releases/latest/"><img src="https://raw.githubusercontent.com/Zoxcore/trifa_material/master/images/AsahiLinux_logo_svg.svg" height="45"></a>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="https://github.com/Zoxcore/trifa_material/releases/latest/"><img src="https://raw.githubusercontent.com/Zoxcore/trifa_material/master/images/NixOS_logo.svg" height="40"></a>

- Install (minimum) JDK 17 as your default java
- Download or checkout the source repository
- enter the ```trifa_material``` directory
- run:
  -  Linux: ```./gradlew run```
  -  Windows: ```gradlew.bat run```
  -  MacOS: ```./gradlew run```
  -  raspi 64bit:
     ```
     export MESA_EXTENSION_OVERRIDE="-GL_ARB_invalidate_subdata"
     ./gradlew run
     ```
  -  [Asahi Linux](https://asahilinux.org/):
     ```
     export MESA_EXTENSION_OVERRIDE="-GL_ARB_invalidate_subdata"
     export MESA_GL_VERSION_OVERRIDE=3.0
     ./gradlew run
     ```
  -  NixOS:
     ```
     chmod a+x trifa-material-x86_64.appimage
     nix-shell -p appimage-run
     appimage-run trifa-material-x86_64.appimage
     ```

or download any of the release [packages](https://github.com/Zoxcore/trifa_material/releases/latest)

##  Features
- One to one chat with friends
- use Tor Proxy (to hide your IP address)
- reliable File transfers
- Drag & Drop for File transfers with friends
- NGC (new group chats)
- Group Images in NGC group chats
- Drag & Drop for Images in NGC group chats
- Group History Sync (the last 130 minutes)
- Notifications
- Multiaccount Support
- Screensharing
- Audio calls
- Noise Suppression for Audio Calls
- Video calls
- Screenshots
- Emoticons
- Emoji Popup while you type (activate by typing ```:smile``` for example)
- And many more options!


## Help Translate the App in your Language
Use Weblate:
https://hosted.weblate.org/projects/trifa-a-tox-client-for-android/trifa-material/

## Remove my Background from the Camera Video like Zoom or MS Teams does (Linux only!)
check instructions on how to do it here:
https://github.com/floe/backscrub

## Add Acoustic Echo Cancellation (AEC) to your microphone input (Linux only!)
Activate AEC on Linux with pulseaudio.<br>
You need to have an audio in source already plugged in (like a headset with a microphone)

```
pactl unload-module module-echo-cancel
pactl load-module module-echo-cancel \
 aec_method=webrtc \
 aec_args='noise_suppression=1,intelligibility_enhancer=0,comfort_noise=0' \
 source_name=echocancel sink_name=echocancel1

pacmd set-default-source echocancel
pacmd set-default-sink echocancel1
```

to unload the AEC again
```
pactl unload-module module-echo-cancel
```

## Capture Video and Audio
ffmpeg lib is used to capture Video and Audio.<br>
The Java and JNI parts are built here: https://github.com/zoff99/ffmpeg_av_jni

## Notifications
Java can do Notifications on Windows properly, but on macOS and Linux a native library is needed.<br>
The Java and JNI parts are built here: https://github.com/zoff99/jni_notifications

## arm64 macOS toxcore lib
The lib for arm64 macOS is built on https://codemagic.io/

## BOM (Bill of materials)
What dependencies are used by trifa material:<br>
https://github.com/Zoxcore/trifa_material/releases/download/nightly/bom_implementation.txt

Full list of compile time dependencies:<br>
https://github.com/Zoxcore/trifa_material/releases/download/nightly/bom_compileclasspath.txt

Full list of run time dependencies:<br>
https://github.com/Zoxcore/trifa_material/releases/download/nightly/bom_runtimeclasspath.txt

## Custom fonts
```NotoColorEmoji.ttf```<br>
downloaded from this repo https://github.com/googlefonts/noto-emoji<br>
https://github.com/googlefonts/noto-emoji/raw/main/fonts/NotoColorEmoji.ttf


```NotoSans-Regular.ttf```<br>
```NotoSans-SemiBold.ttf```<br>
both downloaded from this repo https://github.com/openmaptiles/fonts/tree/master/noto-sans<br>

```NotoSans-Regular-COLRv1.ttf__004_3547_plus_49_glyphs_selected.ttf```<br>
```NotoSans-SemiBold-COLRv1.ttf__005_3548_glyphs_selected.ttf```<br>

these are made by merging ```NotoColorEmoji-SVG.otf```
(from this repo https://github.com/adobe-fonts/noto-emoji-svg/blob/main/fonts/NotoColorEmoji-SVG.otf)
into their NotoSans google fonts above


<br>
Any use of this project's code by GitHub Copilot, past or present, is done
without our permission.  We do not consent to GitHub's use of this project's
code in Copilot.
