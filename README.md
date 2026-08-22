# Orbit

![GitHub License](https://img.shields.io/github/license/Ayydxn/Orbit)
![GitHub Issues](https://img.shields.io/github/issues/Ayydxn/Orbit)
![GitHub Pull Requests](https://img.shields.io/github/issues-pr/Ayydxn/Orbit)

---

Orbit is a developer-facing UI library for Minecraft that lets mod creators build custom in-game screens and HUD overlays using HTML, CSS, and JavaScript instead of hand-rolling Screen subclasses and raw GUI code.

It's powered by [Ultralight](https://ultralig.ht/), a lightweight GPU/CPU-accelerated web renderer, bound directly into the game's rendering and input pipelines.

⚠️ **Status Notice:** Orbit is currently under active development. Bugs, connection edge cases, or crashes may occur. Feature requests are welcome, but development is primarily focused on cross-platform compatibility, multi-loader support, and bug fixes.

---

## 🔽 Installation

> **NOTE:** The mod is still under development and no stable builds have been released yet. Check back later!

---

## 🐛 Reporting Issues

If you encounter bugs or crashes, please report them using the [issue tracker](https://github.com/Ayydxn/Orbit/issues).

Before opening a new issue:
* Use the search bar to check if your issue has already been reported.
* Ensure you include your mod loader, Minecraft version, operating system, and relevant log files (`latest.log` or crash reports).

Duplicate issues or those missing the required diagnostic logs may be closed.

---

## 🛠 Building From Source

Orbit uses a standard Gradle multi-project setup via Architectury. You can build artifacts for all supported mod loaders by executing the default Gradle `build` task:

```bash
./gradlew build
```

Once the build completes, the compiled `.jar` files will be located in the following folders:

* **Specific Loader / Common JARs:** Inside the `build/libs` folder of that module's directory (For example, `fabric/build/libs` or `common/build/libs`).
* **Universal Multi-Loader JAR:** Inside the root `build/forgix` directory. (If absent, run the `mergeJars` task from Forgix)

---

## 📜 License

Orbit is licensed under the free and open-source license, GNU LGPLv3. For more information, please read the [license](https://choosealicense.com/licenses/lgpl-3.0/).