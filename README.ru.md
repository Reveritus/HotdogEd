# HotdogEd

<div align="center">

HotdogEd - это хорошо известный и любимый проект FTN (FidoNet Technology Networks) для чтения и записи нетмейла и эх в FidoNet и совместимых сетях.

[![Latest release](https://img.shields.io/github/v/release/reveritus/hotdoged?include_prereleases&label=Последний%20релиз&style=for-the-badge)](https://github.com/reveritus/hotdoged/releases/latest)
![Downloads](https://img.shields.io/github/downloads/reveritus/hotdoged/total?style=for-the-badge&label=Загрузок)
![GitHub repo size](https://img.shields.io/github/repo-size/reveritus/hotdoged?style=for-the-badge)
![Code-size](https://shields.io/github/languages/code-size/reveritus/hotdoged?style=for-the-badge)
![Languages](https://shields.io/github/languages/count/reveritus/hotdoged?style=for-the-badge)
![Languages/top](https://shields.io/github/languages/top/reveritus/hotdoged?style=for-the-badge)
![Directory-file-count](https://shields.io/github/directory-file-count/reveritus/hotdoged?style=for-the-badge)
![LICENSE](https://img.shields.io/github/license/reveritus/hotdoged?color=blue&style=for-the-badge)
![Issues](https://shields.io/github/issues/reveritus/hotdoged?style=for-the-badge)
![Issues-pr](https://shields.io/github/issues-pr/reveritus/hotdoged?style=for-the-badge)
![Discussions](https://shields.io/github/discussions/reveritus/hotdoged?style=for-the-badge)
![Forks](https://shields.io/github/forks/reveritus/hotdoged?style=for-the-badge)
![Stars](https://shields.io/github/stars/reveritus/hotdoged?style=for-the-badge)
![Watchers](https://shields.io/github/watchers/reveritus/hotdoged?style=for-the-badge)
![Contributors](https://shields.io/github/contributors/reveritus/hotdoged?style=for-the-badge)
</div>

The source texts obtained using reverse engineering are published in another fork (the location of which will be announced later ;) for informational purposes. A complete compilation of the application from them is artificially made impossible both to preserve copyrights, and so that "life does not seem like a raspberry" and "you can't get a fish out without a pond". However, despite this, the application can be assembled by the special way.

All compiled libraries are included in the source code, including well-known ones, since the source application was built on currently outdated APIs, JDK and SDK/NDK and has not been updated again, in addition, the source texts contain multiple intersections with these libraries and vice versa.

The purpose of this project is to update an existing application to work under current Android versions in the absence of the author's support.
The author, Sergey Positurin, left FidoNet in 2022 and has no plans to support his application. He also did not want to leave the source texts, despite repeated requests from the community. Nevertheless, to a direct question about the possibility of publishing the application and the data contained in it, in the summer of 2024, Sergey Positurin answered in the affirmative, as evidenced by the corresponding screenshot. Thus, the application, according to the author, goes into the public domain.

We invite everyone to take part in the project as much as possible.

## Existing versions

Current stable: 2.14.5 (32) r1 or r2

Current unstable: 2.14.5 (33)

Latest original version: 2.13.5 (26)

First disassembly/reassembly: 2.14.5 (27)

Thus, the version after disassembly is 10 units higher than the original version after the first paragraph. The release order (in parentheses) has been preserved. The versions may differ in revisions for devices from different manufacturers (for example, Xiaomi and Samsung).

## Doesn't support (yet or already):

- [ ] Sync lastrid's
- [ ] NNTP
- [ ] IPv6

## Support:

- [x] JDK17
- [x] Java 1.8
- [ ] Deep Black theme (for AMOLED displays)

## Features:

- [x] Yggdrasil mesh network connections
- [x] Localhost Syncronous network connections
- [x] Native Russian interface
- [x] Android v.13+ (succesfully tested v.14)
- [ ] The minimum version of Android v.5.1 (but this is not accurate; perhaps building an Android application from version 11 to 14+ sounds easier than a completely universal application from early versions of Android 2-4-6 and up to current ones);
- [ ] Database and incoming in a folder accessible to the user
- [x] Multilink
- [x] Copypaste edit
