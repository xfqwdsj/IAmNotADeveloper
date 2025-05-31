[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/xfqwdsj/IAmNotADeveloper)

[简体中文](README.md) | **English** | [Português (Brasil)](README_pt-BR.md)

# IAmNotADeveloper

A module for hiding the status of Android system developer options.

## FAQ

### Q: I have activated the module, but it reports "Module not activated". What should I do?

Troubleshooting steps:

1. Make sure you have activated the module.
2. Make sure you have force stopped the module app after successful activation.
3. Search for related issues in the [Issues](https://github.com/xfqwdsj/IAmNotADeveloper/issues) section.
4. If you cannot find a related issue, please capture logs and take screenshots of the module app, ensuring the full module status card is visible (if it cannot fit in one screenshot, use multiple).
5. Submit a new issue in [Issues](https://github.com/xfqwdsj/IAmNotADeveloper/issues) and upload the logs.

### Q: How do I confirm that the module is activated in LSPosed?

You can confirm by following these steps:

1. Launch the LSPosed Manager by any means.
2. On the "Modules" page, find the "IAmNotADeveloper" module.
3. Make sure the "Enable module" switch is turned on.

### Q: How do I capture logs?

You can capture logs by following these steps:

1. Launch the LSPosed Manager by any means.
2. On the "Logs" page, tap the "Save" icon button in the upper right corner.
3. Choose a suitable save location, such as "Downloads", and don't change the file name.
4. Tap the "Save" button.

### Q: I activated the module for a certain app, but the app crashes/has no effect/is detected by a detector app. What should I do?

This module works by directly injecting into the target app. For apps with built-in injection protection, the module may not work or may cause the app to refuse to run.

Solution: None at the moment. See [Issue #104](https://github.com/xfqwdsj/IAmNotADeveloper/issues/104) for details. Please wait patiently; there is currently no ETA (Estimated Time of Arrival). **Do not submit any issues regarding this problem; they will be closed without further explanation.**

## How to Contribute

If you want to contribute code to this project, please refer to [CONTRIBUTING.md](CONTRIBUTING.md).

## Privacy Agreement

The "Test" function in this app will obtain the status of your corresponding system switch, including:

- Developer mode
- USB debugging
- Wireless debugging

However, this app won't collect any information about you.
