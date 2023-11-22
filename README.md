## gdx-guava-eventbus
[![Release](https://jitpack.io/v/crykn/gdx-guava-eventbus.svg)](https://jitpack.io/#crykn/gdx-guava-eventbus) ![GWT Compatible](https://img.shields.io/badge/GWT-compatible-informational)

This is a fork of [guava-eventbus](https://github.com/crykn/guava-eventbus) which adds GWT support via [libGDX](https://github.com/libgdx/libgdx)'s reflection classes. To use it, add the following dependencies to your project:

```groovy
implementation "com.github.crykn:gdx-guava-eventbus:$eventbusVersion:sources"
implementation "com.github.crykn:gdx-guava-eventbus:$eventbusVersion"
```

Also add the following line to your module file:
```xml
<inherits name="com.google.common.eventbus.guava-eventbus" />
```
