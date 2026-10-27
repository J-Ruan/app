# 验证记录

## 当前已执行（2026-10-27）

- JDK 21 + Gradle Wrapper 8.10.2：分发包校验通过，`./gradlew --version` 成功。
- `assembleDebug`：成功，生成 API 26+ 安装包。
- `testDebugUnitTest`：9 项，全部通过，无跳过。覆盖空数据、平均间隔 / 持续时间、实际经期天数、预测逾期不滚动、波动提示、未来 / 重叠 / 反向日期拒绝。
- `assembleDebugAndroidTest`：设备测试代码成功编译。
- `lintDebug`：无错误；固定依赖有新版可用的提示，不代表功能故障。
- Android 8.0 / API 26 软件模拟器：APK 安装、启动成功。`RepositoryTest` 1 项通过；`UserJourneyTest` 1 项通过（68 秒）。
- UI 流程测试覆盖全部主要页面、记录保存、经期结束、深色设置持久化、Activity 重建及删除 / 清空。滚动后控件使用 Compose 语义点击；固定顶部保存按钮与底部导航使用触摸点击。
- 测试中改进了键盘完成操作与固定顶部保存按钮，最终完整流程在无人工干预下通过。
- 真正进程重启：`ProcessPersistenceTest` mode=prepare 通过 → adb force-stop → 冷启动 MainActivity → mode=verify 通过。核对两条周期、备注、默认天数和深色设置。
- `connectedDebugAndroidTest` 在下载 Android UTP 测试运行器依赖时遇到网络 403，尚未完成。已使用安装在同一设备上的官方 AndroidJUnitRunner 实际运行测试，未用编译成功代替执行。
- 合并执行 `adb shell am instrument -w -r com.warmcycle.test/androidx.test.runner.AndroidJUnitRunner`：**OK (3 tests)**，66.458 秒，3 项通过、无失败、无跳过。
- 最终源码离线构建 `assembleDebug testDebugUnitTest lintDebug assembleDebugAndroidTest`：成功。APK v2 签名校验通过；minSdk 26、targetSdk 35。

## 测试实现

`RepositoryTest`：关闭并重新打开磁盘 Room 数据库，核对记录持久化；备份往返；错误格式不破坏旧数据；修改、删除。

`UserJourneyTest`：首次同意与设置 → 首页 → 情绪 / 身体状态 / 备注 → 保存 → 日历 → 统计 → 结束经期 → 设置深色 → Activity 重建 → 数据仍在 → 删除每日 / 经期 → 清空回到首次使用。

## 截图区域

正式应用启用 FLAG_SECURE，禁止截屏，仓库不包含真实私密记录截图。可安装 APK 直接查看完整交互。若后续添加截图，应使用独立测试设备与虚构记录，并仅在测试中临时移除安全窗口标志。

## 必须在个人真机补充验收

Android 13+ 通知授权、后台省电行为、锁屏通知隐私、不同厂商设备身份验证与设备迁移排除规则。软件模拟器无法代表这些设备行为。独立 PIN 与 Widget 未实现。
