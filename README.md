[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/xfqwdsj/IAmNotADeveloper)

**简体中文** | [English](README_en.md) | [Português (Brasil)](README_pt-BR.md)

# 我不是开发者

一个用于隐藏Android系统开发者相关选项状态的模块。

## FAQ

### Q: 我激活了模块，但模块报告“未激活”，这是怎么回事？

排查步骤：

1. 确认您已经激活了模块。
2. 确认您在成功激活模块后，强行停止了模块应用。
3. 在[Issues](https://github.com/xfqwdsj/IAmNotADeveloper/issues)中搜索相关问题。
4. 如果没有找到相关问题，请先抓取日志并对模块应用截图，确保包含完整的模块状态卡片（如果无法在一张截图中包含，请分多张截图）。
5. 在[Issues](https://github.com/xfqwdsj/IAmNotADeveloper/issues)中提交新问题，并上传日志。

### Q: 如何确认LSPosed中，模块已经激活？

您可以通过以下步骤确认：

1. 通过任意方式启动LSPosed管理器。
2. 在“模块”页面中，找到“我不是开发者”模块。
3. 确认“启用模块”开关处于开启状态。

### Q: 如何抓取日志？

您可以通过以下步骤抓取日志：

1. 通过任意方式启动LSPosed管理器。
2. 在“日志”页面中，点击右上角的“保存”图标按钮。
3. 选择一个合适的保存位置，如“下载内容”，不要修改文件名。
4. 点击“保存”按钮。

### Q: 我对某应用程序激活了模块，但是应用程序崩溃了/没有任何效果/被检测器应用检测到了，怎么办？

本模块的实现方式为直接注入目标应用程序，对于内置注入防护的应用程序，模块可能会不起作用，甚至会使应用拒绝工作。

解决方案：无。[#104](https://github.com/xfqwdsj/IAmNotADeveloper/issues/104)已经立项，您可以耐心等待，没有预计完成时间。**请不要提交关于此问题的任何Issue，它将会被直接关闭。**

## 如何贡献

如果您想为本项目贡献代码，请参考[CONTRIBUTING.md](CONTRIBUTING_zh-CN.md)。

## 隐私协议

本应用的“测试”功能会获取您对应系统开关的状态，包括：

- 开发者模式
- USB 调试
- 无线调试

但是本应用不会收集您的任何信息。
