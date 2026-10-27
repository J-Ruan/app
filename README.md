# WarmCycle 暖期 🌷

**记住她的重要日子。** 一款简洁、温柔、隐私优先的原生 Android 生理期记录 App。仅在双方知情并同意时使用。无需账号、服务器、付费 API 或广告，核心功能完全离线。

> 预测是基于历史记录的简单估算，不用于诊断、排卵判断或避孕。持续或明显不适请咨询专业医疗人员。

## 功能

- 首次隐私说明、知情同意确认与初始周期设置；不确定时可跳过初始日期。
- 首页周期状态、预计倒计时、逾期提示、今日关怀、今天开始 / 今天结束。
- 月历：实际经期、预计经期、今天和选中日期；切换月份、补录、编辑当天记录。
- 周期记录添加 / 修改 / 删除；删除确认、日期和重叠校验。
- 每日情绪、身体状态、经量和备注，本地持久化；支持修改、确认删除。
- 最近 6 次周期间隔、平均周期与持续时间、分页展示历史周期。
- 本地预计提醒：提前 1 / 2 / 3 天，默认关闭；隐私通知默认开启。
- 明文 JSON 备份导出 / 导入，导入前确认并完整校验，记录在 Room 事务中替换。
- 浅色 / 深色 / 跟随系统；设备身份验证应用锁（可选）。
- 清除全部记录及设置、取消提醒；无网络权限，禁用系统备份，禁止截图和任务预览。

## 界面

奶油粉背景、低饱和粉紫卡片、大圆角、底部五栏导航：**首页 / 日历 / 记录 / 统计 / 设置**。

截图区域：真实运行截图与验证结果见 [验证记录](docs/VALIDATION.md)。正式 App 为保护隐私禁止截屏，不将真实私密记录放入仓库。

## 技术栈与结构

Kotlin 2.0.21、Jetpack Compose、Material 3、Navigation Compose、ViewModel + Flow、Room 2.6.1、DataStore、WorkManager、BiometricPrompt。

```text
app/src/main/java/com/warmcycle/
├── MainActivity.kt           # 入口及前后台应用锁
├── WarmCycleApp.kt           # 应用级依赖
├── data/                    # Room 实体、DAO、数据库、仓储、DataStore
├── domain/                  # 纯 Kotlin 周期计算及日期校验
├── reminder/                # 离线后台通知
└── ui/                      # 页面、组件、主题、ViewModel
app/schemas/                 # Room v1 导出的数据库架构
app/src/test/                # 周期计算单元测试
app/src/androidTest/         # 数据持久化 / 备份 / UI 流程测试
scripts/                     # 云环境安装与工具链激活
.github/workflows/           # 构建、验证、Tag 发布 APK
```

`CycleSummary` 为实际记录推导的领域模型，不重复入库；设置存 DataStore，提醒配置复用设置，不另建冗余表。数据库当前为 v1：保留导出的架构，升级必须提供显式 Migration，不使用破坏性迁移。

## 运行环境

- Android 8.0 / API 26 及以上；compile / target SDK 35。
- Linux / macOS / Windows，完整 JDK 21（JDK 17 也适用于本项目构建工具）。
- Android Studio 或 Android SDK：platforms;android-35、build-tools;35.0.0、platform-tools。
- Gradle Wrapper 8.10.2，固定分发包 SHA-256。

用 Android Studio 打开仓库、同步 Gradle、选择模拟器或手机后运行 `app`。命令行设置 `ANDROID_HOME` 指向 SDK，或在**本地忽略的** `local.properties` 写入 `sdk.dir=你的SDK路径`。

```bash
./gradlew assembleDebug testDebugUnitTest lintDebug
./gradlew assembleDebugAndroidTest
# 已连接并启动设备时运行：
./gradlew connectedDebugAndroidTest
# 若云环境阻止下载额外 UTP 运行器，可直接运行同一组设备测试：
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
adb shell am instrument -w -r com.warmcycle.test/androidx.test.runner.AndroidJUnitRunner
```

Windows 使用 `gradlew.bat`。新云机器可运行 `bash scripts/cloud-setup.sh`，后续任务 `source scripts/cloud-env.sh`。脚本仅安装工具链并构建，不创建 Git worktree，不覆写源码和依赖清单。优先使用既有 checkout。

## APK 构建与安装

```bash
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

输出：`app/build/outputs/apk/debug/app-debug.apk`，版本 **1.0.0**，可安装的 **Debug 签名**。不是具有长期私钥的正式 Release 签名包。`assembleRelease` 可生成未签名包，正式分发前需要你自行保管长期签名私钥；不要提交私钥和密码。

手机可从 GitHub Releases 下载 APK，允许该下载来源“安装未知应用”后安装。导入备份前仔细核对来源。更换调试签名（例如换机器或 CI 构建）可能不能覆盖安装：**先导出备份，再卸载旧包、安装并导入**。卸载会清除设备内数据。若 targetSdk 安装校验受手机系统策略限制，请查看系统提示。

## GitHub 与自动发布

仓库：https://github.com/J-Ruan/app

恢复 GitHub 授权后，可在已激活工具链的仓库目录执行 `bash scripts/publish.sh v1.0.0`，推送源码与 Tag 并由 Actions 发布。

每次推送 main / PR 执行构建、单元测试、Lint 和测试 APK 编译。推送 `v*` Tag 还会自动创建 / 更新 GitHub Release，附加 Debug APK 与 SHA-256；Actions 页面也保留 APK Artifact。CI 编译测试 APK **不代表已运行设备测试**。

```bash
git tag v1.0.1
git push origin v1.0.1
```

CI 当前采用临时 Debug 签名，不承诺不同运行间签名一致。首次公开发布可从 Releases 的标题和说明辨认构建类型。

## 隐私与数据

- 数据在应用私有目录内的 Room SQLite 和 DataStore 中，**未额外加密**，依赖 Android 沙箱与设备安全。Debug 包可通过开发调试工具读取数据，不适合高安全要求场景。
- 无账号、广告 SDK、统计追踪 SDK、云同步或 INTERNET 权限。仅在开启提醒时请求 Android 13+ 通知权限。
- 关闭系统云备份并声明排除设备迁移；具体厂商系统的行为仍受系统实现控制。
- 导出为**未加密的明文 JSON**（版本 1，包含周期和每日记录，不包含偏好或应用锁）。请保存在可信位置并自行保护 / 删除。选择系统文件提供方中的云盘属于用户主动行为。
- 导入上限约 2 MB；错误文件、未来日期、日期重叠、重复日期、非法枚举不会替换现有记录。请在导入前先导出。
- 应用锁使用 Android 系统身份验证：API 30+ 支持强生物识别或设备凭据；API 26–29 需支持强生物识别。**没有独立应用 PIN**。验证后切后台会重新锁定。
- 清除会删除记录与设置、取消提醒、回到首次使用；不会删除已经导出的文件，不承诺闪存取证级安全擦除。

## 预测与提醒的边界

周期＝相邻实际开始日的天数差。平均周期用最近最多 6 个间隔，持续时间用最近最多 6 次已结束记录（首尾日期均计入）；不足时使用用户默认值。下一次预测＝最后一次开始日＋平均周期。不自动滚动或生成“实际经期”。间隔极值差大于 7 天时显示波动提示。它不是医学风险分类。

进行中的经期一直保持“进行中”，直到用户明确结束；不会按默认天数悄悄结束。首次设置可显式勾选“这次已经结束”并选择真实结束日期；通常持续天数仅用于预测，不自动生成实际结束记录。

提醒由 WorkManager 约每 12 小时检查一次，在提前天数至预计日之间每次预计周期最多提醒一次；无精确闹钟权限，可能受省电、强行停止或设备关闭影响而延迟或错过。无记录、仍在实际经期或预计日期已过去时不发送。

## 测试与限制

实际执行结果与设备覆盖见 [docs/VALIDATION.md](docs/VALIDATION.md)。云环境的 Git HTTPS / UTP 下载有网络限制，源码与发布可走已授权的 GitHub API，设备测试可走官方 AndroidJUnitRunner。独立 PIN、桌面 Widget、主题色自定义和正式 Release 签名尚未提供；不影响离线记录。暂不宣称未经验证的机型兼容性或预测准确率。

## License

[MIT](LICENSE)。第三方 AndroidX / Kotlin / Gradle 组件遵循各自许可证。
