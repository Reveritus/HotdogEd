# HotdogEd

<div align="center">

HotdogEd - это хорошо известный и любимый проект FTN (FidoNet Technology Networks) для чтения и записи нетмейла и эх в FidoNet и совместимых сетях.

[![Latest release](https://img.shields.io/github/v/release/reveritus/hotdoged?include_prereleases&label=Последний%20релиз&style=for-the-badge)](https://github.com/reveritus/hotdoged/releases/latest)
![Downloads](https://img.shields.io/github/downloads/reveritus/hotdoged/total?style=for-the-badge&label=Загрузок)
![GitHub repo size](https://img.shields.io/github/repo-size/reveritus/hotdoged?style=for-the-badge&label=размер%20репы)
![Code-size](https://shields.io/github/languages/code-size/reveritus/hotdoged?style=for-the-badge&label=размер%20кода)
![Languages](https://shields.io/github/languages/count/reveritus/hotdoged?style=for-the-badge&label=языки)
![Languages/top](https://shields.io/github/languages/top/reveritus/hotdoged?style=for-the-badge&label=основной%20язык)
![Directory-file-count](https://shields.io/github/directory-file-count/reveritus/hotdoged?style=for-the-badge&label=число%20файлов)
![LICENSE](https://img.shields.io/github/license/reveritus/hotdoged?color=blue&style=for-the-badge&label=лицензия)
![Issues](https://shields.io/github/issues/reveritus/hotdoged?style=for-the-badge&label=проблемы)
![Issues-pr](https://shields.io/github/issues-pr/reveritus/hotdoged?style=for-the-badge&label=задачи)
![Discussions](https://shields.io/github/discussions/reveritus/hotdoged?style=for-the-badge&label=задачи)
![Forks](https://shields.io/github/forks/reveritus/hotdoged?style=for-the-badge&label=форк)
![Stars](https://shields.io/github/stars/reveritus/hotdoged?style=for-the-badge&label=звезд)
![Watchers](https://shields.io/github/watchers/reveritus/hotdoged?style=for-the-badge&label=смотрящие)
![Contributors](https://shields.io/github/contributors/reveritus/hotdoged?style=for-the-badge&label=вкладчики)
</div>

Исходные тексты, полученные с помощью реверс-инжиниринга, публикуются в другом форке (местоположение которого будет объявлено позже ;) в ознакомительных целях. Полная компиляция приложения из них искусственно сделана невозможной как для сохранения авторских прав, так и для того, чтобы "жизнь малиной не казалась" и "без пруда не вытащишь и рыбку из него". Однако, несмотря на это, приложение может быть собрано специальным образом.

В исходный код включены все скомпилированные библиотеки, в том числе хорошо известные, поскольку исходное приложение было построено на устаревших на данный момент API, JDK и SDK/NDK и больше не рефармилось, кроме того, исходные тексты содержат множественные пересечения с этими библиотеками и наоборот.

Целью этого проекта является обновление существующего приложения для работы под текущими версиями Android в отсутствие поддержки автора. Автор, Сергей Позитурин, покинул FidoNet в 2022 году и не планирует поддерживать свое приложение. Он также не захотел оставлять исходные тексты, несмотря на неоднократные просьбы сообщества. Тем не менее, на прямой вопрос о возможности публикации приложения и  данных, содержащихся в нем, летом 2024 года Сергей Позитурин ответил утвердительно, о чем свидетельствует соответствующий скриншот. Таким образом, приложение, по словам автора, переходит в общественное достояние.

Мы приглашаем всех желающих принять максимально возможное участие в проекте, включая посильное.

## Существующие версии

Текущая стабильная версия: 2.14.5 (32) r1 или r2

Текущая версия нестабильна: 2.14.5 (33)

Последняя оригинальная версия: 2.13.5 (26)

Первая разборка/повторная сборка: 2.14.5 (27)

Таким образом, версия после дизассемблирования на 10 единиц выше исходной версии после первой точки (параграфа). Порядок выпуска (в скобках) сохранен. Версии могут отличаться ревизиями для устройств разных производителей (например, Xiaomi и Samsung).

## Не поддерживается (пока или уже):
 - [  ] - Синхронизация последней прочитанной строки
 - [  ] - NNTP
 - [  ] - IPv6

## Поддерживается:
 - [x] - JDK17
 - [x] - Java 1.8
 - [x] - Тема Deep Black (для AMOLED-дисплеев)
## Особенности:
 - [x] - Сетевые соединения Yggdrasil mesh
 - [x] - Синхронные сетевые подключения Localhost
 - [x] - Собственный русский интерфейс
 - [x] - Android версии 13+ (успешно протестирована версия 14)
 - [x] - Минимальная версия Android v.5.1 (но это не точно; возможно, создание приложения для Android с версии 11 по 14+ звучит проще, чем создание полностью универсального приложения от ранних версий Android 2-4-6 и выше до текущих);
 - [ ] - Базу данных и входящие файлы в папке, доступной пользователю
 - [x] - Multilink
 - [x] - Редактирование копипасты
