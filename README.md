<div align="center">

# HotdogEd

![](https://img.shields.io/badge/-000000?style=for-the-badge&logo=fidoalliance&logoSize=auto)![Static Badge](https://img.shields.io/badge/-000000?style=for-the-badge&logo=.net&logoSize=auto)

HotdogEd is a well-known and beloved FTN (FidoNet Technology Networks) project for reading and writing in the netmail and echoes of FidoNet and compatible networks.

[![Latest release](https://img.shields.io/github/v/release/reveritus/hotdoged?include_prereleases&label=latest%20release&style=for-the-badge)](https://github.com/reveritus/hotdoged/releases/latest)![Downloads](https://img.shields.io/github/downloads/reveritus/hotdoged/total?style=for-the-badge)![GitHub repo size](https://img.shields.io/github/repo-size/reveritus/hotdoged?style=for-the-badge)![Code-size](https://shields.io/github/languages/code-size/reveritus/hotdoged?style=for-the-badge)
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

HotDog aka HotdogEd - reader 
FidoNet [and news] for Android.
Written by Sergey Poziturin aka 2:5020/2140 back in 2013.
In April 2022, Positurin drank out of Fido, never leaving 
sources, as well as Russification, decent documentation and support.

The source texts obtained using reverse engineering are published in another fork (the location of which will be announced later ;) for informational purposes. A complete compilation of the application from them is artificially made impossible both to preserve copyrights, and so that "life does not seem like a raspberry" and "you can't get a fish out without a pond". However, despite this, the application can be assembled by the special way.

All compiled libraries are included in the source code, including well-known ones, since the source application was built on currently outdated APIs, JDK and SDK/NDK and has not been updated again, in addition, the source texts contain multiple intersections with these libraries and vice versa.

The purpose of this project is to update an existing application to work under actual Android versions in the absence of the author's support.
The author, Sergey Positurin, left FidoNet in 2022 and has no plans to support his application. He also did not want to leave the source texts, despite repeated requests from the community. Nevertheless, to a direct question about the possibility of publishing the application and the data contained in it, in the summer of 2024, Sergey Positurin answered in the affirmative, as evidenced by the corresponding screenshot. Thus, the application, according to the author, goes into the public domain.

We invite everyone to take part in the project as much as possible.

## Existing versions

Current stable (editor only): 2.14.5 (31)

Current stable (FTN-provider only): 2.14.5 (32) r1 or r2

Current unstable: 2.14.5 (33)

Latest original version: 2.13.5 (26) from 2017.

First disassembly/reassembly: 2.13.5.01b (27)

Thus, the version after disassembly is 10 units higher than the original version after the first paragraph. The release order (in parentheses) has been preserved. The versions may differ in revisions for devices from different manufacturers (for example, Xiaomi and Samsung).

## Doesn't support (yet or already):

- [ ] Sync lastrid's
- [ ] NNTP
- [ ] IPv6
- [ ] Russification of values stored in the database (for compatibility with your existing database), including
the names of some sections
of the settings, for example, "Scores" instead of a tweet
- [  ] The minimum version of Android v.5.1 (but this is not accurate; perhaps building an Android application from version 11 to 14+ sounds easier than a completely universal application from early versions of Android 2-4-6 and up to current ones);

## Support:

- [x] JDK17
- [x] Java 1.8
- [ ] Deep Black theme (for AMOLED displays)

## Features:

![](https://img.shields.io/badge/Yggdrasil%20mesh%20network%20connections-000000?style=for-the-badge&label=√&labelColor=8a2be2)
![](https://img.shields.io/badge/Localhost%Syncronous%20network%20connections-000000?style=for-the-badge&label=√&labelColor=8a2be2)
![](https://img.shields.io/badge/Native%Russian%20interface-000000?style=for-the-badge&label=√&labelColor=8a2be2)
![](https://img.shields.io/badge/Android%20v.14-000000?style=for-the-badge&label=√&labelColor=8a2be2)![](https://img.shields.io/badge/Documentation-000000?style=for-the-badge&label=√&labelColor=8a2be2)
![](https://img.shields.io/badge/DB%20and%20incoming%20in%20a%20user%20accessible%20folder-000000?style=for-the-badge&label=√&labelColor=8a2be2)
![](https://img.shields.io/badge/Multilink-000000?style=for-the-badge&label=√&labelColor=8a2be2)
![](https://img.shields.io/badge/Copypaste%20edit-000000?style=for-the-badge&label=√&labelColor=8a2be2)
![](https://img.shields.io/badge/Garbage%20disposal-000000?style=for-the-badge&label=√&labelColor=8a2be2)
![](https://img.shields.io/badge/Fixed%20quotas%20displayed%20crookedly%20on%20PC's-000000?style=for-the-badge&label=√&labelColor=8a2be2)

## ToDo

![](https://img.shields.io/badge/normal%20English%20build-000000?style=for-the-badge)![](https://img.shields.io/badge/human--friendly%20DATABASE%20transfer%20to%20external%20storage-000000?style=for-the-badge)
![](https://img.shields.io/badge/setting%20up%20templates-000000?style=for-the-badge)
![](https://img.shields.io/badge/incoming%20connections-000000?style=for-the-badge)
![](https://img.shields.io/badge/new%20provider%20for%20nodes-000000?style=for-the-badge)




