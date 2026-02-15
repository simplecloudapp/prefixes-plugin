# Prefixes Plugin

![Banner][banner]

<div align="center">

[![Modrinth][badge-modrinth]][modrinth]
[![License][badge-license]][license]
<br>

[![Discord][badge-discord]][social-discord]
[![Follow @simplecloudapp][badge-x]][social-x]
[![Follow @simplecloudapp][badge-bluesky]][social-bluesky]
[![Follow @simplecloudapp][badge-youtube]][social-youtube]
<br>

[Report a Bug][issue-bug-report]
·
[Request a Feature][issue-feature-request]
<br>

🌟 Give us a star — your support means the world to us!
</div>
<br>

> All information about this project can be found in our detailed [documentation][docs-thisproject].

The Prefixes Plugin provides comprehensive rank management for chat and tab list displays. It integrates with LuckPerms by default but can be extended with custom implementations.

## Features

- [x] **LuckPerms Integration**: The plugin automatically syncs with LuckPerms groups, converting them to prefix groups.
- [x] **Supported Server Software**: Supports Paper, Spigot and Forks.
- [x] **Quick Setup**: Easy installation process for all supported software.
- [x] **Placeholders**: Retrieve real-time information with dynamic placeholders.

## Dependency

> For always up-to-date artifacts visit [dev artifacts][dev-artifacts] or [artifacts][artifacts].

> Note: If you want to use the dev version, you have to use the [snapshot repository][snapshots].

### Gradle Kotlin
```kt
implementation("app.simplecloud.plugin:prefixes-api:VERSION")
```
### Gradle Groovy
```groovy
implementation 'app.simplecloud.plugin:prefixes-api:VERSION'
```

### Maven
```xml
<dependency>
    <groupId>app.simplecloud.plugin</groupId>
    <artifactId>prefixes-api</artifactId>
    <version>VERSION</version>
</dependency>
```

## Contributing
Contributions to SimpleCloud are welcome and highly appreciated. However, before you jump right into it, we would like you to read our [Contribution Guide][docs-contribute].

## License
This repository is licensed under [Apache 2.0][license].


<!-- LINK GROUP -->

<!-- ✅ PLEASE EDIT -->
[banner]: https://github.com/simplecloudapp/branding/blob/main/readme/banner/plugin/prefixes.png?raw=true
[issue-bug-report]: https://github.com/simplecloudapp/prefixes-plugin/issues/new?labels=bug&projects=template=01_BUG-REPORT.yml&title=%5BBUG%5D+%3Ctitle%3E
[issue-feature-request]: https://github.com/simplecloudapp/prefixes-plugin/discussions/new?category=ideas
[docs-thisproject]: https://docs.simplecloud.app/en/manual/plugin/prefixes
[docs-contribute]: https://docs.simplecloud.app/contribute

[modrinth]: https://modrinth.com/organization/simplecloud
[dev]: https://repo.simplecloud.app/#/snapshots/app/simplecloud/plugin/prefixes

[artifacts]: https://repo.simplecloud.app/#/snapshots/app/simplecloud/plugin/prefixes
[dev-artifacts]: https://repo.simplecloud.app/#/snapshots/app/simplecloud/plugin/prefixes

[badge-dev]: https://repo.simplecloud.app/api/badge/latest/snapshots/app/simplecloud/plugin/prefixes-shared?name=Dev&style=flat-square&color=0ea5e9

<!-- ⛔ DON'T TOUCH -->
[license]: ./LICENSE
[snapshots]: https://repo.simplecloud.app/#/snapshots

[social-x]: https://x.com/simplecloudapp
[social-bluesky]: https://bsky.app/profile/simplecloud.app
[social-youtube]: https://www.youtube.com/@thesimplecloud9075
[social-discord]: https://discord.simplecloud.app

[badge-modrinth]: https://img.shields.io/badge/modrinth-18181b.svg?style=flat-square&logo=modrinth
[badge-license]: https://img.shields.io/badge/apache%202.0-blue.svg?style=flat-square&label=license&labelColor=18181b&style=flat-square&color=e11d48
[badge-discord]: https://img.shields.io/badge/Community_Discord-d95652.svg?style=flat-square&logo=discord&color=27272a
[badge-x]: https://img.shields.io/badge/Follow_@simplecloudapp-d95652.svg?style=flat-square&logo=x&color=27272a
[badge-bluesky]: https://img.shields.io/badge/Follow_@simplecloud.app-d95652.svg?style=flat-square&logo=bluesky&color=27272a
[badge-youtube]: https://img.shields.io/badge/youtube-d95652.svg?style=flat-square&logo=youtube&color=27272a
